package jmri.jmrit.etcs.dmi.swing;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiDemo.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiDemoTest {

    @Test
    public void testDemo(){

        try {
            AudioSystem.getClip();
        } catch (IllegalArgumentException | LineUnavailableException ex) {
            Assumptions.assumeFalse(true, "Unable to initialize AudioSystem");
        }

        DmiDemo.setDelayMultiplier(0);

        DmiFrame df = new DmiFrame("DmiDemoTest");
        DmiPanel p = df.getDmiPanel();
        Assertions.assertNotNull(p);
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        DmiDemo d = new DmiDemo(p);
        d.runDemo();

        JLabelOperator label1oper = JemmyUtil.getLabelOperatorByName(jfo, "msglabel1");
        JUnitUtil.waitFor( () -> label1oper.getText().equals("Demo Complete"), "Demo Complete");

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
