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
 * @author Pete Cressman Copyright (C) 2019
 */
public class EditSignalFrameTest {

    OBlockManager blkMgr;

    @Test
    @org.junit.Ignore("Cannot get button pushed!")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor("EditSignalFrameTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        Assert.assertNotNull("exists", cb);
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");
        EditSignalFrame pFrame = new EditSignalFrame("Edit Signal Frame", cb, ob1);
        Assert.assertNotNull("exists", pFrame);
        
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JFrameOperator jfo = new JFrameOperator(pFrame);
        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("incompleteCircuit"), 
                Bundle.getMessage("needPortal", ob1.getDisplayName(), Bundle.getMessage("BlockSignal")), "OK");
        
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

    // private final static Logger log = LoggerFactory.getLogger(EditSignalFrameTest.class);
}
