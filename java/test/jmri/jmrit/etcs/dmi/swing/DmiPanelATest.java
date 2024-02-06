package jmri.jmrit.etcs.dmi.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanelA.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelATest {

    @Test
    public void testCountDownScales() {
        DmiFrame df = new DmiFrame("testCountDownScales");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        p.setDistanceToTarget(0);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(100);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(200);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(300);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(400);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(500);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(600);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(700);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(800);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(900);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(1000);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(1100);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(0);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(100);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(0);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(100);
        // JUnitUtil.waitFor(1000);

        p.setDistanceToTarget(0);
        // JUnitUtil.waitFor(1000);

        for (int i = 0; i <= 1200; i++) {
            p.setDistanceToTarget(i);
            // JUnitUtil.waitFor(5);
        }

        for (int i = 1200; i > 0; i-- ) {
            p.setDistanceToTarget(i);
            p.setLimitedSupervisionSpeed(i/10);
            // JUnitUtil.waitFor(50);
        }

        jfo.requestClose();
        jfo.waitClosed();
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
