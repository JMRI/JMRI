package jmri.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
        Assert.assertEquals("Year", 2014, testCal.get(Calendar.YEAR));
        Assert.assertEquals("Day of Year", 20, testCal.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void testCalFromJulianDateEpocStart() {
        // this test checks to see if the julian date
        // 2440588 at 12:00 gives us the correct
        // date in the calendar, which is January 1,1970
        GregorianCalendar testCal = DateUtil.calFromJulianDate(2440588);
        Assert.assertEquals("Year", 1970, testCal.get(Calendar.YEAR));
        Assert.assertEquals("Day of Year", 1, testCal.get(Calendar.DAY_OF_YEAR));
    }

    @Test
    public void testJulianDayFromCalendar() {
        // this test checks to see that the julian date
        // 2456678 is returned when a calendar set to January 20,2014 is
        // proivded as input to the julianDayFromCalendar method.
        GregorianCalendar testCal = new GregorianCalendar(2014, GregorianCalendar.JANUARY, 20, 12, 0);
        Assert.assertEquals("Julian Day", 2456678, DateUtil.julianDayFromCalendar(testCal));
    }

    @Test
    public void testJulianDayFromCalendarEpocStart() {
        // this test checks to see that the julian date
        // 2440588 is returned when a calendar set to January 1,1970 is
        // proivded as input to the julianDayFromCalendar method.
        GregorianCalendar testCal = new GregorianCalendar(1970, GregorianCalendar.JANUARY, 1, 12, 0);
        Assert.assertEquals("Julian Day", 2440588, DateUtil.julianDayFromCalendar(testCal));
    }

    @Test
    public void testUserDurationFromSeconds(){
        Assertions.assertEquals("- 00:01:01", DateUtil.userDurationFromSeconds(-61));
        Assertions.assertEquals("00:00:00", DateUtil.userDurationFromSeconds(0));
        Assertions.assertEquals("00:00:01", DateUtil.userDurationFromSeconds(1));
        Assertions.assertEquals("00:00:59", DateUtil.userDurationFromSeconds(59));
        Assertions.assertEquals("00:01:00", DateUtil.userDurationFromSeconds(60));
        Assertions.assertEquals("00:01:01", DateUtil.userDurationFromSeconds(61));
        Assertions.assertEquals("00:10:01", DateUtil.userDurationFromSeconds(601));
        Assertions.assertEquals("01:00:01", DateUtil.userDurationFromSeconds(3601));
        Assertions.assertEquals("12:34:56", DateUtil.userDurationFromSeconds(45296));
        Assertions.assertEquals("23:59:59", DateUtil.userDurationFromSeconds(86399));
        Assertions.assertEquals("1 00:00:00", DateUtil.userDurationFromSeconds(86400));
        Assertions.assertEquals("12 03:45:01", DateUtil.userDurationFromSeconds(1050301));
        Assertions.assertEquals("- 12 03:45:01", DateUtil.userDurationFromSeconds(-1050301));
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
