package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Block;
import jmri.BlockManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the BlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class BlockWhereUsedTest {

    @Test
    public void testBlockWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        BlockWhereUsed ctor = new BlockWhereUsed();
        Assert.assertNotNull("exists", ctor);

//         Block block = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("B-Main");
//         JTextArea result = BlockWhereUsed.getWhereUsed(block);
//         Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
//         JUnitUtil.resetProfileManager();
//         JUnitUtil.initRosterConfigManager();
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
//         java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
//         cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockWhereUsedTest.class);
}
