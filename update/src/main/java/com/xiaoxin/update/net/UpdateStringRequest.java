package com.xiaoxin.update.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.nio.charset.StandardCharsets;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public class UpdateStringRequest extends StringRequest {

    public UpdateStringRequest(
            String url,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener) {
        super(Method.GET, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success(new String(response.data, StandardCharsets.UTF_8),
                HttpHeaderParser.parseCacheHeaders(response));
    }
}
