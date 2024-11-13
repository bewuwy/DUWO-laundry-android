package dev.bewu.duwolaundry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatcherAlarmReceiver extends BroadcastReceiver {

    private final int notificationNumber = 1;
    private final String CHANNEL_ID = "DUWO_Laundry_channel";

    public WatcherAlarmReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
        Log.d("Watcher Alarm", "Alarm went off");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        MultiPossScraper scraper = ((LaundryApplication) context.getApplicationContext()).getMultiPossScraper();

        executor.execute(() -> {
            HashMap<String, Integer> availability = scraper.fetchAvailability();

            createNotificationChannel(context);

            // create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("DUWO Laundry")
                    .setContentText(availability.toString())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // send notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationNumber, builder.build());
            // notificationNumber++; // don't update - then the old notification will be updated
        });
    }

    private void createNotificationChannel(Context context) {
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
            assert context != null;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
