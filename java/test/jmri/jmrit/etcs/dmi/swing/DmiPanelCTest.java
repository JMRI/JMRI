package jmri.jmrit.etcs.dmi.swing;

import java.beans.PropertyChangeEvent;

import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanelC.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelCTest {

    private boolean ackTriggered = false;

    @Test
    public void testLevelTransitions() {

        DmiFrame df = new DmiFrame("testLevelTransitions");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setLevelTransition(1, true);

        ackTriggered = false;
        p.addPropertyChangeListener(DmiPanel.PROP_CHANGE_LEVEL_1_TRANSITION_ACK, (PropertyChangeEvent evt) -> {
            ackTriggered = true;
            Assertions.assertEquals(DmiPanel.PROP_CHANGE_LEVEL_1_TRANSITION_ACK, evt.getPropertyName());
        });

        JButtonOperator jbo = JemmyUtil.getButtonOperatorByName(jfo, "levelTransitionNotificationButton");
        JUnitUtil.waitFor(() -> jbo.isEnabled(), "button ready");
        jbo.doClick();
        JUnitUtil.waitFor(() -> ackTriggered, "ack change event triggered");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testNoAck() {
        DmiFrame df = new DmiFrame("testNoAck");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setLevelTransition(1, false);

        JButtonOperator jbo = JemmyUtil.getButtonOperatorByName(jfo, "levelTransitionNotificationButton");
        Assertions.assertFalse(jbo.isEnabled());

        // JUnitUtil.waitFor(10000);
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testTunnelStop() {
        ackTriggered = false;
        DmiFrame df = new DmiFrame("testTunnelStop");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.addPropertyChangeListener(DmiPanel.PROP_CHANGE_TUNNEL_STOP_AREA_ACK, (PropertyChangeEvent evt) -> {
            ackTriggered = true;
            Assertions.assertEquals(DmiPanel.PROP_CHANGE_TUNNEL_STOP_AREA_ACK, evt.getPropertyName());
        });
        // JUnitUtil.waitFor(2000);

        p.setTunnelStoppingIconVisible(true, true);

        // JUnitUtil.waitFor(4000);
        JButtonOperator jbo = JemmyUtil.getButtonOperatorByName(jfo, "TunnelStopNotificationButton");
        JUnitUtil.waitFor(() -> jbo.isEnabled(), "button ready");
        jbo.doClick();
        JUnitUtil.waitFor(() -> ackTriggered, "ack change event triggered");
        // JUnitUtil.waitFor(4000);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testTunnelCountdownFont(){
        DmiFrame df = new DmiFrame("testTunnelCountdown");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());
        p.setTunnelStoppingIconVisible(true, false);
        p.setTunnelStoppingDistance(150);
        // JUnitUtil.waitFor(5000);
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testModeTransitionAcknowledgments() {

        DmiFrame df = new DmiFrame("testModeTransitionAcknowledgments");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_SHUNTING_ACK, DmiPanel.MODE_SHUNTING, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_TRIP_ACK, DmiPanel.MODE_TRIP, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_ON_SIGHT_ACK, DmiPanel.MODE_ON_SIGHT, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_STAFF_RESPONSIBLE_ACK, DmiPanel.MODE_STAFF_RESPONSIBLE, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_REVERSING_ACK, DmiPanel.MODE_REVERSING, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_UNFITTED_ACK, DmiPanel.MODE_UNFITTED, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_NATIONAL_SYSTEM_ACK, DmiPanel.MODE_NATIONAL_SYSTEM, p);
        checkModeAcknowledge(jfo, DmiPanel.PROP_CHANGE_MODE_LIMITED_SUPERVISION_ACK, DmiPanel.MODE_LIMITED_SUPERVISION, p);

        //JUnitUtil.waitFor(4000);
        jfo.requestClose();
        jfo.waitClosed();
    }

    private void checkModeAcknowledge(JFrameOperator jfo, String actionCommand, int mode, DmiPanel p){
        p.setModeAcknowledge(mode);
        ackTriggered = false;
        p.addPropertyChangeListener(actionCommand, (PropertyChangeEvent evt) -> {
            ackTriggered = true;
            Assertions.assertEquals(actionCommand, evt.getPropertyName());
        });
        // JUnitUtil.waitFor(JUnitUtil.getRandom().nextInt(1500));

        JButtonOperator jbo = JemmyUtil.getButtonOperatorByActionComnmand(jfo, actionCommand);
        JUnitUtil.waitFor(() -> jbo.isEnabled(), "button ready");
        jbo.doClick();
        JUnitUtil.waitFor(() -> ackTriggered, "ack change event triggered");

        // JUnitUtil.waitFor(JUnitUtil.getRandom().nextInt(1500));

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
