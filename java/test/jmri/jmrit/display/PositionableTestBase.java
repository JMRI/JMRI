package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

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
    @DisabledIfHeadless
    public void testGetAndSetPositionable() {
        assertTrue( p.isPositionable(), "Default Positionable");
        p.setPositionable(false);
        assertFalse( p.isPositionable(), "Positionable after set false");
        p.setPositionable(true);
        assertTrue( p.isPositionable(), "Positionable after set true");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetEditable() {
        assertTrue( p.isEditable(), "Default Editable");
        p.setEditable(false);
        assertFalse( p.isEditable(), "Editable after set false");
        p.setEditable(true);
        assertTrue( p.isEditable(), "Editable after set true");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetShowToolTip() {
        assertTrue( p.showToolTip(), "Default ShowToolTip");
        p.setShowToolTip(false);
        assertFalse( p.showToolTip(), "showToolTip after set false");
        p.setShowToolTip(true);
        assertTrue( p.showToolTip(), "showToolTip after set true");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetToolTip() {
        assertNull( p.getToolTip(), "default tool tip");
        p.setToolTip(new ToolTip("hello",0,0,null));
        assertNotNull( p.getToolTip(), "tool tip after set");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetViewCoordinates() {
        assertTrue( p.getViewCoordinates(), "Default View Coordinates");
        p.setViewCoordinates(false);
        assertFalse( p.getViewCoordinates(), "View Coordinates after set false");
        p.setViewCoordinates(true);
        assertTrue( p.getViewCoordinates(), "View Coordinates after set true");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetControlling() {
        assertTrue( p.isControlling(), "Default ShowToolTip");
        p.setControlling(false);
        assertFalse( p.isControlling(), "Controlling after set false");
        p.setControlling(true);
        assertTrue( p.isControlling(), "Controlling after set true");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetHidden() {
        assertFalse( p.isHidden(), "Default Hidden");
        p.setHidden(true);
        assertTrue( p.isHidden(), "Hidden after set true");
        p.setHidden(false);
        assertFalse( p.isHidden(), "Hidden after set false");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetDisplayLevel(){
        p.setDisplayLevel(2);
        assertEquals( 2, p.getDisplayLevel(), "Display Level");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetEditor(){
        Editor es = new EditorScaffold();
        p.setEditor(es);
        assertEquals( es, p.getEditor(), "Editor");
        JUnitUtil.dispose(es);
    }

    @Test
    @DisabledIfHeadless
    public void testClone() {
        p.deepClone();

        // this next line is consistently failing (on all object types).
        // It should pass.
        //Assert.assertFalse("clone object (not content) equality", p.equals(p));

        assertTrue( p.getClass().equals(p.getClass()), "class type equality");
    }

    @Test
    @DisabledIfHeadless
    public void testMaxWidth() {
        assertTrue( 0 <= p.maxWidth(), "Max Width");
    }

    @Test
    @DisabledIfHeadless
    public void testMaxHeight() {
        assertTrue( 0 <= p.maxHeight(), "Max Height");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetScale(){
        assertEquals( 1.0D, p.getScale(), 0.0, "Default Scale");
        p.setScale(5.0D);
        assertEquals( 5.0D, p.getScale(), 0.0, "Scale");
    }

    @Test
    @DisabledIfHeadless
    public void testGetAndSetRotationDegrees(){
        p.rotate(50);
        assertEquals( 50, p.getDegrees(), "Degrees");
    }

    @Test
    @DisabledIfHeadless
    public void testGetTextComponent(){
        assertNotNull( p.getTextComponent(), "text component");
    }

    @Test
    @DisabledIfHeadless
    public void testStoreItem(){
        assertTrue( p.storeItem(), "Store Item");
    }

    @Test
    @DisabledIfHeadless
    public void testDoViemMenu(){
        assertTrue( p.doViemMenu(), "Do Viem Menu");
    }

    @Test
    @DisabledIfHeadless
    public void testGetNameString(){
        assertNotNull( p.getNameString(), "Name String");
    }

    @Test
    @DisabledIfHeadless
    public void testShow() throws Positionable.DuplicateIdException {

        JFrame jf = new jmri.util.JmriJFrame("Positionable Target Panel");
        JPanel panel = new JPanel();
        jf.getContentPane().add(panel);
        ThreadingUtil.runOnGUI( () -> {
            jf.pack();
            jf.setVisible(true);
        });

        editor.putItem(p);
        p.setDisplayLevel(jmri.jmrit.display.Editor.LABELS);

        Assertions.assertEquals(jmri.jmrit.display.Editor.LABELS, p.getDisplayLevel(), "Display Level ");

        editor.setLocation(150, 150);

        editor.setTitle();

        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });

        // close the frame.
        EditorFrameOperator jfo = new EditorFrameOperator(jf);
        jfo.requestClose();
        jfo.waitClosed();
    }

}
