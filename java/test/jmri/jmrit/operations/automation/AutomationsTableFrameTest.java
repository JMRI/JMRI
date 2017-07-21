package jmri.jmrit.operations.automation;

<<<<<<< HEAD
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AutomationsTableFrameTest {

    @Test
    @Ignore("ignore constructor tests for Frames until test dependencies resovled")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationsTableFrame t = new AutomationsTableFrame();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationsTableFrameTest.class.getName());

=======
import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AutomationsTableFrameTest extends OperationsSwingTestCase {

    @Test
    public void testFrameCreation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() throws Exception {
        // apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
>>>>>>> JMRI/master
}
