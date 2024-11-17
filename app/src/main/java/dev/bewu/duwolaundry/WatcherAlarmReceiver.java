package dev.bewu.duwolaundry;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatcherAlarmReceiver extends BroadcastReceiver {

    private final int statusNotificationNumber = 1;
    private final String STATUS_CHANNEL_ID = "DUWO_Laundry_channel_status";

    private final int targetNotificationNumber = 2;
    private final String TARGET_CHANNEL_ID = "DUWO_Laundry_channel_target";

    private final int washingMachineTarget = 0;  // TODO: add setting

    public WatcherAlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Watcher Alarm", "Alarm went off");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        MultiPossScraper scraper = ((LaundryApplication) context.getApplicationContext()).getMultiPossScraper();

        executor.execute(() -> {
            HashMap<String, Integer> availability = scraper.fetchAvailability();

            if (availability.get("Washing Machine") >= washingMachineTarget) {
                sendTargetReachedNotification(context);
            }

            sendStatusNotification(context, availability);
        });
    }

    private void sendTargetReachedNotification(Context context) {
        createTargetNotificationChannel(context);

        String notificationString = "Reached your target! (" + washingMachineTarget + " washing machines)";

        // create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TARGET_CHANNEL_ID)
                .setSmallIcon(R.drawable.laundry_24)
                .setContentTitle("Laundry Target Reached")
                .setContentText(notificationString)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // send notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(targetNotificationNumber, builder.build());
    }

    private void sendStatusNotification(Context context, HashMap<String, Integer> availability) {
        createStatusNotificationChannel(context);

        StringBuilder notificationStringBuilder = new StringBuilder();
        for (HashMap.Entry<String, Integer> entry : availability.entrySet()) {
            String key = entry.getKey();
            int val = entry.getValue();

            notificationStringBuilder.append(val).append(" ").append(key);

            if (val != 1) {
                notificationStringBuilder.append("s, ");
            } else {
                notificationStringBuilder.append(", ");
            }
        }

        // remove last ", "
        String notificationString = notificationStringBuilder.toString();
        notificationString = notificationString.substring(0, notificationString.length()-2);

        // create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
                .setSmallIcon(R.drawable.laundry_24)
                .setContentTitle("Laundry Status")
                .setContentText(notificationString)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // send notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(statusNotificationNumber, builder.build());
    }

    private void createStatusNotificationChannel(Context context) {
        CharSequence name = "Laundry status notifications";
        String description = "Availability status";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(STATUS_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        assert context != null;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void createTargetNotificationChannel(Context context) {
        CharSequence name = "Laundry target notifications";
        String description = "Availability target reached";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(TARGET_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        assert context != null;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
