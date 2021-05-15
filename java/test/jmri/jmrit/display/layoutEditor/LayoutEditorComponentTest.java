package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutEditorComponentTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor("Layout Editor Component Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(le);
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

}
