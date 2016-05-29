package jmri.jmrix.rps.serial;

import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Reading;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.serial.SerialAdapter class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class SerialAdapterTest extends TestCase {

    public void testStringParsing3() throws java.io.IOException {
        // String input = "DATA,TIME,4105,3751,1423,2835";
        String input = "4105,3751,1423,2835";
        new Engine() {
            {
                _instance = this;
                setDefaultAlignment();
            }

            protected void setInitialAlignment() {
                setDefaultAlignment();
            }
        };
        SerialAdapter s = new SerialAdapter();
        Reading r = s.makeReading(input);
        Assert.assertEquals("n sample OK", 4, r.getNValues());
        Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(1) - 4105.));
        Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(2) - 3751.));
        Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(3) - 1423.));
        Assert.assertTrue("val 4", 0.001 > Math.abs(r.getValue(4) - 2835.));
    }

    public void testStringParsing12() throws java.io.IOException {
        // String input = "DATA,TIME,1,2,3,4,5,6,7,8,9,10,11,12";
        String input = "1,2,3,4,5,6,7,8,9,10,11,12";
        new Engine() {
            {
                _instance = this;
                setDefaultAlignment();
            }

            protected void setInitialAlignment() {
                setDefaultAlignment();
            }
        };
        SerialAdapter s = new SerialAdapter();
        Reading r = s.makeReading(input);
        Assert.assertEquals("n sample OK", 12, r.getNValues());
        Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(1) - 1.));
        Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(2) - 2.));
        Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(3) - 3.));
    }

// 	public void testStringParsingV2A() throws java.io.IOException {
//         // String input = "DATA,TIME,1,2,3,4,5,6,7,8,9,10,11,12";
//         String input = "DAT, TIME, 3,300,4,400,2,200";
//         new Engine(){ {_instance = this; setDefaultAlignment();} protected void setInitialAlignment(){setDefaultAlignment();}};
//         SerialAdapter s = new SerialAdapter();
//         s.version=2;
// 	    Reading r;
// 	    r = s.makeReading(input);
// 	    JUnitAppender.assertWarnMessage("Data from unexpected receiver 3, creating receiver");
// 	    JUnitAppender.assertWarnMessage("Data from unexpected receiver 4, creating receiver");
// 	    // getValue indexed from 1
// 	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(1)-0.));
// 	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(2)-200.));
// 	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(3)-300.));
// 	    Assert.assertTrue("val 4", 0.001 > Math.abs(r.getValue(4)-400.));
// 	}
// 
// 	public void testStringParsingV2B() throws java.io.IOException {
//         // String input = "DATA,TIME,1,2,3,4,5,6,7,8,9,10,11,12";
//         String input = "DAT, TIME, 1,100,2,200,3,300,4,400";
//         new Engine(){ {_instance = this; setDefaultAlignment();} protected void setInitialAlignment(){setDefaultAlignment();}};
//         SerialAdapter s = new SerialAdapter();
//         s.version=2;
// 	    Reading r;
// 	    r = s.makeReading(input);
// 	    JUnitAppender.assertWarnMessage("Data from unexpected receiver 3, creating receiver");
// 	    JUnitAppender.assertWarnMessage("Data from unexpected receiver 4, creating receiver");
// 	    // getValue indexed from 1
// 	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(1)-100.));
// 	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(2)-200.));
// 	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(3)-300.));
// 	    Assert.assertTrue("val 4", 0.001 > Math.abs(r.getValue(4)-400.));
// 	}
    // from here down is testing infrastructure
    public SerialAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SerialAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
