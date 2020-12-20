/*
 * MIT License
 *
 * Copyright (c) 2018 Gokul Swaminathan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gsnathan.pdfviewer;

import android.os.Binder;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

public class AboutActivity extends CyaneaAppCompatActivity {

    TextView versionView;   //shows the version
    private final String APP_VERSION_RELEASE = "Version " + Utils.getAppVersion();   //contains Version + the version number
    private final String APP_VERSION_DEBUG = "Version " + Utils.getAppVersion() + "-debug";   //contains Version + the version number + debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initUI();
        setUpToolBar();
    }

    private void setUpToolBar() {
        Binder.clearCallingIdentity();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initUI() {
        //initialize the textview
        versionView = (TextView) findViewById(R.id.versionTextView);

        // check if app is debug
        if (BuildConfig.DEBUG) {
            versionView.setText(APP_VERSION_DEBUG);
        } else    //if app is release
        {
            versionView.setText(APP_VERSION_RELEASE);
        }
    }

    public void replayIntro(View v) {
        //navigate to intro class (replay the intro)
        startActivity(Utils.navIntent(getApplicationContext(), MainIntroActivity.class));
    }

    public void showLog(View v) {
        Utils.showLog(this);
    }

    public void showPrivacy(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01/PdfViewer/blob/master/privacy_policy.md"));
    }

    public void showLicense(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01/PdfViewer/blob/master/LICENSE"));
    }

    public void showLibraries(View v) {
        AttributionPresenter attributionPresenter = new AttributionPresenter.Builder(this)
                .addAttributions(
                        new Attribution.Builder("AttributionPresenter")
                                .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android PdfViewer")
                                .addCopyrightNotice("Copyright 2017 Bartosz Schiller")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/barteksc/AndroidPdfViewer")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("AndroidAnnotations")
                                .addCopyrightNotice("Copyright 2012-2016 eBusiness Information\n" +
                                        "Copyright 2016-2017 the AndroidAnnotations project")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/androidannotations/androidannotations")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("AppIntro")
                                .addCopyrightNotice("Copyright 2018 paorotolo")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/paolorotolo/AppIntro")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android Open Source Project")
                                .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                                .addLicense(License.APACHE)
                                .setWebsite("http://developer.android.com/tools/support-library/index.html")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android Support Libraries")
                                .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                                .addLicense(License.APACHE)
                                .setWebsite("http://developer.android.com/tools/support-library/index.html")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Material Design Icons")
                                .addCopyrightNotice("Copyright 2014, Austin Andrews")
                                .addLicense("SIL Open Font", "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE")
                                .setWebsite("https://materialdesignicons.com/")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("WhatsNew")
                                .addCopyrightNotice("Copyright 2017 Lizhaotailang")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/TonnyL/WhatsNew")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("RateThisApp")
                                .addCopyrightNotice("Copyright 2017 Keisuke Kobayashi")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/kobakei/Android-RateThisApp")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Cyanea")
                                .addCopyrightNotice("Copyright 2018 Jared Rummler")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/jaredrummler/Cyanea")
                                .build()
                )
                .build();

        //show license dialogue
        attributionPresenter.showDialog("Open Source Libraries");
    }

    public void emailDev(View v) {
        startActivity(Utils.emailIntent("gokulswamilive@gmail.com", "Pdf Viewer Plus", APP_VERSION_RELEASE, "Send email..."));
    }

    public void navToGit(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01"));
    }

    public void navToSourceCode(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01/PdfViewer"));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

}