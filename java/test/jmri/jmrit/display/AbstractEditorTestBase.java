package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import jmri.util.JUnitUtil;
import jmri.util.SystemType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 * A Base set of tests for Editor objects.
 *
 * @author Paul Bender Copyright (C) 2016
 */
abstract public class AbstractEditorTestBase {

    protected Editor e = null;

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

    // from here down is testing infrastructure
    @Before
    abstract public void setUp(); // must set Editor e

    @After
    abstract public void tearDown();

}
