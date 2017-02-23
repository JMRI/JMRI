package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

public class AutomationsTableFrameGuiTest extends OperationsSwingTestCase {

    public void testFrameCreation() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        AutomationManager manager = AutomationManager.instance();
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);

        // confirm that the add automation frame isn't available
        JmriJFrame addAutomationFrame = JmriJFrame.getFrame("Add Automation");
        Assert.assertNull(addAutomationFrame);

        // now create the add automation frame
        f.addButton.doClick();
        // the following fails on a 13" laptop
        //enterClickAndLeave(f.addButton);
        addAutomationFrame = JmriJFrame.getFrame("Add Automation");
        Assert.assertNotNull(addAutomationFrame);

        addAutomationFrame.dispose();
        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public AutomationsTableFrameGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",
            AutomationsTableFrameGuiTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AutomationsTableFrameGuiTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        // apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
