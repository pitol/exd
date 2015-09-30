package ppitol.exd.app;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import ppitol.exd.app.model.CurrencyModel;
import ppitol.exd.app.model.CurrencyRepository;
import ppitol.exd.app.rate.RateImport;
import ppitol.exd.app.rate.RateService;

/**
 * Combine the known currencies and the imported rates.
 * Check http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html for an introduction
 * to implementing loaders and a nice sample.
 */
public class CurrencyLoader extends AsyncTaskLoader<List<CurrencyModel>> {

    private static final String TAG = CurrencyLoader.class.getSimpleName();

    private class RateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onContentChanged();
        }
    };

    private final CurrencyRepository currencyRepository;
    private final RateImport rateImport;

    private List<CurrencyModel> currencies;
    private RateReceiver receiver;

    public CurrencyLoader(Context context, CurrencyRepository currencyRepository, RateImport rateImport) {
        super(context);
        this.currencyRepository = currencyRepository;
        this.rateImport = rateImport;
    }

    @Override
    public List<CurrencyModel> loadInBackground() {
        Log.d(TAG, "loadInBackground()");
        List<CurrencyModel> currs = currencyRepository.getLatestCurrencies();
        if (currs == null || currs.isEmpty()) {
            rateImport.importRates();
            currs = currencyRepository.getLatestCurrencies();
        }
        return currs;
    }

    @Override
    public void deliverResult(List<CurrencyModel> result) {
        Log.d(TAG, "deliverResult()");

        if (isReset()) {
            // Should release any resources held by the just loaded result.
            // At this point, it pretty much loaded for nothing.
            return;
        }

        // Any previous result should be kept from being disposed until the new result is
        // delivered just below.
        List<CurrencyModel> prevCurrs = currencies;
        currencies = result;

        if (isStarted()) {
            // Might be in stopped state, there is no delivering then.
            super.deliverResult(currencies);
        }

        // Should release any resources held by the previous results.
        // A simple list (our prevCurrs) will just be garbage collected.
        // In a stopped state we hold to the newly loaded result (our currencies), and deliver
        // it when the loader is restarted, see the beginning of onStartLoading.
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading()");

        if (currencies != null) {
            // Any previous result should be delivered before loading new results
            deliverResult(currencies);
        }

        // Start monitoring the data source, if not already.
        if (receiver == null) {
            receiver = new RateReceiver();
            rateImport.addImportReceiver(receiver);
        }

        // If data changed at the source, or there is no current result, force the load
        if (takeContentChanged() || currencies == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading()");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset()");

        onStopLoading();

        // This is when our results can be disposed
        // Any held resources should be released here.
        currencies = null;

        // Stop monitoring the data source.
        if (receiver != null) {
            rateImport.removeImportReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onCanceled(List<CurrencyModel> data) {
        Log.d(TAG, "onCanceled()");
        super.onCanceled(data);
    }
}
