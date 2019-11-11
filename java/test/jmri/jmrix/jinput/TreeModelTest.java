package jmri.jmrix.jinput;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.rules.Timeout;

/**
 * Test simple functioning of TreeModel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TreeModelTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(20); // timeout (seconds) for all test methods in this test class.

    @Test
    @Ignore("fails in CI if now hardware present, and exception handled internal to library in useless way")
    public void testInstance() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            Assert.assertNotNull("exists", TreeModel.instance());
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

    @Test
    public void testControllers() {
        Assert.assertFalse(TreeModel.isInstanceInitialzed());
            // this also ensures at least one line in the class is tested, as needed by JaCoCo
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeModelTest.class);
}
