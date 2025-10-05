package jmri.jmrit.throttle;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of ThrottlesPreferencesPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesPreferencesPaneTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        ThrottlesPreferencesPane panel = new ThrottlesPreferencesPane();
        Assertions.assertNotNull(panel, "exists");
        JFrame f = new JFrame(panel.getPreferencesItemText());
        f.add(panel);
        
        ThreadingUtil.runOnGUI(() -> {
            f.pack();
            f.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(panel.getPreferencesItemText());
        JUnitUtil.dispose(jfo.getWindow());
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
