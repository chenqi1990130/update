package com.ycb.www.update;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.io.File;

/**
 * notification更新的广播接收者,根据action不同,做出的结果不同,
 * 其中intent因为是同一个intent的,所以并没有new 新的
 *
 */
public class UpdateReceiver extends BroadcastReceiver {


    private NotificationManager manager;
    private RemoteViews views;
    private Notification notification;


    @Override
        public void onReceive(Context context, Intent intent) {
        if(notification==null) {

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN)
                initNotification(context);//如果系统的版本大于android4.1 那么
            else{
                initNotificationForLowVersion(context);
            }
        }


        String action = intent.getAction();

            switch (action) {
                case "com.ycb.www.cancel":
                    manager.cancel(0);
                    UpdateService.downLoadHandler.cancel();
                    break;

                case "com.ycb.www.failed":

                    intent.setAction("com.ycb.www.restart");
                    PendingIntent failedpendingIntent=PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    views.setOnClickPendingIntent(R.id.ll_content, failedpendingIntent);
                    views.setTextViewText(R.id.tv_info, "下载失败,点击重试");
                    manager.notify(0, notification);

                    break;
                case "com.ycb.www.restart":

                    manager.cancel(0);
                    intent.setClass(context, UpdateService.class);
                    context.startService(intent);

                    break;
                case "com.ycb.www.install":

                    manager.cancel(0);

                    Intent startInstall =new Intent();
                    startInstall.setAction(Intent.ACTION_VIEW);
                    String filepath = intent.getStringExtra("filepath");
                    startInstall.setDataAndType(Uri.fromFile(new File(filepath)), "application/vnd.android.package-archive");
                    startInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startInstall);

                    break;
                case "com.ycb.www.complete":

                    intent.setAction("com.ycb.www.install");

                    PendingIntent pendingIntent=PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    views.setOnClickPendingIntent(R.id.ll_content, pendingIntent);
                    views.setTextViewText(R.id.tv_info, "下载完成,点击安装");
                    views.setProgressBar(R.id.progressBar, 100, 100, false);
                    manager.notify(0, notification);

                    break;

                case "com.ycb.www.updating":

                    int rate = intent.getIntExtra("rate", 0);
                    views.setTextViewText(R.id.tv_info,"正在下载...."+rate+"%");
                    views.setProgressBar(R.id.progressBar,100,rate,false);
                    manager.notify(0, notification);

            }

        }

    private void initNotificationForLowVersion(Context context) {//为低版本的兼容的notification手机模式

        //设置notifiction布局
        views = new RemoteViews(context.getPackageName(), R.layout.notification_update);

        notification=new Notification();

        notification.when=System.currentTimeMillis();

        notification.tickerText="xxxx新版正在下载";
        //设置view
        notification.contentView=views;
        //设置小图标
        notification.icon=R.drawable.ic_launcher;

        //设置布局文件中的textView的内容
        views.setTextViewText(R.id.tv_info, "下载中....0%");

        //设置布局文件中的ProgressBar进度
        views.setProgressBar(R.id.progressBar, 100, 0, false);

        //退出的intent
        Intent intent=new Intent("com.ycb.www.cancel");
        //退出的延迟意图
        PendingIntent mPendingIntent =PendingIntent.getBroadcast(context.getApplicationContext(),200,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        //点击之后退出
        views.setOnClickPendingIntent(R.id.ib_close, mPendingIntent);

    }


    /**
     * 初始化notification
     * @param context
     */
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private void initNotification(Context context){

                manager = (NotificationManager)context. getSystemService(Context.NOTIFICATION_SERVICE);

                views = new RemoteViews(context.getPackageName(), R.layout.notification_update);

                Notification.Builder builder=new Notification.Builder(context.getApplicationContext());

                notification = builder.setAutoCancel(false)//
                        .setSmallIcon(R.drawable.ic_launcher)//
                        .setContentText("下载中")//
                        .setContentTitle("下载")//
                        .setWhen(System.currentTimeMillis())//
                        .setTicker("xxxxx新版正在下载")//
                        .setContent(views)//
                        .build();//

                views.setTextViewText(R.id.tv_info, "下载中....0%");
                views.setProgressBar(R.id.progressBar, 100, 0, false);

                Intent intent=new Intent("com.ycb.www.cancel");
                PendingIntent mPendingIntent =PendingIntent.getBroadcast(context.getApplicationContext(),200,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                views.setOnClickPendingIntent(R.id.ib_close, mPendingIntent);

            }

}
