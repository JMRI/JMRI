package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test common functioning of LayoutTrack.
 * Other tests inherit from this so that the
 * classes are still checked against the basic
 * LayoutTrack contract.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTrackTest {

    protected LayoutEditor le = null;

    // from here down is testing infrastructure
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            le = new LayoutEditor();
        }
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
    }
}
