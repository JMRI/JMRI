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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import jmri.util.junit.rules.RetryRule;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.WindowOperator;


/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class LearnWarrantTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    private OBlockManager _OBlockMgr;
//    PortalManager _portalMgr;
    private SensorManager _sensorMgr;
//    TurnoutManager _turnoutMgr;

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    @Test
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

        JFrameOperator jfo = new JFrameOperator(frame);

        pressButton(jfo, Bundle.getMessage("Calculate"));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
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

        pressButton(jfo, Bundle.getMessage("Start"));
        // dismiss warning "starting block not occupied
        confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), "OK");

        // occupy starting block
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        sensor.setState(Sensor.ACTIVE);
        pressButton(jfo, Bundle.getMessage("Start"));

        JUnitUtil.waitFor(() -> {
            return (frame._learnThrottle != null);
        }, "Found throttle");
        Assert.assertNotNull("Throttle not found", frame._learnThrottle.getThrottle());

        sensor = recordtimes(route, frame._learnThrottle.getThrottle());

        pressButton(jfo, Bundle.getMessage("Stop"));

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
        pressButton(jfo, Bundle.getMessage("ARun"));

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

        pressButton(jfo, Bundle.getMessage("ButtonSave"));
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
        jfo.requestClose();
        JFrameOperator jfo2 = new JFrameOperator(tableFrame);
        jfo2.requestClose();
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("LearnWarrantTest");
        panel.dispose(true);    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    private void pressButton(WindowOperator frame, String text) {
        JButtonOperator jbo = new JButtonOperator(frame,text);
        jbo.push();
    }

    private void confirmJOptionPane(WindowOperator wo, String title, String buttonLabel) {
        // the previous version of this message verified the text string
        // in the dialog matched the passed message value.  We need to
        // determine how to do that using Jemmy.
        JDialogOperator jdo = new JDialogOperator(wo,title);
        JButtonOperator jbo = new JButtonOperator(jdo,buttonLabel);
        jbo.push();
    }

    /**
     * @param array of OBlock names
     * @param throttle
     * @return - active end sensor
     * @throws Exception
     */
    private Sensor recordtimes(String[] route, DccThrottle throttle) throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        float speed = 0.1f;
        if (throttle == null) {
            throw new Exception("recordtimes: No Throttle");
        }
        throttle.setSpeedSetting(speed);
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        for (int i=1; i<route.length; i++) {
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
            if (i<3) {
                speed += 0.1f;
            } else {
                speed -= 0.1f;
            }
            throttle.setSpeedSetting(speed);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
            Sensor sensorNext = _OBlockMgr.getBySystemName(route[i]).getSensor();
            sensorNext.setState(Sensor.ACTIVE);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        // leaving script with non-zero speed adds 2 more speed commands (-0.5f & 0.0f)
        throttle.setSpeedSetting(0.0f);
        return sensor;
    }

    private Sensor runtimes(String[] route) throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Sensor sensor = _OBlockMgr.getBySystemName(route[0]).getSensor();
        for (int i=1; i<route.length; i++) {
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
            Sensor sensorNext = _OBlockMgr.getBySystemName(route[i]).getSensor();
            sensorNext.setState(Sensor.ACTIVE);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        return sensor;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
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

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
