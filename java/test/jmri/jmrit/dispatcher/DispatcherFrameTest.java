package jmri.jmrit.dispatcher;

import jmri.Scale;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.TestHelper;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Swing jfcUnit tests for dispatcher options
 *
 * @author	Dave Duchamp
 */
public class DispatcherFrameTest extends jmri.util.SwingTestCase {

    public void testShowAndClose() throws Exception {
        new jmri.configurexml.ConfigXmlManager() {
        }; // replace manager

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = DispatcherFrame.instance();
        // Find new table window by name
        JmriJFrame dw = JmriJFrame.getFrame("Dispatcher");
        // test some options from file
        Assert.assertTrue("File AutoTurnouts", d.getAutoTurnouts());
        Assert.assertTrue("File HasOccupancyDetection", d.getHasOccupancyDetection());
        // set all options
        d.setLayoutEditor(null);
        d.setUseConnectivity(false);
        d.setTrainsFromRoster(true);
        d.setTrainsFromTrains(false);
        d.setTrainsFromUser(false);
        d.setAutoAllocate(false);
        d.setAutoTurnouts(false);
        d.setHasOccupancyDetection(false);
        d.setUseScaleMeters(false);
        d.setShortActiveTrainNames(false);
        d.setShortNameInBlock(true);
        d.setExtraColorForAllocated(false);
        d.setNameInAllocatedBlock(false);
        d.setScale(Scale.HO);
        // test all options
        Assert.assertNull("LayoutEditor", d.getLayoutEditor());
        Assert.assertFalse("UseConnectivity", d.getUseConnectivity());
        Assert.assertTrue("TrainsFromRoster", d.getTrainsFromRoster());
        Assert.assertFalse("TrainsFromTrains", d.getTrainsFromTrains());
        Assert.assertFalse("TrainsFromUser", d.getTrainsFromUser());
        Assert.assertFalse("AutoAllocate", d.getAutoAllocate());
        Assert.assertFalse("AutoTurnouts", d.getAutoTurnouts());
        Assert.assertFalse("HasOccupancyDetection", d.getHasOccupancyDetection());
        Assert.assertFalse("UseScaleMeters", d.getUseScaleMeters());
        Assert.assertFalse("ShortActiveTrainNames", d.getShortActiveTrainNames());
        Assert.assertTrue("ShortNameInBlock", d.getShortNameInBlock());
        Assert.assertFalse("ExtraColorForAllocated", d.getExtraColorForAllocated());
        Assert.assertFalse("NameInAllocatedBlock", d.getNameInAllocatedBlock());
        Assert.assertEquals("Scale", Scale.HO, d.getScale());
        // check changing some options
        d.setAutoTurnouts(true);
        Assert.assertTrue("New AutoTurnouts", d.getAutoTurnouts());
        d.setHasOccupancyDetection(true);
        Assert.assertTrue("New HasOccupancyDetection", d.getHasOccupancyDetection());
        d.setShortNameInBlock(false);
        Assert.assertFalse("New ShortNameInBlock", d.getShortNameInBlock());
        d.setScale(Scale.N);
        Assert.assertEquals("New Scale", Scale.N, d.getScale());

        // Ask to close Dispatcher window
        TestHelper.disposeWindow(dw, this);
    }

    // from here down is testing infrastructure
    public DispatcherFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DispatcherFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DispatcherFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
