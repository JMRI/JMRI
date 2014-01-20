// DateUtilTest.java

package jmri.util;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Tests for the jmri.util.DateUtil class.
 *
 *
 * @author      Paul Bender Copyright 2014
 * @version     $Revision: 22710 $
 */
public class DateUtilTest extends TestCase {

       public void testCalFromJulianDate(){
           // this test checks to see if the julian date
           // 2456678 at 16:00 gives us the correct
           // date in the calendar, which is January 20,2014
           GregorianCalendar testCal=DateUtil.calFromJulianDate(2456678);
           Assert.assertEquals("Year",2014,testCal.get(Calendar.YEAR));
           Assert.assertEquals("Day of Year",20,testCal.get(Calendar.DAY_OF_YEAR));
       }

       public void testCalFromJulianDateEpocStart(){
           // this test checks to see if the julian date
           // 2440588 at 12:00 gives us the correct
           // date in the calendar, which is January 1,1970
           GregorianCalendar testCal=DateUtil.calFromJulianDate(2440588);
           Assert.assertEquals("Year",1970,testCal.get(Calendar.YEAR));
           Assert.assertEquals("Day of Year",1,testCal.get(Calendar.DAY_OF_YEAR));
       }

       public void testJulianDayFromCalendar(){
           // this test checks to see that the julian date
           // 2456678 is returned when a calendar set to January 20,2014 is
           // proivded as input to the julianDayFromCalendar method.
           GregorianCalendar testCal=new GregorianCalendar(2014,1,20,12,0);
           Assert.assertEquals("Julian Day",2456678,DateUtil.julianDayFromCalendar(testCal));
       }

       public void testJulianDayFromCalendarEpocStart(){
           // this test checks to see that the julian date
           // 2440588 is returned when a calendar set to January 1,1970 is
           // proivded as input to the julianDayFromCalendar method.
           GregorianCalendar testCal=new GregorianCalendar(1970,1,1,12,0);
           Assert.assertEquals("Julian Day",2440588,DateUtil.julianDayFromCalendar(testCal));
       }


        // from here down is testing infrastructure

        public DateUtilTest(String s) {
                super(s);
        }

        // Main entry point
        static public void main(String[] args) {
                String[] testCaseName = {DateUtilTest.class.getName()};
                junit.swingui.TestRunner.main(testCaseName);
        }

        // test suite from all defined tests
        public static Test suite() {
                TestSuite suite = new TestSuite(DateUtilTest.class);
                return suite;
        }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

         static Logger log = Logger.getLogger(DateUtilTest.class.getName());

}

