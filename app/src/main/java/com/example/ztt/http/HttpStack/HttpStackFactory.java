package com.example.ztt.http.HttpStack;

import android.os.Build;

/**
 * Created by ztt on 16/5/8.
 */
public final class HttpStackFactory {
    private static final int GINGERBREAD_SDK_NUM = 9;

    /**
     * \
     * 根据SDK版本号来创建不同的HTTP执行器，即SDK 9 之前使用HttpClient，
     * 之后则使用HttpUrlConnection
     *
     * @return 具体的HttpStack
     */
    public static HttpStack createHttpStack() {
        //获取当前手机版本
        int runtimeSDKApi = Build.VERSION.SDK_INT;

        if (runtimeSDKApi >= GINGERBREAD_SDK_NUM) {
            return new HttpUrlConnStack();
        }
        return new HttpClientStack();
    }
}
