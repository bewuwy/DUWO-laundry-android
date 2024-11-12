package dev.bewu.duwolaundry.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import dev.bewu.duwolaundry.LaundryApplication;
import dev.bewu.duwolaundry.MultiPossScraper;
import dev.bewu.duwolaundry.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    LaundryApplication laundryApplication;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        assert getContext() != null;
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        laundryApplication = (LaundryApplication) requireActivity().getApplication();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, @Nullable String s) {
        // reinit scraper after changing settings

        // initialise scraper
        MultiPossScraper scraper = new MultiPossScraper(
            preferences.getString("userMail", ""),
            preferences.getString("userPwd", ""),
            preferences.getString("multipossURL", "https://duwo.multiposs.nl")
        );

        laundryApplication.setMultiPossScraper(scraper);
    }
}
