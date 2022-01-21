package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Base tests for JmriJFrame derived frames.
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class JmriJFrameTestBase {

    protected JmriJFrame frame = null;

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",frame);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testShowAndClose() {
        frame.initComponents();
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            frame.setVisible(true);
        });
        JFrameOperator fo = new JFrameOperator(frame);
        // It's up at this point, and can be manipulated
        // Ask to close window
        fo.requestClose();
    }

    @BeforeEach
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
