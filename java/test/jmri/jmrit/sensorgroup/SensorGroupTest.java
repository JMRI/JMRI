package jmri.jmrit.sensorgroup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.sensorgroup package
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @version	$Revision$
 */
public class SensorGroupTest extends TestCase {

    public void testFrameCreate() {
        new SensorGroupFrame();
    }

    public void testActionCreateAndFire() {
        SensorGroupAction a = new SensorGroupAction("Sensor Group");
        a.actionPerformed(null);
    }

    // from here down is testing infrastructure
    public SensorGroupTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SensorGroupTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorGroupTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
