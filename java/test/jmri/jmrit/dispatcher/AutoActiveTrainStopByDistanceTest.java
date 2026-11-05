package jmri.jmrit.dispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jmri.Block;
import jmri.BlockManager;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Stop by distance with a speed profile and a stopping sensor ("combined
 * mode"): the train slows to its minimum reliable operating speed over the
 * distance, then stops when the stopping sensor goes active.
 * <p>
 * The train runs Block 1 to Block 7 on the MultiBlockStop layout. The final
 * Section, B5-7, has three Blocks; its forward stopping sensor is set by the
 * test, which also adds a Block 8 beyond Block 7, so that a train overrunning
 * its stop has somewhere to go.
 */
@DisabledIfHeadless
public class AutoActiveTrainStopByDistanceTest {

    private static final double TOLERANCE = 0.0001;
    private static final float MIN_RELIABLE = 0.2f;

    private DispatcherFrame dispatcher;
    private JFrameOperator dispatcherWindow;
    private AutoActiveTrain aat;
    private Sensor[] blockSensor;
    private Sensor stopSensor;
    private Sensor block8Sensor;

    /**
     * Load the layout, give Section B5-7 a forward stopping sensor, and start a
     * roster train with a speed profile and a stop distance of 500 mm.
     */
    private void startTrain() {
        startTrain(500.0f, null);
    }

    /**
     * Load the layout and start the train.
     *
     * @param stopDistanceMm the train's stop distance; 0 for none
     * @param stoppingSensorName the forward stopping sensor of Section B5-7,
     *                           or null for a sensor of its own
     */
    private void startTrain(float stopDistanceMm, String stoppingSensorName) {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
        Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> cm.load(f)));
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        blockSensor = new Sensor[8];
        for (int i = 1; i <= 7; i++) {
            blockSensor[i] = sm.getSensor("Block " + i);
            assertNotNull(blockSensor[i]);
        }
        // Block 8, beyond Block 7: a train would enter Section B5-7 from it travelling in reverse
        block8Sensor = sm.provideSensor("IS_BLOCK8");
        JUnitUtil.setBeanStateAndWait(block8Sensor, Sensor.INACTIVE);

        stopSensor = sm.provideSensor(stoppingSensorName == null ? "IS_STOP_B7" : stoppingSensorName);
        JUnitUtil.setBeanStateAndWait(stopSensor, Sensor.INACTIVE);
        Section finalSection = InstanceManager.getDefault(SectionManager.class).getSection("Section B5-7");
        assertNotNull(finalSection);
        finalSection.setForwardStoppingSensorName(stopSensor.getSystemName());

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        Block block8 = bm.provideBlock("IB_BLOCK8");
        block8.setUserName("Block8");
        block8.setSensor(block8Sensor.getSystemName());
        block8.setLength(279.4f);
        Block block7 = bm.getBlock("Block7");
        assertNotNull(block7);
        block7.addPath(new jmri.Path(block8, jmri.Path.EAST, jmri.Path.WEST));
        block8.addPath(new jmri.Path(block7, jmri.Path.WEST, jmri.Path.EAST));
        EntryPoint fromBlock8 = new EntryPoint(block7, block8, "West");
        fromBlock8.setTypeReverse();
        finalSection.addToReverseList(fromBlock8);

        RosterEntry entry = new RosterEntry();
        entry.setId("StopByDistance");
        entry.setDccAddress("3");
        RosterSpeedProfile profile = new RosterSpeedProfile(entry);
        for (int step = 1; step <= 10; step++) {
            float throttle = step / 10.0f;
            profile.setForwardSpeed(throttle, throttle * 200.0f);  // 200 mm/s at full throttle
            profile.setReverseSpeed(throttle, throttle * 200.0f);
        }
        entry.setSpeedProfile(profile);
        Roster.getDefault().addEntry(entry);

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        dispatcher = InstanceManager.getDefault(DispatcherFrame.class);
        dispatcherWindow = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        JUnitUtil.setBeanStateAndWait(blockSensor[1], Sensor.ACTIVE);
        dispatcher.loadTrainFromTrainInfo("FWDREV40.xml", DispatcherFrame.OVERRIDETYPE_ROSTER, "StopByDistance");
        assertEquals(1, dispatcher.getActiveTrainsList().size(), "Train loaded");
        ActiveTrain at = dispatcher.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();
        assertNotNull(at.getRosterEntry(), "roster entry");
        aat.setMinReliableOperatingSpeed(MIN_RELIABLE);
        aat.setStopByDistanceMm(stopDistanceMm);

        JUnitUtil.waitFor(() -> dispatcher.getAllocatedSectionsList().size() == 3, "Allocated sections should be 3");
        JUnitUtil.waitFor(() -> aat.getThrottle() != null && aat.getThrottle().getSpeedSetting() > 0.0f,
                "Failed to start");
    }

    private void set(int block, int state) {
        JUnitUtil.setBeanStateAndWait(blockSensor[block], state);
    }

    /** Move the head into a Block and wait for the train to register it. */
    private void headInto(int block) {
        set(block, Sensor.ACTIVE);
        Sensor sensor = blockSensor[block];
        JUnitUtil.waitFor(() -> aat.getCurrentBlock() != null && aat.getCurrentBlock().getSensor() == sensor,
                "train did not register its head in Block " + block);
    }

    /** Run the train from Block 1 until its head is in Block 5, the entry to the final Section. */
    private void runToFinalSection() {
        headInto(2);
        set(1, Sensor.INACTIVE);
        headInto(3);
        set(2, Sensor.INACTIVE);
        headInto(4);
        set(3, Sensor.INACTIVE);
        headInto(5);
        set(4, Sensor.INACTIVE);
    }

    private float speed() {
        return aat.getThrottle().getSpeedSetting();
    }

    private void finish() {
        JUnitUtil.waitFor(200);
        new JButtonOperator(dispatcherWindow, Bundle.getMessage("TerminateTrain")).push();
        JUnitUtil.waitFor(() -> dispatcher.getActiveTrainsList().isEmpty(), "All trains terminated");
        new JFrameOperator("AutoTrains").requestClose();
        dispatcherWindow.requestClose();
        JUnitUtil.dispose(dispatcher);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
    }

    /**
     * The train slows to its minimum reliable speed, keeps that speed, and
     * stops when the stopping sensor goes active.
     */
    @Test
    public void testSlowsToMinimumThenStopsOnSensor() {
        startTrain();
        runToFinalSection();
        JUnitUtil.waitFor(() -> Math.abs(speed() - MIN_RELIABLE) < 0.01f,
                () -> "did not slow to the minimum reliable speed, speed " + speed());
        JUnitUtil.waitFor(500);
        assertEquals(MIN_RELIABLE, speed(), 0.01f, "keeps the minimum reliable speed until the sensor");
        JUnitUtil.setBeanStateAndWait(stopSensor, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> speed() < TOLERANCE, () -> "did not stop on the sensor, speed " + speed());
        finish();
    }

    /**
     * While the train approaches its stop, it keeps following its own Blocks:
     * each Block its head enters in the final Section is registered.
     */
    @Test
    public void testFollowsBlocksDuringTheApproach() {
        startTrain();
        runToFinalSection();
        headInto(6);
        set(5, Sensor.INACTIVE);
        headInto(7);
        set(6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(stopSensor, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> speed() < TOLERANCE, () -> "did not stop on the sensor, speed " + speed());
        finish();
    }

    /**
     * The stopping sensor fails and the train runs on into Block 8, beyond its
     * Section, which was free: it must stop there and then.
     */
    @Test
    public void testStopsWhenItEntersAFreeBlockBeyondItsSection() {
        startTrain();
        runToFinalSection();
        JUnitUtil.waitFor(() -> Math.abs(speed() - MIN_RELIABLE) < 0.01f,
                () -> "did not slow to the minimum reliable speed, speed " + speed());
        headInto(6);
        set(5, Sensor.INACTIVE);
        headInto(7);
        set(6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(block8Sensor, Sensor.ACTIVE);  // stopping sensor never fired
        JUnitUtil.waitFor(() -> speed() < TOLERANCE,
                () -> "did not stop on entering the Block beyond its Section, speed " + speed());
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("AAA: stopping sensor IS_STOP_B7 has not stopped the train");
        finish();
    }

    /**
     * The stopping sensor fails and the Block beyond the Section is already
     * occupied, so the train entering it cannot be seen: by reckoning from its
     * speed, the train must stop at the end of its Section.
     */
    @Test
    public void testStopsByReckoningWhenTheBlockBeyondIsOccupied() {
        // the train crawls through Block 7 (279.4 mm) at 40 mm/s, about 7 s: allow nearly three times that
        JUnitUtil.WAITFOR_MAX_DELAY = 20000;
        startTrain();
        JUnitUtil.setBeanStateAndWait(block8Sensor, Sensor.ACTIVE);  // a train standing in Block 8
        runToFinalSection();
        JUnitUtil.waitFor(() -> Math.abs(speed() - MIN_RELIABLE) < 0.01f,
                () -> "did not slow to the minimum reliable speed, speed " + speed());
        headInto(6);
        set(5, Sensor.INACTIVE);
        headInto(7);
        set(6, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> speed() < TOLERANCE,
                () -> "did not stop by reckoning at the end of its Section, speed " + speed());
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("AAA: stopping sensor IS_STOP_B7 has not stopped the train");
        finish();
    }

    /**
     * Without a stop distance the train stops at its stopping sensor in the
     * usual way, and the Block beyond its Section is watched just the same.
     */
    @Test
    public void testWithoutStopDistanceStopsWhenItEntersAFreeBlockBeyondItsSection() {
        startTrain(0.0f, null);
        runToFinalSection();
        headInto(6);
        set(5, Sensor.INACTIVE);
        headInto(7);
        set(6, Sensor.INACTIVE);
        Assertions.assertTrue(speed() > TOLERANCE, "still running, waiting for the stopping sensor");
        JUnitUtil.setBeanStateAndWait(block8Sensor, Sensor.ACTIVE);  // stopping sensor never fired
        JUnitUtil.waitFor(() -> speed() < TOLERANCE,
                () -> "did not stop on entering the Block beyond its Section, speed " + speed());
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("AAA: stopping sensor IS_STOP_B7 has not stopped the train");
        finish();
    }

    /**
     * Where the stopping sensor is the detection sensor of the Block beyond
     * the Section, the train is meant to enter that Block: it stops on the
     * sensor, and nothing is reported.
     */
    @Test
    public void testStoppingSensorOfTheBlockBeyondIsNotABackstop() {
        startTrain(500.0f, "IS_BLOCK8");
        runToFinalSection();
        JUnitUtil.waitFor(() -> Math.abs(speed() - MIN_RELIABLE) < 0.01f,
                () -> "did not slow to the minimum reliable speed, speed " + speed());
        headInto(6);
        set(5, Sensor.INACTIVE);
        headInto(7);
        set(6, Sensor.INACTIVE);
        JUnitUtil.waitFor(1000);
        assertEquals(MIN_RELIABLE, speed(), 0.01f, "keeps going to the stopping sensor in Block 8");
        JUnitUtil.setBeanStateAndWait(block8Sensor, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> speed() < TOLERANCE, () -> "did not stop on the sensor, speed " + speed());
        finish();
    }

    private Path outPathTrainInfo;
    private Path outPathWarrantPreferences;

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();
        File outBaseTrainInfo = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        File outBaseSignal = new File(FileUtil.getUserFilesPath(), "signal");
        FileUtil.createDirectory(outBaseTrainInfo);
        outPathTrainInfo = new File(outBaseTrainInfo, "FWDREV40.xml").toPath();
        Files.copy(new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                "FWDREV40.xml").toPath(), outPathTrainInfo, StandardCopyOption.REPLACE_EXISTING);
        FileUtil.createDirectory(outBaseSignal);
        outPathWarrantPreferences = new File(outBaseSignal, "WarrantPreferences.xml").toPath();
        Files.copy(new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/signal"),
                "WarrantPreferences.xml").toPath(), outPathWarrantPreferences, StandardCopyOption.REPLACE_EXISTING);
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() throws IOException {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false);
        Files.deleteIfExists(outPathTrainInfo);
        Files.deleteIfExists(outPathWarrantPreferences);
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }
}
