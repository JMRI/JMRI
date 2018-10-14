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

        // ask for a Light, and check type
        Light tl = l.newLight("DCCPPL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (DCCppLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("DCCPPL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("DCCPPL21"));
        Assert.assertTrue(null != l.getByUserName("my name"));
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        // create and register the manager object
        l = new DCCppLightManager(xnis, "DCCPP");
        jmri.InstanceManager.setLightManager(l);

    }

    @After
    public void tearDown() {
        l.dispose();
        l = null;
        xnis = null;
        jmri.util.JUnitUtil.clearShutDownManager();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightManagerTest.class);

}
