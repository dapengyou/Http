package com.example.ztt.http.core;

import android.util.Log;

import com.example.ztt.httpsimplenet.HttpStack.HttpStack;
import com.example.ztt.httpsimplenet.HttpStack.HttpStackFactory;
import com.example.ztt.httpsimplenet.base.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ztt on 16/5/8.
 */
public final class RequestQueue {
    //线程安全的请求队列
    private BlockingQueue<Request<?>> mRequestQueue =
            new PriorityBlockingQueue<Request<?>>();
    //请求的序列化生成器
    private AtomicInteger mSerialNumGenerator = new AtomicInteger(0);
    //默认的核心数 为CPU格式加1
    public static int DEFAULT_CORE_NUMS = Runtime.getRuntime().availableProcessors() + 1;
    //CPU核心数 + 1个分发线程数
    private int mDispatcherNums = DEFAULT_CORE_NUMS;
    //NetworkExecutor 执行网络请求的线程
    private NetworkExecutor[] mDispatchers = null;
    //Http请求的真正执行者
    private HttpStack mHttpStack;

    protected RequestQueue(int coreNums, HttpStack httpStack) {
        mDispatcherNums = coreNums;
        mHttpStack = httpStack != null ? httpStack : HttpStackFactory.createHttpStack();
    }

    //启动NetworkExecutor
    private final void startNetworkExecutors() {
        mDispatchers = new NetworkExecutor[mDispatcherNums];
        for (int i = 0; i < mDispatcherNums; i++) {
            mDispatchers[i] = new NetworkExecutor(mRequestQueue, mHttpStack);
            mDispatchers[i].start();
        }
    }

    public void start() {
        stop();
        startNetworkExecutors();
    }

    //停止NetworkExecutors
    public void stop() {
        if (mDispatchers != null && mDispatchers.length > 0) {
            for (int i = 0; i < mDispatchers.length; i++) {
                mDispatchers[i].quit();
            }
        }
    }
    //添加请求到队列中
    public void  addRequest(Request<?> request){
        if(!mRequestQueue.contains(request)){
            //为请求设置序列号
            request.setSerialNumber(this.generateSerialNumber());
            mRequestQueue.add(request);
        }else{
            Log.d("","### 请求队列中已经含有");
        }
    }
    //为每个请求生成一个系列号
    private int generateSerialNumber(){
        return mSerialNumGenerator.incrementAndGet();
    }

    public void clear() {
        mRequestQueue.clear();
    }

    public BlockingQueue<Request<?>> getAllRequests() {
        return mRequestQueue;
    }

}
