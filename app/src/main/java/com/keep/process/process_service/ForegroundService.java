package com.keep.process.process_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ForegroundService extends Service {

    private static final int SERVICE_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ForegroundServiceNew", "开启ForegroundService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ForegroundServiceNew", "销毁ForegroundService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //判断版本
        if (Build.VERSION.SDK_INT < 18) {//Android4.3以下版本

            //将Service设置为前台服务，可以取消通知栏消息
            startForeground(SERVICE_ID, new Notification());

        } else if (Build.VERSION.SDK_INT < 24) {//Android4.3 - 7.0之间
            //将Service设置为前台服务，可以取消通知栏消息
            startForeground(SERVICE_ID, new Notification());
            startService(new Intent(this, InnerService.class));

        } else {//Android 8.0以上
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel("channel","name",NotificationManager.IMPORTANCE_NONE);
                manager.createNotificationChannel(channel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channel");

                //将Service设置为前台服务,Android 8.0 App启动不会弹出通知栏消息，退出后台会弹出通知消息
                //Android9.0启动时候会立刻弹出通知栏消息
                startForeground(SERVICE_ID,new Notification());
            }
        }

        return START_STICKY;
    }

    public static class InnerService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(SERVICE_ID, new Notification());
            stopForeground(true);//移除通知栏消息
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

}
