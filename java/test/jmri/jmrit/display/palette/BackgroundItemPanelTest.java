package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BackgroundItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        DisplayFrame df = new DisplayFrame();
        BackgroundItemPanel t = new BackgroundItemPanel(df,"test");
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(df);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BackgroundItemPanelTest.class);

}
