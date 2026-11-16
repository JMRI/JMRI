package jmri.jmrit.throttle;

import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrit.throttle.panels.AddressPanel;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

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
        // Throttle acquisition (and its usage-count update) is now deferred
        // (see AbstractThrottleManager), so not available synchronously.
        JUnitUtil.waitFor(() -> tm.getThrottleUsageCount(locoAddress) == 1, "wait for throttle acquired");
        assertEquals( 1, tm.getThrottleUsageCount(locoAddress), "Throttle is used 1 times");
        PropertyChangeEvent pce = new PropertyChangeEvent(this,"ThrottleConnected", true, false);
        // sent from this thread, the panel handles it on the GUI thread
        panel.propertyChange(pce);
        JUnitUtil.waitFor(() -> tm.getThrottleUsageCount(locoAddress) == 0, "wait for throttle released");
        assertEquals( 0, tm.getThrottleUsageCount(locoAddress), "Throttle is used 0 times");
    }

    /**
     * A change on another thread is shown on the GUI thread.
     *
     * @throws InterruptedException if interrupted while holding the GUI thread
     */
    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testChangeOnOtherThreadShownOnGuiThread() throws InterruptedException {
        AddressPanel panel = ThreadingUtil.runOnGUIwithReturn(() -> new AddressPanel(null));
        javax.swing.JButton dispatch = ThreadingUtil.runOnGUIwithReturn(() -> findButton(panel,
                jmri.jmrit.throttle.Bundle.getMessage("ButtonDispatch")));
        Assertions.assertNotNull(dispatch, "dispatch button");
        boolean before = ThreadingUtil.runOnGUIwithReturn(dispatch::isEnabled);
        boolean whileHeld;
        try (GuiThreadHold hold = new GuiThreadHold()) {
            panel.propertyChange(new PropertyChangeEvent(this, "DispatchEnabled", before, !before));
            whileHeld = dispatch.isEnabled();
        }
        assertEquals(before, whileHeld, "button not changed off the GUI thread");
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> dispatch.isEnabled() != before),
            "button follows the change");
    }

    private static javax.swing.JButton findButton(java.awt.Container c, String text) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JButton && text.equals(((javax.swing.JButton) child).getText())) {
                return (javax.swing.JButton) child;
            }
            if (child instanceof java.awt.Container) {
                javax.swing.JButton found = findButton((java.awt.Container) child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
