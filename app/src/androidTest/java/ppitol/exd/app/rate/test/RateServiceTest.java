package ppitol.exd.app.rate.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.test.RenamingDelegatingContext;
import android.test.ServiceTestCase;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ppitol.exd.app.model.RateContract;
import ppitol.exd.app.rate.RateService;
import ppitol.exd.app.model.test.RateFixture;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.transform;
import static ppitol.exd.app.model.RateContract.rate;

public class RateServiceTest extends ServiceTestCase<RateService> {

    public static class DoneReceiver extends BroadcastReceiver {
        boolean done = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized(this) {
                done = true;
                notify();
            }
        }

        public void waitService() throws InterruptedException {
            synchronized (this) {
                wait(5000);
            }
        }
    }

    private static final int SCALE = 7;

    private static Function<RateFixture, String> rateFixtureToCsv = new Function<RateFixture, String>() {
        @Override
        public String apply(RateFixture fix) {
            Locale ptbr = new Locale("pt", "BR");
            NumberFormat numFmt = NumberFormat.getNumberInstance(ptbr);
            numFmt.setMinimumFractionDigits(SCALE);
            return Joiner.on(";").join(
                    "ignored",
                    "ignored",
                    "ignored",
                    fix.currencyCode,
                    "ignored",
                    numFmt.format(fix.rate),
                    "ignored",
                    "ignored");
        }
    };

    public RateServiceTest() {
        super(RateService.class);
        setContext(new RenamingDelegatingContext(getContext(), "RateServiceTest."));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getContext().getContentResolver().delete(RateContract.RATE_URI, null, null);
    }

    public void testImportRates() throws Exception {
        Date date = new Date();
        List<RateFixture> fixtures = of(
                new RateFixture("USD", date, rate(23456789, SCALE)),
                new RateFixture("EUR", date, rate(34567899, SCALE)),
                new RateFixture("VND", date, rate(1213, SCALE)));

        MockWebServer server = new MockWebServer();
        server.play();

        String body = Joiner.on("\n").join(transform(fixtures, rateFixtureToCsv));
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        final DoneReceiver receiver = new DoneReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(RateService.INTENT_SERVICE_DONE));

        RateService.startCurrencyService(getContext(), server.getUrl("/rates/").toString(), date);

        receiver.waitService();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);

        server.shutdown();

        if (!receiver.done) {
            fail("Never received done broadcast");
        }

        Cursor c = getContext().getContentResolver().query(
                RateContract.RATE_URI, RateContract.DEFAULT_PROJECTION, null, null, RateContract.RateColumns.ID);

        fixtures = ImmutableList.<RateFixture> builder()
                .addAll(fixtures)
                .add(new RateFixture("BRL", date, BigDecimal.ONE)).build();

        assertEquals(fixtures.size(), c.getCount());
        for (RateFixture fixture : fixtures) {
            assertTrue(c.moveToNext());
            fixture.assertRate(c);
        }
        c.close();
    }

    public void testRatesNotAvailable() throws Exception {
        MockWebServer server = new MockWebServer();
        server.play();
        server.enqueue(new MockResponse().setResponseCode(404).setBody("Nope!"));

        final DoneReceiver receiver = new DoneReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(RateService.INTENT_SERVICE_DONE));

        Date date = new Date();
        RateService.startCurrencyService(getContext(), server.getUrl("/rates/").toString(), date);

        receiver.waitService();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);

        server.shutdown();

        if (!receiver.done) {
            fail("Never received done broadcast");
        }

        Cursor c = getContext().getContentResolver().query(
                RateContract.RATE_URI, RateContract.DEFAULT_PROJECTION, null, null, RateContract.RateColumns.ID);
        assertEquals(0, c.getCount());
        c.close();
    }
}
