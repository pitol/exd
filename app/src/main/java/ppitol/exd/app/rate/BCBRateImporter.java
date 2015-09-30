package ppitol.exd.app.rate;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ppitol.exd.app.model.CurrencyRepository;

/**
 * Import rates from BCB public dataset
 */
class BCBRateImporter {

    private static final String TAG = BCBRateImporter.class.getSimpleName();

    public static final String BCB_RATES_BASE_URL = "http://www4.bcb.gov.br/Download/fechamento/";

    private static final int CURRENCY_CODE_FIELD_IDX = 3;
    private static final int SELL_RATE_FIELD_IDX = 5;
    private static final String FIELD_SEPARATOR = ";";

    private final CurrencyRepository currencyRepository;
    private final Date fetchDateTime;
    private final String ratesUrl;

    BCBRateImporter(CurrencyRepository currencyRepository, Date fetchDateTime, Date availableDateTime) {
        this(currencyRepository, fetchDateTime, availableDateTime, BCB_RATES_BASE_URL);
    }

    BCBRateImporter(CurrencyRepository currencyRepository, Date fetchDateTime, Date availableDateTime, String ratesBaseUrl) {
        this.currencyRepository = currencyRepository;
        this.fetchDateTime = fetchDateTime;
        DateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        String availableDatePrefix = fmt.format(availableDateTime) + ".csv";
        this.ratesUrl = ratesBaseUrl + availableDatePrefix;
    }

    public void importRates() {
        NumberFormat ptbrDecFmt = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        try {
            URL url = new URL(ratesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = rdr.readLine();
                while (line != null) {
                    String[] fields = line.split(FIELD_SEPARATOR);

                    String currencyCode = fields[CURRENCY_CODE_FIELD_IDX];
                    if (currencyRepository.isKnownCurrencyCode(currencyCode)) {
                        Number n = ptbrDecFmt.parse(fields[SELL_RATE_FIELD_IDX]);
                        BigDecimal rate = BigDecimal.valueOf(n.doubleValue());
                        currencyRepository.insertCurrency(fetchDateTime, currencyCode, rate);
                    }

                    line = rdr.readLine();
                }
            } finally {
                conn.disconnect();
            }

            // The file does not include BCB's own currency
            currencyRepository.insertCurrency(fetchDateTime, "BRL", BigDecimal.ONE);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Rates not available at " + ratesUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
