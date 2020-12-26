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
public class EditManifestTextFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestTextFrame t = new EditManifestTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditManifestTextFrameReset() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestTextFrame f = new EditManifestTextFrame();
        f.initComponents();
        
        EditManifestTextPanel p = (EditManifestTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.manifestForTrainTextField.setText("Test text reset");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.resetButton);
        Assert.assertFalse("dirty3", p.isDirty());
        
        Assert.assertEquals("confirm", EditManifestTextPanel.rb.getString("ManifestForTrain"), p.manifestForTrainTextField.getText());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditManifestTextFrameSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestTextFrame f = new EditManifestTextFrame();
        f.initComponents();
        
        EditManifestTextPanel p = (EditManifestTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.manifestForTrainTextField.setText("Test text");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.saveButton);
        
        // confirm change by reloading panel
        EditManifestTextFrame f2 = new EditManifestTextFrame();
        f2.initComponents();
        
        EditManifestTextPanel p2 = (EditManifestTextPanel) f2.getContentPane();
        Assert.assertFalse("dirty3", p2.isDirty());
        Assert.assertEquals("confirm", "Test text", p2.manifestForTrainTextField.getText());
        
        JemmyUtil.enterClickAndLeave(p2.resetButton);
        Assert.assertTrue("dirty4", p2.isDirty());
        
        JemmyUtil.enterClickAndLeave(p2.saveButton);
        Assert.assertFalse("dirty5", p2.isDirty());

        // done
        JUnitUtil.dispose(f);
        JUnitUtil.dispose(f2);
    }

    // private final static Logger log = LoggerFactory.getLogger(EditManifestTextFrameTest.class);

}
