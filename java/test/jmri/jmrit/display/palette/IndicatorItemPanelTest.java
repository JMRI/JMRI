package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.DisplayFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IndicatorItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DisplayFrame df = new DisplayFrame();
        IndicatorItemPanel t = new IndicatorItemPanel(df,"test1","test2");
        Assert.assertNotNull("exists",t);
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorItemPanelTest.class);

}
