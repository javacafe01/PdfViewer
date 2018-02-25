package com.gsnathan.pdfviewer;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Gokul Swaminathan on 2/24/2018.
 */

public class Utils extends AppCompatActivity{

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }
}
