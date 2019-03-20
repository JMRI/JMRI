package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionableJComponentTest extends PositionableTestBase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", p);
    }

    @Override
    @Test
    public void testGetAndSetViewCoordinates() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Defalt View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
    }

    @Test
    @Override
    @Ignore("PositionableJComponent does not support rotate")
    @ToDo("implement rotate in PositionableJComponent, then remove overriden test so parent class test can execute")
    public void testGetAndSetRotationDegrees() {
        Assert.fail("Should support rotation but doesn't");
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            p = new PositionableJComponent(editor);
            ((PositionableJComponent) p).setName("PositionableJComponent");
        }
    }

}
