package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import jmri.ConfigureManager;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
    public RetryRule retryRule = new RetryRule(2);  // allow retry

    private OBlockManager _OBlockMgr;

    @Test
    public void testLearnWarrant() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

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

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        InstanceManager.getDefault(SensorManager.class);

        Warrant warrant = new Warrant("IW00", "Learning");
        WarrantFrame frame = new WarrantFrame(warrant, true);

        frame._origin.blockBox.setText("OB1");
        frame._destination.blockBox.setText("OB5");
        String[] route = {"OB1", "OB2", "OB3", "OB4", "OB5"};

        JFrameOperator jfo = new JFrameOperator(frame);
        pressButton(jfo, Bundle.getMessage("Calculate"));
        
/*        JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("DialogTitle"));
        pressButton(jdo, Bundle.getMessage("ButtonSelect"));
*/
        JUnitUtil.waitFor(() -> {
            return (frame.getOrders() != null);
        }, "Found orders");
        List<BlockOrder> orders = frame.getOrders();
        Assert.assertEquals("5 BlockOrders", 5, orders.size());

        frame._speedUtil.setDccAddress("99");
        frame.setTrainInfo(null);
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
        OBlock blk = _OBlockMgr.getOBlock(route[0]);
        NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.ACTIVE, blk);
        
        JUnitUtil.waitFor(() -> {
            return  (blk.getState() & OBlock.ALLOCATED | OBlock.OCCUPIED) != 0;
        }, "Train occupies block ");
        pressButton(jfo, Bundle.getMessage("Start"));

        JUnitUtil.waitFor(() -> {
            return (frame._learnThrottle != null);
        }, "Found throttle");
        Assert.assertNotNull("Throttle not found", frame._learnThrottle.getThrottle());

        Sensor lastSensor = recordtimes(route, frame._learnThrottle.getThrottle());

        // After stopping train, wait a bit before pressing stop
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        pressButton(jfo, Bundle.getMessage("Stop"));
        JUnitUtil.waitFor(() -> {
            return  (blk.getState() & OBlock.ALLOCATED) == 0;
        }, "Warrant deallocated");
        // warrant has been recorded using engine 99
        // now playback using engine 111

        NXFrameTest.setAndConfirmSensorAction(lastSensor, Sensor.INACTIVE, 
                _OBlockMgr.getOBlock(route[route.length-1]));
        // change address and run
        frame._speedUtil.setDccAddress("111");
        frame.setTrainInfo("111");
        JUnitUtil.waitFor(() -> {
            return (frame._speedUtil.getDccAddress() != null);
        }, "Found address");
        address = frame._speedUtil.getDccAddress();
        Assert.assertEquals("address=111", 111, address.getNumber());

        NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.ACTIVE, blk);
        JUnitUtil.waitFor(() -> {
            return  (blk.getState() & OBlock.ALLOCATED | OBlock.OCCUPIED) != 0;
        }, "Train 111 occupies block ");
        
        pressButton(jfo, Bundle.getMessage("ARun"));
        List<ThrottleSetting> list = warrant.getThrottleCommands();
        Assert.assertTrue("No Throttle Commands", list != null && !list.isEmpty());

        final Warrant w = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #3.");
        }, "Train starts to move at 3rd command");

        sensor = NXFrameTest.runtimes(route, _OBlockMgr);
        Assert.assertNotNull("Sensor not null", sensor);

        // wait for done
        final String name =  w.getDisplayName();
        jmri.util.JUnitUtil.waitFor(()->{
            return w.getRunModeMessage().equals(Bundle.getMessage("NotRunning",name));
        }, "warrant NotRunning message");
         
        pressButton(jfo, Bundle.getMessage("ButtonSave"));
        warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("Learning");
        List<ThrottleSetting> commands = w.getThrottleCommands();
        Assert.assertEquals("12 ThrottleCommands", 12, commands.size());

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
//        WarrantTableFrame tableFrame = (WarrantTableFrame)jmri.util.JmriJFrame.getFrame(Bundle.getMessage("WarrantTable"));
        Assert.assertNotNull("Warrant Table save", tableFrame);

        // passed test - cleanup.  Do it here so failure leaves traces.
        jfo.requestClose();
        JFrameOperator jfo2 = new JFrameOperator(tableFrame);
        jfo2.requestClose();
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("LearnWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
//        jmri.util.JUnitAppender.assertWarnMessage("Path NorthToWest in block North has length zero. Cannot run NXWarrants or ramp speeds through blocks with zero length."); 
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
     * @param route Array of OBlock names
     * @param throttle
     * @return Active end sensor
     * @throws Exception
     */
    private Sensor recordtimes(String[] route, DccThrottle throttle) throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        float speed = 0.1f;
        if (throttle == null) {
            throw new Exception("recordtimes: No Throttle");
        }
        throttle.setSpeedSetting(speed);
        OBlock block = _OBlockMgr.getBySystemName(route[0]);
        Sensor sensor = block.getSensor();
        for (int i=1; i<route.length; i++) {
            if (i<3) {
                speed += 0.1f;
            } else {
                speed -= 0.1f;
            }
            throttle.setSpeedSetting(speed);
            OBlock blockNext = _OBlockMgr.getBySystemName(route[i]);
            Sensor sensorNext = blockNext.getSensor();
            NXFrameTest.setAndConfirmSensorAction(sensorNext, Sensor.ACTIVE, blockNext);
            NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.INACTIVE, block);
            sensor = sensorNext;
            block = blockNext;
            // Need to have some time elapse between commands. - Especially the last
            new org.netbeans.jemmy.QueueTool().waitEmpty(150);
        }
        // leaving script with non-zero speed adds 2 more speed commands (-0.5f & 0.0f)
        throttle.setSpeedSetting(0.0f);
        return sensor;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
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
    }

    @After
    public void tearDown() throws Exception {
        InstanceManager.getDefault(WarrantManager.class).dispose();
        jmri.util.JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

}
