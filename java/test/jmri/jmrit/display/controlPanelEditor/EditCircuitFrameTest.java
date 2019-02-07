package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditCircuitFrameTest {

    ControlPanelEditor frame;
    EditCircuitFrame t;
    CircuitBuilder cb;
    OBlock ob;
    
    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame = new ControlPanelEditor();
            cb = new CircuitBuilder(frame);
        });

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ob = new OBlock("OB01");
            t = new EditCircuitFrame("Edit Circuit Frame", cb, ob);
        });
        
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(frame);
        JUnitUtil.dispose(t);
        frame = null;
        t = null;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditCircuitFrameTest.class);

}
