package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IndicatorItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        DisplayFrame df = new DisplayFrame();
        IndicatorItemPanel t = new IndicatorItemPanel(df,"test1","test2");
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

    // private final static Logger log = LoggerFactory.getLogger(IndicatorItemPanelTest.class);

}
