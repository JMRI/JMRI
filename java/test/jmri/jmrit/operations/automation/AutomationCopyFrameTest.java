package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class AutomationCopyFrameTest extends OperationsTestCase {

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationCopyFrame f = new AutomationCopyFrame(null);
        Assert.assertNotNull("test creation", f);

        f.setVisible(true);

        JemmyUtil.enterClickAndLeave(f.copyButton);
        // dialog window requesting name for automation should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotCopyAutomation"), Bundle.getMessage("ButtonOK"));

        // enter a name for the automation
        f.automationNameTextField.setText("Name of new automation");
        JemmyUtil.enterClickAndLeave(f.copyButton);
        // dialog window requesting automation to copy should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotCopyAutomation"), Bundle.getMessage("ButtonOK"));

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

        JemmyUtil.enterClickAndLeave(copyFrame.copyButton);
        // dialog window requesting name for automation should appear
        JemmyUtil.pressDialogButton(copyFrame, Bundle.getMessage("CanNotCopyAutomation"), Bundle.getMessage("ButtonOK"));

        // enter a name for the automation
        copyFrame.automationNameTextField.setText("Name of new automation 2");
        JemmyUtil.enterClickAndLeave(copyFrame.copyButton);

        // dialog window requesting automation to copy should appear
        JemmyUtil.pressDialogButton(copyFrame, Bundle.getMessage("CanNotCopyAutomation"), Bundle.getMessage("ButtonOK"));

        // still only one automation
        Assert.assertEquals("Number of automations", 1, manager.getSize());

        //now select the automation to copy
        copyFrame.automationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(copyFrame.copyButton);

        Automation copiedAutomation = manager.getAutomationByName("Name of new automation 2");
        Assert.assertNotNull(copiedAutomation);
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", copiedAutomation.getComment());

        // There should be an edit automation frame
        // confirm that the add automation frame isn't available
        AutomationTableFrame editAutomationFrame = (AutomationTableFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationEdit"));
        Assert.assertNotNull(editAutomationFrame);

        Assert.assertEquals("confirm name is correct", "Name of new automation 2", editAutomationFrame.automationNameTextField.getText());
        Assert.assertEquals("confirm comment is correct", "Comment for automation to copy", editAutomationFrame.commentTextField.getText());

        JUnitUtil.dispose(editAutomationFrame);
        JUnitUtil.dispose(copyFrame);
    }
}
