package jmri.jmrix.rps;

import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the RPS SensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class RpsSensorManagerTest extends TestCase {

    public void testCtor() {
        SensorManager s = new RpsSensorManager();
        Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public RpsSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsSensorManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RpsSensorManagerTest.class);
        return suite;
    }

}
