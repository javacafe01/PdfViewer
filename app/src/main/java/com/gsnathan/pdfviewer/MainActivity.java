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
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.print.PrintManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.gsnathan.pdfviewer.databinding.ActivityMainBinding;
import com.gsnathan.pdfviewer.databinding.PasswordDialogBinding;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends CyaneaAppCompatActivity {

    private static final String TAG = "MainActivity";

    /** For performance reasons, we won't hash the entire PDF but only up to this many bytes. */
    private static final int HASH_SIZE = 1024 * 1024;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private PrintManager mgr;
    private SharedPreferences prefManager;
    private AppDatabase appDb;

    private Uri uri;
    private int pageNumber = 0;
    private String pdfPassword;
    private String pdfFileName = "";

    private byte[] downloadedPdfFileContent;
    private String fileContentHash = null;

    private boolean isBottomNavigationHidden = false;
    private boolean isFullscreenToggled = false;

    private ActivityMainBinding viewBinding;

    private final ActivityResultLauncher<String[]> documentPickerLauncher = registerForActivityResult(
        new OpenDocument(),
        this::openSelectedDocument
    );

    private final ActivityResultLauncher<String> saveToDownloadPermissionLauncher = registerForActivityResult(
        new RequestPermission(),
        this::saveDownloadedFileAfterPermissionRequest
    );

    private final ActivityResultLauncher<String> readFileErrorPermissionLauncher = registerForActivityResult(
        new RequestPermission(),
        this::restartAppIfGranted
    );

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
        new StartActivityForResult(),
        result -> displayFromUri(uri)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        Constants.THUMBNAIL_RATIO = 1f;
        setBottomBarListeners();

        // Workaround for https://stackoverflow.com/questions/38200282/
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        prefManager = PreferenceManager.getDefaultSharedPreferences(this);

        mgr = (PrintManager) getSystemService(PRINT_SERVICE);
        appDb = AppDatabase.getInstance(getApplicationContext());
        onFirstInstall();
        onFirstUpdate();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            uri = getIntent().getData();
            if (uri == null)
                pickFile();
        }
        displayFromUri(uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (prefManager.getBoolean("screen_on_pref", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        // Workaround for android:background XML attribute not being applied on all devices
        viewBinding.bottomNavigation.setBackgroundColor(Cyanea.getInstance().getPrimary());
    }

    private void onFirstInstall() {
        boolean isFirstRun = prefManager.getBoolean("FIRSTINSTALL", true);
        if (isFirstRun) {
            startActivity(new Intent(this, MainIntroActivity.class));
            SharedPreferences.Editor editor = prefManager.edit();
            editor.putBoolean("FIRSTINSTALL", false);
            editor.apply();
        }
    }

    private void onFirstUpdate() {
        boolean isFirstRun = prefManager.getBoolean(Utils.getAppVersion(), true);
        if (isFirstRun) {
            Utils.showLog(this);
            SharedPreferences.Editor editor = prefManager.edit();
            editor.putBoolean(Utils.getAppVersion(), false);
            editor.apply();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("uri", uri);
        outState.putInt("pageNumber", pageNumber);
        outState.putString("pdfPassword", pdfPassword);
        super.onSaveInstanceState(outState);
    }

    private void restoreInstanceState(Bundle savedState) {
        uri = savedState.getParcelable("uri");
        pageNumber = savedState.getInt("pageNumber");
        pdfPassword = savedState.getString("pdfPassword");
    }

    void shareFile() {
        Intent sharingIntent;
        if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
            sharingIntent = Utils.plainTextShareIntent(getString(R.string.share), uri.toString());
        } else {
            sharingIntent = Utils.fileShareIntent(getString(R.string.share), pdfFileName, uri);
        }
        startActivity(sharingIntent);
    }

    private void openSelectedDocument(Uri selectedDocumentUri) {
        if (selectedDocumentUri == null) {
            return;
        }

        if (uri == null || selectedDocumentUri.equals(uri)) {
            uri = selectedDocumentUri;
            displayFromUri(uri);
        } else {
            Intent intent = new Intent(this, getClass());
            intent.setData(selectedDocumentUri);
            startActivity(intent);
        }
    }

    private void pickFile() {
        try {
            documentPickerLauncher.launch(new String[] { "application/pdf" });
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setBottomBarListeners() {
        viewBinding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.pickFile:
                    pickFile();
                    break;
                case R.id.metaFile:
                    if (uri != null)
                        showPdfMetaDialog();
                    break;
                case R.id.shareFile:
                    if (uri != null)
                        shareFile();
                    break;
                case R.id.printFile:
                    if (uri != null)
                        printDocument();
                    break;
                case R.id.fullscreen:
                    toggleFullscreen();
                    return true;
                default:
                    break;
            }
            return false;
        });
    }

    String computeHash() {
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            if (downloadedPdfFileContent != null) {
                int size = Math.min(HASH_SIZE, downloadedPdfFileContent.length);
                digester.update(downloadedPdfFileContent, 0, size);
            } else {
                InputStream is = getContentResolver().openInputStream(uri);
                byte[] buffer = new byte[HASH_SIZE];
                int amountRead = is.read(buffer);
                if (amountRead == -1) {
                    return null;
                }
                digester.update(buffer, 0, amountRead);
            }
            return String.format("%032x", new BigInteger(1, digester.digest()));
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }

    void configurePdfViewAndLoad(PDFView.Configurator viewConfigurator) {
        if (pageNumber == 0) { // attempt to find a saved location
            executor.execute(() -> { // off UI thread
                fileContentHash = computeHash();
                Integer maybePageNumber = fileContentHash == null
                        ? Integer.valueOf(0)
                        : appDb.savedLocationDao().findSavedPage(fileContentHash);
                handler.post(() -> // back on UI thread
                    configurePdfViewAndLoadWithPageNumber(
                        viewConfigurator,
                        maybePageNumber != null ? maybePageNumber : 0
                    )
                );
            });
        } else {
            configurePdfViewAndLoadWithPageNumber(viewConfigurator, pageNumber);
        }
    }

    void configurePdfViewAndLoadWithPageNumber(PDFView.Configurator viewConfigurator, int pageNum) {
        if (!prefManager.getBoolean("pdftheme_pref", false)) {
            viewBinding.pdfView.setBackgroundColor(Color.LTGRAY);
        } else {
            viewBinding.pdfView.setBackgroundColor(0xFF212121);
        }

        viewBinding.pdfView.useBestQuality(prefManager.getBoolean("quality_pref", false));
        viewBinding.pdfView.setMinZoom(0.5f);
        viewBinding.pdfView.setMidZoom(2.0f);
        viewBinding.pdfView.setMaxZoom(5.0f);
        viewConfigurator
                .defaultPage(pageNum)
                .onPageChange(this::setCurrentPage)
                .enableAnnotationRendering(true)
                .enableAntialiasing(prefManager.getBoolean("alias_pref", true))
                .onTap(this::toggleBottomNavigationVisibility)
                .onPageScroll(this::toggleBottomNavigationAccordingToPosition)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onError(this::handleFileOpeningError)
                .onPageError((page, err) -> Log.e(TAG, "Cannot load page " + page, err))
                .pageFitPolicy(FitPolicy.WIDTH)
                .password(pdfPassword)
                .swipeHorizontal(prefManager.getBoolean("scroll_pref", false))
                .autoSpacing(prefManager.getBoolean("scroll_pref", false))
                .pageSnap(prefManager.getBoolean("snap_pref", false))
                .pageFling(prefManager.getBoolean("fling_pref", false))
                .nightMode(prefManager.getBoolean("pdftheme_pref", false))
                .load();
    }

    private void handleFileOpeningError(Throwable exception) {
        if (exception instanceof PdfPasswordException) {
            if (pdfPassword != null) {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                pdfPassword = null;  // prevent the toast from being shown again if the user rotates the screen
            }
            askForPdfPassword();
        } else if (couldNotOpenFileDueToMissingPermission(exception)) {
            readFileErrorPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error when opening file", exception);
        }
    }

    private boolean couldNotOpenFileDueToMissingPermission(Throwable e) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED)
            return false;

        String exceptionMessage = e.getMessage();
        return e instanceof FileNotFoundException &&
            exceptionMessage != null && exceptionMessage.contains("Permission denied");
    }

    private void restartAppIfGranted(boolean isPermissionGranted) {
        if (isPermissionGranted) {
            // This is a quick and dirty way to make the system restart the current activity *and the current app process*.
            // This is needed because on Android 6 storage permission grants do not take effect until
            // the app process is restarted.
            System.exit(0);
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show();
        }
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
        viewBinding.bottomNavigation.animate()
                .translationY(viewBinding.bottomNavigation.getHeight())
                .setDuration(100);
    }

    private void showBottomNavigationView() {
        isBottomNavigationHidden = false;
        viewBinding.bottomNavigation.animate()
                .translationY(0)
                .setDuration(100);
    }

    private void toggleFullscreen() {
        final View view = viewBinding.pdfView;
        if (!isFullscreenToggled) {
            getSupportActionBar().hide();
            isFullscreenToggled = true;
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getSupportActionBar().show();
            isFullscreenToggled = false;
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    void displayFromUri(Uri uri) {
        if (uri == null) {
            setTitle("");
            return;
        }

        pdfFileName = getFileName(uri);
        setTitle(pdfFileName);
        setTaskDescription(new ActivityManager.TaskDescription(pdfFileName));

        String scheme = uri.getScheme();
        if (scheme != null && scheme.contains("http")) {
            downloadOrShowDownloadedFile(uri);
        } else {
            configurePdfViewAndLoad(viewBinding.pdfView.fromUri(uri));
        }
    }

    private void downloadOrShowDownloadedFile(Uri uri) {
        if (downloadedPdfFileContent == null) {
            downloadedPdfFileContent = (byte[]) getLastCustomNonConfigurationInstance();
        }
        if (downloadedPdfFileContent != null) {
            configurePdfViewAndLoad(viewBinding.pdfView.fromBytes(downloadedPdfFileContent));
        } else {
            // we will get the pdf asynchronously with the DownloadPDFFile object
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            DownloadPDFFile downloadPDFFile = new DownloadPDFFile(this);
            downloadPDFFile.execute(uri.toString());
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadedPdfFileContent;
    }

    public void hideProgressBar() {
        viewBinding.progressBar.setVisibility(View.GONE);
    }

    void saveToFileAndDisplay(byte[] pdfFileContent) {
        downloadedPdfFileContent = pdfFileContent;
        saveToDownloadFolderIfAllowed(pdfFileContent);
        configurePdfViewAndLoad(viewBinding.pdfView.fromBytes(pdfFileContent));
    }

    private void saveToDownloadFolderIfAllowed(byte[] fileContent) {
        if (Utils.canWriteToDownloadFolder(this)) {
            trySaveToDownloadFolder(fileContent, false);
        } else {
            saveToDownloadPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    private void saveDownloadedFileAfterPermissionRequest(boolean isPermissionGranted) {
        if (isPermissionGranted) {
            trySaveToDownloadFolder(downloadedPdfFileContent, true);
        } else {
            Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void navToSettings() {
        settingsLauncher.launch(new Intent(this, SettingsActivity.class));
    }

    private void setCurrentPage(int page, int pageCount) {
        String hash = fileContentHash; // Don't want fileContentHash to change out from under us
        if (hash != null) {
            executor.execute(() -> // off UI thread
                appDb.savedLocationDao().insert(new SavedLocation(hash, pageNumber))
            );
        }

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
            } catch (Exception e) {
                Log.w(TAG, "Couldn't retrieve file name", e);
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

    void askForPdfPassword() {
        PasswordDialogBinding dialogBinding = PasswordDialogBinding.inflate(getLayoutInflater());
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.protected_pdf)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    pdfPassword = dialogBinding.passwordInput.getText().toString();
                    displayFromUri(uri);
                })
                .setIcon(R.drawable.lock_icon)
                .create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    void showPdfMetaDialog() {
        PdfDocument.Meta meta = viewBinding.pdfView.getDocumentMeta();
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
                    .setIcon(R.drawable.info_icon)
                    .create();
        }
    }
}
