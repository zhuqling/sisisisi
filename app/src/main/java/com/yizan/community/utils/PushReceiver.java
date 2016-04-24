package com.yizan.community.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;

import com.yizan.community.R;
import com.yizan.community.activity.MainActivity;
import com.yizan.community.activity.OrderDetailActivity;
import com.yizan.community.activity.WebViewActivity;
import com.fanwe.seallibrary.comm.Constants;

import java.io.IOException;

import cn.jpush.android.api.JPushInterface;

/**
 * push广播接收器
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            // 接收Registeration

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            // 接收到推送下来的自定义消息
            // FIXME: 15/11/14 服务人员接单时，推送即收到了自定义消息，又收到了通知
            context.sendBroadcast(new Intent(Constants.ACTION_MSG));
//            refresh(context, extras);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
                .getAction())) {
            // 接收到推送下来的通知
            refresh(context, extras);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
                .getAction())) {
            // 用户点击打开了通知
            startIntebt(context, extras);
        }

    }

    /**
     * 消息处理
     * @param context
     * @param extras
     */
    private void refresh(final Context context, String extras) {
        try {
            JSONObject jsonObject = new JSONObject(extras);
            String type = "";
            String args = "";
            String sound = "";
            if (jsonObject.has("type"))
                type = jsonObject.getString("type");
            if (jsonObject.has("args")) {
                args = jsonObject.getString("args");
            }
            if(jsonObject.has("sound")){
                sound = jsonObject.getString("sound");
                if(!TextUtils.isEmpty(sound)){
                    if(sound.compareTo("music2.caf") == 0){
                        playLocalMp3(context, R.raw.push_music2);
                    }else if(sound.compareTo("music3.caf") == 0){
                        playLocalMp3(context, R.raw.push_music3);
                    }
                }
            }
            if (type.equals("4")) {
                Intent mIntent = new Intent(Constants.ACTION_NAME);
                context.sendBroadcast(mIntent);
                return;
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据消息处理跳转
     * @param context
     * @param extras
     */
    private void startIntebt(Context context, String extras) {
        Intent i = null;

        try {
            JSONObject jsonObject = new JSONObject(extras);
            String type = "";
            String args = "";
            if (jsonObject.has("type"))
                type = jsonObject.getString("type");
            if (jsonObject.has("args"))
                args = jsonObject.getString("args");
            if (type.equals("1")) {
                i = new Intent(context, MainActivity.class);
            } else if (type.equals("2")) {
                i = new Intent(context, WebViewActivity.class);
                i.putExtra(Constants.EXTRA_URL, args);

            } else if (type.equals("3")) {
                i = new Intent(context, OrderDetailActivity.class);
                i.putExtra(Constants.EXTRA_DATA, Integer.parseInt(args));
            } else if (type.equals("4")) {
                Intent mIntent = new Intent(Constants.ACTION_NAME);
                context.sendBroadcast(mIntent);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (i != null) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            JPushInterface.clearAllNotifications(context);
        }

    }

    static MediaPlayer mediaPlayer = null;

    /**
     * 播放声音文件
     * @param context
     * @param musicId
     */
    public void playLocalMp3(Context context, int musicId) {
        /**
         * 创建音频文件的方法：
         * 1、播放资源目录的文件：MediaPlayer.create(MainActivity.this,R.raw.beatit);//播放res/raw 资源目录下的MP3文件
         * 2:播放sdcard卡的文件：mediaPlayer=new MediaPlayer();
         *   mediaPlayer.setDataSource("/sdcard/beatit.mp3");//前提是sdcard卡要先导入音频文件
         */

        mediaPlayer = MediaPlayer.create(context, musicId);
        mediaPlayer.stop();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();//释放音频资源
            }
        });
        try {
            //在播放音频资源之前，必须调用Prepare方法完成些准备工作
            mediaPlayer.prepare();
            //开始播放音频
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
