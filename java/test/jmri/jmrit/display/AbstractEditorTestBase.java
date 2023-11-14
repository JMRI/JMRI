package jmri.jmrit.display;

import javax.swing.UIManager;

import jmri.util.JUnitUtil;
import jmri.util.SystemType;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 * A Base set of tests for Editor objects.
 *
 * @param <T> specific subclass of Editor to test
 * @author Paul Bender Copyright (C) 2016
 */
abstract public class AbstractEditorTestBase<T extends Editor> {

    /**
     * The instance of the Editor to test.
     */
    protected T e = null;

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void checkFileMenuExists() {

        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));
        Assert.assertNotNull("File Menu Exists", jmo);
    }

    @Test
    @Disabled("The test sometimes has trouble finding the file menu")
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void checkFileDeleteMenuItem() {

        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        jfo.deleteViaFileMenuWithConfirmations();
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void checkWindowMenuExists() {

        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuWindow"));
        Assert.assertNotNull("Window Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 0, jmo.getItemCount());
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void checkHelpMenuExists() {

        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuHelp"));
        Assert.assertNotNull("Help Menu Exists", jmo);
        if (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            // macOS w/ native L&F help menu does not include "About" menu item
            // or the preceding separator
            Assert.assertEquals("Menu Item Count", 8, jmo.getItemCount());
        } else {
            Assert.assertEquals("Menu Item Count", 10, jmo.getItemCount());
        }
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
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
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void testChangeView() throws Positionable.DuplicateIdException {

        // create a new Positionable Label on the existing editor (e);
        PositionableLabel to = new PositionableLabel("one", e);
        to.setBounds(80, 80, 40, 40);
        e.putItem(to);

        Editor newEditor = e.changeView("jmri.jmrit.display.EditorScaffold");
        Assert.assertNotNull("changeView Result Not Null", newEditor);

        // verify the editor object on to was changed to newEditor.
        Assert.assertEquals("to moved to new editor", newEditor, to.getEditor());

        // and that the object is now in the new editor's list of objects.

        Assert.assertTrue("new editor includes to", newEditor.getContents().contains(to));
        newEditor.dispose();
    }

    /**
     * Subclasses must instantiate {@link #e} in the setUp method.
     */
    abstract public void setUp();

    abstract public void tearDown();

}
