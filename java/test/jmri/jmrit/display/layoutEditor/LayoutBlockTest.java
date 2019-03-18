package jmri.jmrit.display.layoutEditor;

import jmri.Block;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutBlock
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutBlockTest {

    @Test
    public void testCtor() {
        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testBlockRename() {
        // Create layout block and the related automatic block
        LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
        layoutBlock.initializeLayoutBlock();
        Assert.assertNotNull(layoutBlock);
        Assert.assertEquals("Test Block", layoutBlock.getUserName());

        // Get the referenced block and change its user name
        Block block = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName("Test Block");
        Assert.assertNotNull(block);
        block.setUserName("New Test Block");

        // Verify that the block user name change propagated to the layout block
        Assert.assertEquals("New Test Block", layoutBlock.getUserName());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutBlockTest.class);
}
