package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        jmri.util.JUnitUtil.initDebugCommandStation();
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(123, false);
        c = new WiFiConsist(addr);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Override
    @Test
    public void checkAddRemoveWithRosterUpdateAdvanced() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        super.checkAddRemoveWithRosterUpdateAdvanced();
    }

    @Override
    @Test
    public void checkGetSetLocoRosterIDAdvanced() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        super.checkGetSetLocoRosterIDAdvanced();
    }
    
    @Override
    @Test
    public void checkRemoveWithGetRosterIDAdvanced() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        super.checkRemoveWithGetRosterIDAdvanced();
    }
}
