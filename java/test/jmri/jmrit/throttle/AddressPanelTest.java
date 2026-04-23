package jmri.jmrit.throttle;

import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.PropertyChangeEvent;



/**
 * Test simple functioning of AddressPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AddressPanelTest {

    @Test
    public void testCtor() {
        AddressPanel panel = new AddressPanel(null);
        Assertions.assertNotNull( panel, "exists");
    }

    @Test
    public void testDisconnect() {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);
        int locoAddress = 1234;
        AddressPanel panel = new AddressPanel(tm);
        assertEquals( 0, tm.getThrottleUsageCount(locoAddress), "Throttle is used 0 times");
        panel.setAddress(locoAddress, false);
        assertEquals( 1, tm.getThrottleUsageCount(locoAddress), "Throttle is used 1 times");
        PropertyChangeEvent pce = new PropertyChangeEvent(this,"ThrottleConnected", true, false);
        panel.propertyChange(pce);
        assertEquals( 0, tm.getThrottleUsageCount(locoAddress), "Throttle is used 0 times");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
