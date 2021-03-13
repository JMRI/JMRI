package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditCircuitFrameTest {

    OBlockManager blkMgr;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor fr = new ControlPanelEditor("EditCircuitFrameTest");
        fr.makeCircuitMenu(true);
        CircuitBuilder cb = fr.getCircuitBuilder();
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");
        EditCircuitFrame cFrame = new EditCircuitFrame("Edit Circuit Frame", cb, ob1);
        Assert.assertNotNull("exists", cFrame);

        JUnitUtil.dispose(cFrame);
        JUnitUtil.dispose(fr);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        blkMgr = new OBlockManager();
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
    }


    @AfterEach
    public void tearDown() {
        blkMgr.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditCircuitFrameTest.class);

}
