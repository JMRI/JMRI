package jmri.jmrit.display;

import javax.swing.UIManager;

import jmri.util.JUnitUtil;
import jmri.util.SystemType;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JMenuOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Base set of tests for Editor objects.
 *
 * @param <T> specific subclass of Editor to test
 * @author Paul Bender Copyright (C) 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
abstract public class AbstractEditorTestBase<T extends Editor> {

    /**
     * The instance of the Editor to test.
     */
    protected T e = null;

    @Test
    public void checkFileMenuExists() {

        ThreadingUtil.runOnGUI( () -> e.setVisible(true) );
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));
        assertNotNull( jmo, "File Menu Exists");
    }

    @Test
    @Disabled("The test sometimes has trouble finding the file menu")
    public void checkFileDeleteMenuItem() {

        ThreadingUtil.runOnGUI( () -> e.setVisible(true) );
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        jfo.deleteViaFileMenuWithConfirmations();
    }

    @Test
    public void checkWindowMenuExists() {

        ThreadingUtil.runOnGUI( () -> e.setVisible(true) );
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuWindow"));
        assertNotNull(jmo, "Window Menu Exists");
        assertEquals( 0, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkHelpMenuExists() {

        ThreadingUtil.runOnGUI( () -> e.setVisible(true) );
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuHelp"));
        assertNotNull( jmo, "Help Menu Exists");
        if (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            // macOS w/ native L&F help menu does not include "About" menu item
            // or the preceding separator
            assertEquals( 8, jmo.getItemCount(), "Menu Item Count");
        } else {
            assertEquals( 11, jmo.getItemCount(), "Menu Item Count");
        }
    }

    @Test
    @Disabled("This test seems to be reliable on Linux, but fails on Windows (appveyor)")
    public void testSetSize() {

        java.awt.Dimension d0 = e.getSize();
        e.setSize(100, 100);
        JUnitUtil.waitFor(() -> {
            return d0 != e.getSize();
        },"dimension still equals initial editor size");
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        assertEquals( 100.0, d.getWidth(), 0.0, "Width Set");
        assertEquals( 100.0, d.getHeight(), 0.0, "Height Set");
    }

    @Test
    public void testChangeView() throws Positionable.DuplicateIdException {

        // create a new Positionable Label on the existing editor (e);
        PositionableLabel to = new PositionableLabel("one", e);
        to.setBounds(80, 80, 40, 40);
        e.putItem(to);

        Boolean complete = ThreadingUtil.runOnGUIwithReturn(() -> {
            Editor newEditor= e.changeView("jmri.jmrit.display.EditorScaffold");
            assertNotNull( newEditor, "changeView Result Not Null");

            // verify the editor object on to was changed to newEditor.
            assertEquals( newEditor, to.getEditor(), "to moved to new editor");

            // and that the object is now in the new editor's list of objects.

            assertTrue( newEditor.getContents().contains(to), "new editor includes to");

            JUnitUtil.dispose(newEditor);
            return true;
        });
        assertTrue(complete, "Editor completed changeView on GUI Thread");
    }

    /**
     * Subclasses must instantiate {@link #e} in the setUp method.
     */
    abstract public void setUp();

    abstract public void tearDown();

}
