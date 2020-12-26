package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditSwitchListTextFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditSwitchListTextFrame t = new EditSwitchListTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditSwitchListTextFrameReset() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditSwitchListTextFrame f = new EditSwitchListTextFrame();
        f.initComponents();
        
        EditSwitchListTextPanel p = (EditSwitchListTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.switchListForTextField.setText("Test text reset");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.resetButton);
        Assert.assertFalse("dirty3", p.isDirty());
        
        Assert.assertEquals("confirm", EditSwitchListTextPanel.rb.getString("SwitchListFor"), p.switchListForTextField.getText());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditSwitchListTextFrameSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditSwitchListTextFrame f = new EditSwitchListTextFrame();
        f.initComponents();
        
        EditSwitchListTextPanel p = (EditSwitchListTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.switchListForTextField.setText("Test text");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.saveButton);
        
        // confirm change by reloading panel
        EditSwitchListTextFrame f2 = new EditSwitchListTextFrame();
        f2.initComponents();
        
        EditSwitchListTextPanel p2 = (EditSwitchListTextPanel) f2.getContentPane();
        Assert.assertFalse("dirty3", p2.isDirty());
        Assert.assertEquals("confirm", "Test text", p2.switchListForTextField.getText());
        
        JemmyUtil.enterClickAndLeave(p2.resetButton);
        Assert.assertTrue("dirty4", p2.isDirty());
        
        JemmyUtil.enterClickAndLeave(p2.saveButton);
        Assert.assertFalse("dirty5", p2.isDirty());

        // done
        JUnitUtil.dispose(f);
        JUnitUtil.dispose(f2);
    }

    // private final static Logger log = LoggerFactory.getLogger(EditSwitchListTextFrameTest.class);

}
