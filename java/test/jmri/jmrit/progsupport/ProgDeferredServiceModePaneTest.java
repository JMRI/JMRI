package jmri.jmrit.progsupport;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgDeferredServiceModePaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ProgDeferredServiceModePane t = new ProgDeferredServiceModePane();
        Assert.assertNotNull("exists", t);
        jmri.util.JUnitAppender.assertErrorMessage("This is missing code to listen to the programmer and update the mode display");
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProgDeferredServiceModePaneTest.class);
}
