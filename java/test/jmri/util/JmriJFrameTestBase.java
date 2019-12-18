package jmri.util;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Base tests for JmriJFrame derived frames.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class JmriJFrameTestBase {

    protected JmriJFrame frame = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",frame);
    }

    @Test
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.initComponents();
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            frame.setVisible(true);
        });
        JFrameOperator fo = new JFrameOperator(frame);
        // It's up at this point, and can be manipulated
        // Ask to close window
        fo.requestClose();
    }

    @Before
    abstract public void setUp();  // set the value of frame.  
                                   // do not call initComponents.

    @After
    public void tearDown() {
        if(frame!=null) {
           JUnitUtil.dispose(frame);
        }
        frame = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
