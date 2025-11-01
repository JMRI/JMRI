package jmri.jmrit.display.controlPanelEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditPortalDirectionTest {

    private OBlockManager blkMgr;

    @Test
    @DisabledIfHeadless
    public void testSetup() {
        Assumptions.assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        ControlPanelEditor frame = new ControlPanelEditor("EditPortalDirectionTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");

        Thread t = new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator("Edit Direction Arrows");
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        });
        t.start();

        EditPortalDirection dFrame = new EditPortalDirection("Edit Direction Arrows", cb, ob1);
        assertNotNull( dFrame, "exists");
        JUnitUtil.waitThreadTerminated(t);

        JFrameOperator jfo = new JFrameOperator("Edit Direction Arrows");

        // Block circuit (OBlock) "a" has no portals . . .
        Thread tt = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("editCiruit"), "OK");
        new JButtonOperator(jfo, "Done").doClick();

        JUnitUtil.waitThreadTerminated(tt);
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        blkMgr = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        blkMgr.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        //JUnitUtil.clearShutDownManager();  // only needed intermittently; better to find and remove, but that would require lots o' refactoring
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditPortalDirectionTest.class);

}
