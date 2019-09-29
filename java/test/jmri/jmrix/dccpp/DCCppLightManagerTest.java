package jmri.jmrix.dccpp;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        return "DL" + i;
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Light, and check type
        Light tl = l.newLight("DL21", "my name");

        Assert.assertNotNull(tl);

        // make sure loaded into tables
        Assert.assertNotNull(l.getBySystemName("DL21"));
        Assert.assertNotNull(l.getByUserName("my name"));
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(xnis);
        xnis.setSystemConnectionMemo(memo);
        // create and register the manager object
        l = new DCCppLightManager(xnis.getSystemConnectionMemo());
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppLightManagerTest.class);
}
