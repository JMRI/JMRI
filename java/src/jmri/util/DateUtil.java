package jmri.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Common utility methods for working with Calendar and Date objects.
 *
 * @author Paul Bender Copyright 2014
 */
public class DateUtil {

    // return a GregorianCalendar representation of the given julian date 
    // for reference, see:
    // http://aa.usno.navy.mil/faq/docs/JD_Formula.php
    // @param long julianDay number of days since January 1,4713BC.
    // @return {@link java.util.GregorianCalendar} representation of julianDay
    static public GregorianCalendar calFromJulianDate(long julianDay) {
        long L = julianDay + 68569;
        long N = 4 * L / 146097;
        L = L - (146097 * N + 3) / 4;
        long year = 4000 * (L + 1) / 1461001;
        L = L - 1461 * year / 4 + 31;
        long month = 80 * L / 2447;
        long day = L - 2447 * (month) / 80 - 31;
        L = month / 11;
        month = month + 2 - 12 * L;
        year = 100 * (N - 49) + year + L;
        GregorianCalendar returnCal = new GregorianCalendar((int) year, (int) month, (int) day, 0, 0);

        return (returnCal);
    }

    // return a julian date representation of the given GregorianCalendar date 
    // for reference, see:
    // http://aa.usno.navy.mil/faq/docs/JD_Formula.php
    // @param {@link java.util.GregorianCalendar} cal i
    // @return julianDate representation of the date represented by cal.
    static public long julianDayFromCalendar(java.util.GregorianCalendar cal) {
        int I = cal.get(Calendar.YEAR);
        int J = cal.get(Calendar.MONTH) + 1; // GregorianCalendar starts month
        // at 0.  Calculation requres month
        // starting at 1.

        int K = cal.get(Calendar.DAY_OF_MONTH);

        long day = K - 32075 + 1461 * (I + 4800 + (J - 14) / 12) / 4 + 367 * (J - 2 - (J - 14) / 12 * 12) / 12 - 3 * ((I + 4900 + (J - 14) / 12) / 100) / 4;

        return day;
    }

}
