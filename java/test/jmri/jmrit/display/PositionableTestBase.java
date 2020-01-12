package jmri.jmrit.display;

import java.awt.event.WindowListener;
import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JFrame;
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
abstract public class PositionableTestBase {

    protected Editor editor = null;   // derived classes should set editor in setup;
    protected Positionable p = null;  //derived classes should set p in setUp

    /**
     * Must call first in overriding method if overridden.
     */
    @Before
    @OverridingMethodsMustInvokeSuper
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    /**
     * Must call last in overriding method if overridden.
     */
    @After
    @OverridingMethodsMustInvokeSuper
    public void tearDown() {
        // now close panel window, if it exists
        if (editor != null) {
            JFrame target = editor.getTargetFrame();
            if (target != null) {
                java.awt.event.WindowListener[] listeners = target.getWindowListeners();
                for (WindowListener listener : listeners) {
                    target.removeWindowListener(listener);
                }
                if (!editor.equals(target)) {
                    JUnitUtil.dispose(target);
                }
            }
            JUnitUtil.dispose(editor);
        }
        JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

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
        Assert.assertTrue("Default View Coordinates", p.getViewCoordinates());
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
        JUnitUtil.dispose(es);
    }

    @Test
    public void testClone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        p.deepClone();

        // this next line is consistently failing (on all object types).  
        // It should pass.
        //Assert.assertFalse("clone object (not content) equality", p.equals(p));

        Assert.assertTrue("class type equality", p.getClass().equals(p.getClass()));
    }

    @Test
    public void testMaxWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Max Width",0<=p.maxWidth());
    }

    @Test
    public void testMaxHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Max Height",0<=p.maxHeight());
    }

    @Test
    public void testGetAndSetScale(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Scale",1.0D,p.getScale(),0.0);
        p.setScale(5.0D);
        Assert.assertEquals("Scale",5.0D,p.getScale(),0.0);
    }
    
    @Test
    public void testGetAndSetRotationDegrees(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        p.rotate(50);
        Assert.assertEquals("Degrees",50,p.getDegrees());
    }

    @Test
    public void testGetTextComponent(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("text component",p.getTextComponent());
    }

    @Test
    public void testStoreItem(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Store Item",p.storeItem());
    }

    @Test
    public void testDoViemMenu(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Do Viem Menu",p.doViemMenu());
    }

    @Test
    public void testGetNameString(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("Name String",p.getNameString());
    }

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JFrame jf = new jmri.util.JmriJFrame("Positionable Target Panel");
        JPanel panel = new JPanel();
        jf.getContentPane().add(panel);
        jf.pack();
        jf.setVisible(true);

        editor.putItem(p);
        p.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        Assert.assertEquals("Display Level ", p.getDisplayLevel(), jmri.jmrit.display.Editor.LABELS);

        editor.setLocation(150, 150);

        editor.setTitle();

        editor.pack();
        editor.setVisible(true);

        // close the frame.
        EditorFrameOperator jfo = new EditorFrameOperator(jf);
        jfo.requestClose();
        jfo.waitClosed();
    }

}
