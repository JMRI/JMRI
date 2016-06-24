package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomationTableFrameGuiTest extends OperationsSwingTestCase {

    public void testNewFrameCreation() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationTableFrame f = new AutomationTableFrame(null);
        Assert.assertNotNull("test creation", f);
        f.automationNameTextField.setText("New Automation Test Name");
        f.commentTextField.setText("New Automation Test Comment");
        flushAWT(); // to allow time to take effect

        // Confirm enable state of buttons
        Assert.assertTrue(f.addAutomationButton.isEnabled());
        Assert.assertFalse(f.saveAutomationButton.isEnabled());
        Assert.assertFalse(f.deleteAutomationButton.isEnabled());
        Assert.assertFalse(f.addActionButton.isEnabled());
        Assert.assertFalse(f.stepActionButton.isEnabled());
        Assert.assertFalse(f.runActionButton.isEnabled());
        Assert.assertFalse(f.resumeActionButton.isEnabled());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.addAutomationButton));

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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));
        Assert.assertEquals(3, automation.getSize());
        jmri.util.JUnitUtil.waitFor(() -> 
            { return automation.getCurrentAutomationItem() != null && "1c1".equals(automation.getCurrentAutomationItem().getId()); },
            "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        
        Assert.assertEquals("1c1", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(3).getId());

        // test step button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.stepActionButton));
        jmri.util.JUnitUtil.waitFor(() -> 
            { return automation.getCurrentAutomationItem() != null && "1c2".equals(automation.getCurrentAutomationItem().getId()); },
            "The 2nd item: getId() was 1c2");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.stepActionButton));
        jmri.util.JUnitUtil.waitFor(() -> 
            { return automation.getCurrentAutomationItem() != null && "1c3".equals(automation.getCurrentAutomationItem().getId()); },
            "The 3rd item: getId() was 1c3");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        // back to the start
        getHelper().enterClickAndLeave(new MouseEventData(this, f.stepActionButton));
        jmri.util.JUnitUtil.waitFor(() -> 
        { return automation.getCurrentAutomationItem() != null && "1c1".equals(automation.getCurrentAutomationItem().getId()); },
        "The 1st item: getId() was 1c1");
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());

        f.dispose();
    }

    public void testFrameCreation() {
        Automation automation = AutomationManager.instance().newAutomation("Automation Table Frame Name");
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionAtTopRadioButton));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));

        Assert.assertNotNull("The first item", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(1, automation.getSize());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));

        Assert.assertNotNull("The first item is now in second place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(2, automation.getSize());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(2).getId());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));

        Assert.assertNotNull("The first item is now in 3rd place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(3, automation.getSize());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(3).getId());
        
        // test add action in middle radio button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionAtMiddleRadioButton));
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addActionButton));
        
        Assert.assertNotNull("The first item is now in second place", automation.getCurrentAutomationItem());
        Assert.assertEquals("1c1", automation.getCurrentAutomationItem().getId());
        Assert.assertEquals("Do Nothing", automation.getCurrentAutomationItem().getActionName());
        Assert.assertEquals(4, automation.getSize());
        Assert.assertEquals("1c3", automation.getItemBySequenceId(1).getId());
        Assert.assertEquals("1c4", automation.getItemBySequenceId(2).getId());
        Assert.assertEquals("1c2", automation.getItemBySequenceId(3).getId());
        Assert.assertEquals("1c1", automation.getItemBySequenceId(4).getId());

        // test delete button
        Assert.assertEquals(1, AutomationManager.instance().getSize());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteAutomationButton));
        // confirm delete dialog window should appear
        pressDialogButton(f, "Yes");
        Assert.assertEquals(0, AutomationManager.instance().getSize());

        f.dispose();
    }

    public void testFrameCreationWithAction() {
        Automation automation = AutomationManager.instance().newAutomation("Automation Table Frame Name");
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
        Assert.assertEquals(1, AutomationManager.instance().getSize());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteAutomationButton));
        // confirm delete dialog window should appear
        pressDialogButton(f, "No");
        Assert.assertEquals(1, AutomationManager.instance().getSize());

        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public AutomationTableFrameGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",
                AutomationTableFrameGuiTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AutomationTableFrameGuiTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        // apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
