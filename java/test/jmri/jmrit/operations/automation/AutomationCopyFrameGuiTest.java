package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomationCopyFrameGuiTest extends OperationsSwingTestCase {

    public void testFrameCreation() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationCopyFrame f = new AutomationCopyFrame(null);
        Assert.assertNotNull("test creation", f);
        
        f.setVisible(true);
        
        getHelper().enterClickAndLeave(new MouseEventData(this, f.copyButton));
        // dialog window requesting name for automation should appear
        pressDialogButton(f, "OK");
        
        // enter a name for the automation
        f.automationNameTextField.setText("Name of new automation");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.copyButton));
        // dialog window requesting automation to copy should appear
        pressDialogButton(f, "OK");
          
        f.dispose();
    }
    
    public void testFrameCreationWithAutomation() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertEquals("Number of automations", 0, manager.getSize());
        
        // create an automation to copy
        Automation automation = manager.newAutomation("Test automation to copy");
        Assert.assertNotNull(automation);
        Assert.assertEquals("Number of automations", 1, manager.getSize());
        
        automation.setComment("Comment for automation to copy");

        AutomationCopyFrame copyFrame = new AutomationCopyFrame(null);
        Assert.assertNotNull("test creation", copyFrame);
        
        copyFrame.setVisible(true);
        
        getHelper().enterClickAndLeave(new MouseEventData(this, copyFrame.copyButton));
        // dialog window requesting name for automation should appear
        pressDialogButton(copyFrame, "OK");
        
        // enter a name for the automation
        copyFrame.automationNameTextField.setText("Name of new automation 2");
        getHelper().enterClickAndLeave(new MouseEventData(this, copyFrame.copyButton));
        
        // dialog window requesting automation to copy should appear
        pressDialogButton(copyFrame, "OK");
 
        // still only one automation
        Assert.assertEquals("Number of automations", 1, manager.getSize());
        
        //now select the automation to copy
        copyFrame.automationBox.setSelectedIndex(1);
        getHelper().enterClickAndLeave(new MouseEventData(this, copyFrame.copyButton));
        
        Automation copiedAutomation = manager.getAutomationByName("Name of new automation 2");
        Assert.assertNotNull(copiedAutomation);
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", copiedAutomation.getComment());
        
        // There should be an edit automation frame
        // confirm that the add automation frame isn't available
        AutomationTableFrame editAutomationFrame = (AutomationTableFrame) JmriJFrame.getFrame("Edit Automation");
        Assert.assertNotNull(editAutomationFrame);
        
        Assert.assertEquals("confirm name is correct", "Name of new automation 2", editAutomationFrame.automationNameTextField.getText());
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", editAutomationFrame.commentTextField.getText());
        
        editAutomationFrame.dispose();      
        copyFrame.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public AutomationCopyFrameGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",
                AutomationCopyFrameGuiTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AutomationCopyFrameGuiTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        // apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
