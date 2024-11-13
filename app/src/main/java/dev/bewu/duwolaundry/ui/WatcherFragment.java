package dev.bewu.duwolaundry.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.bewu.duwolaundry.LaundryApplication;
import dev.bewu.duwolaundry.MultiPossScraper;
import dev.bewu.duwolaundry.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WatcherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WatcherFragment extends Fragment {

    private final String CHANNEL_ID = "DUWO_Laundry_Channel";
    private int notificationNumber = 1;

    public WatcherFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment WatcherFragment.
     */
    public static WatcherFragment newInstance() {
        return new WatcherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startButton = requireActivity().findViewById(R.id.watcherStartButton);
        LaundryApplication app = (LaundryApplication) requireActivity().getApplication();
        MultiPossScraper scraper = app.getMultiPossScraper();

        startButton.setOnClickListener(v -> {
            Log.d("Laundry Watcher", "watcher started");

            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {
                HashMap<String, Integer> availability = scraper.fetchAvailability();

                createNotificationChannel();

                // create notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("DUWO Laundry")
                        .setContentText(availability.toString())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // send notification
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(notificationNumber, builder.build());
                // notificationNumber++; // don't update - then the old notification will be updated
            });
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watcher, container, false);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Laundry notifications";
            String description = "Laundry notifications description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            assert getContext() != null;
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}