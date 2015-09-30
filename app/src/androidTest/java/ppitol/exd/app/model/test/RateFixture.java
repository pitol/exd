package ppitol.exd.app.model.test;

import android.content.ContentValues;
import android.database.Cursor;

import java.math.BigDecimal;
import java.util.Date;

import ppitol.exd.app.model.RateContract;
import ppitol.exd.app.model.RateDatabase;

import static junit.framework.Assert.assertEquals;

/**
 * Currency rates data for testing.
 */
public class RateFixture {

    public final String currencyCode;
    public final Date date;
    public final BigDecimal rate;
    public final ContentValues values;

    public RateFixture(String currencyCode, Date date, BigDecimal rate) {
        this.currencyCode = currencyCode;
        this.date = date;
        this.rate = rate;
        this.values = RateContract.rateValues(currencyCode, date, rate);
    }

    /**
     * Insert currency fixture data with the currency content provider uri.
     *
     * @param db to make the insert.
     * @return FixtureInsertion with the id and uri of the insert, if successful.
     */
    public FixtureInsertion insert(RateDatabase db) {
        long id = db.insert(RateContract.RATE_CONTENT, values);
        return new FixtureInsertion(RateContract.RATE_URI, id);
    }

    public void assertRate(Cursor c) {
        assertEquals(currencyCode, c.getString(c.getColumnIndexOrThrow(RateContract.RateColumns.CURRENCY_CODE)));
        assertEquals(date.getTime(), c.getLong(c.getColumnIndexOrThrow(RateContract.RateColumns.DATE)));
        assertEquals(rate.unscaledValue().longValue(), c.getLong((c.getColumnIndexOrThrow(RateContract.RateColumns.RATE_UNSCALED))));
        assertEquals(rate.scale(), c.getInt(c.getColumnIndexOrThrow(RateContract.RateColumns.RATE_SCALE)));
    }
}
