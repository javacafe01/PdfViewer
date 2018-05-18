package com.gsnathan.pdfviewer;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;

/**
 * Created by Gokul Swaminathan on 2/22/2018.
 */

public class AboutActivity extends AppCompatActivity{

    TextView versionView;   //shows the version
    private final String APP_VERSION_RELEASE = "Version " + Utils.getAppVersion();   //contains Version + the version number
    private final String APP_VERSION_DEBUG = "Version " + Utils.getAppVersion() + "-debug";   //contains Version + the version number + debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initUI();
    }

    private void initUI() {
        //initialize the textview
        versionView = (TextView) findViewById(R.id.text_version);

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

    public void showLog(View v)
    {
        LogFragment log = new LogFragment();
        log.show(getSupportFragmentManager(), "Log Fragment");
    }

    public void showPrivacy(View v)
    {
        startActivity(Utils.navIntent(getApplicationContext(), PrivacyActivity.class));
    }

    public void showMaterial(View v)
    {
        startActivity(Utils.linkIntent("https://materialdesignicons.com/"));
    }

    public void showLicense(View v)
    {
        startActivity(Utils.navIntent(getApplicationContext(), LicenseActivity.class));
    }

    public void showLibraries(View v)
    {
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
                        new Attribution.Builder("material-intro")
                                .addCopyrightNotice("Copyright 2017 Jan Heinrich Reimer")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/heinrichreimer/material-intro")
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
                        new Attribution.Builder("HtmlTextView for Android")
                                .addCopyrightNotice("Copyright 2013 Dominik Schürmann")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/PrivacyApps/html-textview")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("LicenseTextView")
                                .addCopyrightNotice("Copyright 2016 JGabrielFreitas")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/jgabrielfreitas/LicenseTextView")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("EasyFeedback")
                                .addCopyrightNotice("Copyright 2017 Ramankit Singh")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/webianks/EasyFeedback")
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
                        new Attribution.Builder("ChangeLog Library")
                                .addCopyrightNotice("Copyright 2013-2015 Gabriele Mariotti")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/gabrielemariotti/changeloglib")
                                .build()
                )
                .build();

        //show license dialogue
        attributionPresenter.showDialog("Open Source Libraries");
    }

    public void emailDev(View v) {
        startActivity(Utils.emailIntent("gokulswaminathan@outlook.com", "Android-Scouter", APP_VERSION_RELEASE, "Send email..."));
    }

    public void navToGit(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01"));
    }

    public void navToSourceCode(View v) {
        startActivity(Utils.linkIntent("https://github.com/JavaCafe01/TorchLight"));
    }
}