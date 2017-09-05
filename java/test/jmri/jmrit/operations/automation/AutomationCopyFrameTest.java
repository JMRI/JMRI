package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class AutomationCopyFrameTest extends OperationsSwingTestCase {

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationCopyFrame f = new AutomationCopyFrame(null);
        Assert.assertNotNull("test creation", f);

        f.setVisible(true);

        enterClickAndLeave(f.copyButton);
        // dialog window requesting name for automation should appear
        pressDialogButton(f, Bundle.getMessage("CanNotCopyAutomation"), "OK");

        // enter a name for the automation
        f.automationNameTextField.setText("Name of new automation");
        enterClickAndLeave(f.copyButton);
        // dialog window requesting automation to copy should appear
        pressDialogButton(f, Bundle.getMessage("CanNotCopyAutomation"), "OK");

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameCreationWithAutomation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        // create an automation to copy
        Automation automation = manager.newAutomation("Test automation to copy");
        Assert.assertNotNull(automation);
        Assert.assertEquals("Number of automations", 1, manager.getSize());

        automation.setComment("Comment for automation to copy");

        AutomationCopyFrame copyFrame = new AutomationCopyFrame(null);
        Assert.assertNotNull("test creation", copyFrame);

        copyFrame.setVisible(true);

        enterClickAndLeave(copyFrame.copyButton);
        // dialog window requesting name for automation should appear
        pressDialogButton(copyFrame, Bundle.getMessage("CanNotCopyAutomation"), "OK");

        // enter a name for the automation
        copyFrame.automationNameTextField.setText("Name of new automation 2");
        enterClickAndLeave(copyFrame.copyButton);

        // dialog window requesting automation to copy should appear
        pressDialogButton(copyFrame, Bundle.getMessage("CanNotCopyAutomation"), "OK");

        // still only one automation
        Assert.assertEquals("Number of automations", 1, manager.getSize());

        //now select the automation to copy
        copyFrame.automationBox.setSelectedIndex(1);
        enterClickAndLeave(copyFrame.copyButton);

        Automation copiedAutomation = manager.getAutomationByName("Name of new automation 2");
        Assert.assertNotNull(copiedAutomation);
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", copiedAutomation.getComment());

        // There should be an edit automation frame
        // confirm that the add automation frame isn't available
        AutomationTableFrame editAutomationFrame = (AutomationTableFrame) JmriJFrame.getFrame("Edit Automation");
        Assert.assertNotNull(editAutomationFrame);

        Assert.assertEquals("confirm name is correct", "Name of new automation 2", editAutomationFrame.automationNameTextField.getText());
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", editAutomationFrame.commentTextField.getText());

        JUnitUtil.dispose(editAutomationFrame);
        JUnitUtil.dispose(copyFrame);
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
