package com.example.ztt.http.Request;

import com.example.ztt.http.base.Request;
import com.example.ztt.http.base.Response;

/**
 * Created by ztt on 16/7/3.
 */
public class StringRequest extends Request<String> {
    public StringRequest(HttpMethod method, String url,
                         RequestListener<String> listener) {
        super(method, url, listener);
    }

    @Override
    public String parseResponse(Response response) {

        return new String(response.getRawData());
    }
}
