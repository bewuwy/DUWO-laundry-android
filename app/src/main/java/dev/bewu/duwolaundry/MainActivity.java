package dev.bewu.duwolaundry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.annotation.NonNull;
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

            String qr = scraper.getQRCode();

            MultiPossScraper finalScraper = scraper;
            handler.post(() -> {
                //UI Thread work here

                TextView statusText = findViewById(R.id.statusTextView);
                TextView balanceValueText = findViewById(R.id.balanceValue);
                View balanceLayout = findViewById(R.id.balanceLayout);
                TextView userBigText = findViewById(R.id.userBigText);
                TextView userSmallText = findViewById(R.id.userSmallText);

                userBigText.setText(String.format("%s %s",
                        getString(R.string.multiposs), finalScraper.getUserLocation()));
                userSmallText.setText(finalScraper.getUserEmail());

                if (statusText == null)
                    return;

                StringBuilder availabilityString = new StringBuilder();
                for (String machine: availability.keySet()) {
                    availabilityString.append(machine).append(": ")
                            .append(availability.get(machine)).append("\n");
                }

                String avString = availabilityString.toString();

                if (!avString.isEmpty()) {
                    statusText.setText(avString);
                    balanceValueText.setText(finalScraper.getUserBalance());
                    if (finalScraper.getUserBalance() != null) {
                        balanceLayout.setVisibility(View.VISIBLE);
                    }
                    setQRCode(qr);

                    Toast.makeText(getApplicationContext(),
                            "Refreshed successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    statusText.setText(R.string.error_while_fetching_status);
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