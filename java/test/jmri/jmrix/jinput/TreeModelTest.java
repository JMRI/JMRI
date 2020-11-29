package jmri.jmrix.jinput;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of TreeModel
 *
 * @author Paul Bender Copyright (C) 2016
 */
@Timeout(10)
public class TreeModelTest {

    @Test
    public void testInstance() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        try {
            Assert.assertNotNull("exists", TreeModel.instance());
        } catch (Throwable e) {
            log.warn("TreeModelTest caught {}", e);
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeModelTest.class);
}
