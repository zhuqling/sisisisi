package com.yizan.community.fragment;

import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.yizan.community.R;

/**
 * User: ldh (394380623@qq.com)
 * Date: 2015-09-17
 * Time: 11:14
 * FIXME
 */
public class CustomDialogFragment extends DialogFragment {

    private TextView mMsgTextView;
    @SuppressWarnings("unused")
    private AnimationDrawable mAnimation;

    public CustomDialogFragment() {
        setCancelable(true);
    }

    private static CustomDialogFragment dialog;

    public static void show(FragmentManager fragmentManager, int msg, String tag) {
        if (dialog == null) {
            dialog = new CustomDialogFragment();
            //http://stackoverflow.com/questions/28205335/handling-and-mitigating-illegalstateexception-failure-saving-state-active-fr
            dialog.setRetainInstance(true);
        }else {
            if(dialog.isAdded()){
                fragmentManager.beginTransaction().show(dialog);
                return;
            }
        }
        try {
            Bundle args = new Bundle(1);
            args.putInt("msg", msg);
            dialog.setArguments(args);
            dialog.show(fragmentManager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissDialog() {
        try {
            if (null != dialog)
                dialog.dismissAllowingStateLoss();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mDialog = new Dialog(getActivity(), R.style.Tantransparent);
        mDialog.setContentView(R.layout.loading_dialog);
        ImageView iv = (ImageView) mDialog.findViewById(R.id.loading_image);
        final AnimationDrawable dra = (AnimationDrawable) iv.getBackground();
        iv.post(new Runnable() {

            @Override
            public void run() {
                dra.start();

            }
        });

        mMsgTextView = (TextView) mDialog.findViewById(R.id.loading_msg);
        int msg;
        Bundle b = getArguments();
        if (null != b && 0 != (msg = b.getInt("msg")))
            mMsgTextView.setText(msg);

        mDialog.show();

        return mDialog;
    }

}
