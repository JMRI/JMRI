package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LevelXing
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LevelXingTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        LayoutEditor e = new LayoutEditor();
        LevelXing t = new LevelXing("test", e);  // new Point2D.Double(0.0, 0.0),
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(e);
    }

    // from here down is testing infrastructure
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
}
