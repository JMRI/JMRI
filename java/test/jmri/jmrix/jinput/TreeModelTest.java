package jmri.jmrix.jinput;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TreeModel
 *
 * @author Paul Bender Copyright (C) 2016
 */
@Timeout(10)
public class TreeModelTest {

    @Test
    @DisabledIfHeadless
    public void testInstance() throws InterruptedException {

        try {
            Assertions.assertNotNull( TreeModel.instance(), "exists");
        } catch (Throwable e) {
            log.warn("TreeModelTest caught ", e);
            if (e instanceof UnsatisfiedLinkError) {
                log.info("TreeModel.instance threw UnsatisfiedLinkError, which means we can't test on this platform");
                return;
            } else if (e instanceof ClassNotFoundException) {
                log.info("TreeModel.instance threw ClassNotFoundException, which means we can't test on this platform");
                return;
            } else {
                Assertions.fail("instance threw ", e);
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
