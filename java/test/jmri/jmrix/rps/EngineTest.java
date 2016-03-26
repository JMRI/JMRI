package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Engine class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class EngineTest extends TestCase {

    public void testCtor() {
        Engine e = new Engine();
        Assert.assertNotNull("exists", e);
    }

    public void testNumReceivers() {
        Engine e = new Engine();
        e.setMaxReceiverNumber(3);
        Assert.assertEquals("number", 3, e.getMaxReceiverNumber());
    }

    // from here down is testing infrastructure
    public EngineTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EngineTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(EngineTest.class);
        return suite;
    }

}
