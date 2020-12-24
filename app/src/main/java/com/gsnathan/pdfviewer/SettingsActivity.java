package com.gsnathan.pdfviewer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;

import androidx.core.app.NavUtils;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();
        addPreferencesFromResource(R.xml.preferences);

        findPreference("reload_pref").setOnPreferenceClickListener(preference -> {
            try {
                Uri documentUri = getIntent().getData();
                Intent intent = new Intent(this, MainActivity_.class);
                intent.setData(documentUri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });

        findPreference("show_in_launcher").setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                setLauncherAliasState((boolean) newValue);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    private void setLauncherAliasState(boolean enableAlias) {
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, "com.gsnathan.pdfviewer.LauncherAlias"),
                enableAlias ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
        );
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

}
