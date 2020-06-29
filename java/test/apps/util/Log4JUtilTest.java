package apps.util;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the apps.util.Log4JUtil class.
 *
 * @author Bob Jacobsen Copyright 2003, 2009, 2010, 2015
 */
public class Log4JUtilTest {

    @Test
    public void testLog4JWarnMessage() {
        log.warn("WARN message succeeds");
        JUnitAppender.assertWarnMessage("WARN message succeeds");

        log.debug("DEBUG message"); // should be suppressed see tests.lcf

        Assert.assertTrue(JUnitAppender.verifyNoBacklog());        
    }

    @Test
    public void testSendJavaUtilLogInfoMessage() {
        // test that java.util.logging is getting to Log4J
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Log4JUtilTest.class.getName());
        logger.log(java.util.logging.Level.WARNING, "j.u.l WARNING message");
        JUnitAppender.assertWarnMessage("j.u.l WARNING message");

        logger.log(java.util.logging.Level.FINER, "j.u.l FINER message"); // should be suppressed see tests.lcf

        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Log4JUtilTest.class);

}
