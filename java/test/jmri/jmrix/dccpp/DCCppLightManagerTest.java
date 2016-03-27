package jmri.jmrix.dccpp;

import jmri.Light;
import jmri.LightManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppLightManager class.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    DCCppInterfaceScaffold xnis = null;

    public String getSystemName(int i) {
        return "DCCPPL" + i;
    }

    public void testAsAbstractFactory() {
        // create and register the manager object
        DCCppLightManager xlm = new DCCppLightManager(xnis, "DCCPP");
        jmri.InstanceManager.setLightManager(xlm);

        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("DCCPPL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (DCCppLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("DCCPPL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("DCCPPL21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));
    }

    // from here down is testing infrastructure
    public DCCppLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppLightManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface, register
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        // create and register the manager object
        l = new DCCppLightManager(xnis, "DCCPP");
        jmri.InstanceManager.setLightManager(l);

    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightManagerTest.class.getName());

}
