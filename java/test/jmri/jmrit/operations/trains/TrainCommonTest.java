//TrainCommonTest.java
package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

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
