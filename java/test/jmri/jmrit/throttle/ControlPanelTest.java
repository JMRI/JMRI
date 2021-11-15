package jmri.jmrit.throttle;

import java.awt.Container;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jmri.util.JUnitUtil;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


/**
 * Test simple functioning of ControlPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ControlPanelTest {
    ControlPanel panel;
    JFrame frame;
    DccThrottle throttle;

    private void setupControlPanel() {
        panel = new ControlPanel();
        Assert.assertNotNull("exists", panel);

        frame = new JFrame("ControlPanelTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(200, 400);

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(true);
        mainPanel.add(new JDesktopPane());
        mainPanel.add(panel);

        panel.toFront();
        panel.setVisible(true);

        frame.add(mainPanel);
    }

    private void checkFrameOverlap(Container f) {
        synchronized(f.getTreeLock()) {
            int count = f.getComponentCount();
            for (int i1 =0 ; i1 < count; i1++) {
                Component c1 = f.getComponent(i1);
                for (int i2 = i1+1; i2 < count; i2++ ) {
                    Component c2 = f.getComponent(i2);
                    if (c1 == c2) {
                        continue;
                    }
                    if (!c1.isVisible()) {
                        continue;
                    }
                    if (!c2.isVisible()) {
                        continue;
                    }
                    Rectangle r1 = c1.getBounds();
                    Rectangle r2 = c2.getBounds();
                    if (r1.intersects(r2)) {
                        System.out.printf("Components %s(%s) and %s(%s) overlap%n",
                            c1.getName(), c1.getClass().getName(),
                            c2.getName(), c2.getClass().getName());
                    }
                    Assert.assertFalse(r1.intersects(r2));
                }

                if (c1 instanceof Container) {
                    checkFrameOverlap((Container)c1);
                }
            }
        }
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupControlPanel();

        checkFrameOverlap(panel.getContentPane());
        Assert.assertTrue(panel.getSpeedSlider() != null);

    }

    @Test
    public void testExtendedThrottle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(ThrottlesPreferences.class).setUsingFunctionIcon(false);
        setupControlPanel();

        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.STEPDISPLAY);
        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.SLIDERDISPLAY);
        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.SLIDERDISPLAYCONTINUOUS);
        checkFrameOverlap(panel.getContentPane());
    }

    @Test
    public void testIconThrottle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.throttleManagerInstance().supportedSpeedModes();
        InstanceManager.getDefault(ThrottlesPreferences.class).setUsingFunctionIcon(true);
        setupControlPanel();

        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.STEPDISPLAY);
        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.SLIDERDISPLAY);
        checkFrameOverlap(panel.getContentPane());

        panel.setSpeedController(ControlPanel.SLIDERDISPLAYCONTINUOUS);
        checkFrameOverlap(panel.getContentPane());
    }

    @ParameterizedTest
    @EnumSource(SpeedStepMode.class)
    public void testSpeedStepModes(SpeedStepMode mode) {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(ThrottlesPreferences.class).setUsingFunctionIcon(true);
        setupControlPanel();
        throttle = null;
        InstanceManager.throttleManagerInstance().requestThrottle(3,
            new ThrottleListener(){
              @Override
              public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                throttle.setSpeedStepMode(mode);
                panel.notifyAddressThrottleFound(t);
              }

              @Override
              public void notifyFailedThrottleRequest(LocoAddress address,
                  String reason) {
              }
              @Override
              public void notifyDecisionRequired(LocoAddress address,
                  DecisionType question) {
              }
            });

        Assert.assertTrue(throttle != null);
        Assert.assertEquals(throttle.getSpeedSetting(), 0.0, 1e-7);
        Assert.assertEquals(throttle.getSpeedStepMode(), mode);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
        if (!GraphicsEnvironment.isHeadless()) {
            if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
                jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
            }
            InstanceManager.getDefault(ThrottlesPreferences.class).setUseExThrottle(true);
        }
    }

    @AfterEach
    public void tearDown() {
        if( frame != null ) {
            JUnitUtil.dispose(frame);
            frame = null;
        }

        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
