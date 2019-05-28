package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import android.os.Handler;
import android.os.Message;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author yif
 * 下载任务类
 */
public class DownloadTask extends Handler {
    //线程数量
    private static final int THREAD_COUNT = 4;

    private FilePoint mPoint;
    /**
     * 下载文件总大小
     */
    private long mFileLength;

    private volatile boolean isDonwloading = false;
    /**
     * 子线程取消数量
     */
    private AtomicInteger childCancelCount = new AtomicInteger(0);
    /**
     * 子线程暂停数量
     */
    private AtomicInteger childPauseCount = new AtomicInteger(0);
    /**
     * 子线程完成数量
     */
    private AtomicInteger childFinishCount = new AtomicInteger(0);
    private HttpUtil mHttpUtil;
    private long[] mProgress;

    private File[] mCacheFile;
    /**
     * 在本地创建一个与资源大小相同的文件来占位
     */
    private File mTempFile;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    /**
     * 下载进度
     */
    private static final int MSG_PROGRESS = 1;
    /**
     * 下载完成
     */
    private static final int MSG_FINISH = 2;
    /**
     * 下载暂停
     */
    private static final int MSG_PAUSE = 3;
    /**
     * 下载取消
     */
    private static final int MSG_CANCEL = 4;
    /**
     * 回调接口
     */
    private DownloadListener mDownloadListener;

    public DownloadTask(FilePoint point, DownloadListener l) {
        this.mPoint = point;
        this.mDownloadListener = l;
        this.mProgress = new long[THREAD_COUNT];
        this.mCacheFile = new File[THREAD_COUNT];
        this.mHttpUtil = HttpUtil.getInstance();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (null == mDownloadListener) {
            return;
        }
        switch (msg.what) {
            case MSG_PROGRESS:
                long progress = 0;
                for (int i = 0; i < mProgress.length; i++) {
                    progress += mProgress[i];
                }
                mDownloadListener.onProgress(progress * 1.0f / mFileLength);
                break;
            case MSG_FINISH:
                if (confrimStatus(childFinishCount)) {
                    return;
                }
                //下载完成重命名
                mTempFile.renameTo(new File(mPoint.getFilePath(), mPoint.getFileName()));
                resetStatus();
                mDownloadListener.onFinished();
                break;
            //只有所有线程都暂停，才是真正的暂停
            case MSG_PAUSE:
                if (confrimStatus(childPauseCount)) {
                    //线程还有没有暂停的就返回
                    return;
                }
                resetStatus();
                mDownloadListener.onPause();
                break;
            case MSG_CANCEL:
                if (confrimStatus(childCancelCount)) {
                    return;
                }
                resetStatus();
                mProgress = new long[THREAD_COUNT];
                mDownloadListener.onCancel();
                break;
            default:
                break;
        }
    }

    public synchronized void start() {
        try {
            if (isDonwloading) {
                return;
            }
            isDonwloading = true;
            mHttpUtil.getContentLength(mPoint.getUrl(), new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    resetStatus();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() != 200) {
                        close(response.body());
                        resetStatus();
                        return;
                    }
                    //下载文件大小
                    mFileLength = response.body().contentLength();
                    close(response.body());
                    // 在本地创建一个与资源同样大小的文件来占位
                    mTempFile = new File(mPoint.getFilePath(), mPoint.getFileName() + ".tmp");
                    if (!mTempFile.getParentFile().exists()) {
                        mTempFile.getParentFile().mkdirs();
                    }
                    RandomAccessFile tmpAccessFile = new RandomAccessFile(mTempFile, "rw");
                    tmpAccessFile.setLength(mFileLength);
                    //将下载任务分配给线程
                    long blockSize = mFileLength / THREAD_COUNT;
                    for (int threadId = 0; threadId < THREAD_COUNT; threadId++) {
                        long startIndex = threadId * blockSize;
                        long endIndex = (threadId + 1) * blockSize - 1;
                        //如果是最后一个线程，那么剩下任务都交给它完成
                        if (threadId == (THREAD_COUNT - 1)) {
                            endIndex = mFileLength - 1;
                        }
                        downLoad(startIndex, endIndex, threadId);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            resetStatus();
        }
    }

    /**
     * 各线程下载指定好的文件内容，并写入指定的空文件位置，直到四个线程都下载完毕，这个空文件就相当于我们完整的文件，只是格式不对，重命名就可以
     *
     * @param startIndex
     * @param endIndex
     * @param threadId
     * @throws IOException
     */
    private void downLoad(final long startIndex, long endIndex, final int threadId) throws IOException {
        long newStartIndex = startIndex;
        //分段进行下载请求，分段将文件保存到本地
        //加载下载位置缓存文件
        final File cacheFile = new File(mPoint.getFilePath(), "thread" + threadId + "_" + mPoint.getFileName() + ".cache");
        mCacheFile[threadId] = cacheFile;
        final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rwd");
        //如果文件存在,比如当前文件下载一半，退出，避免重复下载存在的文件部分
        if (cacheFile.exists()) {
            String startIndexStr = cacheAccessFile.readLine();
            try {
                //重新设置下载起点
                newStartIndex = Integer.parseInt(startIndexStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        final long finalStartIndex = newStartIndex;
        mHttpUtil.downloadFileRange(mPoint.getUrl(), finalStartIndex, endIndex, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isDonwloading = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //返回状态码206表示服务端支持断点下载
                if (response.code() != 206) {
                    resetStatus();
                    return;
                }
                //获取流
                InputStream is = response.body().byteStream();
                //获取前面创建的文件
                RandomAccessFile tmpAccessFile = new RandomAccessFile(mTempFile, "rw");
                //文件写入的开始位置
                tmpAccessFile.seek(finalStartIndex);
                //将网络流写入到本地
                byte[] bytes = new byte[1024 << 2];
                int length = -1;
                //记录下载文件大小
                int total = 0;
                long progress = 0;
                while ((length = is.read(bytes)) > 0) {
                    //下载取消，关闭资源，并删除文件
                    if (isCancel) {
                        close(cacheAccessFile, is, response.body());
                        deleteFile(cacheFile);
                        sendEmptyMessage(MSG_CANCEL);
                        return;
                    }

                    if (isPause) {
                        close(cacheAccessFile, is, response.body());
                        sendEmptyMessage(MSG_PAUSE);
                        return;
                    }
                    tmpAccessFile.write(bytes, 0, length);
                    total += length;
                    progress = finalStartIndex + total;

                    //将该线程最新完成下载的位置记录并保存到缓存数据文件中,将指针移动到当前下载的缓存数据起始位置，并写入文件中
                    cacheAccessFile.seek(0);
                    cacheAccessFile.write((progress + "").getBytes("UTF-8"));
                    //发送进度消息
                    mProgress[threadId] = progress - startIndex;
                    sendEmptyMessage(MSG_PROGRESS);
                }

                close(cacheAccessFile, is, response.body());
                deleteFile(cacheFile);
                sendEmptyMessage(MSG_FINISH);
            }
        });
    }

    /**
     * try-with-resources语句是一个声明一个或多个资源的 try 语句。一个资源作为一个对象，必须在程序结束之后随之关闭。
     * try-with-resources语句确保在语句的最后每个资源都被关闭 。任何实现了 java.lang.AutoCloseable的对象, 包括所有实现了 java.io.Closeable 的对象,
     * 都可以用作一个资源。
     * 关闭资源
     *
     * @param closeables 自动执行资源关闭过程
     */
    private void close(Closeable... closeables) {
        int length = closeables.length;
        try {
            for (int i = 0; i < length; i++) {
                Closeable closeable = closeables[i];
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < length; i++) {
                closeables[i] = null;
            }
        }
    }

    /**
     * 删除文件
     *
     * @param files
     */
    private void deleteFile(File... files) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file != null) {
                file.delete();
            }
        }
    }

    public void pause() {
        isPause = true;
    }

    public void cancel() {
        isCancel = true;
        deleteFile(mTempFile);
        if (!isDonwloading) {
            if (mDownloadListener != null) {
                deleteFile(mCacheFile);
                resetStatus();
                mDownloadListener.onCancel();
            }
        }
    }

    /**
     * 重置下载状态
     */
    private void resetStatus() {
        isPause = false;
        isCancel = false;
        isDonwloading = false;
    }

    /**
     * 确认下载状态，四个线程都停止，取消或者下载完成，返回false，否则返回true表示没有线程任务没有完成
     *
     * @param count
     * @return
     */
    private boolean confrimStatus(AtomicInteger count) {
        return count.incrementAndGet() % THREAD_COUNT != 0;
    }

    public boolean isDonwloading() {
        return isDonwloading;
    }
}
