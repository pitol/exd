package ppitol.exd.app.rate;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Calculate the next or previous date and time the rates are available for importing relative to
 * a reference time.
 * BCB make available a new rates file daily at around 01:00 PM, in it's local time (America/Sao_Paulo),
 * except on weekends and holidays.
 * This implementation will use the 02:00 PM, bcb local time, as the time a new rate file is available.
 * This implementation actually don't check for holidays, clients will have to handle the rates
 * unavailability on these dates.
 */
public class RateAvailableTime {

    private static final int AVAILABLE_HOUR = 14;

    /**
     * @return the next time for the service's execution, after the reference time.
     */
    public Date getNextAvailableTime(Date referenceTime) {
        Calendar cal = zonedCalendar();
        cal.setTime(referenceTime);
        adjustTimeToOnePM(cal);
        if (cal.getTime().before(referenceTime)) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        rollCalendarOutsideWeekend(cal, true);
        return cal.getTime();
    }

    /**
     * @return the previous time for the service's execution, before the reference time.
     */
    public Date getPreviousAvailableTime(Date referenceTime) {
        Calendar cal = zonedCalendar();
        cal.setTime(referenceTime);
        if (cal.get(Calendar.HOUR_OF_DAY) < AVAILABLE_HOUR) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        rollCalendarOutsideWeekend(cal, false);
        adjustTimeToOnePM(cal);
        return cal.getTime();
    }

    private Calendar zonedCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    private void adjustTimeToOnePM(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, AVAILABLE_HOUR);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void rollCalendarOutsideWeekend(Calendar cal, boolean forward) {
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        while (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
            cal.add(Calendar.DAY_OF_MONTH, forward ? 1 : -1);
            weekday = cal.get(Calendar.DAY_OF_WEEK);
        }
    }
}
