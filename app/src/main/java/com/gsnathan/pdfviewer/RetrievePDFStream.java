package com.gsnathan.pdfviewer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is used to get a Stream from an URL
 */
public class RetrievePDFStream extends AsyncTask<String, Void, File> {

    // a reference to our MainActivity, this is used to be able to call displayFromFile()
    private WeakReference<MainActivity> mainActivityWR;

    public RetrievePDFStream(MainActivity activity) {
        mainActivityWR = new WeakReference<>(activity);
    }

    @Override
    protected File doInBackground(String... strings) {
        File file = null;
        InputStream inputStream = null;
        MainActivity activity = mainActivityWR.get();
        try{
            URL uri = new URL(strings[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
            }

            file = File.createTempFile(strings[1], null, activity.getCacheDir());
            OutputStream outputStream = new FileOutputStream(file);
            Utils.readFromInputStreamToOutputStream(inputStream, outputStream);
        } catch (IOException e){
            Log.e("RetrievePDFStream", "Error cannot get file at URL");
            return null;
        }

        return file;
    }

    @Override
    protected void onPostExecute(File file) {
        MainActivity activity = mainActivityWR.get();

        if (activity != null) {
            activity.saveFileAndDisplay(file);
        }
    }
}
