package jmri.jmrit.dispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author SG 2021
 *
 * Tests Multiblock stopping.
 * First move a too long train into a two block section. Stops immediately
 * Move a very short train in, stop when it gets to far end
 * Move a medium train
 *
 */
@DisabledIfHeadless
public class AutoTrainsMultiBlockStopTest {

    // Only one aat at a time
    private AutoActiveTrain aat = null;

    private static final double TOLERANCE = 0.0001;

    // Adjust this if timeouts on the server
    private static final int waitInterval = 200;

    /*
     * "Head and Tail" train tests. The train, 40 long, runs Block 1 to Block 7
     * and stops in the final Section (Blocks 5, 6, 7). It fits in Block 7, so
     * Block 6 is the stopping block: the train must stop when its tail leaves
     * Block 6. The middle of the train is not detected.
     */
    private DispatcherFrame htDispatcher;
    private JFrameOperator htWindow;
    private Sensor[] htSensor;

    /**
     * Load the layout, start the train and run it until its head enters
     * Block 5 with the tail in Block 4.
     */
    private void startHeadAndTailToFinalSection() {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f)
        );
        Assertions.assertTrue(loaded);
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        htDispatcher = InstanceManager.getDefault(DispatcherFrame.class);
        htWindow = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        htSensor = new Sensor[8];
        for (int i = 1; i <= 7; i++) {
            htSensor[i] = sm.getSensor("Block " + i);
            assertNotNull(htSensor[i]);
        }

        JUnitUtil.setBeanStateAndWait(htSensor[1], Sensor.ACTIVE);
        htDispatcher.loadTrainFromTrainInfo("HEADTAIL40.xml");
        assertThat(htDispatcher.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);
        ActiveTrain at = htDispatcher.getActiveTrainsList().get(0);
        assertEquals(ActiveTrain.TrainDetection.TRAINDETECTION_HEADANDTAIL, at.getTrainDetection());
        aat = at.getAutoActiveTrain();

        // same steps and waits as testFwdAndReverse40, which runs this route as Entire Train
        JUnitUtil.waitFor(() -> {
            return(htDispatcher.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start");
        headInto(2);
        setHt(1, Sensor.INACTIVE);
        headInto(3);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        setHt(2, Sensor.INACTIVE);
        headInto(4);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        setHt(3, Sensor.INACTIVE);
        headInto(5);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
    }

    private void setHt(int block, int state) {
        JUnitUtil.setBeanStateAndWait(htSensor[block], state);
    }

    /**
     * Move the head into a Block and wait for the train to register it, so
     * the next head move is not seen out of order by strict train tracking.
     */
    private void headInto(int block) {
        setHt(block, Sensor.ACTIVE);
        Sensor sensor = htSensor[block];
        JUnitUtil.waitFor(() -> {
            return aat.getCurrentBlock() != null && aat.getCurrentBlock().getSensor() == sensor;
            }, "train did not register its head in Block " + block);
    }

    private void assertStillRunning(String message) {
        JUnitUtil.waitFor(waitInterval);
        assertEquals(speedStopping, aat.getThrottle().getSpeedSetting(), TOLERANCE, message);
    }

    private void assertStops(String message) {
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, message);
    }

    private void finishHeadAndTail() {
        JUnitUtil.waitFor(waitInterval);
        JButtonOperator bo = new JButtonOperator(htWindow, Bundle.getMessage("TerminateTrain"));
        bo.push();
        JUnitUtil.waitFor(waitInterval);
        assertThat((htDispatcher.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        htWindow.requestClose();
        JUnitUtil.dispose(htDispatcher);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
    }

    /**
     * Block 6 is short and spanned by the undetected middle of the train: it
     * goes unoccupied when the locomotive leaves it while the tail is still in
     * Block 5, occupied again when the tail arrives, and unoccupied when the
     * tail leaves.
     */
    @Test
    public void testHeadAndTailStopsWhenTailClearsStoppingBlock() {
        startHeadAndTailToFinalSection();
        setHt(4, Sensor.INACTIVE);   // tail into 5
        headInto(6);                 // head into 6
        headInto(7);                 // head into 7
        setHt(6, Sensor.INACTIVE);   // head leaves 6, tail still in 5
        assertStillRunning("Must not stop when the locomotive leaves the stopping block");
        setHt(6, Sensor.ACTIVE);     // tail into 6
        setHt(5, Sensor.INACTIVE);   // tail leaves 5
        assertStillRunning("Tail still in the stopping block");
        setHt(6, Sensor.INACTIVE);   // tail leaves 6
        assertStops("Should have stopped when the tail left Block 6");
        finishHeadAndTail();
    }

    /**
     * Short train: the tail is already in Block 6 when the locomotive leaves it,
     * so Block 6 only goes unoccupied once.
     */
    @Test
    public void testHeadAndTailShortTrainStopsWhenTailClearsStoppingBlock() {
        startHeadAndTailToFinalSection();
        setHt(4, Sensor.INACTIVE);
        headInto(6);
        setHt(5, Sensor.INACTIVE);   // tail into 6
        headInto(7);                 // head into 7
        assertStillRunning("Tail still in the stopping block");
        setHt(6, Sensor.INACTIVE);   // tail leaves 6
        assertStops("Should have stopped when the tail left Block 6");
        finishHeadAndTail();
    }

    /**
     * The tail loses detection in Block 5 after the locomotive has left Block 6:
     * Block 6 will not go occupied again, so the train must stop (short) rather
     * than run on.
     */
    @Test
    public void testHeadAndTailStopsIfTailLosesDetection() {
        startHeadAndTailToFinalSection();
        setHt(4, Sensor.INACTIVE);
        headInto(6);
        headInto(7);
        setHt(6, Sensor.INACTIVE);   // head leaves 6, tail still in 5
        assertStillRunning("Must not stop when the locomotive leaves the stopping block");
        setHt(5, Sensor.INACTIVE);   // tail lost, 6 stays unoccupied
        assertStops("Should have stopped when the tail lost detection");
        JUnitUtil.waitFor(() -> {
            return JUnitAppender.checkForMessageStartingWith("HT40: tail lost detection before reaching stopping block") != null;
            }, "warning logged");
        finishHeadAndTail();
    }

    @Test
    public void testFwdAndReverse40() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 40 long and fits in furthers block..
        // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f)
        );
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock5 = sm.getSensor("Block 5");
        Sensor sensorBlock6 = sm.getSensor("Block 6");
        Sensor sensorBlock7 = sm.getSensor("Block 7");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");
        Sensor sensorTrainReverseRestart = sm.getSensor("TrainReverseRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock5);
        assertNotNull(sensorBlock6);
        assertNotNull(sensorBlock7);
        assertNotNull(sensorTrainRestart);
        assertNotNull(sensorTrainReverseRestart);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait(sensorBlock1, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start left to right train fits in deepest block
        //  Also tests train done, and block tracking during stop.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("FWDREV40.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop on block 6 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.
        JUnitUtil.setBeanState(sensorTrainRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To restart 3 sections clear");
        // set train to not restart
        at.setResetWhenDone(false);

        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to begin section");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop on block 2 inactive.");
        JUnitUtil.waitFor(waitInterval);
        assertEquals("DONE",at.getStatusText(),"Train is not set as done");
        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        resetSensors(sm);

        // *******************************************************************************
        //  Here start left to right train fits in 2 deepest blocks
        // *******************************************************************************

        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("FWDREV80.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        assertEquals(speedStopping, aat.getThrottle().getSpeedSetting(), TOLERANCE , "Throttle should be in reverse");
        
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6, 5 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 5 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.
        JUnitUtil.setBeanState( sensorTrainRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To restart 3 sections clear");
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to begin section");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 2 inactive.");
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        resetSensors(sm);

        // *******************************************************************************
        //  Here start left to right train fits in all three blocks
        // *******************************************************************************
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("FWDREV120.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        // fully in block 4
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 4 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.- use the v4 reverse restart sensor.
        JUnitUtil.setBeanState( sensorTrainReverseRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To restart 3 sections clear");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to begin section");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2 , 3");
         JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2, 3, 4 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 4 inactive.");

        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        resetSensors(sm);

        // *******************************************************************************
        //  Here start right to left train fits in deepest block
        // *******************************************************************************
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("REVFWD40.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 2 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.
        JUnitUtil.setBeanState( sensorTrainRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
       JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 6 inactive.");

        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        resetSensors(sm);

        // *******************************************************************************
        //  Here start right to left train fits in deepest 2 blocks
        // *******************************************************************************
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("REVFWD80.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        // fully in block 4
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section block 3");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2, 3");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 3 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.
        JUnitUtil.setBeanState( sensorTrainRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        // fully in block 4
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section Block 5");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 5, 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6, 5 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 5 inactive.");
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        resetSensors(sm);

        // *******************************************************************************
        //  Here start right to left train fits in entire section
        // *******************************************************************************
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("REVFWD120.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();


        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1, 2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 2 inactive.");

        JUnitUtil.waitFor(waitInterval);

        // now return.
        JUnitUtil.setBeanState( sensorTrainRestart, Sensor.ACTIVE);

        // and reverses
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getIsForward() == false;
            },"Throttle should be in reverse");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Speed goes to medium in reverse");

        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "2 sections clear speed normal");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        // fully in block 4
        JUnitUtil.setBeanStateAndWait( sensorBlock5, Sensor.ACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Failed To Slow on entry to final section");
        JUnitUtil.setBeanStateAndWait( sensorBlock6, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 6");
        JUnitUtil.setBeanStateAndWait( sensorBlock7, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 7, 6, 5 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
            }, "Should have stop on block 6 inactive.");

        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testRoundAndRound40() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 40 long and fits in furthers block..
        // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock4_1);
        assertNotNull(sensorTrainRestart);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 3 stops at block 3 and goes again.
        // TRain fits in one block stops on block 2 inactive.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND3BLK40.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
       // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop on Block 2 inactive.");
        JUnitUtil.waitFor(waitInterval);
        
        JUnitUtil.setBeanStateAndWait( sensorTrainRestart, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        // JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop on Block 2 inactive.");

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    /**
     * As testRoundAndRound80, but the train is longer: its head enters Block 1,
     * at the end of the Transit, while its tail is still in Block 4. Section B4
     * is then both the Section behind the train, not yet released, and the next
     * Section of the new lap. The train must still start to stop in Section B1-3.
     */
    @Test
    public void testRoundAndRound80TailStillInBlock4() {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock4_1);
        assertNotNull(sensorTrainRestart);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 3 stops at block 3 and goes again.
        //  Train fits in 2 blocks stops on block 1 going inactive.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND3BLK80.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        // the train is longer: its head enters Block 1 while its tail is still in Block 4
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1 with the tail in Block 4");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still slowing in Block 1 after the tail has left Block 4");

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
    }

    @Test
    public void testRoundAndRound80() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 40 long and fits in furthers block..
        // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock4_1);
        assertNotNull(sensorTrainRestart);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 3 stops at block 3 and goes again.
        //  Train fits in 2 blocks stops on block 1 going inactive.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND3BLK80.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Still stop on block 1 inactive,  block 2, 3 still active");
        JUnitUtil.waitFor(waitInterval);
        
        JUnitUtil.setBeanStateAndWait( sensorTrainRestart, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Still stop on block 1 inactive,  block 2, 3 still active");

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testRoundAndRound120() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 40 long and fits in furthers block..
        // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock4_1);
        assertNotNull(sensorTrainRestart);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 3 stops at block 3 and goes again.
        //  Train fits in 3 blocks stops on block 4-1 going inactive.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND3BLK120.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 as we are allocating as far as we can
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Still stop on block 1 inactive,  block 2, 3 still active");
        JUnitUtil.waitFor(waitInterval);
        
        JUnitUtil.setBeanStateAndWait( sensorTrainRestart, Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // now can allocate Section1-3 again.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "2 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Slowing in Block 1");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedStopping ) < TOLERANCE );
            }, "Still going in block 1,2, 3 still active");
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Still stop on block 4-1 inactive,  block 1, 2, 3 still active");

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testRoundAndRound120_NoDelay() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 120 long and and doesnt fit in the section (2 blocks)..
        
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");

        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 2 stops at block 1 and goes again.
        //  Train does not fit in section stops on block 1 going active.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND2BLK120NoDelay.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(getSectionsClearAhead(at)==2);
        },"Allocated sections clear ahead should be 2");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // Section 1 know available
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        
        // no delay keep going into loop2 we in 1 and 4 -1 not released and one (4) clear ahead. 
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");
        JUnitUtil.waitFor(() -> {
            return(getSectionsClearAhead(at)==1);
        },"Allocated sections clear ahead should be 1");
        
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "Still stop on block 1 active,  block 2, 3 not active");
        JUnitUtil.waitFor(waitInterval);
        
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
                JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // 2 section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // now can allocate Section1-3 again.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "2 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        // begin 3rd loop keep going...done terminate test.        
        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testRoundAndRound120_ShortSection() {
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        // THe train is 120 long and and doesnt fit in the section (2 blocks)..
        
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStopRoundAndRound.xml");
        boolean loaded = Assertions.assertDoesNotThrow(() ->
            cm.load(f));
        Assertions.assertTrue(loaded);

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Sensor sensorBlock1 = sm.getSensor("Block 1");
        Sensor sensorBlock2 = sm.getSensor("Block 2");
        Sensor sensorBlock3 = sm.getSensor("Block 3");
        Sensor sensorBlock4 = sm.getSensor("Block 4");
        Sensor sensorBlock4_1 = sm.getSensor("Block 4-1");
        Sensor sensorTrainRestart = sm.getSensor("TrainRestart");

        assertNotNull(sensorBlock1);
        assertNotNull(sensorBlock2);
        assertNotNull(sensorBlock3);
        assertNotNull(sensorBlock4);
        assertNotNull(sensorBlock4_1);
        assertNotNull(sensorTrainRestart);

        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start Round And Round train starts block 2 stops at block 1 and goes again.
        //  Train does not fit in section stops on block 1 going active.
        // *******************************************************************************
        d.loadTrainFromTrainInfo("RNDRND2BLK120.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // Section 2 know available
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed normal "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting()) < TOLERANCE );
            }, "Failed To Stop and wait for sensor");

        assertEquals(0,getSectionsClearAhead(at),"No sections allocated ahead");
        JUnitUtil.setBeanStateAndWait( sensorTrainRestart, Sensor.ACTIVE);

        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        
        // manually reset restart sensor.
        JUnitUtil.setBeanStateAndWait( sensorTrainRestart, Sensor.INACTIVE);

        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.ACTIVE);
                JUnitUtil.waitFor(waitInterval);
        // only one section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock2, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // 2 section ahead.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock3, Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // now can allocate Section1-3 again.
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
            }, () -> "2 section clear speed normal "+ speedNormal  + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4_1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedMedium ) < TOLERANCE );
            }, () -> "1 section clear speed medium "+ speedMedium + " but was " + aat.getThrottle().getSpeedSetting());
        JUnitUtil.setBeanStateAndWait( sensorBlock4, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait( sensorBlock1, Sensor.ACTIVE);
        JUnitUtil.waitFor(waitInterval);
        // and stopped at end of second loop
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "And going again failed.");

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval/2);
        bo.push();
        
        // wait for cleanup to finish
        JUnitUtil.waitFor(waitInterval);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }
    // reset to inactive all sensors except clock.
    private void resetSensors(SensorManager sm) {
        for (Sensor sensor:sm.getNamedBeanSet()) {
            if (!sensor.getSystemName().equals("ISCLOCKRUNNING")) {
                JUnitUtil.setBeanState(sensor, Sensor.INACTIVE);
            }
        }
    }

    // count sections allocated but not yet entered
    private int getSectionsClearAhead(ActiveTrain at) {
        int sectionsAhead = 0;
        for (AllocatedSection allocatedSection : at.getAllocatedSectionList()) {
            if (!allocatedSection.getEntered()) {
                sectionsAhead++;
            }
        }
        return sectionsAhead;
    }

    private float speedMedium = 0.0f;
    private float speedStopping = 0.0f;
    private float speedSlow = 0.0f;
    private float speedRestrictedSlow = 0.0f;
    private float speedRestricted = 0.0f;
    private float speedNormal = 0.0f;

    private void checkAndSetSpeedsSML() {
        // Check we have got the right signal map
        speedStopping = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName())/100.0f;
        assertEquals(0.1f, speedStopping, TOLERANCE );
        speedNormal = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Normal")/100.0f;
        assertEquals(1.0f, speedNormal, TOLERANCE );
        speedMedium = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Medium")/100.0f;
        assertEquals(0.5f, speedMedium, TOLERANCE);
        speedSlow = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Slow")/100.0f;
        assertEquals(0.31f, speedSlow, TOLERANCE);
        speedRestricted = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("Restricted")/100.0f;
        assertEquals(0.35f, speedRestricted, TOLERANCE);
        speedRestrictedSlow = InstanceManager.getDefault(SignalSpeedMap.class)
                .getSpeed("RestrictedSlow")/100.0f;
        assertEquals(0.1f, speedRestrictedSlow, TOLERANCE);
        assertEquals(SignalSpeedMap.PERCENT_THROTTLE, InstanceManager.getDefault(SignalSpeedMap.class)
                .getInterpretation(), TOLERANCE);
    }

    // Where in user space the "signals" file tree should live
    private File outBaseTrainInfo = null;
    private File outBaseSignal = null;

    // the file we create that we will delete
    private Path outPathTrainInfo1 = null;
    private Path outPathWarrentPreferences = null;

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();
        // set up users files in temp tst area
        outBaseTrainInfo = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        outBaseSignal = new File(FileUtil.getUserFilesPath(), "signal");

        FileUtil.createDirectory(outBaseTrainInfo);
        {
            Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "FWDREV40.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "FWDREV40.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "FWDREV80.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "FWDREV80.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "FWDREV120.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "FWDREV120.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "REVFWD40.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "REVFWD40.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "HEADTAIL40.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "HEADTAIL40.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "REVFWD80.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "REVFWD80.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "REVFWD120.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "REVFWD120.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "RNDRND3BLK40.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "RNDRND3BLK40.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "RNDRND3BLK80.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "RNDRND3BLK80.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "RNDRND3BLK120.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "RNDRND3BLK120.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "RNDRND2BLK120.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "RNDRND2BLK120.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
            inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                    "RNDRND2BLK120NoDelay.xml").toPath();
            outPathTrainInfo1 = new File(outBaseTrainInfo, "RNDRND2BLK120NoDelay.xml").toPath();
            Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);

        }
        FileUtil.createDirectory(outBaseSignal);
        {
            Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/signal"),
                    "WarrantPreferences.xml").toPath();
            outPathWarrentPreferences = new File(outBaseSignal, "WarrantPreferences.xml").toPath();
            Files.copy(inPath, outPathWarrentPreferences, StandardCopyOption.REPLACE_EXISTING);
        }

        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false,false);
        
        try {
            Files.delete(outPathTrainInfo1);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        try {
            Files.delete(outPathWarrentPreferences);
        } catch  (IOException e) {
            // doesnt matter its gonezo
        }
        
        
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }
}
