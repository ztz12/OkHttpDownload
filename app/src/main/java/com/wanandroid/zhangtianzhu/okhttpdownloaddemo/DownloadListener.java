package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

/**
 * @author yif
 *  下载监听
 */
public interface DownloadListener {
    /**
     * 下载完成
     */
    void onFinished();

    /**
     * 下载进度
     * @param progress
     */
    void onProgress(float progress);

    /**
     * 下载暂停
     */
    void onPause();

    /**
     * 下载取消
     */
    void onCancel();
}
