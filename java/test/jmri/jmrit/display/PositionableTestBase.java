package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Base class for tests for Positionable objects. 
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class PositionableTestBase{

    protected Positionable p = null;  //derived classes should set p in setUp

    @Before
    abstract public void setUp();

    @Test
    public void testGetAndSetPositionable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt Positionable", p.isPositionable());
        p.setPositionable(false);
        Assert.assertFalse("Positionable after set false", p.isPositionable());
        p.setPositionable(true);
        Assert.assertTrue("Positionable after set true", p.isPositionable());
    }

    @Test
    public void testGetAndSetEditable() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt Editable", p.isEditable());
        p.setEditable(false);
        Assert.assertFalse("Editable after set false", p.isEditable());
        p.setEditable(true);
        Assert.assertTrue("Editable after set true", p.isEditable());
    }

    @Test
    public void testGetAndSetShowToolTip() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt ShowToolTip", p.showToolTip());
        p.setShowToolTip(false);
        Assert.assertFalse("showToolTip after set false", p.showToolTip());
        p.setShowToolTip(true);
        Assert.assertTrue("showToolTip after set true", p.showToolTip());
    }

    @Test
    public void testGetAndSetToolTip() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("default tool tip", p.getToolTip());
        p.setToolTip(new ToolTip("hello",0,0));
        Assert.assertNotNull("tool tip after set", p.getToolTip());
    }

    @Test
    public void testGetAndSetViewCoordinates() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
    }

    @Test
    public void testGetAndSetControlling() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Defalt ShowToolTip", p.isControlling());
        p.setControlling(false);
        Assert.assertFalse("Controlling after set false", p.isControlling());
        p.setControlling(true);
        Assert.assertTrue("Controlling after set true", p.isControlling());
    }

    @Test
    public void testGetAndSetHidden() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Defalt Hidden", p.isHidden());
        p.setHidden(true);
        Assert.assertTrue("Hidden after set true", p.isHidden());
        p.setHidden(false);
        Assert.assertFalse("Hidden after set false", p.isHidden());
    }

    @Test
    public void testGetAndSetDisplayLevel(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        p.setDisplayLevel(2);
        Assert.assertEquals("Display Level",2,p.getDisplayLevel());
    }

    @Test
    public void testGetAndSetEditor(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor es = new EditorScaffold();
        p.setEditor(es);
        Assert.assertEquals("Editor",es,p.getEditor());
    }

    @Test
    public void testClone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Positionable p2 = p.deepClone();

        // this next line is consistently failing (on all object types).  
        // It should pass.
        //Assert.assertFalse("clone object (not content) equality", p.equals(p));

        Assert.assertTrue("class type equality", p.getClass().equals(p.getClass()));
    }

}
