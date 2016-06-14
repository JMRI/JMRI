//TrainCommonTest.java
package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the TrainCommon class 
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class TrainCommonTest extends OperationsTestCase {

    public void testGetDate_DateArgument(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       String date = TrainCommon.getDate(calendar.getTime());
       Assert.assertNotNull("Date String",date);
    }

    public void testGetDate_BooleanArgument(){
       String date = TrainCommon.getDate(false);
       Assert.assertNotNull("Date String",date);
    }

    public void testConvertStringDateToDouble(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       calendar.set(java.util.Calendar.MONTH,4);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,26);
       calendar.set(java.util.Calendar.YEAR,2015);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,23);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d = calendar.getTime();
       String date = TrainCommon.getDate(d);
       TrainCommon tc = new TrainCommon();
       Assert.assertTrue("Double Time 0",0<tc.convertStringDateToDouble(date)); 
    }

    public void testComparableDateToDouble(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       calendar.set(java.util.Calendar.MONTH,12);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,31);
       calendar.set(java.util.Calendar.YEAR,2015);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,23);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d = calendar.getTime();
       String date = TrainCommon.getDate(d);
       calendar.set(java.util.Calendar.MONTH,1);
       calendar.set(java.util.Calendar.DAY_OF_MONTH,1);
       calendar.set(java.util.Calendar.YEAR,2016);
       calendar.set(java.util.Calendar.HOUR_OF_DAY,00);
       calendar.set(java.util.Calendar.MINUTE,30);
       java.util.Date d1 = calendar.getTime();
       String date1 = TrainCommon.getDate(d1);
       TrainCommon tc = new TrainCommon();
       Assert.assertTrue("Comparable Dates",tc.convertStringDateToDouble(date)<tc.convertStringDateToDouble(date1)); 
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public TrainCommonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrainCommonTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrainCommonTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
