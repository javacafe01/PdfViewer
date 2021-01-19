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

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.print.PrintManager;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity;
import com.kobakei.ratethisapp.RateThisApp;
import com.shockwave.pdfium.PdfDocument;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@EActivity(R.layout.activity_main)
public class MainActivity extends CyaneaAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PrintManager mgr = null;

    private final static int REQUEST_CODE = 42;
    private final static int PERMISSION_WRITE = 42041;

    private static String PDF_PASSWORD = "";
    private SharedPreferences prefManager;

    private boolean isBottomNavigationHidden = false;

    @ViewById PDFView pdfView;
    @ViewById BottomNavigationView bottomNavigation;
    @ViewById ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pdfFileName = "";

        prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        onFirstInstall();
        onFirstUpdate();
        handleIntent(getIntent());

        if (uri == null) {
            pickFile();
        }

        mgr = (PrintManager) getSystemService(PRINT_SERVICE);

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
        if (appLinkData != null) {
            uri = appLinkData;
        }
    }

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    private String pdfFileName;

    private byte[] downloadedPdfFileContent;

    void shareFile() {
        startActivity(Utils.emailIntent(pdfFileName, "", getResources().getString(R.string.share), uri));
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
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
        pdfView.setBackgroundColor(Color.LTGRAY);
        Constants.THUMBNAIL_RATIO = 1f;
        if (uri != null) {
            displayFromUri(uri);
        }
        setTitle(pdfFileName);
        setBottomBarListeners();
    }

    private void setBottomBarListeners() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.pickFile:
                    pickFile();
                    break;
                case R.id.metaFile:
                    if (uri != null)
                        showPdfMetaDialog();
                    break;
                case R.id.unlockFile:
                    if (uri != null)
                        unlockPDF();
                    break;
                case R.id.shareFile:
                    if (uri != null)
                        shareFile();
                    break;
                case R.id.printFile:
                    if (uri != null)
                        printDocument();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    void configurePdfViewAndLoad(PDFView.Configurator viewConfigurator) {
        pdfView.useBestQuality(prefManager.getBoolean("quality_pref", false));
        pdfView.setMinZoom(0.5f);
        pdfView.setMidZoom(2.0f);
        pdfView.setMaxZoom(5.0f);
        viewConfigurator
                .defaultPage(pageNumber)
                .onPageChange(this::setCurrentPage)
                .enableAnnotationRendering(true)
                .enableAntialiasing(prefManager.getBoolean("alias_pref", true))
                .onTap(this::toggleBottomNavigationVisibility)
                .onPageScroll(this::toggleBottomNavigationAccordingToPosition)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError((page, err) -> Log.e(TAG, "Cannot load page " + page, err))
                .pageFitPolicy(FitPolicy.WIDTH)
                .password(PDF_PASSWORD)
                .swipeHorizontal(prefManager.getBoolean("scroll_pref", false))
                .autoSpacing(prefManager.getBoolean("scroll_pref", false))
                .pageSnap(prefManager.getBoolean("snap_pref", false))
                .pageFling(prefManager.getBoolean("fling_pref", false))
                .load();
    }

    private void toggleBottomNavigationAccordingToPosition(int page, float positionOffset) {
        if (positionOffset == 0) {
            showBottomNavigationView();
        } else if (!isBottomNavigationHidden) {
            hideBottomNavigationView();
        }
    }

    private boolean toggleBottomNavigationVisibility(MotionEvent e) {
        if (isBottomNavigationHidden) {
            showBottomNavigationView();
        } else {
            hideBottomNavigationView();
        }
        return true;
    }

    private void hideBottomNavigationView() {
        isBottomNavigationHidden = true;
        bottomNavigation.animate().translationY(bottomNavigation.getHeight()).setDuration(100);
    }

    private void showBottomNavigationView() {
        isBottomNavigationHidden = false;
        bottomNavigation.animate().translationY(0).setDuration(100);
    }

    void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);
        String scheme = uri.getScheme();

        if (scheme != null && scheme.contains("http")) {
            // we will get the pdf asynchronously with the DownloadPDFFile object
            progressBar.setVisibility(View.VISIBLE);
            DownloadPDFFile downloadPDFFile = new DownloadPDFFile(this);
            downloadPDFFile.execute(uri.toString());
        } else {
            configurePdfViewAndLoad(pdfView.fromUri(uri));
        }
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    void saveToFileAndDisplay(byte[] pdfFileContent) {
        downloadedPdfFileContent = pdfFileContent;
        saveToDownloadFolderIfAllowed(pdfFileContent);
        configurePdfViewAndLoad(pdfView.fromBytes(pdfFileContent));
    }

    private void saveToDownloadFolderIfAllowed(byte[] fileContent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            trySaveToDownloadFolder(fileContent, false);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_WRITE
            );
        }
    }

    private void trySaveToDownloadFolder(byte[] fileContent, boolean showSuccessMessage) {
        try {
            File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Utils.writeBytesToFile(downloadDirectory, pdfFileName, fileContent);
            if (showSuccessMessage) {
                Toast.makeText(this, R.string.saved_to_download, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while saving file to download folder", e);
            Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void navToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    @OnActivityResult(REQUEST_CODE)
    void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    private void setCurrentPage(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName + " ", page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int indexDisplayName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (indexDisplayName != -1) {
                        result = cursor.getString(indexDisplayName);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void printDocument() {
        mgr.print(pdfFileName, new PdfDocumentAdapter(this, uri), null);
    }

    void unlockPDF() {

        final EditText input = new EditText(this);
        input.setPadding(19, 19, 19, 19);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle(R.string.password)
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    PDF_PASSWORD = input.getText().toString();
                    if (uri != null)
                        displayFromUri(uri);
                })
                .setIcon(R.drawable.lock_icon)
                .show();
    }

    void showPdfMetaDialog() {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        if (meta != null) {
            Bundle dialogArgs = new Bundle();
            dialogArgs.putString(PdfMetaDialog.TITLE_ARGUMENT, meta.getTitle());
            dialogArgs.putString(PdfMetaDialog.AUTHOR_ARGUMENT, meta.getAuthor());
            dialogArgs.putString(PdfMetaDialog.CREATION_DATE_ARGUMENT, meta.getCreationDate());
            DialogFragment dialog = new PdfMetaDialog();
            dialog.setArguments(dialogArgs);
            dialog.show(getSupportFragmentManager(), "meta_dialog");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        switch (requestCode) {
            case PERMISSION_WRITE:
                int indexPermission = Arrays.asList(permissions).indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (grantResults[indexPermission] == PackageManager.PERMISSION_GRANTED) {
                    trySaveToDownloadFolder(downloadedPdfFileContent, true);
                } else {
                    Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
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

    public static class PdfMetaDialog extends DialogFragment {

        public static final String TITLE_ARGUMENT = "title";
        public static final String AUTHOR_ARGUMENT = "author";
        public static final String CREATION_DATE_ARGUMENT = "creation_date";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            return builder.setTitle(R.string.meta)
                    .setMessage(getString(R.string.pdf_title, getArguments().getString(TITLE_ARGUMENT)) + "\n" +
                            getString(R.string.pdf_author, getArguments().getString(AUTHOR_ARGUMENT)) + "\n" +
                            getString(R.string.pdf_creation_date, getArguments().getString(CREATION_DATE_ARGUMENT)))
                    .setPositiveButton(R.string.ok, (dialog, which) -> {})
                    .setIcon(R.drawable.alert_icon)
                    .create();
        }
    }
}

