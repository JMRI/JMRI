package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OptionsMenuTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        OptionsMenu t = new OptionsMenu(d);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("Stopping Speed Name", "Restricted", d.getStoppingSpeedName());
        Assert.assertEquals("Use Connectivity Option", false, d.getUseConnectivity());
        Assert.assertEquals("Trains From Roster", true, d.getTrainsFromRoster());
        Assert.assertEquals("Trains From Trains", false, d.getTrainsFromTrains());
        Assert.assertEquals("Trains From User", false, d.getTrainsFromUser());
        Assert.assertEquals("AutoAllocate", false, d.getAutoAllocate());
        Assert.assertEquals("Auto Turnouts", true, d.getAutoTurnouts());
        Assert.assertEquals("Occupancy detection", true, d.getHasOccupancyDetection());
        Assert.assertEquals("Short Active Train Name", false, d.getShortActiveTrainNames());
        Assert.assertEquals("Short Train Name in Block", true, d.getShortNameInBlock());
        Assert.assertEquals("Extra Colour for allocate", false, d.getExtraColorForAllocated());
        Assert.assertEquals("Name In Allocated Block", false, d.getNameInAllocatedBlock());
        Assert.assertEquals("Layout Scale", "G", d.getScale().getScaleName());
        Assert.assertEquals("Use Metres", true, d.getUseScaleMeters());
        JUnitUtil.dispose(d);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OptionsMenuTest.class);

}
