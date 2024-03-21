package jmri.jmrit.etcs.dmi.swing;

import java.util.ArrayList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import jmri.jmrit.etcs.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanel.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelTest {

    @Test
    public void testCTor() {
        DmiPanel p = new DmiPanel();
        Assertions.assertNotNull(p);
        p.dispose();
    }

    @Test
    public void testVarious() {
        DmiFrame df = new DmiFrame("DmiPanelTest");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setLevel(-1);

        p.setLevelTransition(-1, true);

        p.setTrackAheadFreeQuestionVisible(true);
        // JUnitUtil.waitFor(2000);

        p.setTunnelStoppingIconVisible(true, false);
        p.setMode(0); // no mode displayed

        for (int i = 0; i < 141; i++) {
            // JUnitUtil.waitFor(50);
            p.setActualSpeed(i);
            p.setTunnelStoppingDistance(i);
            setSpeedHookSpeed(i, p);
        }

        p.setTrackAheadFreeQuestionVisible(false);

        p.setMode(1); // shunting
        // JUnitUtil.waitFor(2000);

        p.setLevelTransition(0, true);

        p.setSafeRadioConnection(0);

        p.setDisplaySpeedUnit("MPH");

        p.setReversingPermittedSymbol(true);

        p.setLevel(0);

        p.setMode(4); // trip

        for (int i = 140; i > -1; i-- ) {
            // JUnitUtil.waitFor(50);
            p.setActualSpeed(i);
            setSpeedHookSpeed(i, p);
            p.setTunnelStoppingDistance(i);
        }

        p.setTunnelStoppingIconVisible(false, false);

        p.setMode(6);
        // JUnitUtil.waitFor(2000);

        p.setLevelTransition(1, true);

        p.setSafeRadioConnection(1);

        p.setReversingPermittedSymbol(false);

        p.setIntervetionSymbol(true);

        p.setMode(7);

        for (int i = 0; i < 21; i++ ) {
            // JUnitUtil.waitFor(150);
            p.setActualSpeed(i);
            setSpeedHookSpeed(i, p);
            p.setLevel(1);
        }

        p.setMode(9);
        // JUnitUtil.waitFor(2000);

        p.setMode(11);
        p.setLevelTransition(2, false);

        p.setIntervetionSymbol(false);

        for (int i = 20; i > -1; i-- ) {
            // JUnitUtil.waitFor(150);
            p.setActualSpeed(i);
            setSpeedHookSpeed(i, p);
        }
        p.setMode(12);

        p.setAdhesionFactorOn(true);
        // p.setLevelTransition(3, true); ERTMS < 4
        // JUnitUtil.waitFor(2000);
        p.setLevel(2);
        p.setMode(14);

        for (int i = 0; i < 21; i++ ) {
            // JUnitUtil.waitFor(150);
            p.setActualSpeed(i);
            setSpeedHookSpeed(i, p);
        }

        p.setDisplaySpeedUnit("");

        p.setSafeRadioConnection(-1);
        p.setMode(16);
        // JUnitUtil.waitFor(2000);

        p.setActualSpeed(70);
        setSpeedHookSpeed(80, p);

        p.setMode(18);
        // p.setLevel(3); ERTMS < 4
        // JUnitUtil.waitFor(2000);
        //  p.setAdhesionFactorOn(false);
        p.setActualSpeed(96);
        setSpeedHookSpeed(100, p);

        p.setMode(21);
        // JUnitUtil.waitFor(4000);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testSounds(){

        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assumptions.assumeFalse(true, "Unable to initialize AudioSystem");
        }

        DmiFrame df = new DmiFrame("DmiPanelTest testSounds");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setLevel(-1);
        p.setMode(11);
        p.playDmiSound(4);
        // JUnitUtil.waitFor(2500);
        
        p.setMode(DmiPanel.MODE_POST_TRIP);
        p.playDmiSound(3);
        // JUnitUtil.waitFor(2500);
        
        p.setMode(DmiPanel.MODE_REVERSING);
        p.playDmiSound(1);
        // JUnitUtil.waitFor(2500);
        
        p.setMode(DmiPanel.MODE_NON_LEADING);
        p.playDmiSound(2);
        // JUnitUtil.waitFor(10000);

        p.stopDmiSound(2);

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testFig23(){
    
        DmiFrame df = new DmiFrame("testFig23");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());
        
        p.setDistanceToTarget(270);
        p.setActualSpeed(67);
        p.setCentreCircleAndDialColor(DmiPanel.ORANGE);
        p.setMaxDialSpeed(250);
        p.setLevel(2);
        p.setSafeRadioConnection(1);
        p.setTunnelStoppingIconVisible(true, false);
        p.setReleaseSpeed(30);
        p.setReleaseSpeedColour(DmiPanel.YELLOW);
        p.setMode(DmiPanel.MODE_FULL_SUPERVISION);
        p.addAnnouncement(TrackCondition.airConClose(0, true));
        p.addAnnouncement(TrackCondition.neutralSection(0, false));
        p.addAnnouncement(TrackCondition.pantographIsLowered());
        
        p.messageDriver(new CabMessage("Example Message",0,true));
        p.setAtoMode(1);
        p.setCoasting(true);
        p.setSkipStoppingPoint(17); // skipping inactive
        p.setStoppingPointLabel("Welwyn South", "17:26:48");
        
        ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new ArrayList<>();
        
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.DARK_GREY, -3, 1, false ));
        
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.YELLOW, 0, 50, true ));
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,
            DmiPanel.ORANGE, 50, 71, true ));
        
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_RELEASE,
            DmiPanel.GREY, 0, 30, false ));
        
        p.setCsgSections(csgSectionList);
        
        
        ArrayList<TrackSection> trackSectionList1 = new ArrayList<>();
        TrackSection s = new TrackSection(130,40,5);
        
        trackSectionList1.add(s);
        s = new TrackSection(130,30,5);
        s.addAnnouncement(TrackCondition.inhibitEddyCurrentBrake(80, true));
        trackSectionList1.add(s);
        
        s = new TrackSection(200,30,-4);
        trackSectionList1.add(s);
        
        s = new TrackSection(80,100,-4);
        trackSectionList1.add(s);

        s = new TrackSection(350,100,-8);
        s.addAnnouncement(TrackCondition.soundHorn(250));
        trackSectionList1.add(s);
        
        s = new TrackSection(450,20,-8);
        trackSectionList1.add(s);

        s = new TrackSection(500,20,2);
        
        trackSectionList1.add(s);
        
        s = new TrackSection(1500,10,2);
        s.addAnnouncement(new StationTrackCondition( 350, "Welwyn South"));
        trackSectionList1.add(s);

        MovementAuthority ma1 = new MovementAuthority(trackSectionList1);
        ArrayList<MovementAuthority> mas1 = new ArrayList<>();
        mas1.add(ma1);
        p.resetMovementAuthorities(mas1);
        p.setScale(2);
        
        // JUnitUtil.waitFor(10000);
        
        jfo.requestClose();
        jfo.waitClosed();
    }
    
    private void setSpeedHookSpeed(int speedHookSpeed, DmiPanel p){

        java.util.ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new java.util.ArrayList<>();

        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.DARK_GREY, -2, 1, false ));

        p.setCentreCircleAndDialColor(DmiPanel.YELLOW);

        if ( speedHookSpeed > 110 ) {

            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,
            DmiPanel.ORANGE, 110, speedHookSpeed, true ));
            p.setCentreCircleAndDialColor(DmiPanel.ORANGE);
        }

        if ( speedHookSpeed > 200 ) {
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,
            DmiPanel.RED, 200, speedHookSpeed, true ));
            p.setCentreCircleAndDialColor(DmiPanel.RED);
        }

        if ( speedHookSpeed <= 50 ){
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.GREY, 0, Math.min(speedHookSpeed, 110 ), false));

            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION,
                DmiPanel.YELLOW, 0, 50, false));
        } else {
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.YELLOW, 0, Math.min(speedHookSpeed, 110 ), true));
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_RELEASE,
            DmiPanel.YELLOW, 0, 50, false));
        }

        p.setCsgSections(csgSectionList);
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
