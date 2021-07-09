package jmri.jmrit.operations.locations.divisions;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class DivisionEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        def.commentTextField.setText("divisionComment");
        JemmyUtil.enterClickAndLeave(def.saveDivisionButton);
        Assert.assertEquals("Name", "testName", division.getName());
        Assert.assertEquals("Comment", "divisionComment", division.getComment());
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("EditDivision"));
        Assert.assertNotNull("exists", f);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        JemmyUtil.enterClickAndLeave(def.saveDivisionButton);
        f = JmriJFrame.getFrame(Bundle.getMessage("EditDivision"));
        Assert.assertNull("does not exist", f);
    }
    
    @Test
    public void testSaveButtonErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        def.commentTextField.setText("divisionComment");
        def.divisionNameTextField.setText("newDivisionNameABCDEFGHIJKLMNOPQ");
        JemmyUtil.enterClickAndLeave(def.saveDivisionButton);
        // new dialog window stating division name too long
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("Name", "testName", division.getName());
        Assert.assertEquals("Comment", "", division.getComment());
        // now test saving a division that already exists
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        dm.newDivision("newDivisionName");
        def.divisionNameTextField.setText("newDivisionName");
        JemmyUtil.enterClickAndLeave(def.saveDivisionButton);
        // new dialog window stating division already exists
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DivisionEditFrame def = new DivisionEditFrame(null);
        Assert.assertNotNull("exists", def);
        def.divisionNameTextField.setText("newDivisionName");
        def.commentTextField.setText("divisionComment");
        JemmyUtil.enterClickAndLeave(def.addDivisionButton);      
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.getDivisionById("1");
        Assert.assertEquals("Comment", "divisionComment", division.getComment());
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testAddButtonErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DivisionEditFrame def = new DivisionEditFrame(null);
        Assert.assertNotNull("exists", def);
        def.divisionNameTextField.setText("   ");
        def.commentTextField.setText("divisionComment");
        JemmyUtil.enterClickAndLeave(def.addDivisionButton);      
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Assert.assertEquals("Number of divisions", 0, dm.getNumberOfdivisions());
        // now test adding a division that already exists
        dm.newDivision("newDivisionName");
        def.divisionNameTextField.setText("newDivisionName");
        JemmyUtil.enterClickAndLeave(def.addDivisionButton);
        // new dialog window stating division already exists
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        // test name too long
        def.divisionNameTextField.setText("newDivisionNameABCDEFGHIJKLMNOPQ");
        JemmyUtil.enterClickAndLeave(def.addDivisionButton);
        // new dialog window stating division name too long
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        Assert.assertNotNull("exists", division);
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        JemmyUtil.enterClickAndLeave(def.deleteDivisionButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonNo"));
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        JemmyUtil.enterClickAndLeave(def.deleteDivisionButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonYes"));
        Assert.assertNull(dm.getDivisionByName("testDivisionName"));
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testDeleteButtonError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        DivisionEditFrame def = new DivisionEditFrame(division);
        // try to delete a division that doesn't exist
        def.divisionNameTextField.setText("newDivisionName");
        JemmyUtil.enterClickAndLeave(def.deleteDivisionButton);
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        JUnitUtil.dispose(def);
    }
}
