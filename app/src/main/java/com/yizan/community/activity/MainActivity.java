package com.yizan.community.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fanwe.seallibrary.comm.Constants;
import com.fanwe.seallibrary.comm.URLConstants;
import com.fanwe.seallibrary.model.LocAddressInfo;
import com.fanwe.seallibrary.model.MessageStatusInfo;
import com.fanwe.seallibrary.model.UserAddressInfo;
import com.fanwe.seallibrary.model.UserInfo;
import com.fanwe.seallibrary.model.event.GotoHomeEvent;
import com.fanwe.seallibrary.model.event.LoginEvent;
import com.fanwe.seallibrary.model.event.ShoppingCartEvent;
import com.fanwe.seallibrary.model.result.MessageStatusResult;
import com.hzblzx.miaodou.sdk.MiaodouKeyAgent;
import com.yizan.community.BuildConfig;
import com.yizan.community.R;
import com.yizan.community.YizanApp;
import com.yizan.community.comm.ShoppingCartMgr;
import com.yizan.community.fragment.Display_item;
import com.yizan.community.fragment.HomePageFragment;
import com.yizan.community.fragment.PersonalCenterFragment;
import com.yizan.community.fragment.ShoppingCartFragment;
import com.yizan.community.utils.ApiUtils;
import com.yizan.community.utils.AppUpdate;
import com.yizan.community.utils.ImgUrl;
import com.yizan.community.utils.O2OUtils;
import com.yizan.community.utils.PushUtils;
import com.ypy.eventbus.EventBus;
import com.zongyou.library.app.AppUtils;
import com.zongyou.library.app.DeviceUtils;
import com.zongyou.library.util.ArraysUtils;
import com.zongyou.library.util.NetworkUtils;
import com.zongyou.library.util.ToastUtils;
import com.zongyou.library.util.storage.PreferenceUtils;

import java.util.HashMap;

/**
 * MainActivity
 */
public class MainActivity extends BaseActivity implements BaseActivity.TitleListener {
    private static final int LOC_REQUEST_CODE = 0x102;
    private String[] mTabText;
    int[] mImageViewArray = new int[]{R.drawable.tab_home_selector,
            R.drawable.tab_shopping_selector,
            R.drawable.tab_order_selector,
            R.drawable.tab_user_selector
    };
    private LayoutInflater mLayoutInflater;
    private FragmentTabHost mTabHost;
    private View mBtnSearch, mBtnLogout;
    private UserAddressInfo mAddressInfo;
    private PopupWindow popupWindow;
    private Button mOk, mCancel;
    private OpenDoorActivity mOpenDoorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        resetCurrUserAddr();
        setTitleListener_RightImage(this, R.layout.titlebar_main);
        mTabText = new String[]{getResources().getString(R.string.home_text), getResources().getString(R.string.shop_cart_text), getResources().getString(R.string.order_text), getResources().getString(R.string.my_text)};
        ImgUrl.init(this);
        JPinit();
        initTabSpec();
        reflashState();
        EventBus.getDefault().register(this);
        versionUpgrade();
        initMessagePushReceiver();
        initOpenDoor();
        initHomeTitle(mAddressInfo);
    }

    private void initOpenDoor() {

        if (!BuildConfig.USER_PROPERTY_GUARD) {
            return;
        }
        mOpenDoorActivity = OpenDoorActivity.getInstance(this);
        mOpenDoorActivity.initMiaodou();
        mOpenDoorActivity.initDoorListFormCache();
        initDoorNeedSensor();

    }

    private void initDoorListFromCache() {
        if (!BuildConfig.USER_PROPERTY_GUARD) {
            return;
        }
        mOpenDoorActivity.initDoorListFormCache();
    }

    private void initDoorNeedSensor() {
        if (!BuildConfig.USER_PROPERTY_GUARD) {
            return;
        }
        if (!O2OUtils.isLogin(this)) {
            MiaodouKeyAgent.setNeedSensor(false);
            return;
        }

        if (mTabHost.getCurrentTab() == 0 && mOpenDoorActivity.haveDoorList()) {
            MiaodouKeyAgent.setNeedSensor(mOpenDoorActivity.isBluetoothOpen());
        } else {
            MiaodouKeyAgent.setNeedSensor(false);
        }
    }

    private void setDoorArgentListener() {
        if (!BuildConfig.USER_PROPERTY_GUARD) {
            return;
        }
        MiaodouKeyAgent.setMDActionListener(mOpenDoorActivity);
    }

    private void uninitDoorAgent() {
        if (!BuildConfig.USER_PROPERTY_GUARD) {
            return;
        }
        MiaodouKeyAgent.unregisterMiaodouAgent();
    }

    public void onEventMainThread(ShoppingCartEvent event) {
        setTabStatus(1, ShoppingCartMgr.getInstance().getTotalCount(0));
    }

    public void onEventMainThread(GotoHomeEvent event) {
        int tab = mTabHost.getCurrentTab();
        if (event.mTabIndex != tab) {
            mTabHost.getTabWidget().getChildAt(event.mTabIndex).performClick();
        }

    }

    public void onEventMainThread(LoginEvent event) {
        try {
            if (O2OUtils.isLogin(this)) {
                initDoorListFromCache();
                initDoorNeedSensor();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        reflashState();
        resetCurrUserAddr();
        try {
            PersonalCenterFragment fragment = (PersonalCenterFragment) getSupportFragmentManager().findFragmentByTag(PersonalCenterFragment.class.getName());
            fragment.reFlashUI();

            ShoppingCartFragment fragmentCart = (ShoppingCartFragment) getSupportFragmentManager().findFragmentByTag(ShoppingCartFragment.class.getName());
            fragmentCart.reflashUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPinit();
    }

    private void reflashState() {
        if (O2OUtils.isLogin(this)) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    loadMessageStatus();
                }
            }, 100);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    ShoppingCartMgr.getInstance().loadCart(getApplicationContext());
                }
            }, 200);

        } else {
            updateMessageStatus(null);
            ShoppingCartMgr.getInstance().clearCart();

        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if (mMessagePushReceiver != null)
            unregisterReceiver(mMessagePushReceiver);
        uninitDoorAgent();
    }

    private void JPinit() {
        // 推送
        PushUtils.initTagsAndAlias(this);
    }

    protected void addTabFragment(int index, Class<?> clss) {
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(
                clss.getName()).setIndicator(
                getTabItemView(index));
        // 将Tab按钮添加进Tab选项卡中
        mTabHost.addTab(tabSpec, clss, null);
    }

    private void initTabSpec() {
        // 实例化布局对象
        mLayoutInflater = LayoutInflater.from(this);
        // 实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.fl_container);

        addTabFragment(0, HomePageFragment.class);
        addTabFragment(1, ShoppingCartFragment.class);
        addTabFragment(2, Display_item.class);
        addTabFragment(3, PersonalCenterFragment.class);

        // 显示
        mTabHost.setCurrentTab(0);

        // 栈上面叠加了很多Fragment的时候，如果想再次点击TabHost的首页，能返回到最初首页的页面的话，那就要把首页的Fragment上面的Fragment的弹出
        for (int i = 0, size = mTabText.length; i < size; i++) {
            final int j = i;
            mTabHost.getTabWidget().getChildAt(i)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            switch (j) {
                                case 0:
                                    Drawable arrowDown = getResources().getDrawable(R.drawable.ic_arrow_down);
                                    arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());
                                    mTitle.setCompoundDrawables(null, null, arrowDown, null);
                                    mBtnSearch.setVisibility(View.VISIBLE);
                                    ((ImageButton) mBtnSearch).setImageResource(R.drawable.ic_search);
                                    mBtnLogout.setVisibility(View.INVISIBLE);
                                    initHomeTitle(PreferenceUtils.getObject(MainActivity.this, UserAddressInfo.class));
                                    break;
                                case 1:
                                    if (O2OUtils.turnLogin(MainActivity.this.getApplicationContext())) {
                                        return;
                                    }
                                    setTitle(mTabText[1]);
                                    mTitle.setCompoundDrawables(null, null, null, null);
                                    mBtnSearch.setVisibility(View.VISIBLE);
                                    ((ImageButton) mBtnSearch).setImageResource(R.drawable.ic_clean);
                                    mBtnLogout.setVisibility(View.INVISIBLE);
                                    break;
                                case 2:
                                    if (O2OUtils.turnLogin(MainActivity.this.getApplicationContext())) {
                                        return;
                                    }
                                    setTitle(mTabText[2]);
                                    mTitle.setCompoundDrawables(null, null, null, null);
                                    mBtnSearch.setVisibility(View.INVISIBLE);
                                    mBtnLogout.setVisibility(View.INVISIBLE);
                                    break;
                                case 3:
//                                    if (O2OUtils.turnLogin(MainActivity.this.getApplicationContext())) {
//                                        return;
//                                    }
                                    setTitle(mTabText[3]);
                                    mTitle.setCompoundDrawables(null, null, null, null);
                                    mBtnSearch.setVisibility(View.INVISIBLE);
                                    mBtnLogout.setVisibility(View.VISIBLE);
                                    break;
                            }
                            getSupportFragmentManager().popBackStack(null,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            mTabHost.setCurrentTab(j);
                            initDoorNeedSensor();
                            reloadHomePage();
                        }
                    });
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    @SuppressLint("InflateParams")
    private View getTabItemView(int index) {
        View view = mLayoutInflater.inflate(R.layout.main_tab_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setBackgroundResource(mImageViewArray[index]);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTabText[index]);

        return view;
    }

    @Override
    public void onBackPressed() {
        AppUtils.startHome(this);
    }

    private void deleteShopping() {
        View convertView = LayoutInflater.from(this).inflate(R.layout.item_pop_delete, null);
        View parent = LayoutInflater.from(this).inflate(R.layout.fragment_shopping_cart, null);
        convertView.setBackgroundResource(R.drawable.style_edt_boder);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        float width = (wm.getDefaultDisplay().getWidth() / 3) * 2;
        popupWindow = new PopupWindow(convertView, (int) width, (int) (width / 5) * 3, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        convertView.findViewById(R.id.sure_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                if (ShoppingCartMgr.getInstance().deleteShopping(MainActivity.this, MainActivity.class.getName())) {

                }
            }
        });
        convertView.findViewById(R.id.cancel_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void setTitle(TextView title, ImageButton left, View right) {
        mBtnSearch = mTitleRight2;
        mBtnLogout = mTitleRight;
        ((Button) mBtnLogout).setText(R.string.logout);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 == mTabHost.getCurrentTab()) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                } else if (1 == mTabHost.getCurrentTab()) {
                    if (ShoppingCartMgr.getInstance().getCart() != null && ShoppingCartMgr.getInstance().getCart().size() > 0) {
                        deleteShopping();
                    } else {
                        View convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_pop_delete, null);
                        View parent = LayoutInflater.from(MainActivity.this).inflate(R.layout.fragment_shopping_cart, null);
                        convertView.setBackgroundResource(R.drawable.style_edt_boder);
                        WindowManager wm = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
                        float width = (wm.getDefaultDisplay().getWidth() / 3) * 2;
                        popupWindow = new PopupWindow(convertView, (int) width, (int) (width / 5) * 3, true);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
                        ((TextView) convertView.findViewById(R.id.hint_text)).setText(getString(R.string.msg_err_shopcart_not_null));
                        ((TextView) convertView.findViewById(R.id.delete_title)).setText(getString(R.string.msg_err_not_delete));
                        convertView.findViewById(R.id.cancel_delete).setVisibility(View.GONE);
                        convertView.findViewById(R.id.sure_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                    }
                } else {

                }

            }
        });
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        initHomeTitle(PreferenceUtils.getObject(MainActivity.this, UserAddressInfo.class));
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 == mTabHost.getCurrentTab()) {
                    Intent intent = new Intent(MainActivity.this, SwitchAddressActivity.class);
                    intent.putExtra("isLocate", "true");
                    startActivityForResult(intent, LOC_REQUEST_CODE);
                } else if (1 == mTabHost.getCurrentTab()) {

                } else {

                }
//                startActivity(new Intent(MainActivity.this, PropertyActivity.class));
            }
        });
    }

    private void logout() {
        if (!O2OUtils.isLogin(this)) {
            ToastUtils.show(this, R.string.msg_not_login);
            return;
        }
        View convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popwindow_layout, null);
        View parent = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
        convertView.setBackgroundResource(R.drawable.style_edt_boder);
        WindowManager wm = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
        int width = (wm.getDefaultDisplay().getWidth() / 3) * 2;
        popupWindow = new PopupWindow(convertView, width, (int) ((width / 5) * 3), true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL | Gravity.DISPLAY_CLIP_VERTICAL, 0, 0);
        mOk = (Button) convertView.findViewById(R.id.sure);
        mCancel = (Button) convertView.findViewById(R.id.cancel);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.show(MainActivity.this,
                        R.string.msg_success_logout);
                initDoorNeedSensor();
                ;
                O2OUtils.logout(MainActivity.this);
                PushUtils.initAlias(MainActivity.this);
                reflashState();
                PersonalCenterFragment fragment = (PersonalCenterFragment) getSupportFragmentManager().findFragmentByTag(PersonalCenterFragment.class.getName());
                fragment.reFlashUI();
                resetCurrUserAddr();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }


    private void loadMessageStatus() {
        if (!O2OUtils.isLogin(this)) {
            return;
        }
        if (!NetworkUtils.isNetworkAvaiable(this)) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        ApiUtils.post(this, URLConstants.MSG_STATUS,
                params,
                MessageStatusResult.class, responseListener(), errorListener());
    }

    private Response.Listener<MessageStatusResult> responseListener() {
        return new Response.Listener<MessageStatusResult>() {
            @Override
            public void onResponse(final MessageStatusResult response) {
                if (response != null && response.data != null) {
                    updateMessageStatus(response.data);
                }
            }
        };

    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
    }

    Handler updateMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //我的->系统消息，数字
            Fragment findFragment = getSupportFragmentManager().findFragmentByTag(PersonalCenterFragment.class.getName());
            if (null != findFragment && findFragment instanceof PersonalCenterFragment) {
                ((PersonalCenterFragment) findFragment).updateMsgCount((MessageStatusInfo) (msg.obj));
            }
        }
    };

    /**
     * 处理消失状态，显示数字
     */
    public void updateMessageStatus(MessageStatusInfo messageStatusInfo) {
        if (messageStatusInfo == null) {
            messageStatusInfo = new MessageStatusInfo();
        }
        setTabStatus(1, messageStatusInfo.cartGoodsCount);
        setTabStatus(3, messageStatusInfo.newMsgCount);
        Message msg = new Message();
        msg.obj = messageStatusInfo;
        updateMessageHandler.sendMessageDelayed(msg, 2000);
    }

    private void setTabStatus(int index, int num) {
        TextView tv = (TextView) mTabHost.getTabWidget().getChildTabViewAt(index).findViewById(R.id.tv_msg_count);
        if (num > 0) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(String.valueOf(num));
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadMessageStatus();
        PushUtils.init(this);
        if (intent.getIntExtra("cart", 0) == 1) {
//            mTabHost.getTabWidget().setCurrentTab(1);
            mTabHost.getTabWidget().getChildAt(1).performClick();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    selectAllCart();
                }
            }, 100);

        } else {
            mTabHost.getTabWidget().getChildAt(0).performClick();
        }
    }

    protected void selectAllCart() {
        try {
            ShoppingCartFragment fragment = (ShoppingCartFragment) getSupportFragmentManager().findFragmentByTag(ShoppingCartFragment.class.getName());
            if (fragment != null) {
                fragment.selectAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reflashDefaultAddress() {
        UserAddressInfo userAddressInfo = PreferenceUtils.getObject(this, UserAddressInfo.class);
        if (O2OUtils.isLogin(this) && userAddressInfo != null && userAddressInfo.id > 0) {
            return;
        }

        userAddressInfo = null;

        if (userAddressInfo == null) {
            LocAddressInfo locAddressInfo = PreferenceUtils.getObject(this, LocAddressInfo.class);
            if (locAddressInfo != null && locAddressInfo.isUsefulAddr()) {
                userAddressInfo = locAddressInfo.toUserAddr();
            }
        }
        if (userAddressInfo == null && O2OUtils.isLogin(this)) {
            UserInfo userInfo = PreferenceUtils.getObject(this, UserInfo.class);
            if (!ArraysUtils.isEmpty(userInfo.address)) {
                for (UserAddressInfo addressInfo : userInfo.address) {
                    if (addressInfo.isDefault) {
                        userAddressInfo = addressInfo;
                        break;
                    }
                }
            }
        }
        if (userAddressInfo != null) {
            PreferenceUtils.setObject(this, userAddressInfo);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LOC_REQUEST_CODE:
//                UserAddressInfo info = (UserAddressInfo) data.getSerializableExtra(Constants.EXTRA_DATA);

                initHomeTitle(null);
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomePageFragment.class.getName());
                if (null != fragment && fragment instanceof HomePageFragment)
                    ((HomePageFragment) fragment).reflashData();
                break;
        }
    }

    private void reloadHomePage() {
        if (mTabHost.getCurrentTab() != 0) {
            return;
        }
        try {
            if (YizanApp.getInstance().isNeedReload()) {
                YizanApp.getInstance().unNeedReload();
                HomePageFragment fragment = (HomePageFragment) getSupportFragmentManager().findFragmentByTag(HomePageFragment.class.getName());
                if (fragment != null) {
                    fragment.reflashData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private boolean needUpdate(String appVersion) {
        boolean bNeed = false;
        try {
            if (TextUtils.isEmpty(appVersion)) {
                return bNeed;
            }
            float newVersion = Float.parseFloat(appVersion);
            float currVersion = Float.parseFloat(DeviceUtils.getPackageInfo(MainActivity.this).versionName);
            if (newVersion > currVersion) {
                bNeed = true;
            }

        } catch (Exception e) {

        }
        return bNeed;
    }

    private void versionUpgrade() {
        AppUpdate.checkUpdate(this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessageStatus();
//        if (mMessageStatusInfo != null) {
//            mMessageStatusInfo.cartGoodsCount = ShoppingCartMgr.getInstance().getTotalCount(0);
//            updateMessageStatus();
//        }
        initDoorNeedSensor();
        setDoorArgentListener();
        checkNeedReloadHomePage();
    }

    protected void checkNeedReloadHomePage() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reloadHomePage();
            }
        }, 1);

    }

    /**
     * title地址显示
     */
    public void initHomeTitle(UserAddressInfo info) {
        if (info == null) {
            info = PreferenceUtils.getObject(MainActivity.this, UserAddressInfo.class);
        }
        if (info == null) {
            this.setTitle(R.string.main_sel_location);
            return;
        }
        String addr = TextUtils.isEmpty(info.detailAddress) ? info.address : info.detailAddress;
        if (TextUtils.isEmpty(addr)) {
            this.setTitle(R.string.main_sel_location);
        } else {
            if (addr.length() > 14) {
                this.setTitle(".." + addr.substring(addr.length() - 12, addr.length()));
            } else {
                this.setTitle(addr);
            }
        }
    }

    private void resetCurrUserAddr() {
        reflashDefaultAddress();
        mAddressInfo = PreferenceUtils.getObject(this, UserAddressInfo.class);
    }

    /**
     * 初始化消息接收器
     */
    private void initMessagePushReceiver() {
        mMessagePushReceiver = new MessagePushReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_MSG);
        registerReceiver(mMessagePushReceiver, filter);
    }

    MessagePushReceiver mMessagePushReceiver;

    /**
     * 消息Receiver
     */
    class MessagePushReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_MSG.equals(intent.getAction())) {
                loadMessageStatus();
            }
        }
    }
}
