package ppitol.exd.app.model;

import com.google.common.base.Predicate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Model for a currency in a country.
 */
@EqualsAndHashCode(of = { "countryCode", "currencyCode" })
public class CurrencyModel implements Serializable, Comparable<CurrencyModel> {

    public static Predicate<CurrencyModel> findByCountry(final String countryCode) {
        return new Predicate<CurrencyModel>() {
            @Override
            public boolean apply(CurrencyModel input) {
                return input != null && Objects.equals(input.countryCode, countryCode);
            }
        };
    }

    public final String countryName;
    public final String countryCode;
    public final String currencyCode;
    public final String currencyName;
    public final String currencySymbol;
    public final String currencyText;
    public final BigDecimal referenceRate;

    public CurrencyModel(Locale locale, Currency currency, BigDecimal referenceRate) {
        Locale defaultLocale = Locale.getDefault();
        this.countryName = locale.getDisplayCountry(defaultLocale);
        this.countryCode = locale.getCountry();
        this.currencyCode = currency.getCurrencyCode();
        this.currencyName = currency.getDisplayName(defaultLocale);
        this.currencySymbol = currency.getSymbol(defaultLocale);
        this.currencyText = this.currencyName + " - " + this.currencySymbol;
        this.referenceRate = referenceRate;
    }

    @Override
    public int compareTo(CurrencyModel other) {
        Collator collator = Collator.getInstance();
        return collator.compare(countryName, other.countryName);
    }
}
