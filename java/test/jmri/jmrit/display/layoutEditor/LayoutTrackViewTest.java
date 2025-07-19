package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTrackView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class LayoutTrackViewTest {

    // LayoutTrackView is abstract, so there's
    // not much we can do here right now. But we provide a single
    // LayoutInstance support to all the subtypes.  This is
    // needed because multiple LayoutEditor objects don't always play nice.

    public LayoutEditor layoutEditor;

    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
