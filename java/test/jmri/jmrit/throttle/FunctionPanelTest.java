package jmri.jmrit.throttle;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.jmrit.throttle.panels.FunctionButton;
import jmri.jmrit.throttle.panels.FunctionPanel;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of FunctionPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class FunctionPanelTest {

    FunctionPanel frame = null; // not a panel despite class name

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testGetFunctionButtons() {
        Assert.assertNotNull(frame);
        FunctionButton fba[] = frame.getFunctionButtons();
        Assert.assertNotNull("Function Button Array exists", fba);        
    }

    /**
     * A function changed on another thread is shown on the GUI thread.
     *
     * @throws InterruptedException if interrupted while holding the GUI thread
     */
    @Test
    public void testFunctionChangeOnOtherThreadShownOnGuiThread() throws InterruptedException {
        DccThrottle[] throttle = new DccThrottle[1];
        InstanceManager.throttleManagerInstance().requestThrottle(3, new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle[0] = t;
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
            }

            @Override
            public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
            }
        });
        Assert.assertNotNull(throttle[0]);
        ThreadingUtil.runOnGUI(() -> frame.notifyAddressThrottleFound(throttle[0]));
        FunctionButton f1 = null;
        for (FunctionButton b : frame.getFunctionButtons()) {
            if (b.getIdentity() == 1) {
                f1 = b;
            }
        }
        Assert.assertNotNull("button for F1", f1);
        FunctionButton button = f1;
        boolean before = ThreadingUtil.runOnGUIwithReturn(button::getState);
        boolean whileHeld;
        try (GuiThreadHold hold = new GuiThreadHold()) {
            throttle[0].setFunction(1, !before); // on this thread, not the GUI thread
            whileHeld = button.getState();
        }
        Assert.assertEquals("button not changed off the GUI thread", before, whileHeld);
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> button.getState() != before),
            "button follows the throttle");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
        frame = new FunctionPanel();
    }

    @AfterEach
    public void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }
}
