package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomationsTableFrameGuiTest extends OperationsSwingTestCase {

    public void testFrameCreation() {
        AutomationManager manager = AutomationManager.instance();
        Assert.assertEquals("Number of automations", 0, manager.getSize());

        AutomationsTableFrame f = new AutomationsTableFrame();
        Assert.assertNotNull("test creation", f);
        
        // confirm that the add automation frame isn't available
        JmriJFrame addAutomationFrame = JmriJFrame.getFrame("Add Automation");
        Assert.assertNull(addAutomationFrame);
        
        // now create the add automation frame
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));       
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
        junit.swingui.TestRunner.main(testCaseName);
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
