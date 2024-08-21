package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import jmri.*;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for running multiple Warrants
 *
 * @author  Pete Cressman 2015
 */
@Timeout(60)
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
@DisabledIfSystemProperty(named ="jmri.skipLinkedWarrantTest", matches ="true")
public class LinkedWarrantTest {

    private OBlockManager _OBlockMgr;
    private SensorManager _sensorMgr;
    private WarrantManager _warrantMgr;

    // tests a warrant launching itself. (origin, destination the same to make continuous loop)
    @Test
    public void testLoopedWarrant() {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");

        assertDoesNotThrow( () -> {
            InstanceManager.getDefault(ConfigureManager.class).load(f);
        }, ("Exception loading "+ f));
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        Sensor sensor1 = _sensorMgr.getBySystemName("IS12");
        assertNotNull(sensor1,"Senor IS12 not found");
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertNotNull(tableFrame,"tableFrame");

        Warrant warrant = _warrantMgr.getWarrant("LoopDeLoop");
        assertNotNull(warrant,"warrant");

        // OBlock of route
        String[] route = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertNull(tableFrame.runTrain(warrant, Warrant.MODE_RUN),"Warrant starts"); // start run

        String lookingFor = Bundle.getMessage("warrantStart", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName());
        JUnitUtil.waitFor(() -> {
            return lookingFor.equals(tableFrame.getStatus());
        }, "LoopDeLoop started first leg expected \"" + lookingFor + "\" but was \"" + tableFrame.getStatus()+"\"");

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 1 starts to move at 8th command");

        // Run the train, then checks end location
        assertDoesNotThrow( () -> {
            assertEquals(block.getSensor(),
                NXFrameTest.runtimes(route, _OBlockMgr),
                "LoopDeLoop Completes first Leg");
        }, ("LoopDeLoop after first leg Exception"));

        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * route.length for return

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 2 starts to move at 8th command");

        assertDoesNotThrow( () -> {
            assertEquals(block.getSensor(),
                NXFrameTest.runtimes(route, _OBlockMgr),
                "LoopDeLoop Completes Second Leg");
        }, ("LoopDeLoop after Second leg Exception"));

        String linkMsg = Bundle.getMessage("warrantComplete", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName());
        JUnitUtil.waitFor(() -> {
            return linkMsg.equals(tableFrame.getStatus());
        }, "LoopDeLoop finished second leg");

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 3 starts to move at 8th command");

        assertDoesNotThrow( () -> {
            assertEquals(block.getSensor(),
                NXFrameTest.runtimes(route, _OBlockMgr),
                "LoopDeLoop Completes third Leg");
        }, ("LoopDeLoop after third leg Exception"));

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName()));
        }, "LoopDeLoop finished third leg");

        JFrameOperator jfo = new JFrameOperator(WarrantTableFrame.getDefault());
        jfo.requestClose();
        jfo.waitClosed();

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame("LinkedWarrantsTest").dispose();
            return true;
        });
        assertTrue(retVal);

    }

    // Tests warrant launching a different warrant with different address. Origin location cannot be destination of the other)
    @Test
    public void testLinkedWarrant() {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        assertDoesNotThrow( () -> {
            InstanceManager.getDefault(ConfigureManager.class).load(f);
        }, ("Exception loading "+ f));
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        final Sensor sensor12 = _sensorMgr.getBySystemName("IS12");
        assertNotNull(sensor12,"Sensor IS12 not found");

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertNotNull(sensor1,"Senor IS1 not found");
        NXFrameTest.setAndConfirmSensorAction(sensor12, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        final Warrant warrant = _warrantMgr.getWarrant("Loop&Fred");
        assertNotNull(warrant,"warrant");

        ThreadingUtil.runOnGUI(() -> {
            WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
            // WarrantTable.runTrain() returns a string that is not null if the
            // warrant can't be started
            assertNull(tableFrame.runTrain(warrant, Warrant.MODE_RUN),"Warrant starts"); // start run
        });

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command, but running message was " + warrant.getRunningMessage());

       // OBlock of route
        String[] route1 = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock lastBlockInRoute1 = _OBlockMgr.getOBlock("OB12");
        assertNotNull(lastBlockInRoute1);

        // Run the train, then checks end location
        assertDoesNotThrow( () -> {
            assertEquals(lastBlockInRoute1.getSensor(),
                NXFrameTest.runtimes(route1, _OBlockMgr),
                "Train after first leg");
        }, ("Exception running route1"));
        
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        // "Loop&Fred" links to "WestToEast". Get start for "WestToEast" occupied quickly
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        Warrant ww = _warrantMgr.getWarrant("WestToEast");
        assertNotNull(ww,"warrant WestToEast exists");

        JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #9.");
        }, "Train Fred starts to move at 8th command");

        String[] route2 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        final OBlock block = _OBlockMgr.getOBlock("OB11");

        assertDoesNotThrow( () -> {
            assertEquals(block.getSensor(),
                NXFrameTest.runtimes(route2, _OBlockMgr),
                "Train after second leg");
        }, ("Exception running route2"));

        JFrameOperator jfo = new JFrameOperator(WarrantTableFrame.getDefault());
        jfo.requestClose();
        jfo.waitClosed();

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame("LinkedWarrantsTest").dispose();
            return true;
        });
        assertTrue(retVal);

    }

    // tests a warrant running a train out and launching a return train
    // Both warrants have the same address and origin of each is destination of the other
    @Test
    public void testBackAndForth() {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        Assertions.assertDoesNotThrow( () -> {
            InstanceManager.getDefault(ConfigureManager.class).load(f);
        }, ("Exception loading "+ f));
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        final Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertNotNull(sensor1,"Senor IS1 not found");

        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertNotNull(tableFrame,"tableFrame");

        Warrant outWarrant = _warrantMgr.getWarrant("WestToEastLink");
        assertNotNull(outWarrant,"WestWarrant");
        Warrant backWarrant = _warrantMgr.getWarrant("EastToWestLink");

        // OBlock of route
        String[] routeOut = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        Sensor outEndSensor = _OBlockMgr.getOBlock("OB11").getSensor();
        String[] routeBack = {"OB11", "OB9", "OB7", "OB6", "OB5", "OB3", "OB1"};
        Sensor backEndSensor = _OBlockMgr.getOBlock("OB1").getSensor();

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertNull(tableFrame.runTrain(outWarrant, Warrant.MODE_RUN),"Warrant starts"); // start run

        JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        // Run the train, then checks end location
        assertDoesNotThrow( () -> {
            assertEquals(outEndSensor,
                NXFrameTest.runtimes(routeOut, _OBlockMgr),
                "Train after first leg");
        }, ("Exception running routeOut"));
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * (route.length - 1) for return

        String outBlockName = _OBlockMgr.getOBlock("OB11").getDisplayName();
        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                                outWarrant.getTrainName(), outWarrant.getDisplayName(),
                                outBlockName));
        }, "WestToEastLink finished first leg out");

        JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        assertDoesNotThrow( () -> {
            assertEquals(backEndSensor,
                NXFrameTest.runtimes(routeBack, _OBlockMgr),
                "Train after second leg");
        }, ("Exception running routeBack"));
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                    backWarrant.getTrainName(), backWarrant.getDisplayName(),
                    _OBlockMgr.getOBlock("OB1").getDisplayName()));
        }, "EastToWestLink finished second leg back");

        JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        assertDoesNotThrow( () -> {
            assertEquals(outEndSensor,
                NXFrameTest.runtimes(routeOut, _OBlockMgr),
                "Train after third leg");
        }, ("Exception running routeOut 3"));
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                    outWarrant.getTrainName(), outWarrant.getDisplayName(),
                    outBlockName));
        }, "WestToEastLink finished third leg");

        JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.") || m.endsWith("Cmd #9.");
        }, "EastToWestLink starts to move at 8th command");

        assertDoesNotThrow( () -> {
            assertEquals(backEndSensor,
                NXFrameTest.runtimes(routeBack, _OBlockMgr),
                "Train after fourth leg");
        }, ("Exception running routeBack 4"));
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        outWarrant.stopWarrant(true, false);
        backWarrant.stopWarrant(true, false);

        JFrameOperator jfo = new JFrameOperator(WarrantTableFrame.getDefault());
        jfo.requestClose();
        jfo.waitClosed();

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame("LinkedWarrantsTest").dispose();
            return true;
        });
        assertTrue(retVal);

    }

    // Tests warrant launching 3 different warrants mid script - tinker to Evers to Chance (1910 Chicago Cubs)
    @Test
    public void testLinkedMidScript() {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        assertDoesNotThrow( () -> {
            InstanceManager.getDefault(ConfigureManager.class).load(f);
        }, ("Exception loading "+ f));
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        // Tinker start block
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        assertNotNull(sensor0,"Senor IS0 not found");
        // wait block OB0 occupied
        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB0"));

        // Evers start block
        Sensor sensor7 = _sensorMgr.getBySystemName("IS7");
        assertNotNull(sensor7,"Senor IS7 not found");
        // wait block OB7 occupied
        NXFrameTest.setAndConfirmSensorAction(sensor7, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB7"));

        // Chance start block
        Sensor sensor6 = _sensorMgr.getBySystemName("IS6");
        assertNotNull(sensor6,"Senor IS6 not found");
        NXFrameTest.setAndConfirmSensorAction(sensor6, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB6"));

        Warrant w = _warrantMgr.getWarrant("Tinker");
        assertNotNull(w,"warrant");

        ThreadingUtil.runOnGUI(() -> {
            WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
            // WarrantTable.runTrain() returns a string that is not null if the
            // warrant can't be started
            assertNull(tableFrame.runTrain(w, Warrant.MODE_RUN),"Warrant starts"); // start run
        });

        JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Tinker starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB0", "OB1", "OB2", "OB3", "OB4", "OB5", "OB10"};
        OBlock block10 = _OBlockMgr.getOBlock("OB10");

        // Run the train, then checks end location
        assertDoesNotThrow( () -> {
            assertEquals(block10.getSensor(),
                NXFrameTest.runtimes(route1, _OBlockMgr),
                "Tinker after first leg");
        }, ("Exception running route1"));
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * route.length for return

        Warrant ww = _warrantMgr.getWarrant("Evers");

        JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Evers starts to move at 8th command");

        String[] route2 = {"OB7", "OB3", "OB2", "OB1"};
        OBlock block1 = _OBlockMgr.getOBlock("OB1");

        assertDoesNotThrow( () -> {
            assertEquals(block1.getSensor(),
                NXFrameTest.runtimes(route2, _OBlockMgr),
                "Evers after second leg");
        }, ("Exception running route2"));
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        Warrant www = _warrantMgr.getWarrant("Chance");

        JUnitUtil.waitFor(() -> {
            String m =  www.getRunningMessage();
            return m.endsWith("Cmd #8.") || m.endsWith("Cmd #9.") || m.endsWith("Cmd #10."); // in case runs fast
        }, "Chance starts to move at 8th command");

        String[] route3 = {"OB6", "OB3", "OB4", "OB5"};
        OBlock block5 = _OBlockMgr.getOBlock("OB5");

        assertDoesNotThrow( () -> {
            assertEquals(block5.getSensor(),
                NXFrameTest.runtimes(route3, _OBlockMgr),
                "Chance after third leg");
        }, ("Exception running route3"));
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        JFrameOperator jfo = new JFrameOperator(WarrantTableFrame.getDefault());
        jfo.requestClose();
        jfo.waitClosed();

        // JFrameOperator requestClose just hides panel, not disposing of it.
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame("NXWarrantTest").dispose();
            return true;
        });
        assertTrue(retVal);
        
    }

    @BeforeEach
    public void setUp() {
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

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        _warrantMgr = InstanceManager.getDefault(WarrantManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("Engineer(");

        _warrantMgr.dispose();
        _warrantMgr = null;
        InstanceManager.getDefault(WarrantManager.class).dispose();
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        _sensorMgr.dispose();
        _sensorMgr = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            List<ShutDownTask> list = new ArrayList<>();
            ShutDownManager sm = InstanceManager.getDefault(ShutDownManager.class);
            for (Runnable r : sm.getRunnables()) {
                if (r instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                    list.add((ShutDownTask)r);
                }
            }
            for ( ShutDownTask t : list) {
                sm.deregister(t);
            }
        }

        JUnitUtil.tearDown();
    }
}
