package dev.bewu.duwolaundry.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import dev.bewu.duwolaundry.LaundryApplication;
import dev.bewu.duwolaundry.MultiPossScraper;
import dev.bewu.duwolaundry.R;
import dev.bewu.duwolaundry.WatcherAlarmReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WatcherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WatcherFragment extends Fragment {

    private ActivityResultLauncher<String> requestPermissionLauncher;

    public WatcherFragment() {}

    /**
     * @return A new instance of fragment WatcherFragment.
     */
    public static WatcherFragment newInstance() {
        return new WatcherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // create permissions launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Log.d("Watcher", "no notification permissions");
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startButton = requireActivity().findViewById(R.id.watcherStartButton);
        Button stopButton = requireActivity().findViewById(R.id.watcherStopButton);

        Intent intent = new Intent(getContext(), WatcherAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        startButton.setOnClickListener(v -> {

            // get targets
            EditText wm = requireActivity().findViewById(R.id.wmNumber);
            EditText d = requireActivity().findViewById(R.id.dNumber);

            int wmNumber = Integer.parseInt(wm.getText().toString());
            int dNumber = Integer.parseInt(d.getText().toString());

            HashMap<String, Integer> targets = new HashMap<>();
            targets.put("Washing Machine", wmNumber);
            targets.put("Dryer", dNumber);

            // set targets
            LaundryApplication application = (LaundryApplication) requireActivity().getApplication();
            MultiPossScraper scraper = application.getMultiPossScraper();
            scraper.setTargets(targets);

            // set up alarm
            AlarmManager alarmMgr = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            // cancel any previous alarms
            alarmMgr.cancel(alarmIntent);

            // set up the update alarm - every 1 minute
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    5000, 60000, alarmIntent);
            Log.d("Laundry Watcher", "watcher started");
            Toast.makeText(getContext(), "Watcher started", Toast.LENGTH_SHORT).show();

            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.areNotificationsEnabled()) {
                // ask for notification permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        });

        stopButton.setOnClickListener(v -> {
            AlarmManager alarmMgr = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            if (alarmMgr!= null) {
                alarmMgr.cancel(alarmIntent);
                Log.d("Laundry Watcher", "watcher stopped");
                Toast.makeText(getContext(), "Watcher stopped", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watcher, container, false);
    }
}