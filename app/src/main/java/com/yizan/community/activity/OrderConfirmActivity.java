package com.yizan.community.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alipay.android.app.IAlixPay;
import com.alipay.sdk.auth.AlipaySDK;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.model.PaymentInfo;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.adapter.OrderGoodGridAdapter;
import com.yizan.community.adapter.PayWayListAdapter;
import com.fanwe.seallibrary.comm.Constants;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.comm.URLConstants;
import com.yizan.community.fragment.CustomDialogFragment;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.fanwe.seallibrary.model.InitInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.event.OrderEvent;
import com.fanwe.seallibrary.model.req.CreateOrderRequest;
import com.fanwe.seallibrary.model.result.OrderResult;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.NumFormat;
import com.yizan.community.utils.O2OUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 订单确认ACtivity
 */
public class OrderConfirmActivity extends BaseActivity implements BaseActivity.TitleListener, View.OnClickListener {
    private final int LOC_REQUEST_CODE = 0x401;
    private ArrayList<CartGoodsInfo> mCartGoodsInfoArrayList;
    private UserAddressInfo mUserAddressInfo;
    private CartSellerInfo mCartSellerInfo;
    private OrderGoodGridAdapter mOrderGoodGridAdapter;
    private PayWayListAdapter mPayWayListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        setTitleListener(this);

        mCartGoodsInfoArrayList = (ArrayList<CartGoodsInfo>) this.getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        mUserAddressInfo = (UserAddressInfo) this.getIntent().getSerializableExtra(Constants.EXTRA_ADDR);
        mCartSellerInfo = (CartSellerInfo) this.getIntent().getSerializableExtra(Constants.EXTRA_SELLER);
        if (mUserAddressInfo == null) {
            mUserAddressInfo = getDefaultAddress();
        }
        initView();
    }

    private UserAddressInfo getDefaultAddress() {
        try {
            UserInfo userInfo = PreferenceUtils.getObject(this, UserInfo.class);
            if (ArraysUtils.isEmpty(userInfo.address)) {
                return null;
            } else {
                for (UserAddressInfo addressInfo : userInfo.address) {
                    if (addressInfo.isDefault) {
                        return addressInfo;
                    }
                }
                return userInfo.address.get(0);
            }
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        title.setText(R.string.title_activity_order_verify);
    }

    private void initAddrView() {
        if (mUserAddressInfo != null) {
            mViewFinder.setText(R.id.tv_user_name, mUserAddressInfo.name);
            mViewFinder.setText(R.id.tv_user_tel, mUserAddressInfo.mobile);
            mViewFinder.setText(R.id.tv_addr, mUserAddressInfo.address);
            mViewFinder.find(R.id.ll_container).setVisibility(View.VISIBLE);
            mViewFinder.find(R.id.tv_notice).setVisibility(View.INVISIBLE);
        } else {
            mViewFinder.find(R.id.ll_container).setVisibility(View.INVISIBLE);
            mViewFinder.find(R.id.tv_notice).setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        initAddrView();
        mViewFinder.onClick(R.id.ll_address_container, this);
        double price = 0.0;
        int nums = 0;
        List<String> list = new ArrayList<String>();
        for (CartGoodsInfo item : mCartGoodsInfoArrayList) {
            list.add(item.logo);
            price += (item.price * item.num);
            nums += item.num;
        }
        mViewFinder.setText(R.id.tv_goods_num, "共" + nums + "件");
        mOrderGoodGridAdapter = new OrderGoodGridAdapter(this, list);
        GridView gv = mViewFinder.find(R.id.gv_list);
        gv.setAdapter(mOrderGoodGridAdapter);

        // 运费
        double totalPrice = price + mCartSellerInfo.deliveryFee;
        mViewFinder.setText(R.id.tv_goods_price, String.format(getResources().getString(R.string.RMB_sign) , NumFormat.formatPrice(price)));
        mViewFinder.setText(R.id.tv_carriage, String.format(getResources().getString(R.string.RMB_sign) , NumFormat.formatPrice(mCartSellerInfo.deliveryFee)));
        mViewFinder.setText(R.id.tv_total_price, String.format(getResources().getString(R.string.RMB_sign) , NumFormat.formatPrice(totalPrice)));
        mViewFinder.setText(R.id.tv_total, String.format(getResources().getString(R.string.RMB_sign) , NumFormat.formatPrice(totalPrice)));

        // 支付方式
        InitInfo initInfo = PreferenceUtils.getObject(this, InitInfo.class);
        if (null != initInfo && !ArraysUtils.isEmpty(initInfo.payments)) {
            ListView listView = mViewFinder.find(R.id.lv_pay_way);
            mPayWayListAdapter = new PayWayListAdapter(this, initInfo.payments);
            listView.setAdapter(mPayWayListAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mPayWayListAdapter.selectItem(position);
                }
            });

        }

        mViewFinder.onClick(R.id.tv_pay, this);
        mViewFinder.onClick(R.id.ll_deliver_time, this);

        if (mCartSellerInfo.type == 2) {
            mViewFinder.find(R.id.tv_deliver_title).setVisibility(View.GONE);
            mViewFinder.find(R.id.ll_deliver_container).setVisibility(View.GONE);
            mViewFinder.setText(R.id.tv_deliver_time_head, R.string.order_d_service_time);

            if (!ArraysUtils.isEmpty(mCartGoodsInfoArrayList) && !TextUtils.isEmpty(mCartGoodsInfoArrayList.get(0).serviceTime)) {
                mViewFinder.setText(R.id.tv_deliver_time, mCartGoodsInfoArrayList.get(0).serviceTime);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pay:
                buyGoods();
                break;
            case R.id.ll_deliver_time:
                selDeliverTime();
                break;
            case R.id.ll_address_container:
                Intent intent = new Intent(this, SwitchAddressActivity.class);
                intent.putExtra("isLocate", "false");
                startActivityForResult(intent, LOC_REQUEST_CODE);
                break;
        }
    }

    private void buyGoods() {
        if (mUserAddressInfo == null) {
            ToastUtils.show(this, R.string.home_notice_sel_location);
            return;
        }
        PaymentInfo info =  mPayWayListAdapter.getSelItem();


        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.addressId = mUserAddressInfo.id;
        createOrderRequest.appTime = ((TextView) mViewFinder.find(R.id.tv_deliver_time)).getText().toString();
        createOrderRequest.giftContent = ((EditText) mViewFinder.find(R.id.et_greeting)).getText().toString();
        createOrderRequest.buyRemark = ((EditText) mViewFinder.find(R.id.et_remark)).getText().toString();
        createOrderRequest.invoiceTitle = ((EditText) mViewFinder.find(R.id.et_invoice)).getText().toString();

        List<Integer> list = new ArrayList<Integer>();
        for (CartGoodsInfo item : mCartGoodsInfoArrayList) {
            list.add(item.id);
        }
        createOrderRequest.cartIds = list;
        if (TextUtils.isEmpty(createOrderRequest.appTime)) {
            if (mCartSellerInfo.type == 1) {
                ToastUtils.show(this, R.string.select_dispatching_time);
            } else {
                ToastUtils.show(this, R.string.select_server_time);
            }
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            ToastUtils.show(this, R.string.loading_err_net);
            return;
        }
        CustomDialogFragment.show(this.getSupportFragmentManager(), R.string.loading, this.getLocalClassName());
        ApiUtils.post(this, URLConstants.ORDER_CREATE,
                createOrderRequest,
                OrderResult.class, new Response.Listener<OrderResult>() {

                    @Override
                    public void onResponse(final OrderResult response) {
                        if (O2OUtils.checkResponse(getApplicationContext(), response)) {
                            if (response.data != null) {
                                Intent intent = new Intent(OrderConfirmActivity.this, OrderDetailActivity.class);
                                intent.putExtra(Constants.EXTRA_DATA, response.data.id);
                                if (mPayWayListAdapter.getSelItem() != null) {
                                    intent.putExtra(Constants.EXTRA_PAY, mPayWayListAdapter.getSelItem().code);
                                }
                                ShoppingCartMgr.getInstance().loadCart(getApplicationContext());
                                EventBus.getDefault().post(new OrderEvent(null));
                                intent.putExtra(Constants.EXTRA_AUTO, response.data.isCanPay);
                                startActivity(intent);
                                finish();
                            }
                        }

                        CustomDialogFragment.dismissDialog();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtils.show(getApplicationContext(), R.string.loading_err_nor);
                        CustomDialogFragment.dismissDialog();
                    }
                });
    }


    private void selDeliverTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_date_time, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
        builder.setView(view);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() + 15 * 60 * 1000);
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));


        builder.setTitle(R.string.select_dispatching_time);
        builder.setPositiveButton(R.string.sure_plus_null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                StringBuffer sb = new StringBuffer();
                sb.append(String.format(getResources().getString(R.string.time_format),
                        datePicker.getYear(),
                        datePicker.getMonth() + 1,
                        datePicker.getDayOfMonth()));
                sb.append(" ");
                sb.append(timePicker.getCurrentHour())
                        .append(":").append(timePicker.getCurrentMinute()).append(":00");

                SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format_2));
                try {
                    Date date = format.parse(sb.toString());
                    Date nowDate = new Date();
                    if (date.compareTo(nowDate) == 1) {
                        mViewFinder.setText(R.id.tv_deliver_time, sb.toString());
                        dialog.cancel();
                    } else {
                        ToastUtils.show(OrderConfirmActivity.this, R.string.msg_error_vaild_time);
                    }

                } catch (Exception e) {

                }

            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LOC_REQUEST_CODE:
                if (data != null) {
                    UserAddressInfo addressInfo = (UserAddressInfo) data.getSerializableExtra(Constants.EXTRA_DATA);
                    if (addressInfo != null) {
                        mUserAddressInfo = addressInfo;
                        initAddrView();
                    }
                }
                break;
        }
    }

}
