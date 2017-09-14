package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.ConfigureManager;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class LearnWarrantTest extends jmri.util.SwingTestCase {

    private OBlockManager _OBlockMgr;
//    PortalManager _portalMgr;
    private SensorManager _sensorMgr;
//    TurnoutManager _turnoutMgr;

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    public void testLearnWarrant() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/LearnWarrantTest.xml");
        /* This layout designed so that the block and path will define a unique
         * route from origin to destination.  i.e. the review and select route
         * never needs to be displayed.  All possible EastBound Routes:
         * OB1/Main - OB5/main (default)    Route {OB1, OB2, OB3, OB4, OB5}
         * OB1/WestSiding - OB5/Main        Route {OB1, OB6, OB3, OB4, OB5}
         * OB1/Main - OB5/EastSiding        Route {OB1, OB2, OB3, OB7, OB5}
         * OB1/WestSiding - OB5/EastSiding  Route {OB1, OB6, OB3, OB7, OB5}
         * OB1/Main - OB7/EastSiding        Route {OB1, OB2, OB3, OB7}
         * OB1/WestSiding - OB7/EastSiding  Route {OB1, OB6, OB3, OB7}
         * OB1/Main - OB6/EastSiding        Route {OB1, OB6}
         * OB1/WestSiding - OB6/EastSiding  Route {OB1, OB6}
        */
        InstanceManager.getDefault(ConfigureManager.class).load(f);
//        ControlPanelEditor panel = (ControlPanelEditor)null;
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Warrant w = new Warrant("IW00", "Learning");
        WarrantFrame frame = new WarrantFrame(w, true);

        frame._origin.blockBox.setText("OB1");
        frame._destination.blockBox.setText("OB5");
        String[] route = {"OB1", "OB2", "OB3", "OB4", "OB5"};

        pressButton(frame, Bundle.getMessage("Calculate"));
        flushAWT();
        JUnitUtil.waitFor(() -> {
            return (frame.getOrders() != null);
        }, "Found orders");
        List<BlockOrder> orders = frame.getOrders();
        Assert.assertEquals("5 BlockOrders", 5, orders.size());

        frame.setTrainInfo("99");
        JUnitUtil.waitFor(() -> {
            return (frame._speedUtil.getDccAddress() != null);
        }, "Found address");
        jmri.DccLocoAddress address = frame._speedUtil.getDccAddress();
        Assert.assertEquals("address=99", 99, address.getNumber());

        pressButton(frame, Bundle.getMessage("Start"));
        // dismiss warning "starting block not occupied
        confirmJOptionPane(frame, Bundle.getMessage("WarningTitle"), "OK");

        // occupy starting block
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        sensor.setState(Sensor.ACTIVE);
        pressButton(frame, Bundle.getMessage("Start"));

        JUnitUtil.waitFor(() -> {
            return (frame._learnThrottle != null);
        }, "Found throttle");
        Assert.assertNotNull("Throttle not found", frame._learnThrottle.getThrottle());

        sensor = recordtimes(route, frame._learnThrottle.getThrottle());

        pressButton(frame, Bundle.getMessage("Stop"));

        // change address and run
        frame.setTrainInfo("111");
        JUnitUtil.waitFor(() -> {
            return (frame._speedUtil.getDccAddress() != null);
        }, "Found address");
        address = frame._speedUtil.getDccAddress();
        Assert.assertEquals("address=111", 111, address.getNumber());

        sensor.setState(Sensor.INACTIVE);

        sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        sensor.setState(Sensor.ACTIVE);
        pressButton(frame, Bundle.getMessage("ARun"));

        final Warrant warrant = w;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #3.");
        }, "Train starts to move at 3rd command");

        sensor = runtimes(route);
        Assert.assertNotNull("Sensor not null", sensor);

        // wait for done
        final String name =  w.getDisplayName();
        jmri.util.JUnitUtil.waitFor(()->{return warrant.getRunModeMessage().equals(Bundle.getMessage("NotRunning",name));}, "warrant not done");
         
//        JUnitUtil.waitFor(() -> {
//            return (warrant.getThrottle() == null);
 //       }, "Wait for run to end");
 //       String msg = w.getRunModeMessage();
//        Assert.assertEquals("run finished", Bundle.getMessage("NotRunning", w.getDisplayName()), msg);
//        sensor.setState(Sensor.INACTIVE);

        pressButton(frame, Bundle.getMessage("ButtonSave"));
        w = InstanceManager.getDefault(WarrantManager.class).getWarrant("Learning");
        List<ThrottleSetting> commands = w.getThrottleCommands();
        Assert.assertEquals("12 ThrottleCommands", 12, commands.size());
        /*
        for (ThrottleSetting ts: commands) {
            System.out.println(ts.toString());
        }*/
        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
//        WarrantTableFrame tableFrame = (WarrantTableFrame)jmri.util.JmriJFrame.getFrame(Bundle.getMessage("WarrantTable"));
        Assert.assertNotNull("Warrant Table save", tableFrame);

        // passed test - cleanup.  Do it here so failure leaves traces.
        TestHelper.disposeWindow(frame, this);
        TestHelper.disposeWindow(tableFrame, this);
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("LearnWarrantTest");
        panel.dispose(true);    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text+" Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        flushAWT();
        return button;
    }

    private void confirmJOptionPane(java.awt.Container frame, String title, String text) {
        DialogFinder finder = new DialogFinder(title);
        JUnitUtil.waitFor(() -> {
            return (java.awt.Container)finder.find() != null;
        }, "Found dialog + \"title\"");
        java.awt.Container pane = (java.awt.Container)finder.find();
        Assert.assertNotNull(title+" JOptionPane not found", pane);
        pressButton(pane, text);
    }

    /**
     * @param array of OBlock names
     * @param throttle
     * @return - active end sensor
     * @throws Exception
     */
    private Sensor recordtimes(String[] route, DccThrottle throttle) throws Exception {
        flushAWT();
        float speed = 0.1f;
        if (throttle == null) {
            throw new Exception("recordtimes: No Throttle");
        }
        throttle.setSpeedSetting(speed);
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        for (int i=1; i<route.length; i++) {
            flushAWT();
            if (i<3) {
                speed += 0.1f;
            } else {
                speed -= 0.1f;
            }
            throttle.setSpeedSetting(speed);
            flushAWT();
            Sensor sensorNext = _OBlockMgr.getBySystemName(route[i]).getSensor();
            sensorNext.setState(Sensor.ACTIVE);
            flushAWT();
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        // leaving script with non-zero speed adds 2 more speed commands (-0.5f & 0.0f)
        throttle.setSpeedSetting(0.0f);
        return sensor;
    }

    private Sensor runtimes(String[] route) throws Exception {
        flushAWT();
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        for (int i=1; i<route.length; i++) {
            flushAWT();
            Sensor sensorNext = _OBlockMgr.getBySystemName(route[i]).getSensor();
            sensorNext.setState(Sensor.ACTIVE);
            flushAWT();
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        return sensor;
    }

    // from here down is testing infrastructure
    public LearnWarrantTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LearnWarrantTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(LearnWarrantTest.class);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
         // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initShutDownManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
