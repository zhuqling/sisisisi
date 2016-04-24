package com.yizan.community.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yizan.community.R;
import com.yizan.community.comm.ShoppingCartMgr;
import com.fanwe.seallibrary.model.CartGoodsInfo;
import com.fanwe.seallibrary.model.CartSellerInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.NumFormat;
import com.zongyou.library.volley.RequestManager;
import com.zongyou.library.widget.adapter.CommonAdapter;
import com.zongyou.library.widget.adapter.ViewHolder;

import java.util.List;

public class CartGoodsListAdapter extends CommonAdapter<CartGoodsInfo> {
    private FragmentActivity mFragmentActivity;
    private PopupWindow popupWindow;
    private RelativeLayout mOk,mCancel;
    private CartSellerInfo mCartSellerInfo;
    private ShoppingCartListAdapter mShoppingCartListAdapter;
    public CartGoodsListAdapter(FragmentActivity context, CartSellerInfo cartSellerInfo, ShoppingCartListAdapter shoppingCartListAdapter) {
        super(context, cartSellerInfo.goods, R.layout.item_shopping_cart_good);
        mFragmentActivity = context;
        mCartSellerInfo = cartSellerInfo;
        mShoppingCartListAdapter = shoppingCartListAdapter;
    }

    public void setList(List<CartGoodsInfo> list) {
        mDatas.clear();
        addAll(list);
    }

    public void setAllChecked(boolean isCheckAll) {
        for (CartGoodsInfo item : mDatas) {
            item.isChecked = isCheckAll;
        }
        notifyDataSetChanged();
    }

    public boolean haveOneChecked(){
        for (CartGoodsInfo item : mDatas) {
            if(item.isChecked){
                return true;
            }
        }
        return false;
    }

    private boolean isAllChecked(){
        for (CartGoodsInfo item : mDatas){
            if(!item.isChecked){
                return false;
            }
        }
        return true;
    }

    @Override
    public void convert(final ViewHolder helper, final CartGoodsInfo item, int position) {
//        helper.setImageUrl(R.id.iv_image, item.logo, RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setImageUrl(R.id.iv_image, ImgUrl.squareUrl(R.dimen.good_list_icon_width, item.logo), RequestManager.getImageLoader(), R.drawable.ic_default_square);
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_price, String.format(mContext.getResources().getString(R.string.RMB_sign), NumFormat.formatPrice(item.price)));
        helper.setText(R.id.tv_num, "" + item.num);

        final CheckBox cb = helper.getView(R.id.cb_sel);
        cb.setChecked(item.isChecked);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean oneChecked = haveOneChecked();
                item.isChecked = !item.isChecked;
                cb.setChecked(item.isChecked);
                if(mCartSellerInfo.isChecked && !item.isChecked){
                    mCartSellerInfo.isChecked = false;
                    mShoppingCartListAdapter.notifyDataSetChanged();
                }else if(item.isChecked && isAllChecked()){
                    mCartSellerInfo.isChecked = true;
                    mShoppingCartListAdapter.notifyDataSetChanged();
                }else if(oneChecked != haveOneChecked()){
                    mShoppingCartListAdapter.notifyDataSetChanged();
                }
            }
        });

        helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = helper.getView(R.id.tv_num);
                int num = Integer.parseInt(tv.getText().toString()) + 1;

                if (ShoppingCartMgr.getInstance().saveShopping(mFragmentActivity, item.goodsId, item.normsId, num)) {
                    helper.setText(R.id.tv_num, String.valueOf(num));
                    item.num = num;
                }
            }
        });
        helper.getView(R.id.iv_sub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = helper.getView(R.id.tv_num);
                int num = Integer.parseInt(tv.getText().toString()) - 1;

                if (ShoppingCartMgr.getInstance().saveShopping(mFragmentActivity, item.goodsId, item.normsId, num)) {
                    helper.setText(R.id.tv_num, String.valueOf(num));
                    item.num = num;
                }
            }
        });



        helper.getView(R.id.iv_clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View convertView = LayoutInflater.from(mFragmentActivity).inflate(R.layout.item_pop_delete, null);
                View parent = LayoutInflater.from(mFragmentActivity).inflate(R.layout.fragment_shopping_cart, null);
                convertView.setBackgroundResource(R.drawable.style_edt_boder);
                WindowManager wm = (WindowManager) mFragmentActivity.getSystemService(Context.WINDOW_SERVICE);
                int width = (wm.getDefaultDisplay().getWidth() / 5) * 4;
                int height = (wm.getDefaultDisplay().getHeight() / 9) * 5;
                popupWindow = new PopupWindow(convertView, width, width/2, true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL | Gravity.DISPLAY_CLIP_VERTICAL, 0, 0);
                mOk = (RelativeLayout) convertView.findViewById(R.id.sure_delete);
                mCancel = (RelativeLayout) convertView.findViewById(R.id.cancel_delete);
                mOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                        if (ShoppingCartMgr.getInstance().saveShopping(mFragmentActivity, item.goodsId, item.normsId, 0)) {
                            helper.setText(R.id.tv_num, String.valueOf(0));
                        }
                    }
                });
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

    }

    private void initPop() {


    }

}
