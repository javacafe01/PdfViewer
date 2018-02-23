package com.gsnathan.pdfviewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.Library;
import com.franmontiel.attributionpresenter.entities.License;
import com.webianks.easy_feedback.EasyFeedback;

/**
 * Created by Gokul Swaminathan on 2/22/2018.
 */

public class AboutActivity extends MaterialAboutActivity {

    private final String EMAIL = "gsnathandev@outlook.com";

    @Override
    @NonNull
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {

        MaterialAboutCard.Builder appBuilder = new MaterialAboutCard.Builder();
        buildApp(appBuilder, context);
        MaterialAboutCard.Builder authorBuilder = new MaterialAboutCard.Builder();
        buildAuthor(authorBuilder, context);
        MaterialAboutCard.Builder openBuilder = new MaterialAboutCard.Builder();
        buildOpenLicenses(openBuilder, context);
        MaterialAboutCard.Builder rateBuilder = new MaterialAboutCard.Builder();
        buildRateAndReview(rateBuilder, context);
        return new MaterialAboutList(appBuilder.build(), authorBuilder.build(), openBuilder.build(), rateBuilder.build());

    }

    private void buildApp(MaterialAboutCard.Builder appBuilder, final Context context){
        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name))
                .icon(R.mipmap.ic_launcher)
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.version)
                .subText(BuildConfig.VERSION_NAME)
                .icon(R.drawable.info_outline)
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.intro)
                .icon(R.drawable.arrow_right_drop_circle_outline)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(getApplicationContext(), MainIntroActivity.class));
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.myLicense)
                .subText(R.string.myLicense_dec)
                .icon(R.drawable.document_icon)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(getApplicationContext(), LicenseActivity.class));
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.privacy)
                .subText(R.string.privacy_dec)
                .icon(R.drawable.file_lock)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(getApplicationContext(), PrivacyActivity.class));
                    }
                })
                .build());

    }

    private void buildAuthor(MaterialAboutCard.Builder appBuilder, final Context context){
        appBuilder.title(R.string.author);
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.author_name)
                .subText(R.string.author_job)
                .icon(R.drawable.account_circle)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JavaCafe01")));
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.email)
                .subText(EMAIL)
                .icon(R.drawable.email)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Uri uri = Uri.parse("mailto:" + EMAIL);
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        startActivity(Intent.createChooser(emailIntent, "Email with... "));
                    }
                })
                .setOnLongClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Dev Email", EMAIL);
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .build());
    }


    private void buildOpenLicenses(MaterialAboutCard.Builder appBuilder, final Context context){
        appBuilder.title(R.string.open_source);
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.source_code)
                .subText(R.string.source_code_dec)
                .icon(R.drawable.code_tags)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JavaCafe01/PdfViewer")));
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.open_license)
                .subText(R.string.open_license_desc)
                .icon(R.drawable.document_icon)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        showLibs();
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.icon)
                .subText(R.string.icon_desc)
                .icon(R.drawable.favicon)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://materialdesignicons.com/")));
                    }
                })
                .build());
    }

    private void buildRateAndReview(MaterialAboutCard.Builder appBuilder, final Context context){
        appBuilder.title(R.string.rateReview_title);
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.rate)
                .icon(R.drawable.star)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.gsnathan.pdfviewer")));
                    }
                })
                .build());
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.review)
                .icon(R.drawable.message_draw)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        showReviewPage();
                    }
                })
                .build());
    }

    private void showReviewPage()
    {
        new EasyFeedback.Builder(this)
                .withEmail("gsnathandev@outlook.com")
                .withSystemInfo()
                .build()
                .start();
    }

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
                .build();

        //show license dialogue
        attributionPresenter.showDialog("Open Source Libraries");
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.action_about);
    }
}