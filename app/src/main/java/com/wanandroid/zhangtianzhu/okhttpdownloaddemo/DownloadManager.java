package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载管理器，断点续传
 *
 * @author yif
 */
public class DownloadManager extends Service {
    /**
     * key为url，用来区别下载文件
     */
    private Map<String, DownloadTask> mDownloadTaskMap;
    private static volatile DownloadManager mInstance;
    private String sdcardDir;
    private DownloadBinder binder = new DownloadBinder();

    public static DownloadManager getInstance() {//管理器初始化
        if (mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownloadManager();
                }
            }
        }
        return mInstance;
    }

    public DownloadManager() {
        mDownloadTaskMap = new HashMap<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class DownloadBinder extends Binder {

        /**
         * 下载文件
         * 单任务下载或者多任务开启下载
         *
         * @param urls
         */
        public void download(String... urls) {
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                if (mDownloadTaskMap.containsKey(url)) {
                    mDownloadTaskMap.get(url).start();
                }
            }
        }

        /**
         * 获取下载文件名称
         *
         * @return
         */
        public String getFileName(String url) {
            return url.substring(url.lastIndexOf("/") + 1);
        }

        public void pause(String... urls) {
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                if (mDownloadTaskMap.containsKey(url)) {
                    mDownloadTaskMap.get(url).pause();
                }
            }
        }

        public void cancel(String... urls) {
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                if (mDownloadTaskMap.containsKey(url)) {
                    mDownloadTaskMap.get(url).cancel();
                }
            }
        }

        /**
         * 添加下载任务
         */
        public void add(String url, DownloadListener l) {
            add(url, null, null, l);
        }

        /**
         * 添加下载任务
         */
        public void add(String url, String filePath, DownloadListener l) {
            add(url, filePath, null, l);
        }

        /**
         * 添加下载任务
         */
        public void add(String url, String filePath, String fileName, DownloadListener l) {
            if (TextUtils.isEmpty(filePath)) {
                if (checkPath()) {
                    filePath = sdcardDir;
                }
            }

            if (TextUtils.isEmpty(fileName)) {
                fileName = getFileName(url);
            }

            mDownloadTaskMap.put(url, new DownloadTask(new FilePoint(url, filePath, fileName), l));
        }

        /**
         * 判断是否正在下载
         * 这里传一个url就是判断一个下载任务
         * 多个url数组适合下载管理器判断是否作操作全部下载或全部暂停下载
         *
         * @param urls
         * @return
         */
        public boolean isDownloading(String... urls) {
            boolean result = false;
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                if (mDownloadTaskMap.containsKey(url)) {
                    result = mDownloadTaskMap.get(url).isDonwloading();
                }
            }
            return result;
        }

    }

    /**
     * 判断sd卡路径是否可用
     *
     * @return 可用返回true，不可用返回false
     */
    private boolean checkPath() {
        long resSize = 1024 * 1024 * 1024L;
        String path = FileStorgeUtil.judgeExrernal(resSize);
        switch (path) {
            case FileStorgeUtil.TOO_SMART:
                return false;
            case FileStorgeUtil.NULL_PATH:
                return false;
            default:
                sdcardDir = path + "Resource" + File.separator;
                return true;
        }
    }

}
