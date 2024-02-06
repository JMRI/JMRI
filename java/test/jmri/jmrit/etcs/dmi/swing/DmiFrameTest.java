package jmri.jmrit.etcs.dmi.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiFrame.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiFrameTest {

    @Test
    public void testCTor() {
        DmiFrame t = new DmiFrame();
        Assertions.assertNotNull(t);
        t.dispose();
    }

    @Test
    public void testDisplay() {

        DmiFrame t = new DmiFrame("testDisplay");
        Assertions.assertNotNull(t);
        t.setVisible(true);

        JFrameOperator jfo = new JFrameOperator(t.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertTrue( jfo.getSize().getWidth() >= 640 );
        Assertions.assertTrue( jfo.getSize().getHeight() >= 480 );

        // JUnitUtil.waitFor(15000);        
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
