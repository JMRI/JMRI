package jmri.implementation;

import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NmraConsist
 *
 * @author	Paul Copyright (C) 2016
 */
public class NmraConsistTest extends AbstractConsistTestBase {

    @Test
    public void testCtor2() {
        // integer constructor test.
        NmraConsist c = new NmraConsist(12);
        Assert.assertNotNull(c);
    }

    @Test
    public void testCtor3() {
        // integer constructor test.
        NmraConsist c = new NmraConsist(new DccLocoAddress(12, true));
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        JUnitUtil.initDebugCommandStation();
        c = new NmraConsist(new DccLocoAddress(12, true), InstanceManager.getDefault(CommandStation.class));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
