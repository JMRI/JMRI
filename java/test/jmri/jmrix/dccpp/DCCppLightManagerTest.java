package jmri.jmrix.dccpp;

import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests for the jmri.jmrix.dccpp.DCCppLightManager class.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    DCCppInterfaceScaffold xnis = null;

    @Override
    public String getSystemName(int i) {
        return "DCCPPL" + i;
    }

    @Test
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
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface, register
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        // create and register the manager object
        l = new DCCppLightManager(xnis, "DCCPP");
        jmri.InstanceManager.setLightManager(l);

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightManagerTest.class);

}
