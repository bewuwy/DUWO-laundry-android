package dev.bewu.duwolaundry;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.bewu.duwolaundry.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainActivityNavDrawer.toolbar);
        binding.appBarMainActivityNavDrawer.floatingRefresh.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(),
                    "Refreshing...", Toast.LENGTH_SHORT).show();

            updateAvailability(false);
        });
        binding.appBarMainActivityNavDrawer.floatingRefresh.setOnLongClickListener(v -> {
            Toast.makeText(getApplicationContext(),
                    "(Hard) refreshing...", Toast.LENGTH_SHORT).show();

            updateAvailability(true);
            return true;
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_watcher, R.id.nav_website, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_website) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        preferences.getString("multipossURL", "https://duwo.multiposs.nl")
                ));
                startActivity(browserIntent);
                return true;
            } else {
                // Fallback for all other (normal) cases.
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

                // This is usually done by the default ItemSelectedListener.
                // But there can only be one! Unfortunately.
                if (handled)
                    drawer.closeDrawer(navigationView);

                // return the result of NavigationUI call
                return handled;
            }
        });

        // check if login is setup - if none open LoginFragment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.contains("userMail") || preferences.getString("userMail", "").isEmpty()) {
            Log.d("MainActivity", "no mail set, opening login activity");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    public void updateAvailability(boolean hardRefresh) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d("MainActivity", "multipossURL: " + preferences.getString("multipossURL", ""));

            MultiPossScraper scraper = ((LaundryApplication) this.getApplication()).getMultiPossScraper();
            if (scraper == null) {
                // initialise scraper
                scraper = new MultiPossScraper(
                        preferences.getString("userMail", ""),
                        preferences.getString("userPwd", ""),
                        preferences.getString("multipossURL", "https://duwo.multiposs.nl")
                );
                ((LaundryApplication) this.getApplication()).setMultiPossScraper(scraper);
            }

            if (hardRefresh) {
                scraper.setForceReInit(true);
            }

            HashMap<String, Integer> availability = scraper.fetchAvailability();

            // check if there is an account associated with the email
            if (scraper.getUserLocation() == null) {

                handler.post(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Could not log in")
                            .setMessage("Incorrect email address!\nCould not find any Multiposs account associated with it.")
                            .setPositiveButton("Sign out", (dialogInterface, i) -> {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove("userMail");
                                editor.apply();

                                Intent loginIntent = new Intent(this, LoginActivity.class);
                                startActivity(loginIntent);
                            })
                            .show();
                });
            }

            String qr = scraper.getQRCode();

            MultiPossScraper finalScraper = scraper;
            handler.post(() -> {
                //UI Thread work here

                TextView wm_statusText = findViewById(R.id.wm_available);
                TextView d_statusText = findViewById(R.id.dryer_available);
                TextView balanceValueText = findViewById(R.id.balanceValue);
                View balanceLayout = findViewById(R.id.balanceLayout);
                TextView userBigText = findViewById(R.id.userBigText);
                TextView userSmallText = findViewById(R.id.userSmallText);

                userBigText.setText(String.format("%s %s",
                        getString(R.string.multiposs), finalScraper.getUserLocation()));
                userSmallText.setText(finalScraper.getUserEmail());

                if (wm_statusText == null)
                    return;

                int wm_status = availability.get("Washing Machine");
                int d_status = availability.get("Dryer");

                if (wm_status > 0) {
                    wm_statusText.setText(String.format("%s %s", wm_status, getString(R.string.available)));
                    wm_statusText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_bg));
                    wm_statusText.setTextColor(ContextCompat.getColor(this, R.color.green_fg));
                } else {
                    wm_statusText.setText(R.string.unavailable);
                    wm_statusText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red_bg));
                    wm_statusText.setTextColor(ContextCompat.getColor(this, R.color.red_fg));
                }

                if (d_status > 0) {
                    d_statusText.setText(String.format("%s %s", d_status, getString(R.string.available)));
                    d_statusText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_bg));
                    d_statusText.setTextColor(ContextCompat.getColor(this, R.color.green_fg));
                } else {
                    d_statusText.setText(R.string.unavailable);
                    d_statusText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red_bg));
                    d_statusText.setTextColor(ContextCompat.getColor(this, R.color.red_fg));
                }

                balanceValueText.setText(finalScraper.getUserBalance());
                if (finalScraper.getUserBalance() != null) {
                    balanceLayout.setVisibility(View.VISIBLE);
                }
                setQRCode(qr);

                Toast.makeText(getApplicationContext(),
                        "Refreshed successfully!", Toast.LENGTH_SHORT).show();

                // TODO: (better) error fetching availability information

                if (!finalScraper.getExceptionString().isEmpty()) {
                    Toast.makeText(this, finalScraper.getExceptionString(), Toast.LENGTH_LONG).show();
//                    statusText.setText(String.format("Error:\n%s", finalScraper.getExceptionString()));
                }
            });
        });
    }

    private void setQRCode(String text) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE,
                    640, 640);
            ImageView qrCodeView = findViewById(R.id.qrPass);
            ImageView qrPassEnlarged = findViewById(R.id.qrPass_expanded);

            qrCodeView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
            qrPassEnlarged.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            Toast.makeText(getApplicationContext(),
                    "Couldn't generate your QR code (WriterException)!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}