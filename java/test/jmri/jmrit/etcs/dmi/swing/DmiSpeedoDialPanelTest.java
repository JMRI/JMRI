package jmri.jmrit.etcs.dmi.swing;

import java.util.ArrayList;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for DmiSpeedoDialPanel.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiSpeedoDialPanelTest {

    @Test
    public void testScalesOnDial() {
        DmiFrame df = new DmiFrame("testScalesOnDial");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setCentreCircleAndDialColor(DmiPanel.YELLOW);

        p.setReleaseSpeed(50);

        stepSpeeds(p, 140);

        p.setCentreCircleAndDialColor(DmiPanel.ORANGE);

        stepSpeeds(p, 180);
        p.setCentreCircleAndDialColor(DmiPanel.RED);

        p.setReleaseSpeed(-0);
        stepSpeeds(p, 250);

        p.setCentreCircleAndDialColor(DmiPanel.YELLOW);

        stepSpeeds(p, 400);
        // stepSpeeds(p, 600);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
    }

    @Test
    public void testTargetAdviceSpeed() {
        DmiFrame df = new DmiFrame("testTargetAdviceSpeed");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setTargetAdviceSpeed(-1);
        // JUnitUtil.waitFor(5000);

        for (int i = 0; i < 141; i++){
            p.setActualSpeed(i);
            p.setTargetAdviceSpeed(i);
            // JUnitUtil.waitFor(100);
        }

        // JUnitUtil.waitFor(1000);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    private void stepSpeeds(DmiPanel p, int maxSpeed){

        p.setMaxDialSpeed(maxSpeed);
        // JUnitUtil.waitFor(1000);
        for (float i = 0; i <= maxSpeed; i += 0.4f) {
            p.setActualSpeed(i);

            DmiCircularSpeedGuideSection csg = new DmiCircularSpeedGuideSection(
                DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,DmiPanel.GREY,0,i, true);

            ArrayList<DmiCircularSpeedGuideSection> csgList = new ArrayList<>();
            csgList.add(csg);

            p.setCsgSections(csgList);

            p.setLimitedSupervisionSpeed(i-30);
            p.setDistanceToTarget(i-10);

            // JUnitUtil.waitFor((i % 10 == 0 ? 100 : 10));
        }
        // JUnitUtil.waitFor(2000);
        for (int i = maxSpeed; i > 0; i-- ) {
            p.setActualSpeed(i);
            setSpeedHookSpeed(i, p);
            p.setLimitedSupervisionSpeed(i-30);
            p.setDistanceToTarget(i-10);
            // JUnitUtil.waitFor(10);
        }

        p.setActualSpeed(1);
        setSpeedHookSpeed(1, p);
        // JUnitUtil.waitFor(100);

        p.setActualSpeed(0);
        setSpeedHookSpeed(0, p);
        // JUnitUtil.waitFor(1000);
    }

    private void setSpeedHookSpeed(float speedHookSpeed, DmiPanel p){
    
        ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new ArrayList<>();
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
