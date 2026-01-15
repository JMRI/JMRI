package jmri.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the jmri.util.DateUtil class.
 * @author Paul Bender Copyright 2014
 */
public class DateUtilTest {

    @Test
    public void testCalFromJulianDate() {
        // this test checks to see if the julian date
        // 2456678 at 16:00 gives us the correct
        // date in the calendar, which is January 20,2014
        GregorianCalendar testCal = DateUtil.calFromJulianDate(2456678);
        assertEquals( 2014, testCal.get(Calendar.YEAR), "Year");
        assertEquals( 20, testCal.get(Calendar.DAY_OF_YEAR), "Day of Year");
    }

    @Test
    public void testCalFromJulianDateEpocStart() {
        // this test checks to see if the julian date
        // 2440588 at 12:00 gives us the correct
        // date in the calendar, which is January 1,1970
        GregorianCalendar testCal = DateUtil.calFromJulianDate(2440588);
        assertEquals( 1970, testCal.get(Calendar.YEAR), "Year");
        assertEquals( 1, testCal.get(Calendar.DAY_OF_YEAR), "Day of Year");
    }

    @Test
    public void testJulianDayFromCalendar() {
        // this test checks to see that the julian date
        // 2456678 is returned when a calendar set to January 20,2014 is
        // proivded as input to the julianDayFromCalendar method.
        GregorianCalendar testCal = new GregorianCalendar(2014, GregorianCalendar.JANUARY, 20, 12, 0);
        assertEquals( 2456678, DateUtil.julianDayFromCalendar(testCal), "Julian Day");
    }

    @Test
    public void testJulianDayFromCalendarEpocStart() {
        // this test checks to see that the julian date
        // 2440588 is returned when a calendar set to January 1,1970 is
        // proivded as input to the julianDayFromCalendar method.
        GregorianCalendar testCal = new GregorianCalendar(1970, GregorianCalendar.JANUARY, 1, 12, 0);
        assertEquals( 2440588, DateUtil.julianDayFromCalendar(testCal), "Julian Day");
    }

    @Test
    public void testUserDurationFromSeconds(){
        assertEquals("- 00:01:01", DateUtil.userDurationFromSeconds(-61));
        assertEquals("00:00:00", DateUtil.userDurationFromSeconds(0));
        assertEquals("00:00:01", DateUtil.userDurationFromSeconds(1));
        assertEquals("00:00:59", DateUtil.userDurationFromSeconds(59));
        assertEquals("00:01:00", DateUtil.userDurationFromSeconds(60));
        assertEquals("00:01:01", DateUtil.userDurationFromSeconds(61));
        assertEquals("00:10:01", DateUtil.userDurationFromSeconds(601));
        assertEquals("01:00:01", DateUtil.userDurationFromSeconds(3601));
        assertEquals("12:34:56", DateUtil.userDurationFromSeconds(45296));
        assertEquals("23:59:59", DateUtil.userDurationFromSeconds(86399));
        assertEquals("1 00:00:00", DateUtil.userDurationFromSeconds(86400));
        assertEquals("12 03:45:01", DateUtil.userDurationFromSeconds(1050301));
        assertEquals("- 12 03:45:01", DateUtil.userDurationFromSeconds(-1050301));
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
