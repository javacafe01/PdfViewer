package com.gsnathan.pdfviewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

/**
 * Created by Gokul Swaminathan on 2/25/2018.
 *
 * IN USE
 */

public class MaterialAboutActivity extends AppCompatActivity{

    private final String EMAIL = "gsnathandev@outlook.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutView view = AboutBuilder.with(this)

                /* ---Developer Actions--- */
                .setPhoto(R.drawable.myprofile)
                .setCover(R.drawable.profile_cover)
                .setName(R.string.author_name)
                .setSubTitle(R.string.author_job)
                .addGitHubLink("JavaCafe01")
                .addEmailLink(EMAIL)
                /* ---Developer Actions--- */

                /* ---App Actions--- */
                .setAppIcon(R.drawable.app_icon_with_blank)
                .setAppName(R.string.app_name)
                .setAppTitle("Version " + Utils.getAppVersion())
                .addAction(Utils.getBitmapFromVectorDrawable(this, R.drawable.arrow_right_drop_circle_outline), R.string.intro, Utils.navIntent(this, MainIntroActivity.class))
                .addAction(com.vansuita.materialabout.R.mipmap.license, R.string.myLicense, Utils.navIntent(this, LicenseActivity.class))
                .addPrivacyPolicyAction(Utils.navIntent(this, PrivacyActivity.class))
                .addAction(Utils.getBitmapFromVectorDrawable(this, R.drawable.code_tags), R.string.source_code, Utils.linkIntent("https://github.com/JavaCafe01/PdfViewer"))
                .addAction(Utils.getBitmapFromVectorDrawable(this, R.drawable.document_icon), R.string.open_license2, onLicenseClick)
                .addAction(com.vansuita.materialabout.R.mipmap.website, R.string.icon, Utils.linkIntent("https://materialdesignicons.com/"))
                .addAction(Utils.getBitmapFromVectorDrawable(this, R.drawable.clipboard_alert), R.string.appChangelog, showLog)
                .addShareAction(R.string.app_name)
                .addAction(com.vansuita.materialabout.R.mipmap.star, R.string.rate, Utils.linkIntent("http://play.google.com/store/apps/details?id=com.gsnathan.pdfviewer"))
                .addAction(com.vansuita.materialabout.R.mipmap.feedback, R.string.review, Utils.emailIntent(EMAIL,"Pdf Viewer Plus Review", Utils.getAndroidVersion() + "\n\nFeedback:\n", "Send Feedback:"))
                 /* ---App Actions--- */

                 /* ---Behaviours--- */
                .setLinksColumnsCount(2)
                .setWrapScrollView(true)
                .setLinksAnimated(true)
                .setShowAsCard(true)
                /* ---Behaviours--- */

                .build();

        setContentView(view);
    }

    private View.OnClickListener onLicenseClick = new View.OnClickListener() {
        public void onClick(View v) {
            showLibs();
        }
    };

    private View.OnClickListener showLog = new View.OnClickListener() {
        public void onClick(View v) {
           LogFragment log = new LogFragment();
           log.show(getSupportFragmentManager(), "Log Fragment");

        }
    };

    private void showLibs()
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
                        new Attribution.Builder("material-about-library")
                                .addCopyrightNotice("Copyright 2016-2018 Daniel Stone")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/daniel-stoneuk/material-about-library")
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
                        new Attribution.Builder("Material About")
                                .addCopyrightNotice("Copyright 2016 Arleu Cezar Vansuita Júnior")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/jrvansuita/MaterialAbout")
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
