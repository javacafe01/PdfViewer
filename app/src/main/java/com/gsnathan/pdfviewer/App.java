package com.gsnathan.pdfviewer;

import android.app.Application;
import android.content.res.Configuration;

import com.jaredrummler.cyanea.Cyanea;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Cyanea.init(this, getResources());
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
