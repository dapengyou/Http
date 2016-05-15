package com.example.ztt.http.base;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ztt on 16/5/7.
 */
public abstract class Request<T> implements Comparable<Request<T>> {
    public static enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        private String mHttpMethod = "";

        private HttpMethod(String method) {
            mHttpMethod = method;
        }
    }

    public static enum Priority {
        LOW,
        NORMAL,
        HIGN,
        IMMEDIATE
    }

    //默认的编码方式
    private static final String DEFAULT_PARAMS_ENCODING = "UTF_8";
    //默认的类型
    public final static String HEADER_CONTENT_TYPE = "Content-Type";
    //请求序列号
    protected int mSerialNum = 0;
    //优先级默认设置为Normal
    protected Priority mPriority = Priority.NORMAL;
    //是否取消该请求
    protected boolean isCancel = false;
    //该请求是否应该缓存
    private boolean mShouldCache = true;
    //请求Listener
    protected RequestListener<T> mRequestListener;
    //请求的url
    private String mUrl = "";
    //请求的方法
    HttpMethod mHttpMethod = HttpMethod.GET;
    //请求的header
    private Map<String, String> mHeaders = new HashMap<String, String>();
    //请求参数
    private Map<String, String> mBodyParams = new HashMap<String, String>();

    /**
     * @param method   请求方式
     * @param url      请求的目标URL
     * @param listener 请求回调，将结果回调给用户
     */
    public Request(HttpMethod method, String url, RequestListener<T> listener) {
        mHttpMethod = method;
        mUrl = url;
        mRequestListener = listener;
    }

    //从原生的网络请求中解析结果子类必须复写
    public abstract T parseResponse(Response response);

    //处理Response,该方法需要运行在UI线程
    public final void deliveryResponse(Response response) {
        //解析得到请求结果
        T result = parseResponse(response);
        if (mRequestListener != null) {
            int stCode = response != null ? response.getStatusCode() : -1;
            String msg = response != null ? response.getMessage() : "unkown error";
            mRequestListener.onComplete(stCode, result, msg);
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public RequestListener<T> getRequestListener() {
        return mRequestListener;
    }

    public int getSerialNumber() {
        return mSerialNum;
    }

    public void setSerialNumber(int mSerialNum) {
        this.mSerialNum = mSerialNum;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority mPriority) {
        this.mPriority = mPriority;
    }

    //获得编码方式
    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public HttpMethod getHttpMethod() {
        return mHttpMethod;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public Map<String, String> getParams() {
        return mBodyParams;
    }

    public boolean isHttps() {
        return mUrl.startsWith("https");
    }

    public void cancel() {
        isCancel = true;
    }

    public boolean isCanceled() {
        return isCancel;
    }

    /**
     * 该请求是否应该缓存
     *
     * @param shouldCache
     */
    public void setShouldCache(boolean shouldCache) {
        this.mShouldCache = shouldCache;
    }

    public boolean shouldCache() {
        return mShouldCache;
    }


    /**
     * 返回POST或者PUT请求时的Body参数字节数组
     */
    public byte[] getBody() {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * 将参数转换为URL编码的参数串，格式为key1=value1&key2=value2
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    @Override
    public int compareTo(Request<T> another) {
        Priority myPriority = this.getPriority();
        Priority anotherPriority = another.getPriority();
        // 如果优先级相等,那么按照添加到队列的序列号顺序来执行
        return myPriority.equals(anotherPriority) ? this.getSerialNumber()
                - another.getSerialNumber()
                : myPriority.ordinal() - anotherPriority.ordinal();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mHeaders == null) ? 0 : mHeaders.hashCode());
        result = prime * result + ((mHttpMethod == null) ? 0 : mHttpMethod.hashCode());
        result = prime * result + ((mBodyParams == null) ? 0 : mBodyParams.hashCode());
        result = prime * result + ((mPriority == null) ? 0 : mPriority.hashCode());
        result = prime * result + (mShouldCache ? 1231 : 1237);
        result = prime * result + ((mUrl == null) ? 0 : mUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Request<?> other = (Request<?>) obj;
        if (mHeaders == null) {
            if (other.mHeaders != null)
                return false;
        } else if (!mHeaders.equals(other.mHeaders))
            return false;
        if (mHttpMethod != other.mHttpMethod)
            return false;
        if (mBodyParams == null) {
            if (other.mBodyParams != null)
                return false;
        } else if (!mBodyParams.equals(other.mBodyParams))
            return false;
        if (mPriority != other.mPriority)
            return false;
        if (mShouldCache != other.mShouldCache)
            return false;
        if (mUrl == null) {
            if (other.mUrl != null)
                return false;
        } else if (!mUrl.equals(other.mUrl))
            return false;
        return true;
    }


    /**
     * 网络请求Listener，会被执行在UI线程
     */
    public static interface RequestListener<T> {
        //请求完成的回调
        public void onComplete(int stCode, T response, String errMsg);
    }
}

