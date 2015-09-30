package ppitol.exd.app.model.test;

import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import java.math.BigDecimal;
import java.util.Date;

import ppitol.exd.app.model.RateContract;
import ppitol.exd.app.model.RateContract.RateColumns;
import ppitol.exd.app.model.RateDatabase;
import ppitol.exd.app.model.RateProvider;

public class RateProviderTest extends ProviderTestCase2<RateProvider> {

    private MockContentResolver resolver;
    private RateDatabase db;

    public RateProviderTest(Class<RateProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public RateProviderTest() {
        this(RateProvider.class, RateContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        db = getProvider().getRateDatabase();
    }

    private void assertRate(long expectedId, RateFixture expected, Cursor c) {
        assertEquals(expectedId, c.getLong(c.getColumnIndexOrThrow(RateColumns.ID)));
        expected.assertRate(c);
    }

    public void testQueryRate() {
        RateFixture rateFix = new RateFixture("FOO", new Date(), new BigDecimal("1.2345"));
        FixtureInsertion rateIns = rateFix.insert(db);

        Cursor c = resolver.query(RateContract.RATE_URI, RateContract.DEFAULT_PROJECTION, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertRate(rateIns.id, rateFix, c);
        c.close();
    }

    public void testInsertRate() {
        RateFixture rateFix = new RateFixture("FOO", new Date(), new BigDecimal("1.2345"));

        Uri insertedUri = resolver.insert(RateContract.RATE_URI, rateFix.values);
        assertNotNull(insertedUri);

        Cursor c = resolver.query(insertedUri, null, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertRate(ContentUris.parseId(insertedUri), rateFix, c);
        c.close();
    }

    public void testCantInsertRatesForTheSameCurrencyWithSameDatetime() {
        Date date = new Date();
        RateFixture rateFix1 = new RateFixture("FOO", date, new BigDecimal("1.2345"));
        RateFixture rateFix2 = new RateFixture("FOO", date, new BigDecimal("6.789"));

        resolver.insert(RateContract.RATE_URI, rateFix1.values);
        try {
            resolver.insert(RateContract.RATE_URI, rateFix2.values);
            fail("Unique currency and date constraint not enforced");
        } catch (SQLiteConstraintException e) {
            String msg = e.getMessage();
            assertTrue(msg, msg.toLowerCase().contains("unique"));
            assertTrue(msg, msg.contains("currencyCode"));
            assertTrue(msg, msg.contains("date"));
       }
    }
}
