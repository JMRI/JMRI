package jmri.jmrit.operations;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExceptionDisplayFrameTest {

    @Test
    @Ignore("Constructor causes modal dialog to launch that is not associated with a JFrame, so it can't be easilly dismissed with a Jemmy operator.")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExceptionContext ec = new ExceptionContext(new Exception("Test"),"Test","Test");
        ExceptionDisplayFrame t = new ExceptionDisplayFrame(ec);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExceptionDisplayFrameTest.class.getName());
}
