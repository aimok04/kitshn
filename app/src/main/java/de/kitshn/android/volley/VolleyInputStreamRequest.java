package de.kitshn.android.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class VolleyInputStreamRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private final Map<String, String> mParams;

    public Map<String, String> responseHeaders;

    public VolleyInputStreamRequest(int method, String mUrl, Response.Listener<byte[]> listener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        super(method, mUrl, errorListener);

        setShouldCache(false);
        mListener = listener;
        mParams = params;
    }

    @Override
    protected Map<String, String> getParams() {
        return mParams;
    }

    ;


    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        responseHeaders = response.headers;
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
