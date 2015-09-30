package ppitol.exd.app.rate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class RateServiceScheduler {

    private static final String TAG = RateServiceScheduler.class.getSimpleName();

    private final RateAvailableTime timer;

    public RateServiceScheduler(RateAvailableTime timer) {
        this.timer = timer;
    }

    public void scheduleNextRun(Context context) {
        Date nextFetchDate = timer.getNextAvailableTime(new Date());
        // Add a random number of ms up to 10 minutes to the next available time, as the schedule time
        Date runDate = new Date(nextFetchDate.getTime() + new Random().nextInt(600000));

        Intent intent = new Intent(context, RateServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, runDate.getTime(), pendingIntent);

        Log.d(TAG, "Scheduled rate import for fetch date " + runDate);
    }

    public void cancelSchedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RateServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }
}
