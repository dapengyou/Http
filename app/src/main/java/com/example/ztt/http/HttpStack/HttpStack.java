package com.example.ztt.http.HttpStack;

import com.example.ztt.httpsimplenet.base.Request;
import com.example.ztt.httpsimplenet.base.Response;

/**
 * Created by ztt on 16/5/8.
 */
public interface HttpStack {
    /**
     * 执行Http请求
     * @param request 待执行的请求
     * @return  返回Response
     */
    public Response performRequest(Request<?> request);
}
