package ppitol.exd.app.rate;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

import ppitol.exd.app.model.CurrencyRepository;

/**
 * Fetch currencies and its rates
 */
public class RateService extends IntentService {

    private static final String TAG = RateService.class.getSimpleName();

    private static final String RATES_BASE_URL_KEY = "rates.base.url";
    private static final String FETCH_DATE_KEY = "fetch.date.key";

    public static final String INTENT_SERVICE_DONE = TAG + ".DONE";

    public static void startCurrencyService(Context ctx) {
        startCurrencyService(ctx, BCBRateImporter.BCB_RATES_BASE_URL, new Date());
    }

    public static void startCurrencyService(Context ctx, String ratesBaseUrl, Date fetchDate) {
        ctx.startService(makeIntent(ctx, ratesBaseUrl, fetchDate));
    }

    private static Intent makeIntent(Context ctx, String ratesBaseUrl, Date fetchDate) {
        Intent intent = new Intent(ctx, RateService.class);
        intent.putExtra(RATES_BASE_URL_KEY, ratesBaseUrl);
        intent.putExtra(FETCH_DATE_KEY, fetchDate);
        return intent;
    }

    public RateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "On rates import service");

        Date fetchDateTime  = (Date) intent.getSerializableExtra(FETCH_DATE_KEY);
        CurrencyRepository currencyRepository = new CurrencyRepository(this);
        RateAvailableTime timer = new RateAvailableTime();
        RateImportPrecondition precondition = new RateImportPrecondition(this, currencyRepository, timer);
        if (precondition.importRatesAt(fetchDateTime)) {
            Log.d(TAG, "Starting rates import");

            Date availableTime = timer.getPreviousAvailableTime(fetchDateTime);
            String ratesBaseUrl = intent.getStringExtra(RATES_BASE_URL_KEY);

            BCBRateImporter importer = new BCBRateImporter(new CurrencyRepository(this), fetchDateTime, availableTime, ratesBaseUrl);
            importer.importRates();

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(INTENT_SERVICE_DONE));
        }
        Log.d(TAG, "Ending rates import service");
    }
}
