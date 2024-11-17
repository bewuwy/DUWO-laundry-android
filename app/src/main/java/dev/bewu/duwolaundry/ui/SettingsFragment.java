package dev.bewu.duwolaundry.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import dev.bewu.duwolaundry.LaundryApplication;
import dev.bewu.duwolaundry.LoginActivity;
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

        Preference signOut = findPreference("signOut");
        assert signOut != null;
        signOut.setOnPreferenceClickListener(p -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("userMail", "");
            editor.putString("userPwd", "");

            editor.apply();

            Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();

            // go to login activity
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);

            return true;
        });
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
