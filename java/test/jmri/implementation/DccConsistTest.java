package jmri.implementation;

import jmri.AddressedProgrammerManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DccConsist
 *
 * @author	Paul Copyright (C) 2011, 2016
 */
public class DccConsistTest extends AbstractConsistTestBase {

    @Test
    public void testCtor2() {
        // integer constructor test.
        DccConsist c = new DccConsist(12);
        Assert.assertNotNull(c);
    }

    @Test
    public void testCtor3() {
        // integer constructor test.
        DccConsist c = new DccConsist(new DccLocoAddress(12, true));
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
        JUnitUtil.initDebugProgrammerManager();
        c = new DccConsist(new DccLocoAddress(12, true), InstanceManager.getDefault(AddressedProgrammerManager.class));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        c = null;
    }

}
