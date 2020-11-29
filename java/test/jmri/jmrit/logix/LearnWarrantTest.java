package jmri.jmrit.logix;

import java.io.File;
import java.util.List;

import jmri.ConfigureManager;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.WindowOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class LearnWarrantTest {

    private OBlockManager _OBlockMgr;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testLearnWarrant() throws Exception {
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

        WarrantFrame frame = new WarrantFrame(null, null);

        frame._origin.blockBox.setText("OB1");
        frame._destination.blockBox.setText("OB5");
        String[] route = {"OB1", "OB2", "OB3", "OB4", "OB5"};

        JFrameOperator jfo = new JFrameOperator(frame);
        pressButton(jfo, Bundle.getMessage("Calculate"));
        
        JUnitUtil.waitFor(() -> (frame.getOrders() != null), "Found orders");
        List<BlockOrder> orders = frame.getOrders();
        assertThat(orders.size()).withFailMessage("5 BlockOrders").isEqualTo(5);

        frame._speedUtil.setDccAddress("99");
        frame.setTrainInfo(null);
        JUnitUtil.waitFor(() -> (frame._speedUtil.getDccAddress() != null), "Found address");
        jmri.DccLocoAddress address = frame._speedUtil.getDccAddress();
        assertThat(address.getNumber()).withFailMessage("address=99").isEqualTo(99);

        pressButton(jfo, Bundle.getMessage("Start"));
        // dismiss warning "starting block not occupied
        confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), "OK");

        // occupy starting block
        final OBlock block0 = _OBlockMgr.getOBlock(route[0]);
        Sensor sensor = block0.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.ACTIVE, block0);

        JUnitUtil.waitFor(() -> oBlockOccupiedOrAllocated(block0), "Train occupies block ");
        pressButton(jfo, Bundle.getMessage("Start"));

        JUnitUtil.waitFor(() -> (frame._learnThrottle != null), "Found throttle");
        assertThat(frame._learnThrottle.getThrottle()).withFailMessage("Throttle not found").isNotNull();

        Sensor lastSensor = recordtimes(route, frame._learnThrottle.getThrottle());

        // After stopping train, wait a bit before pressing stop
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        pressButton(jfo, Bundle.getMessage("Stop"));
        JUnitUtil.waitFor(() -> (block0.getState() & OBlock.ALLOCATED) == 0, "Warrant deallocated");
        // warrant has been recorded using engine 99

        List<ThrottleSetting> list = frame.getThrottleCommands();
        assertThat(list.size()).withFailMessage("12 ThrottleCommands").isEqualTo(12);

        // now playback using engine 111
        NXFrameTest.setAndConfirmSensorAction(lastSensor, Sensor.INACTIVE, 
                _OBlockMgr.getOBlock(route[route.length-1]));
        // change address and run
        frame._speedUtil.setDccAddress("111");
        frame.setTrainInfo("111");
        JUnitUtil.waitFor(() -> (frame._speedUtil.getDccAddress() != null), "Found address");
        address = frame._speedUtil.getDccAddress();
        assertThat(address.getNumber()).withFailMessage("address=111").isEqualTo(111);

        NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.ACTIVE, block0);
        JUnitUtil.waitFor(() -> oBlockOccupiedOrAllocated(block0), "Train 111 occupies first block ");

        pressButton(jfo, Bundle.getMessage("ARun"));    // start play back

        sensor = NXFrameTest.runtimes(route, _OBlockMgr);
        assertThat(sensor).withFailMessage("Sensor not null").isNotNull();

        final OBlock block4 = _OBlockMgr.getOBlock(route[4]);
        sensor = block4.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sensor, Sensor.ACTIVE, block4);
        
        JUnitUtil.waitFor(() -> oBlockOccupiedOrAllocated(block4), "Train 111 occupies last block ");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100); // wait for script to complete

        pressButton(jfo, Bundle.getMessage("ButtonSave"));
/*        warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("Learning");
        List<ThrottleSetting> commands = warrant.getThrottleCommands();
        Assert.assertEquals("12 ThrottleCommands", 12, commands.size());*/

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("Warrant Table save").isNotNull();

        // passed test - cleanup.  Do it here so failure leaves traces.
        jfo.requestClose();
        JFrameOperator jfo2 = new JFrameOperator(tableFrame);
        jfo2.requestClose();
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("LearnWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
//        jmri.util.JUnitAppender.assertWarnMessage("Path NorthToWest in block North has length zero. Cannot run NXWarrants or ramp speeds through blocks with zero length."); 
    }

    private boolean oBlockOccupiedOrAllocated(OBlock b){
        return (b.getState() & (OBlock.ALLOCATED | OBlock.OCCUPIED)) != 0;
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
            // Need to have some time elapse between commands. - Especially the last
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);
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
        }
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        // leaving script with non-zero speed adds 2 more speed commands (-0.5f & 0.0f)
        throttle.setSpeedSetting(0.0f);
        return sensor;
    }

    @BeforeEach
    public void setUp() {
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
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.tearDown();
    }

}
