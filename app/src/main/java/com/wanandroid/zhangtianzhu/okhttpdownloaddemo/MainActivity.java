package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 001;
    private TextView tv_file_name1, tv_progress1, tv_file_name2, tv_progress2;
    private Button btn_download1, btn_download2, btn_download_all;
    private ProgressBar pb_progress1, pb_progress2;

    private DownloadManager mDownloadManager;
    private String wechatUrl = "http://dldir1.qq.com/weixin/android/weixin703android1400.apk";
    private String qqUrl = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
    private DownloadManager.DownloadBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService(new Intent(MainActivity.this, DownloadManager.class), connection, BIND_AUTO_CREATE);
        initViews();
    }

    private void initDownload() {
        mDownloadManager = DownloadManager.getInstance();
        binder.add(wechatUrl, new DownloadListener() {
            @Override
            public void onFinished() {
                showToast(MainActivity.this, "下载完成");
            }

            @Override
            public void onProgress(float progress) {
                pb_progress1.setProgress((int) (progress * 100));
                tv_progress1.setText(String.format("%.2f", progress * 100) + "%");
            }

            @Override
            public void onPause() {
                showToast(MainActivity.this, "下载暂停");
            }

            @Override
            public void onCancel() {
                tv_progress1.setText("0%");
                pb_progress1.setProgress(0);
                btn_download1.setText("下载");
                showToast(MainActivity.this, "下载取消");
            }
        });

        binder.add(qqUrl, new DownloadListener() {
            @Override
            public void onFinished() {
                showToast(MainActivity.this, "下载完成");
            }

            @Override
            public void onProgress(float progress) {
                pb_progress2.setProgress((int) (progress * 100));
                tv_progress2.setText(String.format("%.2f", progress * 100) + "%");
            }

            @Override
            public void onPause() {
                showToast(MainActivity.this, "下载暂停");
            }

            @Override
            public void onCancel() {
                tv_progress2.setText("0%");
                pb_progress2.setProgress(0);
                btn_download2.setText("下载");
                showToast(MainActivity.this, "下载取消");
            }
        });
    }

    /**
     * 初始化View控件
     */
    private void initViews() {
        tv_file_name1 = findViewById(R.id.tv_file_name1);
        tv_progress1 = findViewById(R.id.tv_progress1);
        pb_progress1 = findViewById(R.id.pb_progress1);
        btn_download1 = findViewById(R.id.btn_download1);
        tv_file_name1.setText("微信");

        tv_file_name2 = findViewById(R.id.tv_file_name2);
        tv_progress2 = findViewById(R.id.tv_progress2);
        pb_progress2 = findViewById(R.id.pb_progress2);
        btn_download2 = findViewById(R.id.btn_download2);
        tv_file_name2.setText("qq");

        btn_download_all = findViewById(R.id.btn_download_all);

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            binder = (DownloadManager.DownloadBinder) service;
            initDownload();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /**
     * 下载或暂停下载
     *
     * @param view
     */
    public void downloadOrPause(View view) {
        switch (view.getId()) {
            case R.id.btn_download1:
                if (!binder.isDownloading(wechatUrl)) {
                    binder.download(wechatUrl);
                    btn_download1.setText("暂停");

                } else {
                    btn_download1.setText("下载");
                    binder.pause(wechatUrl);
                }
                break;
            case R.id.btn_download2:
                if (!binder.isDownloading(qqUrl)) {
                    binder.download(qqUrl);
                    btn_download2.setText("暂停");
                } else {
                    btn_download2.setText("下载");
                    binder.pause(qqUrl);
                }
                break;
            default:
                break;
        }
    }

    public void downloadOrPauseAll(View view) {
        if (!binder.isDownloading(wechatUrl, qqUrl)) {
            btn_download1.setText("暂停");
            btn_download2.setText("暂停");
            btn_download_all.setText("全部暂停");
            binder.download(wechatUrl, qqUrl);
        } else {
            binder.pause(wechatUrl, qqUrl);
            btn_download1.setText("下载");
            btn_download2.setText("下载");
            btn_download_all.setText("全部下载");
        }
    }

    /**
     * 取消下载
     *
     * @param view
     */
    public void cancel(View view) {

        switch (view.getId()) {
            case R.id.btn_cancel1:
                binder.cancel(wechatUrl);
                break;
            case R.id.btn_cancel2:
                binder.cancel(qqUrl);
                break;
            default:
                break;
        }
    }

    public void cancelAll(View view) {
        binder.cancel(wechatUrl, qqUrl);
        btn_download1.setText("下载");
        btn_download2.setText("下载");
        btn_download_all.setText("全部下载");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        //针对android6.0动态检测申请权限
        if (!checkPermission(permission)) {
            if (shouldShowRationale(permission)) {
                showToast(this, "需要权限...");
            }
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cancelAll(null);
        unbindService(connection);
    }


    /**
     * 检测用户权限
     *
     * @param permission
     * @return
     */
    protected boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 是否需要显示请求权限的理由
     *
     * @param permission
     * @return
     */
    protected boolean shouldShowRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    /**
     * 避免多次点击重复弹吐司
     */
    private static Toast toast;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
