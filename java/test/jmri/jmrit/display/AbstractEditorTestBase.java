package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.display.EditorFrameOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
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
        JMenuOperator jmo = new JMenuOperator(jfo,Bundle.getMessage("MenuFile"));
        Assert.assertNotNull("File Menu Exists",jmo);
    }

    @Test
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
        JMenuOperator jmo = new JMenuOperator(jfo,Bundle.getMessage("MenuWindow"));
        Assert.assertNotNull("Window Menu Exists",jmo);
        Assert.assertEquals("Menu Item Count",0,jmo.getItemCount());
    }

    @Test
    public void checkHelpMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo,Bundle.getMessage("MenuHelp"));
        Assert.assertNotNull("Help Menu Exists",jmo);
        Assert.assertEquals("Menu Item Count",10,jmo.getItemCount());
    }

    // from here down is testing infrastructure
    @Before
    abstract public void setUp(); // must set Editor e

    @After
    abstract public void tearDown();

}
