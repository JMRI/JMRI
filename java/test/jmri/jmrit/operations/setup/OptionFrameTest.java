package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the OptionFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 * @author Paul Bender Copyright (C) 2017
 */
public class OptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame t = new OptionFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testOptionFrameWrite() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame f = new OptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();
        OptionPanel p = (OptionPanel) f.getContentPane();

        // confirm defaults
        Assert.assertTrue("build normal", p.buildNormal.isSelected());
        Assert.assertFalse("build aggressive", p.buildAggressive.isSelected());
        Assert.assertFalse("local", p.localSpurCheckBox.isSelected());
        Assert.assertFalse("interchange", p.localInterchangeCheckBox.isSelected());
        Assert.assertFalse("yard", p.localYardCheckBox.isSelected());
        Assert.assertFalse("rfid", p.rfidCheckBox.isSelected());
        Assert.assertFalse("car logger", p.carLoggerCheckBox.isSelected());
        Assert.assertFalse("engine logger", p.engineLoggerCheckBox.isSelected());
        Assert.assertTrue("router", p.routerCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.buildAggressive);
        Assert.assertFalse("build normal", p.buildNormal.isSelected());
        Assert.assertTrue("build aggressive", p.buildAggressive.isSelected());

        JemmyUtil.enterClickAndLeave(p.localSpurCheckBox);
        Assert.assertTrue("local", p.localSpurCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.localInterchangeCheckBox);
        Assert.assertTrue("interchange", p.localInterchangeCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.localYardCheckBox);
        Assert.assertTrue("yard", p.localYardCheckBox.isSelected());

        //        JemmyUtil.enterClickAndLeave(p.rfidCheckBox);
        // use doClick() in case the checkbox isn't visible due to scrollbars.
        p.rfidCheckBox.doClick();
        Assert.assertTrue("rfid", p.rfidCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.carLoggerCheckBox);
        Assert.assertTrue("car logger", p.carLoggerCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.engineLoggerCheckBox);
        Assert.assertTrue("engine logger", p.engineLoggerCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.routerCheckBox);
        Assert.assertFalse("router", p.routerCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(p.saveButton);
        // done
        JUnitUtil.dispose(f);

        f = new OptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();
        p = (OptionPanel) f.getContentPane();

        Assert.assertFalse("build normal", p.buildNormal.isSelected());
        Assert.assertTrue("build aggressive", p.buildAggressive.isSelected());
        Assert.assertTrue("local", p.localSpurCheckBox.isSelected());
        Assert.assertTrue("interchange", p.localInterchangeCheckBox.isSelected());
        Assert.assertTrue("yard", p.localYardCheckBox.isSelected());
        Assert.assertTrue("rfid", p.rfidCheckBox.isSelected());
        Assert.assertTrue("car logger", p.carLoggerCheckBox.isSelected());
        Assert.assertTrue("engine logger", p.engineLoggerCheckBox.isSelected());
        Assert.assertFalse("router", p.routerCheckBox.isSelected());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame f = new OptionFrame();
        f.initComponents();
        OptionPanel p = (OptionPanel) f.getContentPane();

        // confirm defaults
        Assert.assertTrue("build normal", p.buildNormal.isSelected());
        Assert.assertFalse("not dirty 1", p.isDirty());
        
        JemmyUtil.enterClickAndLeave(p.buildAggressive);
        Assert.assertTrue("now dirty 1", p.isDirty());
        
        JemmyUtil.enterClickAndLeave(p.buildNormal);
        Assert.assertFalse("not dirty 2", p.isDirty());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame tf = new OptionFrame();
        tf.initComponents();

        JFrameOperator jfo = new JFrameOperator(tf.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        // confirm window appears
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleOptions"));
        Assert.assertNotNull("exists", f);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleOptions"));
        Assert.assertNotNull("exists", f);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        jfo.waitClosed();
        // confirm window is closed
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleOptions"));
        Assert.assertNull("does not exist", f);
    }

    // private final static Logger log = LoggerFactory.getLogger(OptionFrameTest.class);

}
