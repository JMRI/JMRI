package jmri.jmrit.etcs.dmi.swing;

import java.beans.PropertyChangeEvent;

import jmri.jmrit.etcs.TrackCondition;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanelB.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelBTest {

    @Test
    public void testAnnouncementsUnderDial() {

        DmiFrame df = new DmiFrame("testAnnouncementsUnderDial");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.addAnnouncement(TrackCondition.airConClose(0, true));
        p.addAnnouncement(TrackCondition.levelCrossing(0));
        p.addAnnouncement(TrackCondition.soundHorn(0));

        verifyAck(jfo, p, TrackCondition.airConClose(0, true));
        verifyAck(jfo, p, TrackCondition.soundHorn(0));


        p.addAnnouncement(TrackCondition.pantographIsLowered()); // no ack reqd
        p.addAnnouncement(TrackCondition.neutralSection(0, true));
        p.addAnnouncement(TrackCondition.tractionChange25000(0, true));
        p.removeAnnouncement(TrackCondition.airConClose(0, true));
        p.removeAnnouncement(TrackCondition.soundHorn(0));
        p.removeAnnouncement(TrackCondition.levelCrossing(0));

        verifyAck(jfo, p, TrackCondition.neutralSection(0, true));
        verifyAck(jfo, p, TrackCondition.tractionChange25000(0, true));


        p.addAnnouncement(TrackCondition.tractionChange0(0, true));
        p.addAnnouncement(TrackCondition.radioHole(0)); // no ack reqd
        p.addAnnouncement(TrackCondition.airConOpen(0, true));
        p.removeAnnouncement(TrackCondition.pantographIsLowered());
        p.removeAnnouncement(TrackCondition.neutralSection(0, true));
        p.removeAnnouncement(TrackCondition.tractionChange25000(0, true));

        verifyAck(jfo, p, TrackCondition.tractionChange0(0, true));
        verifyAck(jfo, p, TrackCondition.airConOpen(0, true));


        p.addAnnouncement(TrackCondition.inhibitEddyCurrentBrake(0, true));
        p.addAnnouncement(TrackCondition.inhibitMagShoeBrake(0, true));
        p.addAnnouncement(TrackCondition.inhibitRegenerativeBrake(0, true));
        p.removeAnnouncement(TrackCondition.tractionChange0(0, true));
        p.removeAnnouncement(TrackCondition.radioHole(0));
        p.removeAnnouncement(TrackCondition.airConOpen(0, true));

        verifyAck(jfo, p, TrackCondition.inhibitEddyCurrentBrake(0, true));
        verifyAck(jfo, p, TrackCondition.inhibitMagShoeBrake(0, true));
        verifyAck(jfo, p, TrackCondition.inhibitRegenerativeBrake(0, true));


        p.removeAnnouncement(TrackCondition.inhibitEddyCurrentBrake(0, true));
        p.removeAnnouncement(TrackCondition.inhibitMagShoeBrake(0, true));
        p.removeAnnouncement(TrackCondition.inhibitRegenerativeBrake(0, true));
        p.addAnnouncement(TrackCondition.neutralSectionEnd(0, true));
        p.addAnnouncement(TrackCondition.nonStoppingArea(0, true));
        p.addAnnouncement(TrackCondition.pantographLower(0, true));

        verifyAck(jfo, p, TrackCondition.neutralSectionEnd(0, true));
        verifyAck(jfo, p, TrackCondition.nonStoppingArea(0, true));
        verifyAck(jfo, p, TrackCondition.pantographLower(0, true));


        p.removeAnnouncement(TrackCondition.neutralSectionEnd(0, true));
        p.addAnnouncement(TrackCondition.tractionChange1500(0, true));
        p.removeAnnouncement(TrackCondition.pantographLower(0, true));
        p.addAnnouncement(TrackCondition.tractionChange750(0, true));
        p.addAnnouncement(TrackCondition.tractionChange3000(0, true));
        p.removeAnnouncement(TrackCondition.nonStoppingArea(0, true));

        verifyAck(jfo, p, TrackCondition.tractionChange1500(0, true));
        verifyAck(jfo, p, TrackCondition.tractionChange750(0, true));
        verifyAck(jfo, p, TrackCondition.tractionChange3000(0, true));


        p.addAnnouncement(TrackCondition.tractionChange15000(0, true));
        p.removeAnnouncement(TrackCondition.tractionChange1500(0, true));
        p.removeAnnouncement(TrackCondition.tractionChange750(0, true));
        p.removeAnnouncement(TrackCondition.tractionChange3000(0, true));

        verifyAck(jfo, p, TrackCondition.tractionChange15000(0, true));

        jfo.requestClose();
        jfo.waitClosed();
    }

    private boolean triggered = false;

    private void verifyAck(JFrameOperator jfo, DmiPanel p, TrackCondition tc) {
        triggered = false;
        p.addPropertyChangeListener(tc.getAckString(), (PropertyChangeEvent evt) -> {
            triggered = true;
            Assertions.assertEquals(tc.getAckString(), evt.getPropertyName());
        });

        JButtonOperator jbo = JemmyUtil.getButtonOperatorByActionComnmand(jfo,tc.getAckString());
        jbo.doClick();
        // JUnitUtil.waitFor(2000);
        JUnitUtil.waitFor(() -> triggered, tc.getDescription() + " button triggered");
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
