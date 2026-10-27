package jmri.jmrit.logix;

import java.util.Arrays;

import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check that a Tracker moving through a dark block attaches the dark block
 * that connects to the block entered, at the end of the train entering it.
 *
 * Layout: B - D2(dark) - X - Y - D1(dark) - Z
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class TrackerDarkBlockTest {

    private OBlockManager blkMgr;
    private PortalManager portalMgr;

    private OBlock block(String sysName, String userName, int state) {
        OBlock b = blkMgr.createNewOBlock(sysName, userName);
        assertNotNull(b);
        b.setState(state);
        return b;
    }

    private void connect(String name, OBlock from, OBlock to) {
        Portal p = portalMgr.createNewPortal(name);
        assertNotNull(p);
        assertTrue(p.setFromBlock(from, false));
        assertTrue(p.setToBlock(to, false));
    }

    @Test
    public void testTailEntersThroughItsOwnDarkBlock() throws JmriException {
        OBlock b = block("OB1", "B", Block.UNOCCUPIED);
        OBlock d2 = block("OB2", "D2", Block.UNDETECTED);
        OBlock x = block("OB3", "X", Block.OCCUPIED);
        OBlock y = block("OB4", "Y", Block.UNOCCUPIED);
        OBlock d1 = block("OB5", "D1", Block.UNDETECTED);
        OBlock z = block("OB6", "Z", Block.UNOCCUPIED);
        connect("B-D2", b, d2);
        connect("D2-X", d2, x);
        connect("X-Y", x, y);
        connect("Y-D1", y, d1);
        connect("D1-Z", d1, z);

        TrackerTableAction tta = InstanceManager.getDefault(TrackerTableAction.class);
        Tracker tracker = new Tracker(x, "Train", null, tta);

        // TrackerTableAction listens to the blocks next to the train and moves it.
        // The train extends into Y, which becomes the head; X is now the tail.
        y.setState(Block.OCCUPIED);
        assertEquals(Arrays.asList(y, x), tracker.getBlocksOccupied(), "Y joins the head");

        // the tail backs through dark D2 into B. D1, dark and beside the head,
        // does not connect to B.
        b.setState(Block.OCCUPIED);
        assertEquals(Arrays.asList(y, x, d2, b), tracker.getBlocksOccupied(),
                "dark block D2 joins the tail with B");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        blkMgr = InstanceManager.getDefault(OBlockManager.class);
        portalMgr = InstanceManager.getDefault(PortalManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.dispose(JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")));
        blkMgr = null;
        portalMgr = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
