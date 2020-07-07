package jmri.jmrit.operations.trains.excel;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetupExcelProgramSwitchListFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramSwitchListFrame t = new SetupExcelProgramSwitchListFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrameAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramSwitchListFrame f = new SetupExcelProgramSwitchListFrame();
        Assert.assertNotNull("exists",f);
        
        f.initComponents();
        Assert.assertTrue(f.isShowing());
        
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        // abort find file
        JemmyUtil.pressDialogButton(Bundle.getMessage("FindDesiredExcelFile"), "Cancel");
        
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testFrameTestButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramSwitchListFrame f = new SetupExcelProgramSwitchListFrame();
        Assert.assertNotNull("exists",f);
        
        f.initComponents();
        Assert.assertTrue(f.isShowing());
        
        JemmyUtil.enterClickAndLeave(f.testButton);
        
        // kill dialog
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ManifestCreatorNotFound"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testFrameSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramSwitchListFrame f = new SetupExcelProgramSwitchListFrame();
        Assert.assertNotNull("exists",f);
        
        f.initComponents();
        Assert.assertTrue(f.isShowing());
        
        f.fileNameTextField.setText("Test File Name");
        //toggle create manifest checkbox
        JemmyUtil.enterClickAndLeave(f.generateCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        // confirm save
        Assert.assertTrue(Setup.isGenerateCsvSwitchListEnabled());
        Assert.assertEquals("", "Test File Name", InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName());
        
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramSwitchListFrameTest.class);

}
