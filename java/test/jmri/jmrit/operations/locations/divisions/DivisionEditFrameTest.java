package jmri.jmrit.operations.locations.divisions;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
@Timeout(10)
public class DivisionEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testSaveButton() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        JTextFieldOperator commentOperator = new JTextFieldOperator(jfo, new NameComponentChooser("CommentField"));
        commentOperator.setText("divisionComment");

        new JButtonOperator(jfo,Bundle.getMessage("SaveDivision")).doClick();
        Assert.assertEquals("Name", "testName", division.getName());
        Assert.assertEquals("Comment", "divisionComment", division.getComment());
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("EditDivision"));
        Assert.assertNotNull("exists", f);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        new JButtonOperator(jfo,Bundle.getMessage("SaveDivision")).doClick();
        jfo.waitClosed();

        f = JmriJFrame.getFrame(Bundle.getMessage("EditDivision"));
        Assert.assertNull("does not exist", f);
    }
    
    @Test
    public void testSaveButtonErrors() {
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);
        
        JTextFieldOperator commentOperator = new JTextFieldOperator(jfo, new NameComponentChooser("CommentField"));
        commentOperator.setText("divisionComment");

        JTextFieldOperator nameOperator = new JTextFieldOperator(jfo, new NameComponentChooser("DivisionNameField"));
        nameOperator.setText("newDivisionNameABCDEFGHIJKLMNOPQ");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("click testSaveButtonErrors Thread 1");
        t1.start();

        new JButtonOperator(jfo,Bundle.getMessage("SaveDivision")).doClick();

        // new dialog window stating division name too long
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click in dialogue didn't happen");

        Assert.assertEquals("Name", "testName", division.getName());
        Assert.assertEquals("Comment", "", division.getComment());
        // now test saving a division that already exists
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        dm.newDivision("newDivisionName");
        nameOperator.setText("newDivisionName");

        Thread t2 = new Thread(() -> {
             JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("save")}), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("click testSaveButtonErrors Thread 2");
        t2.start();
        
        new JButtonOperator(jfo,Bundle.getMessage("SaveDivision")).doClick();
        // new dialog window stating division already exists
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Click in dialogue didn't happen");
        
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testAddButton() {
        DivisionEditFrame def = new DivisionEditFrame(null);
        Assert.assertNotNull("exists", def);

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        JTextFieldOperator commentOperator = new JTextFieldOperator(jfo, new NameComponentChooser("CommentField"));
        commentOperator.setText("divisionComment");

        JTextFieldOperator nameOperator = new JTextFieldOperator(jfo, new NameComponentChooser("DivisionNameField"));
        nameOperator.setText("newDivisionName");

        new JButtonOperator(jfo,Bundle.getMessage("AddDivision")).doClick();

        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.getDivisionById("1");
        Assert.assertEquals("Comment", "divisionComment", division.getComment());
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testAddButtonErrors() {
        DivisionEditFrame def = new DivisionEditFrame(null);
        Assert.assertNotNull("exists", def);

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        JTextFieldOperator commentOperator = new JTextFieldOperator(jfo, new NameComponentChooser("CommentField"));
        commentOperator.setText("divisionComment");

        JTextFieldOperator nameOperator = new JTextFieldOperator(jfo, new NameComponentChooser("DivisionNameField"));
        nameOperator.setText("   ");

        new JButtonOperator(jfo,Bundle.getMessage("AddDivision")).doClick();

        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Assert.assertEquals("Number of divisions", 0, dm.getNumberOfdivisions());
        // now test adding a division that already exists
        dm.newDivision("newDivisionName");
        nameOperator.setText("newDivisionName");

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        });
        t1.setName("click add division Thread 1");
        t1.start();
        
        new JButtonOperator(jfo,Bundle.getMessage("AddDivision")).doClick();
        // new dialog window stating division already exists
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click in dialogue didn't happen");

        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        // test name too long
        nameOperator.setText("newDivisionNameABCDEFGHIJKLMNOPQ");

        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, MessageFormat.format(
                Bundle.getMessage("CanNotDivision"), new Object[]{Bundle.getMessage("add")}), Bundle.getMessage("ButtonOK"));
        });
        t2.setName("click add division Thread 2");
        t2.start();

        new JButtonOperator(jfo,Bundle.getMessage("AddDivision")).doClick();

        // new dialog window stating division name too long
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Click in dialogue didn't happen");

        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testDeleteButton() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        Assert.assertNotNull("exists", division);
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        // test no
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click delete division Thread 1");
        t1.start();
        new JButtonOperator(jfo,Bundle.getMessage("DeleteDivision")).doClick();
        // confirm delete dialog window should appear
        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click in dialogue 1 didn't happen");
        Assert.assertNotNull(dm.getDivisionByName("testDivisionName"));
        
        // test yes
        Thread t2 = new Thread(() -> {
            JemmyUtil.pressDialogButton(def, Bundle.getMessage("DeleteDivision"), Bundle.getMessage("ButtonYes"));
        });
        t2.setName("click delete division Thread 2");
        t2.start();
        new JButtonOperator(jfo,Bundle.getMessage("DeleteDivision")).doClick();
        // confirm delete dialog window should appear

        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        }, "Click in dialogue 2 didn't happen");
        Assert.assertNull(dm.getDivisionByName("testDivisionName"));
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testDeleteButtonError() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        DivisionEditFrame def = new DivisionEditFrame(division);
        // try to delete a division that doesn't exist

        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        JTextFieldOperator nameOperator = new JTextFieldOperator(jfo, new NameComponentChooser("DivisionNameField"));
        nameOperator.setText("newDivisionName");

        new JButtonOperator(jfo,Bundle.getMessage("DeleteDivision")).doClick();
        Assert.assertEquals("Number of divisions", 1, dm.getNumberOfdivisions());
        JUnitUtil.dispose(def);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        DivisionEditFrame f = new DivisionEditFrame(division);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

}
