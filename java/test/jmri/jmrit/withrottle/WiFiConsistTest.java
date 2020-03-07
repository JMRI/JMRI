package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of WiFiConsist
 *
 * @author	Paul Bender Copyright (C) 2016,2017
 */
public class WiFiConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        jmri.util.JUnitUtil.initDebugCommandStation();
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(123, false);
        c = new WiFiConsist(addr);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
