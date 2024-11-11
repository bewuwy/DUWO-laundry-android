package dev.bewu.duwolaundry.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import dev.bewu.duwolaundry.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}
