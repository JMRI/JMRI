package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.ConfigureManager;
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
import org.netbeans.jemmy.operators.JFrameOperator;


/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 *
 * todo - test error conditions
 */
public class LinkedWarrantTest {

    @Rule
    public RetryRule retryRule = new RetryRule(2);  // allow 3 retries

    private OBlockManager _OBlockMgr;
    private SensorManager _sensorMgr;
    private WarrantManager _warrantMgr;

    // tests a warrant launching itself. (origin, destination the same to make continuous loop)
    @Test
    public void testLoopedWarrant() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");
        panel.setVisible(false);  // hide panel to prevent repaint.

        Sensor sensor1 = _sensorMgr.getBySystemName("IS12");
        Assert.assertNotNull("Senor IS12 not found", sensor1);
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant warrant = _warrantMgr.getWarrant("LoopDeLoop");
        Assert.assertNotNull("warrant", warrant);
      
        // WarrantTable.runTrain() returns a string that is not null if the 
        // warrant can't be started 
        Assert.assertNull("Warrant starts",
              tableFrame.runTrain(warrant, Warrant.MODE_RUN)); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 1 starts to move at 8th command");

       // OBlock of route
        String[] route = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");
        
        // Run the train, then checks end location
        Assert.assertEquals("LoopDeLoop after first leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "LoopDeLoop finished first leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 2 starts to move at 8th command");

        Assert.assertEquals("LoopDeLoop after second leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "LoopDeLoop finished second leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 3 starts to move at 8th command");

        Assert.assertEquals("LoopDeLoop after last leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // Tests warrant launching a different warrant with different address. Origin location cannot be destination of the other)
    @Test
    public void testLinkedWarrant() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");
        panel.setVisible(false);  // hide panel to prevent repaint.

        final Sensor sensor12 = _sensorMgr.getBySystemName("IS12");
        Assert.assertNotNull("Senor IS12 not found", sensor12);

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        Assert.assertNotNull("Senor IS1 not found", sensor1);
        NXFrameTest.setAndConfirmSensorAction(sensor12, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant warrant = _warrantMgr.getWarrant("Loop&Fred");
        Assert.assertNotNull("warrant", warrant);
       
        // WarrantTable.runTrain() returns a string that is not null if the 
        // warrant can't be started 
        Assert.assertNull("Warrant starts",
              tableFrame.runTrain(warrant, Warrant.MODE_RUN)); // start run

        Warrant w = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");

        // Run the train, then checks end location
        Assert.assertEquals("Train after first leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName());

        // "Loop&Fred" links to "WestToEast". Get start for "WestToEast" occupied quickly
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return (m.startsWith("Launching warrant"));
        }, "Train Loopy finished first leg");

        warrant = _warrantMgr.getWarrant("WestToEast");

        Warrant ww = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train Fred starts to move at 8th command");

        String[] route2 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        block = _OBlockMgr.getOBlock("OB11");

        Assert.assertEquals("Train after second leg", block.getSensor().getDisplayName(),
                NXFrameTest.runtimes(route2, _OBlockMgr).getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // tests a warrant running a train out and launching a return train 
    // Both warrants have the same address and origin of each is destination of the other
    @Test
    public void testBackAndForth() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");
        panel.setVisible(false);  // hide panel to prevent repaint.

        final Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        Assert.assertNotNull("Senor IS1 not found", sensor1);

        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant outWarrant = _warrantMgr.getWarrant("WestToEastLink");
        Assert.assertNotNull("WestWarrant", outWarrant);
        Warrant backWarrant = _warrantMgr.getWarrant("EastToWestLink");

        // OBlock of route
        String[] routeOut = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        String outEndSensorName = _OBlockMgr.getOBlock("OB11").getSensor().getDisplayName();
        String[] routeBack = {"OB11", "OB9", "OB7", "OB6", "OB5", "OB3", "OB1"};
        String backEndSensorName = _OBlockMgr.getOBlock("OB1").getSensor().getDisplayName();

        // WarrantTable.runTrain() returns a string that is not null if the 
        // warrant can't be started 
        Assert.assertNull("Warrant starts",
              tableFrame.runTrain(outWarrant, Warrant.MODE_RUN)); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        // Run the train, then checks end location
        Assert.assertEquals("Train after first leg", outEndSensorName, NXFrameTest.runtimes(routeOut, _OBlockMgr).getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "WestToEastLink finished first leg out");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        Assert.assertEquals("Train after second leg", backEndSensorName, NXFrameTest.runtimes(routeBack, _OBlockMgr).getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "EastToWestLink finished second leg back");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        Assert.assertEquals("Train after third leg", outEndSensorName, NXFrameTest.runtimes(routeOut, _OBlockMgr).getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "WestToEastLink finished third leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        Assert.assertEquals("Train after fourth leg", backEndSensorName, NXFrameTest.runtimes(routeBack, _OBlockMgr).getDisplayName());

            // passed test - cleanup.  Do it here so failure leaves traces.
            JFrameOperator jfo = new JFrameOperator(tableFrame);
            jfo.requestClose();
            // we may want to use jemmy to close the panel as well.     
            panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // Tests warrant launching 3 different warrants mid script - tinker to Evers to Chance (1910 Chicago Cubs)
    @Test
    public void testLinkedMidScript() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.setVisible(false);  // hide panel to prevent repaint.

        // Tinker start block
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        Assert.assertNotNull("Senor IS0 not found", sensor0);
        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB0"));

        // Evers start block
        Sensor sensor7 = _sensorMgr.getBySystemName("IS7");
        Assert.assertNotNull("Senor IS7 not found", sensor7);
        NXFrameTest.setAndConfirmSensorAction(sensor7, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB7"));

        // Chance start block
        Sensor sensor6 = _sensorMgr.getBySystemName("IS6");
        Assert.assertNotNull("Senor IS6 not found", sensor6);
        NXFrameTest.setAndConfirmSensorAction(sensor6, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB6"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant w = _warrantMgr.getWarrant("Tinker");
        Assert.assertNotNull("warrant", w);
       
        // WarrantTable.runTrain() returns a string that is not null if the 
        // warrant can't be started 
        Assert.assertNull("Warrant starts",
              tableFrame.runTrain(w, Warrant.MODE_RUN)); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Tinker starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB0", "OB1", "OB2", "OB3", "OB4", "OB5", "OB10"};
        OBlock block = _OBlockMgr.getOBlock("OB10");

        // Run the train, then checks end location
        Assert.assertEquals("Tinker after first leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName());

        Warrant ww = _warrantMgr.getWarrant("Evers");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return (m.startsWith("Launching warrant"));
        }, "Tinker finished first leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Evers starts to move at 8th command");

        String[] route2 = {"OB7", "OB3", "OB2", "OB1"};
        block = _OBlockMgr.getOBlock("OB1");

        Assert.assertEquals("Evers after second leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route2, _OBlockMgr).getDisplayName());

        Warrant www = _warrantMgr.getWarrant("Chance");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return (m.startsWith("Launching warrant"));
        }, "Evers finished second leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  www.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Chance starts to move at 8th command");

        String[] route3 = {"OB6", "OB3", "OB4", "OB5"};
        block = _OBlockMgr.getOBlock("OB5");

        Assert.assertEquals("Chance after third leg", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route3, _OBlockMgr).getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initDebugThrottleManager();

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        _warrantMgr = InstanceManager.getDefault(WarrantManager.class);
    }

    @After
    public void tearDown() throws Exception {
        _warrantMgr.dispose();
        _warrantMgr = null;
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        _sensorMgr.dispose();
        _sensorMgr = null;
        
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }
}
