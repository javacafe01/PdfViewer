package com.gsnathan.pdfviewer;

import android.app.Application;
import android.content.res.Configuration;

import com.jaredrummler.cyanea.Cyanea;

import java.util.Properties;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Cyanea.init(this, getResources());
        Properties system = System.getProperties();
        system.put("http.proxyHost", "localhost");
        system.put("http.proxyPort", "80");

        system.put("https.proxyHost", "localhost");
        system.put("https.proxyPort", "80");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
