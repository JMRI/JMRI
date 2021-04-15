package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of LayoutTurntable
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutTurntableTest extends LayoutTrackTest {

    LayoutEditor layoutEditor = null;
    LayoutTurntable lt = null;

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", lt);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        String ltString = lt.toString();
        Assert.assertNotNull("ltString not null", ltString);
        Assert.assertEquals("LayoutTurntable My Turntable", ltString);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        if(!GraphicsEnvironment.isHeadless()){

            layoutEditor = new LayoutEditor();
            Assert.assertNotNull("LayoutEditor not null", layoutEditor);

            lt = new LayoutTurntable("My Turntable", layoutEditor); // new Point2D.Double(50.0, 100.0),
        }
    }

    @AfterEach
    public void tearDown() {
        if(layoutEditor!=null){
           JUnitUtil.dispose(layoutEditor);
        }
        lt = null;
        layoutEditor = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
