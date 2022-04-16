package jmri.jmrit.operations.locations.divisions;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

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
@Timeout(10)
public class DivisionEditFrameTest extends OperationsTestCase {

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCTor() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        JUnitUtil.dispose(def);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveButton() {
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
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveButtonErrors() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        def.commentTextField.setText("divisionComment");
        def.divisionNameTextField.setText("newDivisionNameABCDEFGHIJKLMNOPQ");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("click testSaveButtonErrors Thread 1");
        t1.start();

        JemmyUtil.enterClickAndLeaveThreadSafe(def.saveDivisionButton);
        // new dialog window stating division name too long
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click in dialogue didn't happen");

        Assert.assertEquals("Name", "testName", division.getName());
        Assert.assertEquals("Comment", "", division.getComment());
        // now test saving a division that already exists
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        dm.newDivision("newDivisionName");
        def.divisionNameTextField.setText("newDivisionName");

        Thread t2 = new Thread(() -> {
             JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("click testSaveButtonErrors Thread 2");
        t2.start();
        
        JemmyUtil.enterClickAndLeaveThreadSafe(def.saveDivisionButton);
        // new dialog window stating division already exists
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Click in dialogue didn't happen");
        
        JUnitUtil.dispose(def);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddButton() {
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
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddButtonErrors() {
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
        JemmyUtil.enterClickAndLeaveThreadSafe(def.addDivisionButton);
        // new dialog window stating division already exists
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(def);
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        // test name too long
        def.divisionNameTextField.setText("newDivisionNameABCDEFGHIJKLMNOPQ");
        JemmyUtil.enterClickAndLeaveThreadSafe(def.addDivisionButton);
        // new dialog window stating division name too long
        JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(def);
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        JUnitUtil.dispose(def);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testDeleteButton() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        Assert.assertNotNull("exists", division);
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        
        // test no
        JemmyUtil.enterClickAndLeaveThreadSafe(def.deleteDivisionButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(def);        
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        
        // test yes
        JemmyUtil.enterClickAndLeaveThreadSafe(def.deleteDivisionButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(def);
        Assert.assertNull(dm.getDivisionByName("testDivisionName"));
        JUnitUtil.dispose(def);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testDeleteButtonError() {
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
