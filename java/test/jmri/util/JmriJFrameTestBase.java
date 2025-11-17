package jmri.util;


import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Base tests for JmriJFrame derived frames.
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class JmriJFrameTestBase {

    protected JmriJFrame frame = null;

    @DisabledIfHeadless
    @Test
    public void testCTor() {
        Assertions.assertNotNull( frame, "exists");
    }

    @DisabledIfHeadless
    @Test
    public void testShowAndClose() {
        frame.initComponents();
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.setVisible(true);
        });
        JFrameOperator fo = new JFrameOperator(frame);
        // It's up at this point, and can be manipulated
        // Ask to close window
        fo.requestClose();
        fo.waitClosed();
    }

    @DisabledIfHeadless
    @Test
    public void testAccessibleContent() {
        frame.initComponents();
        jmri.util.AccessibilityChecks.check(frame);
    }

    abstract public void setUp();  // set the value of frame.  
                                   // do not call initComponents.

    @AfterEach
    public void tearDown() {
        if(frame!=null) {
           JUnitUtil.dispose(frame);
        }
        frame = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
