package ppitol.exd.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.sort;
import static ppitol.exd.app.model.RateContract.rateValues;

/**
 * Access the known currencies.
 */
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class CurrencyRepository {

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    private static class LocaleCurrency {
        final Locale locale;
        final Currency currency;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof  LocaleCurrency)) {
                return false;
            }
            String otherCountry = ((LocaleCurrency) o).locale.getCountry();
            return locale.getCountry().equals(otherCountry);
        }

        @Override
        public int hashCode() {
            return locale.getCountry().hashCode();
        }
    }

    private final Context context;

    private LinkedHashMultimap<String, LocaleCurrency> knownCurrencies;

    private LinkedHashMultimap<String, LocaleCurrency> knownCurrencies() {
        if (knownCurrencies == null) {
            knownCurrencies = LinkedHashMultimap.create();
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale : locales) {
                try {
                    Currency currency = Currency.getInstance(locale);
                    LocaleCurrency lc = new LocaleCurrency(locale, currency);
                    knownCurrencies.put(lc.currency.getCurrencyCode(), lc);
                } catch (IllegalArgumentException e) {
                    // Ignore unsupported locales
                }
            }
        }
        return knownCurrencies;
    }

    private Iterable<CurrencyModel> createCurrencyModels(final String currencyCode, final BigDecimal rate) {
        return transform(knownCurrencies().get(currencyCode), new Function<LocaleCurrency, CurrencyModel>() {
            @Override
            public CurrencyModel apply(LocaleCurrency input) {
                return new CurrencyModel(input.locale, input.currency, rate);
            }
        });
    }

    public boolean isKnownCurrencyCode(String currencyCode) {
        return knownCurrencies().containsKey(currencyCode);
    }

    private Iterable<CurrencyModel> readCurrencyModels(Cursor c) {
        String currencyCode = RateContract.getCurrencyCode(c);
        BigDecimal rate = RateContract.getRate(c);
        return createCurrencyModels(currencyCode, rate);
    }

    public List<CurrencyModel> getLatestCurrencies() {
        List<CurrencyModel> availableCurrencies = new ArrayList<>();
        Cursor c = context.getContentResolver().query(
                RateContract.RATE_URI,
                RateContract.DEFAULT_PROJECTION,
                RateContract.LATEST_RATES,
                null,
                RateContract.DEFAULT_SORT_ORDER);
        while (c.moveToNext()) {
            addAll(availableCurrencies, readCurrencyModels(c));
        }
        c.close();
        sort(availableCurrencies);
        return availableCurrencies;
    }

    public Date getLastMaxRateDate() {
        Date date = null;
        Cursor c = context.getContentResolver().query(
                RateContract.RATE_URI,
                new String[] { "max(" + RateContract.RateColumns.DATE + ")" },
                null,
                null,
                null);
        if (c.moveToNext()) {
            date = new Date(c.getLong(0));
        }
        c.close();
        return date;
    }

    public void insertCurrency(Date date, String currencyCode, BigDecimal rate) {
        ContentValues values = rateValues(currencyCode, date, rate);
        context.getContentResolver().insert(RateContract.RATE_URI, values);
    }
}
