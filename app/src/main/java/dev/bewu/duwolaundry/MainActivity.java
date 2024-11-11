package dev.bewu.duwolaundry;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.bewu.duwolaundry.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    MultiPossScraper scraper = new MultiPossScraper("user@mail.com", "password");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainActivityNavDrawer.toolbar);
        binding.appBarMainActivityNavDrawer.floatingRefresh.setOnClickListener(v -> {

            Toast.makeText(getApplicationContext(),
                    "Refreshing...", Toast.LENGTH_SHORT).show();

            updateAvailability();
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity_nav_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // update availability for the 1st time
        updateAvailability();
    }

    public void updateAvailability() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here

            scraper.initScraper();
            HashMap<String, Integer> availability = scraper.getAvailability();

            String qr = scraper.getQRCode();

            handler.post(() -> {
                //UI Thread work here

                TextView textView = findViewById(R.id.statusTextView);

                if (textView == null)
                    return;

                StringBuilder availabilityString = new StringBuilder();
                for (String machine: availability.keySet()) {
                    availabilityString.append(machine).append(": ")
                            .append(availability.get(machine)).append("\n");
                }

                textView.setText(availabilityString.toString());

                setQRCode(qr);

                Toast.makeText(getApplicationContext(),
                        "Refreshed successfully!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void setQRCode(String text) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE,
                    640, 640);
            ImageView qrCodeView = findViewById(R.id.qrPass);
            qrCodeView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
        }
        catch (WriterException e) {
            Toast.makeText(getApplicationContext(),
                    "Couldn't generate your QR code (WriterException)!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity__nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity_nav_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}