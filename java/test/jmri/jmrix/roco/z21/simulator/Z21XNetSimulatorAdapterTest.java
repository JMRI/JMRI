package jmri.jmrix.roco.z21.simulator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Z21XNetSimulatorAdapterTest.java

 Description:	tests for the jmri.jmrix.roco.z21.simulator.Z21XNetSimulatorAdapter
 class

 Description:	tests for the jmri.jmrix.roco.z21.simulator.z21XNetSimulatorAdapter
 class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21XNetSimulatorAdapterTest extends TestCase {

    public void testCtor() {
        Z21XNetSimulatorAdapter a = new Z21XNetSimulatorAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public Z21XNetSimulatorAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21XNetSimulatorAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XNetSimulatorAdapterTest.class);
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
