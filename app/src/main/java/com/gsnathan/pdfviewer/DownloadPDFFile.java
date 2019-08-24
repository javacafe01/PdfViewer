package com.gsnathan.pdfviewer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * This class is used to get a PDF File from an URL
 */
public class DownloadPDFFile extends AsyncTask<String, Void, Object> {

    // a reference to our MainActivity, this is used to be able to call saveFileAndDisplay()
    private WeakReference<MainActivity> mainActivityWR;

    public DownloadPDFFile(MainActivity activity) {
        mainActivityWR = new WeakReference<>(activity);
    }

    @Override
    protected Object doInBackground(String... strings) {
        File file;
        String url = strings [0];
        String filename = strings[1];
        InputStream inputStream;
        MainActivity activity = mainActivityWR.get();
        int responseCode;

        try {
            URL uri = new URL(url);

            if (url.startsWith("https")) {
                Log.i("DownloadPDFFile", "HTTPS CONNECTION");
                HttpsURLConnection urlConnection = (HttpsURLConnection) uri.openConnection();
                urlConnection.connect();
                responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    Certificate[] certs = urlConnection.getServerCertificates();
                    for (Certificate cert : certs)
                        Log.i("DownloadPDFFile", "Certificate : " +
                                "\n - Type : " + cert.getType() +
                                "\n - Hash Code : " + cert.hashCode() +
                                "\n - Public Key Algorithm : " + cert.getPublicKey().getAlgorithm() +
                                "\n - Public Key Format : " + cert.getPublicKey().getFormat()
                        );
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    file = Utils.createFileFromInputStream(activity.getCacheDir(), filename, inputStream);
                } else {
                    Log.e("DownloadPDFFile", "Error request https, response code : " + responseCode);
                    return responseCode;
                }
                urlConnection.disconnect();

            } else {
                Log.i("DownloadPDFFile", "HTTP CONNECTION");
                HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
                urlConnection.connect();
                responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    file = Utils.createFileFromInputStream(activity.getCacheDir(), filename, inputStream);
                } else {
                    Log.e("DownloadPDFFile", "Error request http, response code : " + responseCode);
                    return responseCode;
                }
                urlConnection.disconnect();
            }
        } catch (SSLPeerUnverifiedException e) {
            Log.e("DownloadPDFFile", "Error SSL Peer Unverified Exception");
            return e;
        } catch (SSLHandshakeException e) {
            Log.e("DownloadPDFFile", "Error SSL Handshake Exception");
            return e;
        } catch (IOException e) {
            Log.e("DownloadPDFFile", "Error cannot get file at URL :" + url);
            e.printStackTrace();
            return e;
        }

        return file;
    }

    @Override
    protected void onPostExecute(Object result) {
        MainActivity activity = mainActivityWR.get();

        if (activity != null) {
            // Manage error
            if (result == null) {
                Toast.makeText(activity.getApplicationContext(), R.string.toast_io_exception, Toast.LENGTH_SHORT).show();
            } else if (result instanceof Integer) {
                Toast.makeText(activity.getApplicationContext(), R.string.toast_http_code_error + String.valueOf(result), Toast.LENGTH_SHORT).show();
            } else if (result instanceof SSLPeerUnverifiedException || result instanceof SSLHandshakeException) {
                Toast.makeText(activity.getApplicationContext(), R.string.toast_ssl_error, Toast.LENGTH_SHORT).show();
            } else if (result instanceof IOException) {
                Toast.makeText(activity.getApplicationContext(), R.string.toast_io_exception, Toast.LENGTH_SHORT).show();
            } else if (result instanceof File) {
                activity.saveFileAndDisplay((File) result);
            }
        }
    }
}
