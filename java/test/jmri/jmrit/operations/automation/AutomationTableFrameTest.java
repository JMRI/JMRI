package jmri.jmrit.operations.automation;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

public class AutomationTableFrameTest extends OperationsTestCase {

    @Test
    public void testNewFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationTableFrame f = new AutomationTableFrame(null);
        Assert.assertNotNull("test creation", f);
        f.automationNameTextField.setText("New Automation Test Name");
        f.commentTextField.setText("New Automation Test Comment");

        // Confirm enable state of buttons
        Assert.assertTrue(f.addAutomationButton.isEnabled());
        Assert.assertFalse(f.saveAutomationButton.isEnabled());
        Assert.assertFalse(f.deleteAutomationButton.isEnabled());
        Assert.assertFalse(f.addActionButton.isEnabled());
        Assert.assertFalse(f.stepActionButton.isEnabled());
        Assert.assertFalse(f.runActionButton.isEnabled());
        Assert.assertFalse(f.resumeActionButton.isEnabled());

        JemmyUtil.enterClickAndLeave(f.addAutomationButton);

        Assert.assertEquals("Number of automations", 1, manager.getSize());
        Automation automation = manager.getAutomationByName("New Automation Test Name");
        Assert.assertNotNull(automation);

        Assert.assertEquals("New Automation Test Comment", automation.getComment());
        Assert.assertEquals("No actions yet", 0, automation.getSize());

        // Confirm enable state of buttons
        Assert.assertFalse(f.addAutomationButton.isEnabled());
        Assert.assertTrue(f.saveAutomationButton.isEnabled());
        Assert.assertTrue(f.deleteAutomationButton.isEnabled());
        Assert.assertTrue(f.addActionButton.isEnabled());
        Assert.assertFalse(f.stepActionButton.isEnabled());
        Assert.assertFalse(f.runActionButton.isEnabled());
        Assert.assertFalse(f.resumeActionButton.isEnabled());

        // Test add action button
        JemmyUtil.enterClickAndLeave(f.addActionButton);
        Assert.assertEquals(1, automation.getSize());
        Assert.assertNotNull("The first item", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        // Confirm enable state of buttons
        Assert.assertFalse(f.addAutomationButton.isEnabled());
        Assert.assertTrue(f.saveAutomationButton.isEnabled());
        Assert.assertTrue(f.deleteAutomationButton.isEnabled());
        Assert.assertTrue(f.addActionButton.isEnabled());
        Assert.assertTrue(f.stepActionButton.isEnabled());
        Assert.assertTrue(f.runActionButton.isEnabled());
        Assert.assertTrue(f.resumeActionButton.isEnabled());

        // add two more actions
        JemmyUtil.enterClickAndLeave(f.addActionButton);
        JemmyUtil.enterClickAndLeave(f.addActionButton);
        Assert.assertEquals(3, automation.getSize());
        jmri.util.JUnitUtil.waitFor(() -> {
            return automation.getCurrentAutomationItem() != null &&
                    "1c1".equals(automation.getCurrentAutomationItem().getId());
        }, "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        Assert.assertEquals("1c1", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(3).getId());

        // test step button
        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(() -> {
            return automation.getCurrentAutomationItem() != null &&
                    "1c2".equals(automation.getCurrentAutomationItem().getId());
        }, "The 2nd item: getId() was 1c2");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(() -> {
            return automation.getCurrentAutomationItem() != null &&
                    "1c3".equals(automation.getCurrentAutomationItem().getId());
        }, "The 3rd item: getId() was 1c3");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        // back to the start
        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(() -> {
            return automation.getCurrentAutomationItem() != null &&
                    "1c1".equals(automation.getCurrentAutomationItem().getId());
        }, "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Automation automation = InstanceManager.getDefault(AutomationManager.class)
                .newAutomation("Automation Table Frame Name");
        automation.setComment("Gui Test Automation Table Frame Comment");

        AutomationTableFrame f = new AutomationTableFrame(automation);
        Assert.assertNotNull("test creation", f);

        Assert.assertEquals("Automation Table Frame Name", f.automationNameTextField.getText());
        Assert.assertEquals("Gui Test Automation Table Frame Comment", f.commentTextField.getText());

        // Confirm enable state of buttons
        Assert.assertFalse(f.addAutomationButton.isEnabled());
        Assert.assertTrue(f.saveAutomationButton.isEnabled());
        Assert.assertTrue(f.deleteAutomationButton.isEnabled());
        Assert.assertTrue(f.addActionButton.isEnabled());
        Assert.assertFalse(f.stepActionButton.isEnabled());
        Assert.assertFalse(f.runActionButton.isEnabled());
        Assert.assertFalse(f.resumeActionButton.isEnabled());

        // test add button, add item at top of table
        JemmyUtil.enterClickAndLeave(f.addActionAtTopRadioButton);
        JemmyUtil.enterClickAndLeave(f.addActionButton);

        Assert.assertNotNull("The first item", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(1, automation.getSize());

        JemmyUtil.enterClickAndLeave(f.addActionButton);

        Assert.assertNotNull("The first item is now in second place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(2, automation.getSize());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(2).getId());

        JemmyUtil.enterClickAndLeave(f.addActionButton);

        Assert.assertNotNull("The first item is now in 3rd place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(3, automation.getSize());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(3).getId());

        // test add action in middle radio button
        JemmyUtil.enterClickAndLeave(f.addActionAtMiddleRadioButton);
        JemmyUtil.enterClickAndLeave(f.addActionButton);

        Assert.assertNotNull("The first item is now in second place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(4, automation.getSize());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c4", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(3).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(4).getId());

        // test delete button
        Assert.assertEquals(1, InstanceManager.getDefault(AutomationManager.class).getSize());
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteAutomationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals(0, InstanceManager.getDefault(AutomationManager.class).getSize());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameCreationWithAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        Assert.assertEquals("Automation Table Frame Name", f.automationNameTextField.getText());
        Assert.assertEquals("Gui Test Automation Table Frame Comment", f.commentTextField.getText());

        // Confirm enable state of buttons
        Assert.assertFalse(f.addAutomationButton.isEnabled());
        Assert.assertTrue(f.saveAutomationButton.isEnabled());
        Assert.assertTrue(f.deleteAutomationButton.isEnabled());
        Assert.assertTrue(f.addActionButton.isEnabled());
        Assert.assertTrue(f.stepActionButton.isEnabled());
        Assert.assertTrue(f.runActionButton.isEnabled());
        Assert.assertTrue(f.resumeActionButton.isEnabled());

        // test delete button
        Assert.assertEquals(1, InstanceManager.getDefault(AutomationManager.class).getSize());
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteAutomationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals(1, InstanceManager.getDefault(AutomationManager.class).getSize());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteAutomationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals(0, InstanceManager.getDefault(AutomationManager.class).getSize());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDownButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // confirm item order
        Assert.assertEquals("1st item", "1c1", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c2", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("3rd item", "1c3", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Id"))));

        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Down")));

        // confirm new item order
        Assert.assertEquals("1st item", "1c2", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c1", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("3rd item", "1c3", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testUpButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // confirm item order
        Assert.assertEquals("1st item", "1c1", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c2", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("3rd item", "1c3", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Id"))));

        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Up")));

        // confirm new item order
        Assert.assertEquals("1st item", "1c2", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c3", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("3rd item", "1c1", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Id"))));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // confirm item order
        Assert.assertEquals("1st item", "1c1", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c2", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("3rd item", "1c3", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Table size", 3, automation.getSize());

        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonDelete")));

        // confirm new item order
        Assert.assertEquals("1st item", "1c1", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("2nd item", "1c3", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Id"))));
        Assert.assertEquals("Table size", 2, automation.getSize());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testHAIF() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // confirm HAIF
        Assert.assertTrue(automation.getItemsBySequenceList().get(1).isHaltFailureEnabled());
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("HaltIfActionFails")));
        Assert.assertFalse(automation.getItemsBySequenceList().get(1).isHaltFailureEnabled());
        
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCurrentPointer() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // confirm pointer location
        Assert.assertEquals("1st item", AutomationTableModel.POINTER,
                tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("2nd item", "", tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("3rd item", "", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Current"))));

        tbl.clickForEdit(1, tbl.findColumn(Bundle.getMessage("Current")));
        JemmyUtil.enterClickAndLeave(f.saveAutomationButton);

        // confirm new pointer location
        Assert.assertEquals("1st item", "", tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("2nd item", AutomationTableModel.POINTER,
                tbl.getValueAt(1, tbl.findColumn(Bundle.getMessage("Current"))));
        Assert.assertEquals("3rd item", "", tbl.getValueAt(2, tbl.findColumn(Bundle.getMessage("Current"))));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        setupTest();

        // test entering a message
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("Message"));
        // find comment window by name
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("Message"));
        JTextAreaOperator jtao = new JTextAreaOperator(jdo);
        jtao.setText("Happy Days");
        JButtonOperator jboOK = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
        jboOK.doClick();
        Assert.assertEquals("confirm message", "Happy Days", automation.getItemsBySequenceList().get(0).getMessage());

        // test canceling a message
        JemmyUtil.clickOnCellThreadSafe(tbl, 2, Bundle.getMessage("Message"));
        // find comment window by name
        jdo = new JDialogOperator(Bundle.getMessage("Message"));
        jtao = new JTextAreaOperator(jdo);
        jtao.setText("Happy Days");
        JButtonOperator jboCancel = new JButtonOperator(jdo, Bundle.getMessage("ButtonCancel"));
        jboCancel.doClick();
        Assert.assertEquals("confirm message", "", automation.getItemsBySequenceList().get(2).getMessage());

        // test default messages
        JemmyUtil.clickOnCellThreadSafe(tbl, 1, Bundle.getMessage("Message"));
        // find comment window by name
        jdo = new JDialogOperator(Bundle.getMessage("Message"));
        JButtonOperator jboDefault = new JButtonOperator(jdo, Bundle.getMessage("DefaultMessages"));
        jboDefault.doClick();
        jboOK = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
        jboOK.doClick();
        Assert.assertEquals("confirm default message", Bundle.getMessage("DefaultMessageOk"),
                automation.getItemsBySequenceList().get(1).getMessage());
        Assert.assertEquals("confirm fail default message", Bundle.getMessage("DefaultMessageFail"),
                automation.getItemsBySequenceList().get(1).getMessageFail());

        JUnitUtil.dispose(f);
    }

    AutomationTableFrame f;
    Automation automation;
    JTableOperator tbl;

    private void setupTest() {
        automation = InstanceManager.getDefault(AutomationManager.class).newAutomation("Automation Table Frame Name");
        automation.setComment("Gui Test Automation Table Frame Comment");
        automation.addItem(); // three items
        AutomationItem ai2 = automation.addItem();
        automation.addItem();
        
        BuildTrainAction action = new BuildTrainAction();
        ai2.setAction(action);

        f = new AutomationTableFrame(automation);
        Assert.assertNotNull("test creation", f);
        f.setSize(new Dimension(1400, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        tbl = new JTableOperator(jfo);
    }
}
