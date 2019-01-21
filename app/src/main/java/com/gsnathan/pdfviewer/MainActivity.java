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

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import android.os.Bundle;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FileUtils;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity;
import com.jaredrummler.cyanea.prefs.CyaneaThemePickerActivity;
import com.jaredrummler.cyanea.prefs.CyaneaThemePickerLauncher;
import com.kobakei.ratethisapp.RateThisApp;
import com.shockwave.pdfium.PdfDocument;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends ProgressActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "pdf_sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static String PDF_PASSWORD = "";
    private SharedPreferences prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        onFirstInstall();
        onFirstUpdate();
        handleIntent(getIntent());

        if (Utils.tempBool && getIntent().getStringExtra("uri") != null)
            uri = Uri.parse(getIntent().getStringExtra("uri"));

        // Custom condition: 5 days and 5 launches
        RateThisApp.Config config = new RateThisApp.Config(5, 5);
        RateThisApp.init(config);
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
    }

    private void onFirstInstall() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean("FIRSTINSTALL", true);
        if (isFirstRun) {
            startActivity(new Intent(this, MainIntroActivity.class));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("FIRSTINSTALL", false);
            editor.apply();
        }
    }

    private void onFirstUpdate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean(Utils.getAppVersion(), true);
        if (isFirstRun) {
            Utils.showLog(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Utils.getAppVersion(), false);
            editor.apply();
        }
    }


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //Kinda not recommended by google but watever
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Uri appLinkData = intent.getData();
        String appLinkAction = intent.getAction();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            uri = appLinkData;
        }
    }


    @ViewById
    PDFView pdfView;

    @ViewById
    FabSpeedDial fabMain;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    private void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void shareFile() {
        startActivity(Utils.emailIntent(pdfFileName, "", getResources().getString(R.string.share), uri));
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        showProgressDialog();
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);
        hideProgressDialog();
    }

    void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.useBestQuality(prefManager.getBoolean("quality_pref", false));

        pdfView.fromAsset(assetFileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .enableAntialiasing(prefManager.getBoolean("alias_pref", false))
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .password(PDF_PASSWORD)
                .swipeHorizontal(prefManager.getBoolean("scroll_pref", false))
                .autoSpacing(prefManager.getBoolean("scroll_pref", false))
                .pageSnap(prefManager.getBoolean("snap_pref", false))
                .pageFling(prefManager.getBoolean("fling_pref", false))
                .load();
    }

    void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);
        Utils.tempBool = true;
        SharedPreferences.Editor editor = prefManager.edit();
        editor.putString("uri", uri.toString());
        editor.apply();

        pdfView.useBestQuality(prefManager.getBoolean("quality_pref", false));

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .enableAntialiasing(prefManager.getBoolean("alias_pref", false))
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .password(PDF_PASSWORD)
                .swipeHorizontal(prefManager.getBoolean("scroll_pref", false))
                .autoSpacing(prefManager.getBoolean("scroll_pref", false))
                .pageSnap(prefManager.getBoolean("snap_pref", false))
                .pageFling(prefManager.getBoolean("fling_pref", false))
                .load();

    }

    void navToSettings() {
        startActivity(Utils.navIntent(this, SettingsActivity.class));
    }

    @OnActivityResult(REQUEST_CODE)
    void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName + " ", page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        Log.d(TAG, "PDF loaded");
        fabMain.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.pickFile:
                        pickFile();
                        break;
                    case R.id.metaFile:
                        getMeta();
                        break;
                    case R.id.unlockFile:
                        unlockPDF();
                        break;
                    case R.id.shareFile:
                        shareFile();
                        break;
                    default:
                        break;

                }
                return false;
            }
        });
    }

    void unlockPDF() {

        final EditText input = new EditText(this);
        input.setPadding(19, 19, 19, 19);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle(R.string.password)
                .setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PDF_PASSWORD = input.getText().toString();
                        if (uri != null)
                            displayFromUri(uri);
                    }
                })
                .setIcon(R.drawable.lock_icon)
                .show();
    }

    void getMeta() {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        if (meta != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.meta)
                    .setMessage("Title: " + meta.getTitle() + "\n" + "Author: " + meta.getAuthor() + "\n" + "Creation Date: " + meta.getCreationDate())
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(R.drawable.alert_icon)
                    .show();
        }

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(Utils.navIntent(this, AboutActivity.class));
                return true;
            case R.id.theme:
                startActivity(Utils.navIntent(getApplicationContext(), CyaneaSettingsActivity.class));
                return true;
            case R.id.settings:
                navToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
