package com.gsnathan.pdfviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.cyanea.app.CyaneaPreferenceActivity;

import org.jetbrains.annotations.Nullable;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class SettingsActivity extends CyaneaPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();
        addPreferencesFromResource(R.xml.preferences);
        setupShowInLauncherPreference();
    }

    private void setupShowInLauncherPreference() {
        Preference showInLauncherPref = findPreference("show_in_launcher");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Starting from Android Q it is not possible anymore to hide the app from launcher
            // See https://developer.android.com/reference/android/content/pm/LauncherApps#getActivityList(java.lang.String,%20android.os.UserHandle)
            getPreferenceScreen().removePreference(showInLauncherPref);
        } else {
            setOptionsListTopMargin();
            showInLauncherPref.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    setLauncherAliasState((boolean) newValue);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            });
        }
    }

    private void setOptionsListTopMargin() {
        int marginSize = (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        View marginView = new View(this);
        marginView.setMinimumHeight(marginSize);
        getListView().addHeaderView(marginView, null, false);
    }

    private void setLauncherAliasState(boolean enableAlias) {
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, "com.gsnathan.pdfviewer.LauncherAlias"),
                enableAlias ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
        );
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                onBackPressed();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }
}
