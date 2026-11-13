package jmri.jmrit.throttle;

import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;

import jmri.Throttle;
import jmri.jmrit.throttle.panels.SpeedPanel;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedPanelTest {

    @Test
    public void testCTor() {
        SpeedPanel t = new SpeedPanel();
        Assert.assertNotNull("exists",t);
    }

    /**
     * A throttle change on another thread is shown on the GUI thread.
     *
     * @throws InterruptedException if interrupted while holding the GUI thread
     */
    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testThrottleChangeOnOtherThreadShownOnGuiThread() throws InterruptedException {
        SpeedPanel t = ThreadingUtil.runOnGUIwithReturn(() -> new SpeedPanel());
        JLabel label = ThreadingUtil.runOnGUIwithReturn(() -> findLabel(t));
        String before = ThreadingUtil.runOnGUIwithReturn(label::getText);
        String whileHeld;
        try (GuiThreadHold hold = new GuiThreadHold()) {
            t.propertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, 0f, 0.5f));
            whileHeld = label.getText();
        }
        Assert.assertEquals("label not changed off the GUI thread", before, whileHeld);
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !label.getText().equals(before)),
            "label follows the throttle");
    }

    private static JLabel findLabel(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof JLabel) {
                return (JLabel) child;
            }
            if (child instanceof java.awt.Container) {
                JLabel found = findLabel((java.awt.Container) child);
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
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SpeedPanelTest.class);

}
