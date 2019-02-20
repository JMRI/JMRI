package jmri.jmrix.jinput.treecontrol;

import jmri.jmrix.jinput.TreeModel;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of TreePanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TreePanelTest {

    @Test
    public void testCtor() throws InterruptedException {
        try {
            // just checking for failure to construct
            new TreePanel();
        } catch (Throwable e) {
            log.warn("TreeModelTest caught "+e);
            if (e instanceof UnsatisfiedLinkError) {
                log.info("TreeModel.instance threw UnsatisfiedLinkError, which means we can't test on this platform");
                return;
            } else if (e instanceof ClassNotFoundException) {
                log.info("TreeModel.instance threw ClassNotFoundException, which means we can't test on this platform");
                return;
            } else {
                Assert.fail("instance threw "+e);
            }
        }
        // then kill the thread
        TreeModel.instance().terminateThreads();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreePanelTest.class);
}
