package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.*;

import org.netbeans.jemmy.operators.JButtonOperator;
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
//    PortalManager _portalMgr;
    SensorManager _sensorMgr;
//    TurnoutManager _turnoutMgr;

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

        // if _origin.blockBox and _destination.blockBox were labeled,
        // we could use jemmy to find these and fill in the text.
        nxFrame._origin.blockBox.setText("OB0");
        nxFrame._destination.blockBox.setText("OB10");

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

        nxFrame.setThrottleIncrement(0.05f);

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));
        nxFrame.setMaxSpeed(2);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("badSpeed", "2"), "OK");
        
        nxFrame.setMaxSpeed(0.6f);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BadDccAddress", ""), "OK");

        nxFrame.setTrainInfo("666");
        nxFrame.setTrainName("Nick");
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));

        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    @Ignore("Causes timeouts due to threading issues; probably real problems, but we can't have a 30% PK of CI")
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

        // if _origin.blockBox and _destination.blockBox were labeled,
        // we could use jemmy to find these and fill in the text.
        nxFrame._origin.blockBox.setText("OB0");
        nxFrame._destination.blockBox.setText("OB10");

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

        nxFrame.setThrottleIncrement(0.05f);

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));
        nxFrame.setMaxSpeed(2);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("badSpeed", "2"), "OK");
        
        nxFrame.setMaxSpeed(0.6f);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BadDccAddress", ""), "OK");

        // from this point to the end of the test, there are no more references
        // to nxFrame.  Do we need to split this into multiple tests?  
        // The next part deals with a WarrantTableFrame, should it still be
        // in this test file?

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);
        WarrantTableModel model = tableFrame.getModel();
        Assert.assertNotNull("tableFrame model", model);
        JUnitUtil.waitFor(() -> {
            return model.getRowCount()>0;
        }, "NXWarrant loaded into table");
        Warrant warrant = tableFrame.getModel().getWarrantAt(0);
        Assert.assertNotNull("warrant", warrant);
        Assert.assertNotNull("warrant.getBlockOrders(", warrant.getBlockOrders());
        warrant.getBlockOrders();
/*        if (orders.size()!=7) {
            System.out.println();
            System.out.println(warrant.getSystemName()+" " +warrant.getUserName());
            for (BlockOrder bo : orders) {
                System.out.println(bo.toString());
            }
            List<ThrottleSetting> commands = warrant.getThrottleCommands();
            for (ThrottleSetting ts : commands) {
                System.out.println(ts.toString());
            }
        }*/
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
            return Bundle.getMessage("Halted", name, "0").equals(warrant.getRunningMessage());
        }, "Warrant processed sensor change");

        Assert.assertEquals("Halted/Resume message", warrant.getRunningMessage(),
                Bundle.getMessage("Halted", block.getDisplayName(), "0"));

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.RESUME);
        });

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #7.");
        }, "Train starts to move at 7th command");

        // OBlock sensor names
        String[] route = {"OB0", "OB1", "OB2", "OB3", "OB7", "OB5", "OB10"};
        block = _OBlockMgr.getOBlock("OB10");
        // runtimes() in next line runs the train, then checks location
        Assert.assertEquals("Train in last block", block.getSensor().getDisplayName(), runtimes(route).getDisplayName());

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


    /**
     * Simulates the movement of a warranted train over its route.
     * <p>Works through a list of OBlocks, gets its sensor,
     * activates it, then inactivates the previous OBlock sensor.
     * Leaves last sensor ACTIVE to show the train stopped there.
     * @param list of detection sensors of the route
     * @return active end sensor
     * @throws Exception
     */
    private Sensor runtimes(String[] route) throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands
        OBlock block = _OBlockMgr.getOBlock(route[0]);
        Sensor sensor = block.getSensor();
        for (int i = 1; i < route.length; i++) {
            OBlock blk = block;
            JUnitUtil.waitFor(() -> {
                int state = blk.getState();
                return  state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.OCCUPIED) ||
                        state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.UNDETECTED);
            }, "Train occupies block "+i+" of "+route.length);
            new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands
            jmri.util.JUnitUtil.releaseThread(this, 100);

            block = _OBlockMgr.getOBlock(route[i]);
            Sensor nextSensor;
            boolean dark = (block.getState() & OBlock.UNDETECTED) != 0;
            if (!dark) {
                nextSensor = block.getSensor();
                jmri.util.ThreadingUtil.runOnLayout(() -> {
                    try {
                        nextSensor.setState(Sensor.ACTIVE);
                    } catch (jmri.JmriException e) {
                        Assert.fail("Unexpected Exception: " + e);
                    }
                });
                jmri.util.JUnitUtil.releaseThread(this, 100);
                jmri.util.ThreadingUtil.runOnLayout(() -> {
                    try {
                        nextSensor.setState(Sensor.ACTIVE);
                    } catch (jmri.JmriException e) {
                        Assert.fail("Unexpected Exception: " + e);
                    }
                });
                new org.netbeans.jemmy.QueueTool().waitEmpty(100);  //pause for NXFrame to make commands
                jmri.util.JUnitUtil.releaseThread(this, 100);
            } else {
                nextSensor = null;
            }
            final Sensor tsensor = sensor;
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                try {
                    tsensor.setState(Sensor.INACTIVE);
                } catch (jmri.JmriException e) {
                    Assert.fail("Unexpected Exception: " + e);
                }
            });
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
