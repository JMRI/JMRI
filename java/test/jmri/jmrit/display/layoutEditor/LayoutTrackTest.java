package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test common functioning of LayoutTrack.
 * Other tests inherit from this so that the
 * classes are still checked against the basic
 * LayoutTrack contract.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public abstract class LayoutTrackTest {

    protected LayoutEditor layoutEditor = null;

    /**
     * Calls JUnitUtil.setUp and creates new LayoutEditor.
     */
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        JUnitUtil.setUp();

        // eventually we'll be using
        // LayoutModels instead of the full LayoutEditor
        // for context, in which case this will be OK headless
        if (!GraphicsEnvironment.isHeadless()) {
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
