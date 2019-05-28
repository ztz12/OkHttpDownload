package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yif
 * 基于OKHTTP的网络请求类
 */
public class HttpUtil {
    private OkHttpClient mHttpClient;
    private static volatile HttpUtil mInstance;
    //设置超时时长
    private static final long CONNECT_TIMEOUT = 60;
    //读取时长
    private static final long READ_TIMEOUT = 60;
    //写入时长
    private static final long WRITE_TIMEOUT = 60;

    /**
     * @param url        下载链接
     * @param startIndex 下载开始位置
     * @param endIndex   结束位置
     * @param callback   回调
     * @throws IOException
     */
    public void downloadFileRange(String url, long startIndex, long endIndex, Callback callback) throws IOException {
        //创建一个Request
        //设置分段下载信息，Range ，断点续传下载区间，如bytes= 0-1024
        Request request = new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();
        doAsync(request, callback);
    }

    /**
     * 获取资源大小
     *
     * @param url
     * @param callback
     */
    public void getContentLength(String url, Callback callback) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        doAsync(request, callback);
    }

    /**
     * 异步请求
     */
    public void doAsync(Request request, Callback callback) {
        //创建会话请求
        Call call = mHttpClient.newCall(request);
        //同步执行会话请求
        call.enqueue(callback);
    }

    /**
     * 同步请求
     */
    public Response doSync(Request request) throws IOException {
        //创建会话请求
        Call call = mHttpClient.newCall(request);
        //同步执行会话请求
        return call.execute();
    }

    /**
     * @return HttpUtil实例对象
     */
    public static HttpUtil getInstance() {
        if (null == mInstance) {
            synchronized (HttpUtil.class) {
                if (null == mInstance) {
                    mInstance = new HttpUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 构造方法,配置OkHttpClient
     */
    public HttpUtil() {
        //创建okHttpClient对象
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        mHttpClient = builder.build();
    }
}
