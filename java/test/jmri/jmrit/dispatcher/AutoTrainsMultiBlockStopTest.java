package jmri.jmrit.dispatcher;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

    /**
     *
     * @author SG 2021
     *
     * Tests Multiblock stopping.
     * First move a too long train into a two block section. Stops imediatly
     * Move a very short train in, stop when it gets to far end
     * Move a medium train
     *
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public class AutoTrainsMultiBlockStopTest {

        // Only one aat at a time
        private AutoActiveTrain aat = null;
        
        // Adjust this if timeouts on the server
        private int waitInterval = 100;

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testFwdAndReverse40() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 40 long and fits in furthers block..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            // trains fills one block 
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in deepest block
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
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start - Stop / Resume");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 6 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To restart 3 sections clear");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to begin section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 2 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testFwdAndReverse80() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 40 long and fits in furthers block..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            // trains fills 2 blocks
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in 2 deepest blocks
            // *******************************************************************************
            d.loadTrainFromTrainInfo("FWDREV80.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();
            
            // trains loads and runs, 3 as we are allocating as far as we can
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==3);
            },"Allocated sections should be 3");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start - Stop / Resume");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            assertEquals(speedStopping, aat.getThrottle().getSpeedSetting(),"Throttle should be in reverse");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6, 5 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 5 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To restart 3 sections clear");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to begin section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2, 3 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 2 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testFwdAndReverse120() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 120 long and just fits in section ..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            // train is 3 blocks long
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in all three blocks
            // *******************************************************************************
            d.loadTrainFromTrainInfo("FWDREV120.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();
            
            // trains loads and runs, 3 as we are allocating as far as we can
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==3);
            },"Allocated sections should be 3");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            // fully in block 4
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 4 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To restart 3 sections clear");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to begin section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2 , 3");
             JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2, 3, 4 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 4 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testReverseFwd40() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 40 long and fits in furthers block..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in deepest block
            // *******************************************************************************
            d.loadTrainFromTrainInfo("REVFWD40.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();
            
            // trains loads and runs, 3 as we are allocating as far as we can
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==3);
            },"Allocated sections should be 3");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 2 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
           JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 6 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testReverseFwd80() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 40 long and fits in furthers block..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in deepest block
            // *******************************************************************************
            d.loadTrainFromTrainInfo("REVFWD80.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();

            // trains loads and runs, 3 as we are allocating as far as we can
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==3);
            },"Allocated sections should be 3");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            // fully in block 4
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section block 3");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2, 3");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2, 3 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 3 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            // fully in block 4
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section Block 5");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 5, 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6, 5 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 5 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testReverseFwd120() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // THe train is 40 long and fits in furthers block..
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // running with no signals
            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            
            // train fills start section
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here start left to right train fits in deepest section
            //  
            // *******************************************************************************
            d.loadTrainFromTrainInfo("REVFWD120.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();
            

            // trains loads and runs, 3 as we are allocating as far as we can
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==3);
            },"Allocated sections should be 3");

            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Failed To Start");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 2");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 1, 2 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 2 inactive.");

            JUnitUtil.waitFor(waitInterval);

            // now return.
            JUnitUtil.setBeanState(sm.getSensor("TrainRestart"), Sensor.ACTIVE);
            
            // and reverses
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
         
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "Speed goes to medium in reverse");

            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.ACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedNormal;
                }, "2 sections clear speed normal");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "1 section clear speed medium");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 2"), Sensor.INACTIVE);
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 3"), Sensor.INACTIVE);
            // fully in block 4 
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 5"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Failed To Slow on entry to final section");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 6"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 6");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 7"), Sensor.ACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedStopping;
                }, "Still going in block 7, 6, 5 still active");
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.INACTIVE);
            JUnitUtil.waitFor(waitInterval);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Should have stop on block 6 inactive.");

            JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
            bo.push();
            // wait for cleanup to finish
            JUnitUtil.waitFor(200);

            assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
            JFrameOperator aw = new JFrameOperator("AutoTrains");

            aw.requestClose();
            dw.requestClose();

            // cleanup window
            JUnitUtil.dispose(d);
        }
        private float speedMedium = 0.0f;
        private float speedStopping = 0.0f;
        private float speedSlow = 0.0f;
        private float speedRestrictedSlow = 0.0f;
        private float speedRestricted = 0.0f;
        private float speedNormal = 0.0f;

        private void checkAndSetSpeedsSML() {
            // Check we have got the right signal map
            speedStopping = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName())/100.0f;
            assertEquals(0.1f, speedStopping);
            speedNormal = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("Normal")/100.0f;
            assertEquals(1.0f, speedNormal );
            speedMedium = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("Medium")/100.0f;
            assertEquals(0.5f, speedMedium);
            speedSlow = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("Slow")/100.0f;
            assertEquals(0.31f, speedSlow);
            speedRestricted = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("Restricted")/100.0f;
            assertEquals(0.35f, speedRestricted);
            speedRestrictedSlow = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("RestrictedSlow")/100.0f;
            assertEquals(0.1f, speedRestrictedSlow);
            assertEquals(SignalSpeedMap.PERCENT_THROTTLE, jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getInterpretation());
        }

        // Where in user space the "signals" file tree should live
        private static File outBaseTrainInfo = null;
        private static File outBaseSignal = null;

        // the file we create that we will delete
        private static Path outPathTrainInfo1 = null;
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
                            "REVFWD80.xml").toPath();
                    outPathTrainInfo1 = new File(outBaseTrainInfo, "REVFWD80.xml").toPath();
                    Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);
                    inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                            "REVFWD120.xml").toPath();
                    outPathTrainInfo1 = new File(outBaseTrainInfo, "REVFWD120.xml").toPath();
                    Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);

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
                Files.delete(outPathWarrentPreferences);
            } catch  (IOException e) {
                // doesnt matter its gonezo
            }
        }

        @BeforeEach
        public void setUp() throws Exception {
            JUnitUtil.setUp();
            JUnitUtil.resetFileUtilSupport();
            JUnitUtil.resetProfileManager();
            JUnitUtil.resetInstanceManager();
            JUnitUtil.initRosterConfigManager();
            JUnitUtil.initDebugThrottleManager();
        }

        @AfterEach
        public void tearDown() throws Exception {
            JUnitUtil.deregisterBlockManagerShutdownTask();
            JUnitUtil.deregisterEditorManagerShutdownTask();
            JUnitUtil.resetWindows(false,false);
            JUnitUtil.resetFileUtilSupport();
            JUnitUtil.tearDown();
        }
    }
