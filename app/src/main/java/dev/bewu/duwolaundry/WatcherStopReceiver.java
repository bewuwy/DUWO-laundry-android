package dev.bewu.duwolaundry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WatcherStopReceiver extends BroadcastReceiver {

    public WatcherStopReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WatcherStop", "Stopping the watcher");

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent watcherIntent = new Intent(context, WatcherAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, watcherIntent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
            Log.d("WatcherStop", "watcher stopped");
        }
    }
}
