package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditCircuitPathsTest {

    OBlockManager blkMgr;

    @Test
    @org.junit.Ignore("Cannot get button pushed!")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor("EditCircuitPathsTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        Assert.assertNotNull("exists", cb);
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");
        EditCircuitPaths pFrame = new EditCircuitPaths("Edit Circuit Paths", cb, ob1);
        Assert.assertNotNull("exists", pFrame);
        
        JFrameOperator jfo = new JFrameOperator(pFrame);
        Thread t = new Thread(() -> {
            JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("incompleteCircuit"), 
                    Bundle.getMessage("needPortal", ob1.getDisplayName(), Bundle.getMessage("BlockPaths")), "OK");
        });
        t.setName("Error Dialog Close Thread");
        t.start();
        
        JUnitUtil.dispose(frame);
        JUnitUtil.dispose(pFrame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        blkMgr = new OBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditCircuitPathsTest.class);
}
