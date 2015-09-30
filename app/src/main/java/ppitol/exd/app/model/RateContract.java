package ppitol.exd.app.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Contract describing the currency rates content provider.
 */
public class RateContract {

    private static final String PACKAGE = RateProvider.class.getPackage().getName();

    public static final String AUTHORITY = RateProvider.class.getName();

    public static final String RATE_CONTENT = "rate";
    public static final Uri RATE_URI = Uri.parse("content://" + AUTHORITY + "/" + RATE_CONTENT);
    public static final String RATE_ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + PACKAGE + "." + RATE_CONTENT;
    public static final String RATE_DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + PACKAGE + "." + RATE_CONTENT;

    public static class RateColumns {
        public static final String ID = BaseColumns._ID;
        public static final String CURRENCY_CODE = "currencyCode";
        public static final String DATE = "date";
        public static final String RATE_UNSCALED = "rate_unscaled";
        public static final String RATE_SCALE = "rate_scale";
    }

    public static final String DEFAULT_SORT_ORDER = String.format("%s ASC, %s ASC",
            RateColumns.CURRENCY_CODE, RateColumns.DATE);
    public static final String[] DEFAULT_PROJECTION = {
            RateColumns.ID,
            RateColumns.CURRENCY_CODE,
            RateColumns.DATE,
            RateColumns.RATE_UNSCALED,
            RateColumns.RATE_SCALE
    };
    public static final String LATEST_RATES = "date = (select max(date) from rate)";

    public static ContentValues rateValues(String currencyCode, Date date, BigDecimal rate) {
        ContentValues values = new ContentValues();
        values.put(RateColumns.CURRENCY_CODE, currencyCode);
        values.put(RateColumns.DATE, date.getTime());
        values.put(RateColumns.RATE_SCALE, rate.scale());
        values.put(RateColumns.RATE_UNSCALED, rate.unscaledValue().longValue());
        return values;
    }

    public static BigDecimal rate(long unscaled, int scale) {
        return BigDecimal.valueOf(unscaled, scale);
    }

    public static String getCurrencyCode(Cursor c) {
        return c.getString(c.getColumnIndex(RateColumns.CURRENCY_CODE));
    }

    public static BigDecimal getRate(Cursor c) {
        return rate(
                c.getLong(c.getColumnIndex(RateColumns.RATE_UNSCALED)),
                c.getInt((c.getColumnIndex(RateColumns.RATE_SCALE)))
        );
    }
}
