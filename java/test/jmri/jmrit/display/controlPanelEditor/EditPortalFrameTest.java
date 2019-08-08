package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;

//import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

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
public class EditPortalFrameTest {

    OBlockManager blkMgr;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor("EditPortalFrameTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");

        new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator("Edit Portal Frame");
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        }).start();

        EditPortalFrame portalFrame = new EditPortalFrame("Edit Portal Frame", cb, ob1);
        Assert.assertNotNull("exists", portalFrame);
        
        JUnitUtil.dispose(frame);
        JUnitUtil.dispose(portalFrame);
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

    // private final static Logger log = LoggerFactory.getLogger(EditPortalFrameTest.class);

}
