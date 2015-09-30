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
    private static final int ONE_PM = 13;

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    static class DateFactory {
        private final Calendar cal;
        private final int year;
        private final int month;
        private final int monthDay;

        public Date time(int hour, int minute, int second, int millisecond) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, monthDay);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, millisecond);
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

    private Dates dates(Date reference, Date expected) {
        return new Dates(reference, expected);
    }

    private DateFactory dateFactory(Calendar cal, int monthDay) {
        return new DateFactory(cal, REF_YEAR, REF_MONTH, monthDay);
    }

    private RateAvailableTime timer = new RateAvailableTime();

    private Calendar bcbCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    private Calendar atWest() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private Calendar atEast() {
        return Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    }

    private String formatDate(Date date) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return fmt.format(date);
    }

    private void assertNextDates(Iterable<Dates> dates) {
        for (Dates d : dates) {
            Date actual = timer.getNextAvailableTime(d.reference);
            assertEquals("Next time for " + formatDate(d.reference), d.expected, actual);
        }
    }

    public void testNextTime() {
        DateFactory bcb = dateFactory(bcbCal(), REF_MONTHDAY_THURSDAY);
        DateFactory east = dateFactory(atEast(), REF_MONTHDAY_THURSDAY);
        DateFactory west = dateFactory(atWest(), REF_MONTHDAY_THURSDAY);

        DateFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);

        Date onePM = bcb.hh(ONE_PM);
        Date onePMNextDay = bcbFriday.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcb.hh(0), onePM))
                .add(dates(bcb.hh(9), onePM))
                .add(dates(bcb.hh(12), onePM))
                .add(dates(bcb.time(12, 59, 59, 999), onePM))
                .add(dates(bcb.hh(ONE_PM), onePM))
                .add(dates(bcb.time(ONE_PM, 0, 0, 1), onePMNextDay))
                .add(dates(bcb.hh(14), onePMNextDay))
                .add(dates(bcb.hh(24), onePMNextDay))

                .add(dates(east.hh(15), onePM))
                .add(dates(east.time(15, 59, 59, 999), onePM))
                .add(dates(east.hh(16), onePM))
                .add(dates(east.time(16, 0, 0, 1), onePMNextDay))
                .add(dates(east.hh(17), onePMNextDay))

                .add(dates(west.hh(6), onePM))
                .add(dates(west.time(6, 59, 59, 999), onePM))
                .add(dates(west.hh(7), onePM))
                .add(dates(west.time(7, 0, 0, 1), onePMNextDay))
                .add(dates(west.time(8, 0, 0, 1), onePMNextDay))
                .build();

        assertNextDates(dates);
    }

    public void testNextTimeWithWeekend() {
        DateFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);
        DateFactory bcbSaturday = dateFactory(bcbCal(), REF_MONTHDAY_SATURDAY);
        DateFactory bcbSunday = dateFactory(bcbCal(), REF_MONTHDAY_SUNDAY);

        DateFactory eastFriday = dateFactory(atEast(), REF_MONTHDAY_FRIDAY);
        DateFactory eastSaturday = dateFactory(atEast(), REF_MONTHDAY_SATURDAY);
        DateFactory eastSunday = dateFactory(atEast(), REF_MONTHDAY_SUNDAY);

        DateFactory westFriday = dateFactory(atWest(), REF_MONTHDAY_FRIDAY);
        DateFactory westSaturday = dateFactory(atWest(), REF_MONTHDAY_SATURDAY);
        DateFactory westSunday = dateFactory(atWest(), REF_MONTHDAY_SUNDAY);

        DateFactory bcbMonday = dateFactory(bcbCal(), 9);

        Date onePMFriday = bcbFriday.hh(ONE_PM);
        Date onePMMonday = bcbMonday.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbFriday.hh(13), onePMFriday))
                .add(dates(bcbFriday.time(13, 0, 0, 1), onePMMonday))
                .add(dates(bcbFriday.hh(14), onePMMonday))

                .add(dates(bcbSaturday.hh(0), onePMMonday))
                .add(dates(bcbSaturday.hh(ONE_PM), onePMMonday))
                .add(dates(bcbSaturday.hh(24), onePMMonday))

                .add(dates(bcbSunday.hh(0), onePMMonday))
                .add(dates(bcbSunday.hh(ONE_PM), onePMMonday))
                .add(dates(bcbSunday.hh(24), onePMMonday))

                .add(dates(eastFriday.hh(16), onePMFriday))
                .add(dates(eastFriday.time(16, 0, 0, 1), onePMMonday))
                .add(dates(eastFriday.hh(17), onePMMonday))

                .add(dates(eastSaturday.hh(0), onePMMonday))
                .add(dates(eastSaturday.hh(16), onePMMonday))
                .add(dates(eastSaturday.hh(24), onePMMonday))

                .add(dates(eastSunday.hh(0), onePMMonday))
                .add(dates(eastSunday.hh(16), onePMMonday))
                .add(dates(eastSunday.hh(24), onePMMonday))

                .add(dates(westFriday.hh(7), onePMFriday))
                .add(dates(westFriday.time(7, 0, 0, 1), onePMMonday))
                .add(dates(westFriday.hh(8), onePMMonday))

                .add(dates(westSaturday.hh(0), onePMMonday))
                .add(dates(westSaturday.hh(7), onePMMonday))
                .add(dates(westSaturday.hh(24), onePMMonday))

                .add(dates(westSunday.hh(0), onePMMonday))
                .add(dates(westSunday.hh(7), onePMMonday))
                .add(dates(westSunday.hh(24), onePMMonday))

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
        DateFactory bcbThursday = dateFactory(bcbCal(), REF_MONTHDAY_THURSDAY);
        DateFactory eastThursday = dateFactory(atEast(), REF_MONTHDAY_THURSDAY);
        DateFactory westThursday = dateFactory(atWest(), REF_MONTHDAY_THURSDAY);

        DateFactory bcbWednesday = dateFactory(bcbCal(), REF_MONTHDAY_WEDNESDAY);

        Date onePM = bcbThursday.hh(ONE_PM);
        Date onePMPrevDay = bcbWednesday.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbThursday.hh(0), onePMPrevDay))
                .add(dates(bcbThursday.hh(9), onePMPrevDay))
                .add(dates(bcbThursday.hh(12), onePMPrevDay))
                .add(dates(bcbThursday.time(12, 59, 59, 999), onePMPrevDay))
                .add(dates(bcbThursday.hh(ONE_PM), onePM))
                .add(dates(bcbThursday.time(ONE_PM, 0, 0, 1), onePM))
                .add(dates(bcbThursday.hh(14), onePM))
                .add(dates(bcbThursday.hh(24), onePM))

                .add(dates(eastThursday.hh(15), onePMPrevDay))
                .add(dates(eastThursday.time(15, 59, 59, 999), onePMPrevDay))
                .add(dates(eastThursday.hh(16), onePM))
                .add(dates(eastThursday.hh(17), onePM))

                .add(dates(westThursday.hh(5), onePMPrevDay))
                .add(dates(westThursday.time(6, 59, 59, 999), onePMPrevDay))
                .add(dates(westThursday.hh(7), onePM))
                .add(dates(westThursday.hh(8), onePM))
                .build();

        assertPreviousDates(dates);
    }

    public void testPreviousTimeWithWeekend() {
        DateFactory bcbFriday = dateFactory(bcbCal(), REF_MONTHDAY_FRIDAY);
        DateFactory bcbSaturday = dateFactory(bcbCal(), REF_MONTHDAY_SATURDAY);
        DateFactory bcbSunday = dateFactory(bcbCal(), REF_MONTHDAY_SUNDAY);

        DateFactory eastFriday = dateFactory(atEast(), REF_MONTHDAY_FRIDAY);
        DateFactory eastSaturday = dateFactory(atEast(), REF_MONTHDAY_SATURDAY);
        DateFactory eastSunday = dateFactory(atEast(), REF_MONTHDAY_SUNDAY);

        DateFactory westFriday = dateFactory(atWest(), REF_MONTHDAY_FRIDAY);
        DateFactory westSaturday = dateFactory(atWest(), REF_MONTHDAY_SATURDAY);
        DateFactory westSunday = dateFactory(atWest(), REF_MONTHDAY_SUNDAY);

        Date onePMFriday = bcbFriday.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbFriday.hh(13), onePMFriday))
                .add(dates(bcbFriday.time(13, 0, 0, 1), onePMFriday))
                .add(dates(bcbFriday.hh(14), onePMFriday))

                .add(dates(bcbSaturday.hh(0), onePMFriday))
                .add(dates(bcbSaturday.hh(ONE_PM), onePMFriday))
                .add(dates(bcbSaturday.hh(24), onePMFriday))

                .add(dates(bcbSunday.hh(0), onePMFriday))
                .add(dates(bcbSunday.hh(ONE_PM), onePMFriday))
                .add(dates(bcbSunday.hh(24), onePMFriday))

                .add(dates(eastFriday.hh(16), onePMFriday))
                .add(dates(eastFriday.time(16, 0, 0, 1), onePMFriday))
                .add(dates(eastFriday.hh(17), onePMFriday))

                .add(dates(eastSaturday.hh(0), onePMFriday))
                .add(dates(eastSaturday.hh(16), onePMFriday))
                .add(dates(eastSaturday.hh(24), onePMFriday))

                .add(dates(eastSunday.hh(0), onePMFriday))
                .add(dates(eastSunday.hh(16), onePMFriday))
                .add(dates(eastSunday.hh(24), onePMFriday))

                .add(dates(westFriday.hh(7), onePMFriday))
                .add(dates(westFriday.time(7, 0, 0, 1), onePMFriday))
                .add(dates(westFriday.hh(8), onePMFriday))

                .add(dates(westSaturday.hh(0), onePMFriday))
                .add(dates(westSaturday.hh(7), onePMFriday))
                .add(dates(westSaturday.hh(24), onePMFriday))

                .add(dates(westSunday.hh(0), onePMFriday))
                .add(dates(westSunday.hh(7), onePMFriday))
                .add(dates(westSunday.hh(24), onePMFriday))

                .build();

        assertPreviousDates(dates);
    }

    public void testPreviousTimeOnFirstOfMonth() {
        DateFactory bcbMarch = new DateFactory(bcbCal(), REF_YEAR, Calendar.MARCH, 31);
        DateFactory bcbApril = new DateFactory(bcbCal(), REF_YEAR, Calendar.APRIL, 1);

        Date expected31_03 = bcbMarch.hh(ONE_PM);
        Date expected01_04 = bcbApril.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbApril.hh(9), expected31_03))
                .add(dates(bcbApril.time(12, 59, 59, 999), expected31_03))
                .add(dates(bcbApril.hh(ONE_PM), expected01_04))
                .build();

        assertPreviousDates(dates);
    }

    public void testNextTimeOnLastOfMonth() {
        DateFactory bcbMarch = new DateFactory(bcbCal(), REF_YEAR, Calendar.MARCH, 31);
        DateFactory bcbApril = new DateFactory(bcbCal(), REF_YEAR, Calendar.APRIL, 1);

        Date expected31_03 = bcbMarch.hh(ONE_PM);
        Date expected01_04 = bcbApril.hh(ONE_PM);

        List<Dates> dates = ImmutableList.<Dates> builder()
                .add(dates(bcbMarch.hh(9), expected31_03))
                .add(dates(bcbMarch.time(12, 59, 59, 999), expected31_03))
                .add(dates(bcbMarch.hh(ONE_PM), expected31_03))
                .add(dates(bcbMarch.time(ONE_PM, 0, 0, 1), expected01_04))
                .build();

        assertNextDates(dates);
    }
}
