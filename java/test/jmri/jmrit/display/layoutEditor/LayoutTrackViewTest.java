package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTrackView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
@DisabledIfHeadless
public class LayoutTrackViewTest {

    // LayoutTrackView is abstract, so there's
    // not much we can do here right now. But we provide a single
    // LayoutInstance support to all the subtypes.  This is
    // needed because multiple LayoutEditor objects don't always play nice.

    protected LayoutEditor layoutEditor;

    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        layoutEditor = new LayoutEditor();

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
