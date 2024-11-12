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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        assert getContext() != null;
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        // reinit scraper after changing settings
        assert getActivity() != null && getActivity().getApplication() != null;

        // initialise scraper
        assert getContext() != null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        MultiPossScraper scraper = new MultiPossScraper(
            preferences.getString("userMail", ""),
            preferences.getString("userPwd", ""),
            preferences.getString("multipossURL", "https://duwo.multiposs.nl")
        );
        ((LaundryApplication) getActivity().getApplication()).setMultiPossScraper(scraper);
    }
}
