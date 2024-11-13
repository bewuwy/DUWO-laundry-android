package dev.bewu.duwolaundry.ui;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
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
import dev.bewu.duwolaundry.WatcherAlarmReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WatcherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WatcherFragment extends Fragment {

    private final String CHANNEL_ID = "DUWO_Laundry_Channel";
    private final int notificationNumber = 1;

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

        startButton.setOnClickListener(v -> {
            AlarmManager alarmMgr = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(getContext(), WatcherAlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

            // set up the update alarm - every 1 minute
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    5000, 60000, alarmIntent);
            Log.d("Laundry Watcher", "watcher started");
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watcher, container, false);
    }
}