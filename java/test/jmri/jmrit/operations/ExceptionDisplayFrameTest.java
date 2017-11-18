package jmri.jmrit.operations;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExceptionDisplayFrameTest.class);
}
