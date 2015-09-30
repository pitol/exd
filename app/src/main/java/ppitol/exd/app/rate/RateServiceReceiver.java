package ppitol.exd.app.rate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RateServiceReceiver extends BroadcastReceiver {

    private static final String TAG = RateServiceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting scheduled rate import");

        RateService.startCurrencyService(context);
        RateServiceScheduler scheduler = new RateServiceScheduler(new RateAvailableTime());
        scheduler.scheduleNextRun(context);
    }
}
