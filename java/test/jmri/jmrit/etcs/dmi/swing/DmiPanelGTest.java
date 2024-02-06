package jmri.jmrit.etcs.dmi.swing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import jmri.jmrit.etcs.*;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for DmiPanelG.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelGTest {

    @Test
    public void testPanelG1G2() {

        DmiFrame df = new DmiFrame("testPanelG1G2");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        // setup main panel as per figures in ERTMSv4 8.5.1
        p.setMaxDialSpeed(400); // max 400 units / hour
        p.setActualSpeed(133); // 36 units / hour
        p.setLevel(2); // etcs level 2
        p.setMode(DmiPanel.MODE_FULL_SUPERVISION);
        p.setSafeRadioConnection(1); // safe radio connection up

        ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new ArrayList<>();
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.DARK_GREY, 0, 140, true, true ));
        p.setCsgSections(csgSectionList);

        ArrayList<TrackSection> trackSectionList1 = new ArrayList<>();
        TrackSection s = new TrackSection(290,140,5);
        trackSectionList1.add(s);
        s = new TrackSection(1000,140,-22);
        trackSectionList1.add(s);
        s = new TrackSection(8000,140,2);
        s.addAnnouncement(new StationTrackCondition( 350, "Welwyn North"));
        trackSectionList1.add(s);
        MovementAuthority ma1 = new MovementAuthority(trackSectionList1);
        ArrayList<MovementAuthority> mas1 = new ArrayList<>();
        mas1.add(ma1);
        p.resetMovementAuthorities(mas1);
        p.setScale(2);

        p.messageDriver(new CabMessage("Test Message",0,false));
        p.setAtoMode(1);
        p.setSkipStoppingPoint(17); // skipping inactive
        
        p.setStoppingPointLabel("Welwyn North", "17:36:48");
        
        // JUnitUtil.waitFor(10000);



        p.setAtoMode(2);
        verifyAck(jfo, p, DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_START);
        // JUnitUtil.waitFor(1000);

        p.setAtoMode(3);
        verifyAck(jfo, p, DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_STOP);
        // JUnitUtil.waitFor(1000);

        p.setAtoMode(4);
        verifyAck(jfo, p, DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_STOP);
        // JUnitUtil.waitFor(1000);

        p.setAtoMode(5);
        // JUnitUtil.waitFor(1000);

        p.setAtoMode(0);
        // JUnitUtil.waitFor(1000);

        p.setAtoMode(2);
        p.setStoppingAccuracy(-1);
        // JUnitUtil.waitFor(1000);

        p.setStoppingAccuracy(0);
        // JUnitUtil.waitFor(1000);

        p.setStoppingAccuracy(1);
        // JUnitUtil.waitFor(1000);

        p.setStoppingAccuracy(-2);
        // JUnitUtil.waitFor(1000);

        
        // JUnitUtil.waitFor(5000);

        p.setSkipStoppingPoint(17);
        // JUnitUtil.waitFor(2000);

        p.setSkipStoppingPoint(18);
        // JUnitUtil.waitFor(2000);

        p.setSkipStoppingPoint(19);
        // JUnitUtil.waitFor(2000);

        p.setSkipStoppingPoint(0);
        // JUnitUtil.waitFor(2000);

        p.setStoppingPointLabel("", "");
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(10);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(11);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(12);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(13);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(14);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(15);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(16);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(0);
        // JUnitUtil.waitFor(1000);

        p.setDoorIcon(10); // request driver open both sides
        p.setAtoMode(1); // ATO Selected
        p.setStoppingAccuracy(0);
        p.setDwellTime(1,9);

        // JUnitUtil.waitFor(1000);
        
        p.advance(8500);
        // JUnitUtil.waitFor(1000);
        
        p.setIndicationMarker(300,0);
        // JUnitUtil.waitFor(5000);
        
        p.setMode(DmiPanel.MODE_AUTOMATIC_DRIVING);
        // JUnitUtil.waitFor(5000);
        
        jfo.requestClose();
        jfo.waitClosed();
    }

    private boolean triggered = false;

    private void verifyAck(JFrameOperator jfo, DmiPanel p, String actionCommand) {

        triggered = false;
        p.addPropertyChangeListener(actionCommand, (PropertyChangeEvent evt) -> {
            triggered = true;
            Assertions.assertEquals(actionCommand, evt.getPropertyName());
        });
        JButtonOperator jbo = JemmyUtil.getButtonOperatorByActionComnmand(jfo,actionCommand);
        jbo.doClick();
        // JUnitUtil.waitFor(2000);
        JUnitUtil.waitFor(() -> triggered, actionCommand + " button triggered");
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
