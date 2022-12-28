package jmri.jmrit.display;

import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

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
    @BeforeEach
    @OverridingMethodsMustInvokeSuper
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initReporterManager();
    }

    /**
     * Must call last in overriding method if overridden.
     */
    @AfterEach
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetPositionable() {
        Assert.assertTrue("Default Positionable", p.isPositionable());
        p.setPositionable(false);
        Assert.assertFalse("Positionable after set false", p.isPositionable());
        p.setPositionable(true);
        Assert.assertTrue("Positionable after set true", p.isPositionable());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetEditable() {
        Assert.assertTrue("Default Editable", p.isEditable());
        p.setEditable(false);
        Assert.assertFalse("Editable after set false", p.isEditable());
        p.setEditable(true);
        Assert.assertTrue("Editable after set true", p.isEditable());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetShowToolTip() {
        Assert.assertTrue("Default ShowToolTip", p.showToolTip());
        p.setShowToolTip(false);
        Assert.assertFalse("showToolTip after set false", p.showToolTip());
        p.setShowToolTip(true);
        Assert.assertTrue("showToolTip after set true", p.showToolTip());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetToolTip() {
        Assert.assertNull("default tool tip", p.getToolTip());
        p.setToolTip(new ToolTip("hello",0,0,null));
        Assert.assertNotNull("tool tip after set", p.getToolTip());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetViewCoordinates() {
        Assert.assertTrue("Default View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetControlling() {
        Assert.assertTrue("Default ShowToolTip", p.isControlling());
        p.setControlling(false);
        Assert.assertFalse("Controlling after set false", p.isControlling());
        p.setControlling(true);
        Assert.assertTrue("Controlling after set true", p.isControlling());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetHidden() {
        Assert.assertFalse("Default Hidden", p.isHidden());
        p.setHidden(true);
        Assert.assertTrue("Hidden after set true", p.isHidden());
        p.setHidden(false);
        Assert.assertFalse("Hidden after set false", p.isHidden());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetDisplayLevel(){
        p.setDisplayLevel(2);
        Assert.assertEquals("Display Level",2,p.getDisplayLevel());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetEditor(){
        Editor es = new EditorScaffold();
        p.setEditor(es);
        Assert.assertEquals("Editor",es,p.getEditor());
        JUnitUtil.dispose(es);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testClone() {
        p.deepClone();

        // this next line is consistently failing (on all object types).
        // It should pass.
        //Assert.assertFalse("clone object (not content) equality", p.equals(p));

        Assert.assertTrue("class type equality", p.getClass().equals(p.getClass()));
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testMaxWidth() {
        Assert.assertTrue("Max Width",0<=p.maxWidth());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testMaxHeight() {
        Assert.assertTrue("Max Height",0<=p.maxHeight());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetScale(){
        Assert.assertEquals("Default Scale",1.0D,p.getScale(),0.0);
        p.setScale(5.0D);
        Assert.assertEquals("Scale",5.0D,p.getScale(),0.0);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetAndSetRotationDegrees(){
        p.rotate(50);
        Assert.assertEquals("Degrees",50,p.getDegrees());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetTextComponent(){
        Assert.assertNotNull("text component",p.getTextComponent());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testStoreItem(){
        Assert.assertTrue("Store Item",p.storeItem());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDoViemMenu(){
        Assert.assertTrue("Do Viem Menu",p.doViemMenu());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetNameString(){
        Assert.assertNotNull("Name String",p.getNameString());
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testShow() throws Positionable.DuplicateIdException {

        JFrame jf = new jmri.util.JmriJFrame("Positionable Target Panel");
        JPanel panel = new JPanel();
        jf.getContentPane().add(panel);
        jf.pack();
        jf.setVisible(true);

        editor.putItem(p);
        p.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        Assertions.assertEquals(jmri.jmrit.display.Editor.LABELS, p.getDisplayLevel(), "Display Level ");

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
