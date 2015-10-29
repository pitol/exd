package ppitol.exd.app.rate.test;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import ppitol.exd.app.rate.RateAvailableTime;

public class TestRateAvailableTimer extends TestCase {

    private static final int REF_YEAR = 2015;
    private static final int REF_MONTH = Calendar.FEBRUARY;
    private static final int REF_MONTHDAY_WEDNESDAY = 4;
    private static final int REF_MONTHDAY_THURSDAY = 5;
    private static final int REF_MONTHDAY_FRIDAY = 6;
    private static final int REF_MONTHDAY_SATURDAY = 7;
    private static final int REF_MONTHDAY_SUNDAY = 8;

    private static final int AVAILABLE = 14;

    static class TimeFactory {

        private static void set(Calendar cal, int year, int month, int monthDay, int hour, int minute, int second, int millisecond) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, monthDay);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, millisecond);
        }

        private final Calendar cal;
        private final int year;
        private final int month;
        private final int monthDay;

        public final Date available;
        public final Date oneMillisBeforeAvailable;
        public final Date oneHourBeforeAvailable;
        public final Date oneMillisAfterAvailable;
        public final Date oneHourAfterAvailable;

        TimeFactory(Calendar cal, int year, int month, int monthDay) {
            this.cal = cal;
            this.year = year;
            this.month = month;
            this.monthDay = monthDay;

            Calendar bcb = bcbCal();
            set(bcb, year, month, monthDay, AVAILABLE, 0, 0, 0);
            this.cal.setTimeInMillis(bcb.getTimeInMillis());
            this.available = cal.getTime();

            this.cal.setTimeInMillis(this.available.getTime());
            this.cal.add(Calendar.MILLISECOND, -1);
            this.oneMillisBeforeAvailable = cal.getTime();

            this.cal.setTimeInMillis(this.available.getTime());
            this.cal.add(Calendar.HOUR_OF_DAY, -1);
            this.oneHourBeforeAvailable = cal.getTime();

            this.cal.setTimeInMillis(this.available.getTime());
            this.cal.add(Calendar.MILLISECOND, 1);
            this.oneMillisAfterAvailable = cal.getTime();

            this.cal.setTimeInMillis(this.available.getTime());
            this.cal.add(Calendar.HOUR_OF_DAY, 1);
            this.oneHourAfterAvailable = cal.getTime();
        }

        private void set(int hour, int minute, int second, int millisecond) {
            set(cal, year, month, monthDay, hour, minute, second, millisecond);
        }

        public int hourAt(long utcTimeMillis) {
            cal.setTimeInMillis(utcTimeMillis);
            return cal.get(Calendar.HOUR_OF_DAY);
        }

        public Date time(int hour, int minute, int second, int millisecond) {
            set(hour, minute, second, millisecond);
            return cal.getTime();
        }

        public Date hh(int hour) {
            return time(hour, 0, 0, 0);
        }
    }

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    static class Dates {
        final Date reference;
        final Date expected;
    }

    private static Dates dates(Date reference, Date expected) {
        return new Dates(reference, expected);
    }

    private static TimeFactory dateFactory(Calendar cal, int monthDay) {
        return new TimeFactory(cal, REF_YEAR, REF_MONTH, monthDay);
    }

    private static Calendar bcbCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    private static Calendar atWest() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private static Calendar atEast() {
        return Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    }

    private static String formatDate(Date date) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return fmt.format(date);
    }

    private RateAvailableTime timer = new RateAvailableTime();

    private void assertNextDates(Iterable<Dates> dates) {
        for (Dates d : dates) {
            Date actual = timer.getNextAvailableTime(d.reference);
            assertEquals("Next time for " + formatDate(d.reference), d.expected, actual);
        }
    }

    public void testNextTime() {
        TimeFactory bcb = dateFactory(bcbCal(), REF_MONTHDAY_THURSDAY);
        TimeFactory east = dateFactory(atEast(), REF_MONTHDAY_THURSDAY);
        TimeFactory west = dateFactory(atWest(), REF_MONTHDAY_THURSDAY);

        TimeFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcb.hh(0), bcb.available))
                .add(dates(bcb.hh(9), bcb.available))
                .add(dates(bcb.oneHourBeforeAvailable, bcb.available))
                .add(dates(bcb.oneMillisBeforeAvailable, bcb.available))
                .add(dates(bcb.available, bcb.available))
                .add(dates(bcb.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(bcb.oneHourAfterAvailable, bcbFriday.available))
                .add(dates(bcb.hh(24), bcbFriday.available))

                .add(dates(east.oneHourBeforeAvailable, bcb.available))
                .add(dates(east.oneMillisBeforeAvailable, bcb.available))
                .add(dates(east.available, bcb.available))
                .add(dates(east.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(east.oneHourAfterAvailable, bcbFriday.available))

                .add(dates(west.oneHourBeforeAvailable, bcb.available))
                .add(dates(west.oneMillisBeforeAvailable, bcb.available))
                .add(dates(west.available, bcb.available))
                .add(dates(west.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(west.oneHourAfterAvailable, bcbFriday.available))
                .build();

        assertNextDates(dates);
    }

    public void testNextTimeWithWeekend() {
        TimeFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);
        TimeFactory bcbSaturday = dateFactory(bcbCal(), REF_MONTHDAY_SATURDAY);
        TimeFactory bcbSunday = dateFactory(bcbCal(), REF_MONTHDAY_SUNDAY);

        TimeFactory eastFriday = dateFactory(atEast(), REF_MONTHDAY_FRIDAY);
        TimeFactory eastSaturday = dateFactory(atEast(), REF_MONTHDAY_SATURDAY);
        TimeFactory eastSunday = dateFactory(atEast(), REF_MONTHDAY_SUNDAY);

        TimeFactory westFriday = dateFactory(atWest(), REF_MONTHDAY_FRIDAY);
        TimeFactory westSaturday = dateFactory(atWest(), REF_MONTHDAY_SATURDAY);
        TimeFactory westSunday = dateFactory(atWest(), REF_MONTHDAY_SUNDAY);

        TimeFactory bcbMonday = dateFactory(bcbCal(), 9);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbFriday.available, bcbFriday.available))
                .add(dates(bcbFriday.oneMillisAfterAvailable, bcbMonday.available))
                .add(dates(bcbFriday.oneHourAfterAvailable, bcbMonday.available))

                .add(dates(bcbSaturday.hh(0), bcbMonday.available))
                .add(dates(bcbSaturday.available, bcbMonday.available))
                .add(dates(bcbSaturday.hh(24), bcbMonday.available))

                .add(dates(bcbSunday.hh(0), bcbMonday.available))
                .add(dates(bcbSunday.available, bcbMonday.available))
                .add(dates(bcbSunday.hh(24), bcbMonday.available))

                .add(dates(eastFriday.available, bcbFriday.available))
                .add(dates(eastFriday.oneMillisAfterAvailable, bcbMonday.available))
                .add(dates(eastFriday.oneHourAfterAvailable, bcbMonday.available))

                .add(dates(eastSaturday.hh(0), bcbMonday.available))
                .add(dates(eastSaturday.available, bcbMonday.available))
                .add(dates(eastSaturday.hh(24), bcbMonday.available))

                .add(dates(eastSunday.hh(0), bcbMonday.available))
                .add(dates(eastSunday.available, bcbMonday.available))
                .add(dates(eastSunday.hh(24), bcbMonday.available))

                .add(dates(westFriday.available, bcbFriday.available))
                .add(dates(westFriday.oneMillisAfterAvailable, bcbMonday.available))
                .add(dates(westFriday.oneHourAfterAvailable, bcbMonday.available))

                .add(dates(westSaturday.hh(0), bcbMonday.available))
                .add(dates(westSaturday.available, bcbMonday.available))
                .add(dates(westSaturday.hh(24), bcbMonday.available))

                .add(dates(westSunday.hh(0), bcbMonday.available))
                .add(dates(westSunday.available, bcbMonday.available))
                .add(dates(westSunday.hh(24), bcbMonday.available))

                .build();

        assertNextDates(dates);
    }

    private void assertPreviousDates(Iterable<Dates> dates) {
        for (Dates d : dates) {
            Date actual = timer.getPreviousAvailableTime(d.reference);
            assertEquals("Previous time for " + formatDate(d.reference), d.expected, actual);
        }
    }

    public void testPreviousTime() {
        TimeFactory bcbThursday = dateFactory(bcbCal(), REF_MONTHDAY_THURSDAY);
        TimeFactory eastThursday = dateFactory(atEast(), REF_MONTHDAY_THURSDAY);
        TimeFactory westThursday = dateFactory(atWest(), REF_MONTHDAY_THURSDAY);

        TimeFactory bcbWednesday = dateFactory(bcbCal(), REF_MONTHDAY_WEDNESDAY);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbThursday.hh(0), bcbWednesday.available))
                .add(dates(bcbThursday.hh(9), bcbWednesday.available))
                .add(dates(bcbThursday.oneHourBeforeAvailable, bcbWednesday.available))
                .add(dates(bcbThursday.oneMillisBeforeAvailable, bcbWednesday.available))
                .add(dates(bcbThursday.available, bcbThursday.available))
                .add(dates(bcbThursday.oneMillisAfterAvailable, bcbThursday.available))
                .add(dates(bcbThursday.oneHourAfterAvailable, bcbThursday.available))
                .add(dates(bcbThursday.hh(24), bcbThursday.available))

                .add(dates(eastThursday.oneHourBeforeAvailable, bcbWednesday.available))
                .add(dates(eastThursday.oneMillisBeforeAvailable, bcbWednesday.available))
                .add(dates(eastThursday.available, bcbThursday.available))
                .add(dates(eastThursday.oneMillisAfterAvailable, bcbThursday.available))
                .add(dates(eastThursday.oneHourAfterAvailable, bcbThursday.available))

                .add(dates(westThursday.oneHourBeforeAvailable, bcbWednesday.available))
                .add(dates(westThursday.oneMillisBeforeAvailable, bcbWednesday.available))
                .add(dates(westThursday.available, bcbThursday.available))
                .add(dates(westThursday.oneMillisAfterAvailable, bcbThursday.available))
                .add(dates(westThursday.oneHourAfterAvailable, bcbThursday.available))
                .build();

        assertPreviousDates(dates);
    }

    public void testPreviousTimeWithWeekend() {
        TimeFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);
        TimeFactory bcbSaturday = dateFactory(bcbCal(), REF_MONTHDAY_SATURDAY);
        TimeFactory bcbSunday = dateFactory(bcbCal(), REF_MONTHDAY_SUNDAY);

        TimeFactory eastFriday = dateFactory(atEast(), REF_MONTHDAY_FRIDAY);
        TimeFactory eastSaturday = dateFactory(atEast(), REF_MONTHDAY_SATURDAY);
        TimeFactory eastSunday = dateFactory(atEast(), REF_MONTHDAY_SUNDAY);

        TimeFactory westFriday = dateFactory(atWest(), REF_MONTHDAY_FRIDAY);
        TimeFactory westSaturday = dateFactory(atWest(), REF_MONTHDAY_SATURDAY);
        TimeFactory westSunday = dateFactory(atWest(), REF_MONTHDAY_SUNDAY);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbFriday.available, bcbFriday.available))
                .add(dates(bcbFriday.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(bcbFriday.oneHourAfterAvailable, bcbFriday.available))

                .add(dates(bcbSaturday.hh(0), bcbFriday.available))
                .add(dates(bcbSaturday.available, bcbFriday.available))
                .add(dates(bcbSaturday.hh(24), bcbFriday.available))

                .add(dates(bcbSunday.hh(0), bcbFriday.available))
                .add(dates(bcbSunday.available, bcbFriday.available))
                .add(dates(bcbSunday.hh(24), bcbFriday.available))

                .add(dates(eastFriday.available, bcbFriday.available))
                .add(dates(eastFriday.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(eastFriday.oneHourAfterAvailable, bcbFriday.available))

                .add(dates(eastSaturday.hh(0), bcbFriday.available))
                .add(dates(eastSaturday.available, bcbFriday.available))
                .add(dates(eastSaturday.hh(24), bcbFriday.available))

                .add(dates(eastSunday.hh(0), bcbFriday.available))
                .add(dates(eastSunday.available, bcbFriday.available))
                .add(dates(eastSunday.hh(24), bcbFriday.available))

                .add(dates(westFriday.available, bcbFriday.available))
                .add(dates(westFriday.oneMillisAfterAvailable, bcbFriday.available))
                .add(dates(westFriday.oneHourAfterAvailable, bcbFriday.available))

                .add(dates(westSaturday.hh(0), bcbFriday.available))
                .add(dates(westSaturday.available, bcbFriday.available))
                .add(dates(westSaturday.hh(24), bcbFriday.available))

                .add(dates(westSunday.hh(0), bcbFriday.available))
                .add(dates(westSunday.available, bcbFriday.available))
                .add(dates(westSunday.hh(24), bcbFriday.available))

                .build();

        assertPreviousDates(dates);
    }

    public void testPreviousTimeOnFirstOfMonth() {
        TimeFactory bcbMarch = new TimeFactory(bcbCal(), REF_YEAR, Calendar.MARCH, 31);
        TimeFactory bcbApril = new TimeFactory(bcbCal(), REF_YEAR, Calendar.APRIL, 1);

        Date expected31_03 = bcbMarch.available;
        Date expected01_04 = bcbApril.available;

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbApril.hh(9), expected31_03))
                .add(dates(bcbApril.oneMillisBeforeAvailable, expected31_03))
                .add(dates(bcbApril.available, expected01_04))
                .build();

        assertPreviousDates(dates);
    }

    public void testNextTimeOnLastOfMonth() {
        TimeFactory bcbMarch = new TimeFactory(bcbCal(), REF_YEAR, Calendar.MARCH, 31);
        TimeFactory bcbApril = new TimeFactory(bcbCal(), REF_YEAR, Calendar.APRIL, 1);

        Date expected31_03 = bcbMarch.available;
        Date expected01_04 = bcbApril.available;

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbMarch.hh(9), expected31_03))
                .add(dates(bcbMarch.oneMillisBeforeAvailable, expected31_03))
                .add(dates(bcbMarch.available, expected31_03))
                .add(dates(bcbMarch.oneMillisAfterAvailable, expected01_04))
                .build();

        assertNextDates(dates);
    }
}
