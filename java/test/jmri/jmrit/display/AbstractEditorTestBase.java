package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import jmri.util.JUnitUtil;
import jmri.util.SystemType;
import org.junit.*;
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
    public void checkFileMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));
        Assert.assertNotNull("File Menu Exists", jmo);
    }

    @Test
    @Ignore("The test sometimes has trouble finding the file menu")
    public void checkFileDeleteMenuItem() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        jfo.deleteViaFileMenuWithConfirmations();
    }

    @Test
    public void checkWindowMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuWindow"));
        Assert.assertNotNull("Window Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 0, jmo.getItemCount());
    }

    @Test
    public void checkHelpMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    @Ignore("This test seems to be reliable on Linux, but fails on Windows (appveyor)")
    public void testSetSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        java.awt.Dimension d0 = e.getSize();
        e.setSize(100, 100);
        JUnitUtil.waitFor(() -> {
            return d0 != e.getSize();
        });
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    public void testChangeView() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    @Before
    abstract public void setUp();

    @After
    abstract public void tearDown();

}
