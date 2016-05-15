package com.example.ztt.http.core;

import com.example.ztt.httpsimplenet.HttpStack.HttpStack;
import com.example.ztt.httpsimplenet.base.Request;
import com.example.ztt.httpsimplenet.base.Response;
import com.example.ztt.httpsimplenet.cache.Cache;
import com.example.ztt.httpsimplenet.cache.LruMemCache;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ztt on 16/5/8.
 */

//网络请求Executor，继承自Thread，从网络请求队列中循环读取请求并执行
final class NetworkExecutor extends Thread {
    //网络请求队列
    private BlockingQueue<Request<?>> mRequestQueue;
    //网络请求站
    private HttpStack mHttpStack;
    //结果分发器，将结果投递到主线程
    private static ResponseDelivery mResponseDelivery = new ResponseDelivery();
    //请求缓存
    private static Cache<String, Response> mReqCache = new LruMemCache();
    //是否停止
    private boolean isStop = false;

    public NetworkExecutor(BlockingQueue<Request<?>> queue, HttpStack httpStack) {
        mRequestQueue = queue;
        mHttpStack = httpStack;
    }

    @Override
    public void run() {
        try {
            while (!isStop) {
                /**
                 * 取走BlockingQueue里排在首位的对象,
                 * 若BlockingQueue为空,阻断进入等待状态直到Blocking有新的对象被加入为止
                 */
                final Request<?> request = mRequestQueue.take();
                Response response = null;
                //取消执行了
                if (request.isCanceled()) {
                    continue;
                }

                if (isUseCache(request)) {
                    //从缓存中取
                    response = mReqCache.get(request.getUrl());
                } else {
                    //从网络上获取数据
                    response = mHttpStack.performRequest(request);
                    //如果该请求需要缓存，那么请求成功则缓存到mResponseCache中
                    if (request.shouldCache() && isSuccess(response)) {
                        mReqCache.put(request.getUrl(), response);
                    }
                }
                //分发请求结果
                mResponseDelivery.deliveryResponse(request, response);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isSuccess(Response response) {
        return response != null && response.getStatusCode() == 200;
    }

    private boolean isUseCache(Request<?> request) {
        return request.shouldCache() && mReqCache.get(request.getUrl()) != null;
    }
    public void quit(){
        isStop = true;
        interrupt();
    }
}
