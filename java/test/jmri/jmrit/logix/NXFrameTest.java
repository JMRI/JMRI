package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the NXFrame class, and it's interactions with Warrants.
 *
 * @author Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class NXFrameTest extends jmri.util.SwingTestCase {

    OBlockManager _OBlockMgr;
//    PortalManager _portalMgr;
    SensorManager _sensorMgr;
//    TurnoutManager _turnoutMgr;

    public void testGetDefault() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        NXFrame nxFrame = NXFrame.getDefault();
        Assert.assertNotNull("NXFrame", nxFrame);
        nxFrame.dispose();
    }

    public void testRoutePanel() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        NXFrame nxFrame = NXFrame.getDefault();
        Assert.assertNotNull("NXFrame", nxFrame);

        nxFrame.setVisible(true);
        pressButton(nxFrame, Bundle.getMessage("Calculate"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        nxFrame._origin.blockBox.setText("NowhereBlock");
        pressButton(nxFrame, Bundle.getMessage("Calculate"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BlockNotFound", "NowhereBlock"), "OK");
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        TestHelper.disposeWindow(nxFrame, this);
    }

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    public void testNXWarrant() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        OBlock block = _OBlockMgr.getBySystemName("OB0");

        NXFrame nxFrame = NXFrame.getDefault();
        nxFrame.setVisible(true);
        nxFrame._origin.blockBox.setText("OB0");
        nxFrame._destination.blockBox.setText("OB10");
        pressButton(nxFrame, Bundle.getMessage("Calculate"));

        DialogFinder finder = new DialogFinder(Bundle.getMessage("DialogTitle"));
        java.awt.Container pickDia = (java.awt.Container) finder.find();
        Assert.assertNotNull("PickRoute Dialog not found", pickDia);
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        List<JRadioButton> list = getRadioButtons(pickDia);
        Assert.assertNotNull("Route RadioButtons not found", list);
        Assert.assertEquals("Number of RadioButton Routes", 4, list.size());
        getHelper().enterClickAndLeave(new MouseEventData(this, list.get(3)));
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));
        getHelper().enterClickAndLeave(new MouseEventData(this, list.get(1)));
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));

        nxFrame.setThrottleIncrement(0.05f);
        pressButton(pickDia, Bundle.getMessage("ButtonSelect"));
        flushAWT();     //pause for NXFrame to make commands

        nxFrame.setMaxSpeed(2);
        pressButton(nxFrame, Bundle.getMessage("ButtonRunNX"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("badSpeed", "2"), "OK");
        flushAWT();
        
        nxFrame.setMaxSpeed(0.6f);
        pressButton(nxFrame, Bundle.getMessage("ButtonRunNX"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BadDccAddress", ""), "OK");
        flushAWT();

        nxFrame.setTrainInfo("666");
        nxFrame.setTrainName("Nick");
        pressButton(nxFrame, Bundle.getMessage("ButtonRunNX"));

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
        List<BlockOrder> orders = warrant.getBlockOrders();
        if (orders.size()!=7) {
            System.out.println();
            System.out.println(warrant.getSystemName()+" " +warrant.getUserName());
            for (BlockOrder bo : orders) {
                System.out.println(bo.toString());
            }
            List<ThrottleSetting> commands = warrant.getThrottleCommands();
            for (ThrottleSetting ts : commands) {
                System.out.println(ts.toString());
            }
        }
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

        flushAWT();
        flushAWT();   // let calm down before running abort

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.ABORT);
        });
        flushAWT();

        // passed test - cleanup.  Do it here so failure leaves traces.
        TestHelper.disposeWindow(tableFrame, this);
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose(true);    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text + " Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        return button;
    }

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    private void confirmJOptionPane(java.awt.Container frame, String title, String message, String buttonLabel) {
        ComponentFinder finder = new ComponentFinder(JOptionPane.class);
        JOptionPane pane;
        if (frame == null) {
            pane = (JOptionPane) finder.find();
            Assert.assertNotNull(title + " JOptionPane not found", pane);
        } else {
            List<JOptionPane> list = finder.findAll(frame);
            Assert.assertNotNull(title + " JOptionPane not found", list);
            Assert.assertTrue(title + " JOptionPane not found", list.size() == 1);
            pane = list.get(0);
        }
        if (message != null) {
            Assert.assertEquals(title + " JOptionPane message", message, pane.getMessage());
        }
        pressButton(pane, buttonLabel);
    }

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    private static List<JRadioButton> getRadioButtons(java.awt.Container frame) {
        ComponentFinder finder = new ComponentFinder(JRadioButton.class);
        List<JRadioButton> list = finder.findAll(frame);
        Assert.assertNotNull("JRadioButton list not found", list);
        return list;
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
        flushAWT();
        OBlock block = _OBlockMgr.getOBlock(route[0]);
        Sensor sensor = block.getSensor();
        for (int i = 1; i < route.length; i++) {
            OBlock blk = block;
            JUnitUtil.waitFor(() -> {
                int state = blk.getState();
                return  state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.OCCUPIED) ||
                        state == (OBlock.ALLOCATED | OBlock.RUNNING | OBlock.UNDETECTED);
            }, "Train occupies block "+i+" of "+route.length);
            flushAWT();
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
                flushAWT();
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

    // from here down is testing infrastructure
    public NXFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", NXFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(NXFrameTest.class);
    }

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

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
