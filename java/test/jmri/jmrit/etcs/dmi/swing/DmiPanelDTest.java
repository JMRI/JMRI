package jmri.jmrit.etcs.dmi.swing;

import jmri.jmrit.etcs.TrackSection;
import jmri.jmrit.etcs.TrackCondition;
import jmri.jmrit.etcs.MovementAuthority;

import java.util.ArrayList;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanelD.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelDTest {

    @Test
    public void testPlanningScales() {
        DmiFrame df = new DmiFrame("testPlanningScales");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setScale(0);


        // create new dmi section 100m long, 5% gradient, speed 50
        ArrayList<TrackSection> trackSectionList1 = new ArrayList<>();
        TrackSection s = new TrackSection(25,50,22);
        trackSectionList1.add(s);
        MovementAuthority ma1 = new MovementAuthority(trackSectionList1);
        ArrayList<MovementAuthority> mas1 = new ArrayList<>();
        mas1.add(ma1);
        p.resetMovementAuthorities(mas1); // 25m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList2 = new ArrayList<>();
        TrackSection s2 = new TrackSection(25,50,-22);
        trackSectionList2.add(s2);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList2)); // 50m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList3 = new ArrayList<>();
        TrackSection s3 = new TrackSection(25,75,22);
        trackSectionList3.add(s3);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList3)); // 75m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList4 = new ArrayList<>();
        TrackSection s4 = new TrackSection(25,75,-22);
        trackSectionList4.add(s4);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList4)); // 100m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList5 = new ArrayList<>();
        TrackSection s5 = new TrackSection(25,75,22);
        trackSectionList5.add(s5);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList5)); // 125m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList6 = new ArrayList<>();
        TrackSection s6 = new TrackSection(125,75,-22);
        trackSectionList6.add(s6);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList6)); // 250m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList7 = new ArrayList<>();
        TrackSection s7 = new TrackSection(250,75,22);
        trackSectionList7.add(s7);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList7)); // 500m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList8 = new ArrayList<>();
        TrackSection s8 = new TrackSection(500,75,-22);
        trackSectionList8.add(s8);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList8)); // 1000m
        // JUnitUtil.waitFor(2000);

        ArrayList<TrackSection> trackSectionList9 = new ArrayList<>();
        TrackSection s9 = new TrackSection(1000,75,22);
        trackSectionList9.add(s9);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList9)); // 2000m
        // JUnitUtil.waitFor(10000);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testRightHandPlanningArea(){

        DmiFrame df = new DmiFrame("testRightHandPlanningArea");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        ArrayList<TrackSection> trackSectionList = new ArrayList<>();
        TrackSection s1 = new TrackSection(75,100,-4);
        TrackSection s2 = new TrackSection(50,140,-2);
        TrackSection s3 = new TrackSection(125,100,1);
        TrackSection s4 = new TrackSection(250,80,2);
        TrackSection s5 = new TrackSection(250,40,4);
        TrackSection s6 = new TrackSection(250,100,8);
        trackSectionList.add(s1);
        trackSectionList.add(s2);
        trackSectionList.add(s3);
        trackSectionList.add(s4);
        trackSectionList.add(s5);
        trackSectionList.add(s6);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList)); // 1000m
        // JUnitUtil.waitFor(1000);

        p.setNextAdviceChange(200);
        // JUnitUtil.waitFor(5000);

        for (int i = 0; i < 201; i++){
            p.advance(1);
            // JUnitUtil.waitFor(100);
        }
        // JUnitUtil.waitFor(5000);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testSymbolsInSection(){
        DmiFrame df = new DmiFrame("testSymbolsInSection");
        DmiPanel p = df.getDmiPanel();
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        ArrayList<TrackSection> trackSectionList = new ArrayList<>();
        TrackSection s1 = new TrackSection(2000,100,4);

        s1.addAnnouncement(TrackCondition.airConClose(100, true));
        s1.addAnnouncement(TrackCondition.airConOpen(200, false));

        s1.addAnnouncement(TrackCondition.pantographLower(500, false));
        s1.addAnnouncement(TrackCondition.pantographRaise(750, false));

        s1.addAnnouncement(TrackCondition.neutralSection(25, false));
        s1.addAnnouncement(TrackCondition.neutralSectionEnd(50, false));

        s1.addAnnouncement(TrackCondition.tractionChange15000(900, false));
        s1.addAnnouncement(TrackCondition.tractionChange750(1000, true));
        s1.addAnnouncement(TrackCondition.tractionChange3000(1200, false));
        s1.addAnnouncement(TrackCondition.tractionChange1500(1300, true));
        s1.addAnnouncement(TrackCondition.tractionChange25000(1400, true));
        s1.addAnnouncement(TrackCondition.tractionChange0(1500, true));

        trackSectionList.add(s1);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList)); // 1000m
        // JUnitUtil.waitFor(10000);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testZeroDistance(){
        DmiFrame df = new DmiFrame("testZeroDistance");
        DmiPanel p = df.getDmiPanel();
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setScale(0);
        advanceBottomOfPlanning(p);
        
        p.setScale(1);
        advanceBottomOfPlanning(p);
        
        p.setScale(2);
        advanceBottomOfPlanning(p);

        p.setScale(3);
        advanceBottomOfPlanning(p);
        
        p.setScale(4);
        advanceBottomOfPlanning(p);
        
        p.setScale(5);
        advanceBottomOfPlanning(p);
        
        jfo.requestClose();
        jfo.waitClosed();
    }
    
    private void advanceBottomOfPlanning(DmiPanel p){
        ArrayList<TrackSection> trackSectionList = new ArrayList<>();
        TrackSection s1 = new TrackSection(100,100,4);
        s1.addAnnouncement(TrackCondition.airConClose(50, false));
        // s1.addAnnouncement(new StationTrackCondition(60,"My Station Name"));
        trackSectionList.add(s1);
        p.extendMovementAuthorities(new MovementAuthority(trackSectionList)); // 2000m
        // JUnitUtil.waitFor(1000);
        for (int i = 0; i < 101; i++){
            p.advance(1);
            // JUnitUtil.waitFor(200);
        }
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
