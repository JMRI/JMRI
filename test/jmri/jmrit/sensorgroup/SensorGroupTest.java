// SensorGroupTest.java

package jmri.jmrit.sensorgroup;

import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.sensorgroup package
 * @author	Bob Jacobsen  Copyright 2003, 2007
 * @version	$Revision: 1.1 $
 */
public class SensorGroupTest extends TestCase {

    public void testFrameCreate() {
    }


    // from here down is testing infrastructure

    public SensorGroupTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SensorGroupTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorGroupTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorGroupTest.class.getName());

}
