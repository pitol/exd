package ppitol.exd.app.rate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;

import ppitol.exd.app.model.CurrencyRepository;

/**
 * Check whether the rate import should proceed.
 */
public class RateImportPrecondition {

    private final Context context;
    private final CurrencyRepository currencyRepository;
    private final RateAvailableTime timer;

    public RateImportPrecondition(Context context, CurrencyRepository currencyRepository, RateAvailableTime timer) {
        this.context = context;
        this.currencyRepository = currencyRepository;
        this.timer = timer;
    }

    private boolean ratesNotImportedAt(Date referenceDate) {
        Date last = currencyRepository.getLastMaxRateDate();
        Date fetchDate = timer.getPreviousAvailableTime(referenceDate);
        return last == null || fetchDate.after(last);
    }

    private boolean networkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean importRatesAt(Date date) {
        return networkAvailable() && ratesNotImportedAt(date);
    }

    public boolean importRatesNow() {
        return importRatesAt(new Date());
    }
}
