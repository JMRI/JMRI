package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

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
        jmri.util.JUnitUtil.waitFor(()
                -> {
            return automation.getCurrentAutomationItem() != null && "1c1".equals(automation.getCurrentAutomationItem().getId());
        },
                "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        Assert.assertEquals("1c1", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(3).getId());

        // test step button
        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(()
                -> {
            return automation.getCurrentAutomationItem() != null && "1c2".equals(automation.getCurrentAutomationItem().getId());
        },
                "The 2nd item: getId() was 1c2");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(()
                -> {
            return automation.getCurrentAutomationItem() != null && "1c3".equals(automation.getCurrentAutomationItem().getId());
        },
                "The 3rd item: getId() was 1c3");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        // back to the start
        JemmyUtil.enterClickAndLeave(f.stepActionButton);
        jmri.util.JUnitUtil.waitFor(()
                -> {
            return automation.getCurrentAutomationItem() != null && "1c1".equals(automation.getCurrentAutomationItem().getId());
        },
                "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Automation automation = InstanceManager.getDefault(AutomationManager.class).newAutomation("Automation Table Frame Name");
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
        JemmyUtil.enterClickAndLeave(f.deleteAutomationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonYes"));
        Assert.assertEquals(0, InstanceManager.getDefault(AutomationManager.class).getSize());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameCreationWithAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Automation automation = InstanceManager.getDefault(AutomationManager.class).newAutomation("Automation Table Frame Name");
        automation.setComment("Gui Test Automation Table Frame Comment");
        automation.addItem();

        AutomationTableFrame f = new AutomationTableFrame(automation);
        Assert.assertNotNull("test creation", f);

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
        JemmyUtil.enterClickAndLeave(f.deleteAutomationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteAutomation?"), Bundle.getMessage("ButtonNo"));
        Assert.assertEquals(1, InstanceManager.getDefault(AutomationManager.class).getSize());

        JUnitUtil.dispose(f);
    }
}
