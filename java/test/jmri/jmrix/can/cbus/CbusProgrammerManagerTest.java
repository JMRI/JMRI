package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusProgrammerManager class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class CbusProgrammerManagerTest extends TestCase {

    public void testGlobal() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("no global mode", !m.isGlobalModePossible());
    }

    public void testAddressed() {
        CbusProgrammerManager m = new CbusProgrammerManager(new TestTrafficController());
        Assert.assertTrue("addressed mode ok", m.isAddressedModePossible());
    }

    // from here down is testing infrastructure
    public CbusProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CbusProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CbusProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
