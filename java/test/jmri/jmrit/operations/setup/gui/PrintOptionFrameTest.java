package jmri.jmrit.operations.setup.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintOptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame t = new PrintOptionFrame();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintOptionFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintOptionPanel pop = (PrintOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);
        
        // confirm default
        Assert.assertTrue(Setup.isSwitchListFormatSameAsManifest());
        Assert.assertFalse(pop.isDirty());
        
        // test save button
        JemmyUtil.enterClickAndLeave(pop.formatSwitchListCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        
        Assert.assertFalse(pop.isDirty());
        Assert.assertFalse(Setup.isSwitchListFormatSameAsManifest());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintOptionFrameTest.class);

}
