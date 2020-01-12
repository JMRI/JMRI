package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OptionsFileTest {

    @Test
    public void testCTor() {
        OptionsFile t = new OptionsFile();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetOptionsFileNameAndRead() {
        // The Dispatcher functionality is tightly coupled to the Dispatcher 
        // Frame.  As a result, we have can currently only test reading the 
        // options file by creating a DispatcherFrame object.  A future 
        // enhancement shold probably break this coupling.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // test some options from file
        Assert.assertTrue("File AutoTurnouts", d.getAutoTurnouts());
        Assert.assertTrue("File HasOccupancyDetection", d.getHasOccupancyDetection());
        // Find the window by name and close it.
        (new org.netbeans.jemmy.operators.JFrameOperator(Bundle.getMessage("TitleDispatcher"))).requestClose();
        JUnitUtil.dispose(d);

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OptionsFileTest.class);

}
