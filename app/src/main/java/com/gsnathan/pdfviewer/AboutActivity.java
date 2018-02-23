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
        return new MaterialAboutList(appBuilder.build(), authorBuilder.build(), openBuilder.build());

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
                .text(R.string.source_code)
                .subText(R.string.source_code_dec)
                .icon(R.drawable.code_tags)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JavaCafe01/Compass")));
                    }
                })
                .build());


        /*
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
        */

        /*
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
        */
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
        /*
        appBuilder.title(R.string.open_source);
        appBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.open_license)
                .icon(R.drawable.document_icon)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        new LibsBuilder()
                                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                .withActivityStyle(theme)
                                .withActivityTheme(style)
                                //start the activity
                                .start(getApplicationContext());
                    }
                })
                .build());
    */
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.action_about);
    }
}