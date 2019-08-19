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
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * This class is used to get a Stream from an URL
 */
public class RetrievePDFStream extends AsyncTask<String, Void, File> {

    // a reference to our MainActivity, this is used to be able to call displayFromStream()
    private WeakReference<MainActivity> mainActivityWR;

    public RetrievePDFStream(MainActivity activity) {
        mainActivityWR = new WeakReference<>(activity);
    }

    @Override
    protected File doInBackground(String... strings) {
        File file = null;
        String url = strings [0];
        String filename = strings[1];
        InputStream inputStream = null;
        MainActivity activity = mainActivityWR.get();

        try {
            URL uri = new URL(url);

            if (url.startsWith("https")) {
                Log.i("RetrievePDFStream", "HTTPS CONNECTION");
                HttpsURLConnection urlConnection = (HttpsURLConnection) uri.openConnection();
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    Certificate[] certs = urlConnection.getServerCertificates();
                    for (Certificate cert : certs)
                        Log.i("RetrievePDFStream", "Certificate : " +
                                "\n - Type : " + cert.getType() +
                                "\n - Hash Code : " + cert.hashCode() +
                                "\n - Public Key Algorithm : " + cert.getPublicKey().getAlgorithm() +
                                "\n - Public Key Format : " + cert.getPublicKey().getFormat()
                        );
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    file = Utils.createFileFromInputStream(activity.getCacheDir(), filename, inputStream);
                }
                urlConnection.disconnect();

            } else {
                Log.i("RetrievePDFStream", "HTTP CONNECTION");
                HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    file = Utils.createFileFromInputStream(activity.getCacheDir(), filename, inputStream);
                }
                urlConnection.disconnect();
            }
        } catch (SSLPeerUnverifiedException e) {
            Log.e("RetrievePDFStream", "Error SSL");
            Toast.makeText(activity, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
            return null;
        } catch (IOException e) {
            Log.e("RetrievePDFStream", "Error cannot get file at URL :" + url);
            e.printStackTrace();
            return null;
        }

        return file;
    }

    @Override
    protected void onPostExecute(File file) {
        MainActivity activity = mainActivityWR.get();

        if (activity != null && file != null) {
            activity.saveFileAndDisplay(file);
        }
    }
}
