package jmri.jmrit.throttle;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of ThrottlesPreferencesPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesPreferencesPaneTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        ThrottlesPreferencesPane panel = new ThrottlesPreferencesPane();
        Assertions.assertNotNull(panel, "exists");
        JFrame f = new JFrame(panel.getPreferencesItemText());
        f.add(panel);
        f.pack();
        ThreadingUtil.runOnGUI(() -> {
            f.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(panel.getPreferencesItemText());
        jfo.requestClose();
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();

    }
}
