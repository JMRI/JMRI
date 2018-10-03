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

public class AutomationsTableFrameTest extends OperationsSwingTestCase {

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);

        // confirm that the add automation frame isn't available
        JmriJFrame addAutomationFrame = JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationAdd"));
        Assert.assertNull(addAutomationFrame);

        // now create the add automation frame
        f.addButton.doClick();
        // the following fails on a 13" laptop
        //JemmyUtil.enterClickAndLeave(f.addButton);
        addAutomationFrame = JmriJFrame.getFrame(Bundle.getMessage("TitleAutomationAdd"));
        Assert.assertNotNull(addAutomationFrame);

        JUnitUtil.dispose(addAutomationFrame);
        JUnitUtil.dispose(f);
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() {
        // jmri.util.JUnitUtil.tearDown();

        super.tearDown();
    }
}
