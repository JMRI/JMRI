package jmri.jmrit.progsupport;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgDeferredServiceModePaneTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        ProgDeferredServiceModePane t = new ProgDeferredServiceModePane();
        Assertions.assertNotNull(t, "exists");
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
