package com.nikogalla.tripbook.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nikogalla.tripbook.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicola on 2016-11-30.
 */

public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.tbSettings)
    Toolbar tbSettings;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        tbSettings.setTitle(getString(R.string.settings));
        setSupportActionBar(tbSettings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsContent, new SettingsFragment())
                .commit();
    }

}
