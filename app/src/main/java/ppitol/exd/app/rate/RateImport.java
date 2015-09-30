package ppitol.exd.app.rate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;

import ppitol.exd.app.model.CurrencyRepository;

/**
 * Facade to import currency rates.
 */
public class RateImport {

    private final Context context;
    private final CurrencyRepository currencyRepository;
    private final RateAvailableTime timer;
    private final RateImportPrecondition precondition;
    private final RateServiceScheduler scheduler;

    public RateImport(Context context, CurrencyRepository currencyRepository) {
        this.context = context;
        this.currencyRepository = currencyRepository;
        this.timer = new RateAvailableTime();
        this.precondition = new RateImportPrecondition(context, currencyRepository, timer);
        this.scheduler = new RateServiceScheduler(timer);
    }

    /**
     * Import the rates synchronously.
     */
    public void importRates() {
        if (precondition.importRatesNow()) {
            Date fetchDateTime = new Date();
            new BCBRateImporter(currencyRepository, fetchDateTime, timer.getPreviousAvailableTime(fetchDateTime)).importRates();
        }
    }

    /*
     * Schedule an asynchronous rate import.
     */
    public void scheduleImportRates() {
        scheduler.scheduleNextRun(context);
    }

    /**
     * Add a broadcast receiver to listen for asynchronous rate imports.
     */
    public void addImportReceiver(BroadcastReceiver importReceiver) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
                importReceiver, new IntentFilter(RateService.INTENT_SERVICE_DONE));
    }

    /**
     * Remove a broadcast receiver from listening for asynchronous rate imports.
     */
    public void removeImportReceiver(BroadcastReceiver importReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(importReceiver);
    }
}
