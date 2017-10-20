package com.ycb.www.update;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.NumberFormat;

/**
 * 下载的services,配合xutils的httputils使用,完成notification的下载功能
 */
public class UpdateService extends Service {

    //是否已经开始下载
    private boolean isBegin = false;


    //这个意图是做什么的？？？
    Intent intent;

    private NumberFormat numberFormat;

    public static Callback.Cancelable downLoadHandler;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.d("tag---------------onCreate");

        intent = new Intent();
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String downUrl = intent.getStringExtra("downUrl");

        LogUtil.d("tag---------------onStartCommand--downUrl-->"+downUrl);
        //如果下载地址为空,则什么都不干
        if (TextUtils.isEmpty(downUrl)) {
            stopSelf();
            //            throw  new IllegalArgumentException("the download url is empty!!!!");
            return START_STICKY_COMPATIBILITY;//START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
        }
        if (isBegin) {
            //此时已经开始了
            LogUtil.d("tag--isBegin是true  已经开始下载了 ------");

            return START_STICKY_COMPATIBILITY;//START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。

        } else {
            isBegin = true;
        }

        downLoad(downUrl);

        return super.onStartCommand(intent, flags, startId);
    }


    private void downLoad(final String downUrl) {

        Log.i("tag", "开始下载!!!");

        RequestParams requestParams = new RequestParams(downUrl);//一个普通的get请求。

        downLoadHandler = x.http().get(requestParams, new Callback.ProgressCallback<File>() {

            @Override
            public void onSuccess(File result) {

                intent.setAction("com.ycb.www.complete");

                intent.putExtra("filepath", result.getAbsolutePath());

                Log.i("tag", result.getAbsolutePath());

                sendBroadcast(intent);

                stopSelf();

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                intent.setAction("com.ycb.www.failed");

                intent.putExtra("downUrl", downUrl);

                //Log.i("tag", "onFailure!!!");

                sendBroadcast(intent);

            }

            @Override
            public void onCancelled(CancelledException cex) {

                Log.i("tag", "onCancelled");

                stopSelf();
            }

            @Override
            public void onFinished() {

                Log.i("tag", "onFinished");

                stopSelf();
            }

            @Override
            public void onWaiting() {

                Log.i("tag", "onWaiting");//这个在onstart之前


            }

            @Override
            public void onStarted() {

                Log.i("tag", "Started");

                intent.putExtra("rate", 0);
                intent.setAction("com.ycb.www.updating");//发送一个正在下载更新的广播

                sendBroadcast(intent);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

                Double rate = (double) current / (double) total;

                String format = numberFormat.format(rate);

                int r = (int) (Double.valueOf(format) * 100);
                //下载的百分比

                Log.i("tag", "" + r);

                intent.putExtra("rate", r);
                intent.setAction("com.ycb.www.updating");

                sendBroadcast(intent);

            }
        });


    }


}
