package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.LocoIcon;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackerTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Tracker t = new Tracker(new OBlock("OB1", "Test"), "Test", 
                new LocoIcon(new EditorScaffold()), 
                InstanceManager.getDefault(TrackerTableAction.class));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackerTableAction trackTable = InstanceManager.getDefault(TrackerTableAction.class);
        OBlock blk1 = new OBlock("OB1", "blk1");
        blk1.setState(OBlock.OCCUPIED);
        Tracker t = new Tracker(blk1, "Test", 
                new LocoIcon(new EditorScaffold()), 
                trackTable);
        Assert.assertNotNull("exists",t);
        List<OBlock> occupied = t.getBlocksOccupied();
        Assert.assertEquals("Number Blocks Occupied", 1, occupied.size());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTest.class);

}
