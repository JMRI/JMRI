package jmri.jmrit.dispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Steve Gigiel 2018
 *
 * Tests the LoadAtStartUp function for Dispatcher In addition it tests auto
 * running of a train.
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LoadAtStartUpTest {

    // Only one aat at a time
    private AutoActiveTrain aat = null;
    private static final double TOLERANCE = 0.0001;
    private static final int TRAIN_MOVE_TIME = 200; // ms

    @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
    @Test
    public void testShowAndClose() throws Exception {
        // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml");
        cm.load(f);
        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        
        // signal mast managerls -l
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

        checkAndSetSpeeds();

        //set sensors inactive
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (Sensor s : sm.getNamedBeanSet()) {
            s.setState(Sensor.INACTIVE);
        }
        // place train on layout
        sm.provideSensor("Occ South Platform").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ West Platform Switch").setState(Sensor.ACTIVE); // set blocker
        
        // and load. only one of 2 trains will load
        d.loadAtStartup();
        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);
        
        sm.provideSensor("Occ West Platform Switch").setState(Sensor.INACTIVE); // release blocker

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        JUnitUtil.waitFor(() -> {
            return d.getAllocatedSectionsList().size() == 4;
        }, "Allocate Sections ahead");
        assertThat(d.getAllocatedSectionsList()).withFailMessage("Allocated sections 4").hasSize(4);
        // get autoactivetrain object
        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();
        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("West End Div").getAspect().equals("Clear");
        }, "Signal West End Div now green");
        // check signals and speed
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("1 West End Div Signal Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("1 West To South  Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("1 South To East Signal Green").isEqualTo("Approach");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("1 East End Throat Signal Green").isEqualTo("Stop");
        float speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);

        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ West Platform Switch").setState(Sensor.ACTIVE);

        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("West End Div").getAspect().equals("Stop");
        }, "Signal Just passed stop");
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("2 West End Div Signal Stop").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("2 West To South  Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("2 South To East Signal Green").isEqualTo("Approach");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("2 East End Throat Signal Green").isEqualTo("Stop");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);

        sm.provideSensor("Occ West Block").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ South Platform").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ West Platform Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("South To East").getAspect().equals("Clear");
        }, "Signal South To East now Clear");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("3 West To South  Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("3 South To East Signal Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("3 East End Throat Signal Approach").isEqualTo("Approach");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);

        sm.provideSensor("Occ South Block").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("West To South").getAspect().equals("Stop");
        }, "Signal Just passed West To South now stop");
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("4 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("4 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("4 South To East Signal Green").isEqualTo("Clear");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("4 East End Throat Signal Green").isEqualTo("Approach");
        String strSigSpeed  = (String) smm.provideSignalMast("South To East").getSignalSystem().getProperty(smm.provideSignalMast("South To East").getAspect(), "speed");
        assertThat(InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(strSigSpeed)/100).isEqualTo(speedNormal);
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(0.6f); //The signal head indicates 1.0f but train is limited to a max of 0.6f

        sm.provideSensor("Occ West Block").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ East Block").setState(Sensor.ACTIVE);

        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("South To East").getAspect().equals("Stop");
        }, "Signal Just passed south to east now stop");
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("5 West End Div Signal Green").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("5 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("5 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("5 East End Throat Signal yellow").isEqualTo("Approach");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);

        sm.provideSensor("Occ South Block").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.provideSignalMast("East End Throat").getAspect().equals("Stop");
        }, "Signal Just passed east end throat now stop");
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("6 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("6 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("6 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("6 East End Throat Signal Red").isEqualTo("Stop");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);

        sm.provideSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        // No change
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("7 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("7 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("7 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("7 East End Throat Signal Red").isEqualTo("Stop");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);
        

        sm.provideSensor("Occ South Platform").setState(Sensor.ACTIVE);
        // signals no change, speed changes
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("8 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("8 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("8 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("8 East End Throat Signal Red").isEqualTo("Stop");
        speed = aat.getThrottle().getSpeedSetting();
        assertThat(speed).isEqualTo(speedRestricted);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);

        sm.provideSensor("Occ East Block").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ East Platform Switch").setState(Sensor.INACTIVE);
        // signals no change, speed changes to stop
        assertThat(smm.provideSignalMast("West End Div").getAspect()).withFailMessage("9 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("West To South").getAspect()).withFailMessage("9 West To South  Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("South To East").getAspect()).withFailMessage("9 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.provideSignalMast("East End Throat").getAspect()).withFailMessage("9 East End Throat Signal Red").isEqualTo("Stop");

        // train slows to stop
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
        }, "Signal Just passed east end throat now stop");

        JUnitUtil.waitFor(200);
        // check for next train note dcc name is original transit name has changed
        Assert.assertEquals("Next Train Load","1000 / SouthPlatFormReturnToSouthCC", d.getActiveTrainsList().get(0).getActiveTrainName());
        JUnitUtil.waitFor(2000);
        // its a new train.
        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedRestricted ) < TOLERANCE );
        }, "Started to move");
        sm.provideSensor("Occ East Block").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ South Platform").setState(Sensor.INACTIVE);

       JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedRestricted ) < TOLERANCE );
        }, "Continuing speed was "+ aat.getThrottle().getSpeedSetting() + " not " + speedRestricted);
        sm.provideSensor("Occ South Block").setState(Sensor.ACTIVE);
//        JUnitUtil.waitFor(200);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedRestricted ) < TOLERANCE );
        }, "Prepare to stop");
        sm.provideSensor("Occ East Platform Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(TRAIN_MOVE_TIME);
        sm.provideSensor("Occ East Block").setState(Sensor.INACTIVE);
        // train slows to stop
//      JUnitUtil.waitFor(200);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
        }, "Did not Stop " + at.getActiveTrainName() + " with throttle SpeedSetting " + aat.getThrottle().getSpeedSetting());
        //terminates at end
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");

        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
    }
    
    private float speedStopping = 0.0f;
    private float speedSlow = 0.0f;
    private float speedRestrictedSlow = 0.0f;
    private float speedRestricted = 0.0f;
    private float speedMedium = 0.0f;
    private float speedNormal = 0.0f;

    private void checkAndSetSpeeds() {
        // Check we have got the right signal map
        speedStopping = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName())/100.0f;
        assertEquals(0.1f, speedStopping, TOLERANCE );
        speedNormal = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Normal")/100.0f;
        assertEquals(1.0f, speedNormal, TOLERANCE );
        speedMedium = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Medium")/100.0f;
        assertEquals(0.5f, speedMedium, TOLERANCE );
        speedSlow = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Slow")/100.0f;
        assertEquals(0.31f, speedSlow, TOLERANCE );
        speedRestricted = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Restricted")/100.0f;
        assertEquals(0.35f, speedRestricted, TOLERANCE );
        speedRestrictedSlow = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("RestrictedSlow")/100.0f;
        assertEquals(0.1f, speedRestrictedSlow, TOLERANCE);
        assertEquals(SignalSpeedMap.PERCENT_THROTTLE, InstanceManager.getDefault(SignalSpeedMap.class)
                .getInterpretation(), TOLERANCE);
    }

    // Where in user space the "signals" file tree should live
    private static File outBaseTrainInfo = null;
    private static File outBaseSignal = null;

    // the file we create that we will delete
    private static Path outPathTrainInfo1 = null;
    private static Path outPathTrainInfo2 = null;
    private static Path outPathTrainInfo3 = null;
    private static Path outPathWarrentPreferences = null;

    @BeforeAll
    public static void doOnce() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();

        // set up users files in temp tst area
        outBaseTrainInfo = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        outBaseSignal = new File(FileUtil.getUserFilesPath(), "signal");
        try {
            FileUtil.createDirectory(outBaseTrainInfo);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTrainCW.xml").toPath();
                outPathTrainInfo1 = new File(outBaseTrainInfo, "TestTrainCW.xml").toPath();
                Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            }
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTrainCW_Return.xml").toPath();
                outPathTrainInfo3 = new File(outBaseTrainInfo, "TestTrainCW_Return.xml").toPath();
                Files.copy(inPath, outPathTrainInfo3, StandardCopyOption.REPLACE_EXISTING);
            }
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTrain.xml").toPath();
                outPathTrainInfo2 = new File(outBaseTrainInfo, "TestTrain.xml").toPath();
                Files.copy(inPath, outPathTrainInfo2, StandardCopyOption.REPLACE_EXISTING);
            }
            FileUtil.createDirectory(outBaseSignal);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/signal"),
                        "WarrantPreferences.xml").toPath();
                outPathWarrentPreferences = new File(outBaseSignal, "WarrantPreferences.xml").toPath();
                Files.copy(inPath, outPathWarrentPreferences, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw e;
        }
    }
    
    @AfterAll
    public static void unDoOnce() {
        try {
            Files.delete(outPathTrainInfo1);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        try {
            Files.delete(outPathTrainInfo2);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        try {
            Files.delete(outPathTrainInfo3);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        try {
            Files.delete(outPathWarrentPreferences);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        JUnitUtil.tearDown();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // JUnitUtil.setUp(); // already called in @BeforeAll
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.resetFileUtilSupport();
        // JUnitUtil.tearDown(); called in @AfterAll
    }
}
