package jmri.jmrit.progsupport;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProgDeferredServiceModePaneTest.class);
}
