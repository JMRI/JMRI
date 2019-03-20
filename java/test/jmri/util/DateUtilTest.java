package jmri.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.*;

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

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
