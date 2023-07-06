package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintMoreOptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintMoreOptionFrame t = new PrintMoreOptionFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPrintMoreOptionFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintMoreOptionFrame f = new PrintMoreOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintMoreOptionPanel pop = (PrintMoreOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);
        
        pop.tab1TextField.setText("11");
        pop.tab2TextField.setText("22");
        pop.tab3TextField.setText("33");
        
        Assert.assertTrue("Should be dirty", pop.isDirty());
        
        // test save button
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        
        Assert.assertEquals("tab 1", 11, Setup.getTab1Length());
        Assert.assertEquals("tab 1", 22, Setup.getTab2Length());
        Assert.assertEquals("tab 1", 33, Setup.getTab3Length());
        Assert.assertFalse("not dirty", pop.isDirty());

        // done
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintMoreOptionFrame f = new PrintMoreOptionFrame();
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
    // private final static Logger log = LoggerFactory.getLogger(PrintMoreOptionFrameTest.class);

}
