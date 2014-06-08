package com.gaya.newsapplication.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.gaya.newsapplication.util.AppUtils;

import android.util.Log;

/**
 * This class does the web service calls related to the application.
 * 
 * @author Gaya
 * 
 */
public class HttpServiceHandler {

    private static final String TAG      = HttpServiceHandler.class.getName();
    private String              response = null;

    /**
     * Initiate the web service call to the server.
     * 
     * @param url
     *            server URL
     * @return json string returned from the server.
     */
    public String invokeRequest(String url) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpEntity httpEntity = null;
        HttpResponse httpResponse = null;

        HttpGet httpGet = new HttpGet(url);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, AppUtils.TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, AppUtils.TIMEOUT);

        try {
            httpResponse = httpClient.execute(httpGet);
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

        } catch (ClientProtocolException e) {
            Log.e(TAG, "Client Protocol Exception");
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
        } catch (Exception e) {
            Log.e(TAG, "Exception occured");

        }

        return response;
    }

}
