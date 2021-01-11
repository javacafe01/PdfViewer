package com.gsnathan.pdfviewer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class is used to get a PDF File from an URL
 */
public class DownloadPDFFile extends AsyncTask<String, Void, Object> {

    private final WeakReference<MainActivity> mainActivityWR;

    public DownloadPDFFile(MainActivity activity) {
        mainActivityWR = new WeakReference<>(activity);
    }

    @Override
    protected Object doInBackground(String... strings) {
        String url = strings[0];
        HttpURLConnection httpConnection = null;

        try {
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.connect();
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HTTP_OK) {
                return Utils.readBytesToEnd(httpConnection.getInputStream());
            } else {
                Log.e("DownloadPDFFile", "Error during http request, response code : " + responseCode);
                return responseCode;
            }
        } catch (IOException e) {
            Log.e("DownloadPDFFile", "Error cannot get file at URL : " + url, e);
            return e;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        MainActivity activity = mainActivityWR.get();

        if (activity != null) {
            activity.hideProgressBar();

            if (result == null) {
                Toast.makeText(activity, R.string.toast_generic_download_error, Toast.LENGTH_LONG).show();
            } else if (result instanceof Integer) {
                Toast.makeText(activity, R.string.toast_http_code_error, Toast.LENGTH_LONG).show();
            } else if (result instanceof SSLException) {
                Toast.makeText(activity, R.string.toast_ssl_error, Toast.LENGTH_LONG).show();
            } else if (result instanceof IOException) {
                Toast.makeText(activity, R.string.toast_generic_download_error, Toast.LENGTH_LONG).show();
            } else if (result instanceof byte[]) {
                activity.saveToFileAndDisplay((byte[]) result);
            }
        }
    }
}
