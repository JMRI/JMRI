package jmri.jmrit.display.controlPanelEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditCircuitPathsTest {

    private OBlockManager blkMgr;

    @Test
    @DisabledIfHeadless
    public void testBasicOps() {
        Assumptions.assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        ControlPanelEditor frame = new ControlPanelEditor("EditCircuitPathsTest");
        frame.makeCircuitMenu(true);
        CircuitBuilder cb = frame.getCircuitBuilder();
        assertNotNull( cb, "exists");
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");

        new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator("Edit Circuit Paths");
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        }).start();

        EditCircuitPaths pFrame = new EditCircuitPaths("Edit Circuit Paths", cb, ob1);
        assertNotNull( pFrame, "exists");

        JUnitUtil.dispose(frame);
        JUnitUtil.dispose(pFrame);
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
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditCircuitPathsTest.class);
}
