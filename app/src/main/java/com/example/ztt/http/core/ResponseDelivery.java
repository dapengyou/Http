package com.example.ztt.http.core;

import android.os.Handler;
import android.os.Looper;

import com.example.ztt.httpsimplenet.base.Request;
import com.example.ztt.httpsimplenet.base.Response;

import java.util.concurrent.Executor;


/**
 * Created by ztt on 16/5/8.
 */
//请求结果投递类，将请求结果投递给UI线程
public class ResponseDelivery implements Executor {
    //关联主线程消息队列的hander
    Handler mResponseHandler = new Handler(Looper.getMainLooper());

    /**
     * 处理请求结果，将请求结果投递给UI线程
     * @param request
     * @param response
     */
    public void deliveryResponse(final Request<?> request, final Response response) {
        Runnable respRunnable = new Runnable() {
            @Override
            public void run() {
                request.deliveryResponse(response);
            }
        };
        execute(respRunnable);
    }

    @Override
    public void execute(Runnable runnable) {
        mResponseHandler.post(runnable);
    }
}
