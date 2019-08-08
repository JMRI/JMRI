package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
//import jmri.util.swing.JemmyUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditPortalDirectionTest {

    OBlockManager blkMgr;

    @Test
    @org.junit.Ignore("Cannot get button pushed!")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor("EditPortalDirectionTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");
        EditPortalDirection dFrame = new EditPortalDirection("Edit Direction Arrows", cb, ob1);
        Assert.assertNotNull("exists", dFrame);
        
        JFrameOperator jfo = new JFrameOperator(dFrame);
        Thread t = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        });
        t.start();
        
        JUnitUtil.dispose(frame);
        JUnitUtil.dispose(dFrame);
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

    // private final static Logger log = LoggerFactory.getLogger(EditPortalDirectionTest.class);

}
