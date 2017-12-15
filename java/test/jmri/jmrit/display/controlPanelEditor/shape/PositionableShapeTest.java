package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponentTest;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionableShapeTest extends PositionableJComponentTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", p);
    }

    @Override
    @Test
    public void testGetAndSetShowToolTip() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Defalt ShowToolTip", p.showToolTip());
        p.setShowToolTip(true);
        Assert.assertTrue("showToolTip after set true", p.showToolTip());
        p.setShowToolTip(false);
        Assert.assertFalse("showToolTip after set false", p.showToolTip());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           p = new PositionableShape(new EditorScaffold()) {
               @Override
               protected Shape makeShape() {
                   // bogus body, not used in tests
                   return null;
               }

               @Override
               public Positionable deepClone() {
                   // bogus body, not used in tests
                   return null;
               }
           };
       }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableShapeTest.class);
}
