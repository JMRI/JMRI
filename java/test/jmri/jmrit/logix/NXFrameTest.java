package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 * Tests for the NXFrame class, and it's interactions with Warrants.
 *
 * @author Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class NXFrameTest {

    OBlockManager _OBlockMgr;
    SensorManager _sensorMgr;

    @Test
    public void testGetDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NXFrame nxFrame = new NXFrame();
        Assert.assertNotNull("NXFrame", nxFrame);
        JUnitUtil.dispose(nxFrame);
    }

    @Test
    public void testRoutePanel() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NXFrame nxFrame = new NXFrame();
        Assert.assertNotNull("NXFrame", nxFrame);

        JFrameOperator jfo = new JFrameOperator(nxFrame);

        nxFrame.setVisible(true);
        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        nxFrame._origin.blockBox.setText("NowhereBlock");

        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BlockNotFound", "NowhereBlock"), "OK");

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        jfo.requestClose();
    }

    @Test
    public void testNXWarrantSetup() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        NXFrame nxFrame = new NXFrame();
        WarrantTableAction.setNXFrame(nxFrame);
        nxFrame.setVisible(true);

        JFrameOperator nfo = new JFrameOperator(nxFrame);

        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB0"));
        nxFrame._origin.portalBox.setSelectedItem("Enter");
        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB10"));
        nxFrame._destination.portalBox.setSelectedItem("Exit");

        JemmyUtil.pressButton(nfo, Bundle.getMessage("Calculate"));

        JDialogOperator jdo = new JDialogOperator(nfo,Bundle.getMessage("DialogTitle"));

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        // click the third radio button in the dialog
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jdo,3);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        // click the 1st radio button in the dialog
        jrbo = new JRadioButtonOperator(jdo,1);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        nxFrame._speedUtil.setRampThrottleIncrement(0.05f);

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));
        nxFrame.setMaxSpeed(2);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("badSpeed", "2"), "OK");
        
        nxFrame.setMaxSpeed(0.6f);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BadDccAddress", ""), "OK");

        nxFrame._speedUtil.setDccAddress("666");
        nxFrame.setTrainInfo("Nick");
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));

        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    public void testNXWarrant() throws Exception {
        // The first part of this test duplicates testNXWarrantSetup().  It
        // then goes on to test a Warrant through the WarrantTableFrame.
        // it is the WarrantTableframe portion of this test that hangs.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        OBlock block = _OBlockMgr.getBySystemName("OB0");

        NXFrame nxFrame = new NXFrame();
        WarrantTableAction.setNXFrame(nxFrame);
        nxFrame.setVisible(true);

        JFrameOperator nfo = new JFrameOperator(nxFrame);

        nxFrame.mouseClickedOnBlock(block);
        nxFrame._origin.portalBox.setSelectedItem("Enter");
        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB10"));
        nxFrame._destination.portalBox.setSelectedItem("Exit");

        JemmyUtil.pressButton(nfo, Bundle.getMessage("Calculate"));

        JDialogOperator jdo = new JDialogOperator(nfo,Bundle.getMessage("DialogTitle"));

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        // click the third radio button in the dialog
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jdo,3);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        // click the 1st radio button in the dialog
        jrbo = new JRadioButtonOperator(jdo,1);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));

        nxFrame._speedUtil.setRampThrottleIncrement(0.05f);     
        nxFrame._speedUtil.setRampTimeIncrement(100);
        nxFrame.setMaxSpeed(0.6f);
        nxFrame._speedUtil.setDccAddress("666");
        nxFrame.setTrainInfo("Nick");
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        WarrantTableModel model = tableFrame.getModel();
        Assert.assertNotNull("tableFrame model", model);
        
        JUnitUtil.waitFor(() -> {
            return model.getRowCount() > 1;
        }, "NXWarrant loaded into table");
        
        Warrant warrant = tableFrame.getModel().getWarrantAt(model.getRowCount()-1);

        Assert.assertNotNull("warrant", warrant);
        Assert.assertNotNull("warrant.getBlockOrders(", warrant.getBlockOrders());
        warrant.getBlockOrders();
        Assert.assertEquals("Num Blocks in Route", 7, warrant.getBlockOrders().size());
        Assert.assertTrue("Num Comands", warrant.getThrottleCommands().size()>5);

        String name = block.getDisplayName();
        jmri.util.JUnitUtil.waitFor(
            ()->{return warrant.getRunningMessage().equals(Bundle.getMessage("waitForDelayStart", warrant.getTrainName(), name));},
            "Waiting message");
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        Assert.assertNotNull("Senor IS0 not found", sensor0);

        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                sensor0.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                Assert.fail("Unexpected Exception: " + e);
            }
        });

        final OBlock testblock = block;
        JUnitUtil.waitFor(() -> {
            return testblock.getState() == (OBlock.ALLOCATED | OBlock.OCCUPIED | OBlock.RUNNING);
        }, "Start Block Active");

        JUnitUtil.waitFor(() -> {
            return Bundle.getMessage("Halted", name, "1").equals(warrant.getRunningMessage());
        }, "Warrant processed sensor change");

        Assert.assertEquals("Halted/Resume message", warrant.getRunningMessage(),
                Bundle.getMessage("Halted", block.getDisplayName(), "1"));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.RESUME);
        });

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

        // OBlock sensor names
        String[] route = {"OB0", "OB1", "OB2", "OB3", "OB7", "OB5", "OB10"};
        block = _OBlockMgr.getOBlock("OB10");
        // runtimes() in next line runs the train, then checks location
        Assert.assertEquals("Train in last block", block.getSensor().getDisplayName(), runtimes(route, _OBlockMgr).getDisplayName());

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.ABORT);
        });
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    public void testWarrantLoopRun() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Sensor sensor3 = _sensorMgr.getBySystemName("IS3");
        Assert.assertNotNull("Senor IS3 not found", sensor3);

        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                sensor3.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                Assert.fail("Unexpected Exception: " + e);
            }
        });
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause light sensor

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant warrant = tableFrame.getModel().getWarrantAt(0);
        Assert.assertNotNull("warrant", warrant);
       
        tableFrame.runTrain(warrant, Warrant.MODE_RUN);
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #3.");
        }, "Train is moving at 3rd command");

       // OBlock sensor names
        String[] route = {"OB3", "OB4", "OB5", "OB10", "OB0", "OB1", "OB2", "OB3"};
        OBlock block = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        Assert.assertEquals("Train in last block", block.getSensor().getDisplayName(), runtimes(route, _OBlockMgr).getDisplayName());

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for to finish run

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }    

    @Test
    public void testWarrantRampHalt() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for to finish run

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        Assert.assertNotNull("Senor IS1 not found", sensor1);

        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                sensor1.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                Assert.fail("Unexpected Exception: " + e);
            }
        });
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause light sensor

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant warrant = tableFrame.getModel().getWarrantAt(1);
        Assert.assertNotNull("warrant", warrant);
       
        tableFrame.runTrain(warrant, Warrant.MODE_RUN);

        SpeedUtil sp = warrant.getSpeedUtil();
        sp.setRampThrottleIncrement(0.15f);
        sp.setRampTimeIncrement(100);

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock sensor names
        String[] route1 = {"OB1", "OB6", "OB3"};
        OBlock block = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        Assert.assertEquals("Train in block OB3", block.getSensor().getDisplayName(), runtimes(route1,_OBlockMgr).getDisplayName());

        warrant.controlRunTrain(Warrant.RAMP_HALT); // user interrupts script
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return (m.startsWith("Halted in block"));
        }, "Train Halted");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands

        warrant.controlRunTrain(Warrant.RESUME);
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.startsWith("Overdue for arrival at block");
        }, "Train Resumed");

        String[] route2 = {"OB3", "OB7", "OB5"};
        block = _OBlockMgr.getOBlock("OB5");
        // runtimes() in next line runs the train, then checks location
        Assert.assertEquals("Train in last block", block.getSensor().getDisplayName(), runtimes(route2, _OBlockMgr).getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    /**
     * Simulates the movement of a warranted train over its route.
     * <p>Works through a list of OBlocks, gets its sensor,
     * activates it, then inactivates the previous OBlock sensor.
     * Leaves last sensor ACTIVE to show the train stopped there.
     * @param route Array of OBlock names of the route
     * @param mgr OBLock manager
     * @return active end sensor
     * @throws Exception exception thrown
     */
    protected static  Sensor runtimes(String[] route, OBlockManager mgr) throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands
        OBlock block = mgr.getOBlock(route[0]);
        Sensor sensor = block.getSensor();
        for (int i = 1; i < route.length; i++) {
            OBlock blk = block;
            JUnitUtil.waitFor(() -> {
                int state = blk.getState();
                return  state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.OCCUPIED) ||
                        state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.UNDETECTED);
            }, "Train occupies block "+i+" ("+blk.getDisplayName()+") of "+route.length);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);

            block = mgr.getOBlock(route[i]);
            Sensor nextSensor;
            boolean dark = (block.getState() & OBlock.UNDETECTED) != 0;
            if (!dark) {
                nextSensor = block.getSensor();
                jmri.util.ThreadingUtil.runOnLayout(() -> {
                    try {
                        nextSensor.setState(Sensor.ACTIVE);
                    } catch (jmri.JmriException e) {
                        Assert.fail("Set "+nextSensor.getDisplayName()+" ACTIVE Exception: " + e);
                    }
                });
                new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands
            } else {
                nextSensor = null;
            }
            if (sensor != null) {
                final Sensor tsensor = sensor;
                jmri.util.ThreadingUtil.runOnLayout(() -> {
                    try {
                        tsensor.setState(Sensor.INACTIVE);
                    } catch (jmri.JmriException e) {
                        Assert.fail("Set "+tsensor.getDisplayName()+" INACTIVE Exception: " + e);
                    }
                });
            }
            if (!dark) {
                sensor = nextSensor;
            }
        }
        return sensor;
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initLayoutBlockManager();
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
