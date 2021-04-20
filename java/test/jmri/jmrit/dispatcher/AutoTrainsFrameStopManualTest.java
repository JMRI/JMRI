package jmri.jmrit.dispatcher;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JSliderOperator;

import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;
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
     * @author SG 2020
     *
     * Tests AutoTrainsFrame Stop/Go Auto/Manual
     * Only 2 blocks are used so we have no sensor overruns
     * First engines is running backwards, so its in reverse gear, going forwards thru the transit
     * At the end it "reverses" back to beginning, so its in forward gear, going reverse thru the transit.
     *
     */
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public class AutoTrainsFrameStopManualTest {

        // Only one aat at a time
        private AutoActiveTrain aat = null;

        @Test
        public void testTaskAllocateRelease() {
            TaskAllocateRelease t = new TaskAllocateRelease(TaskAction.SCAN_REQUESTS);
            Assert.assertNotNull(t);
            Assert.assertEquals(TaskAction.SCAN_REQUESTS, t.getAction());
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testToManual() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // signal mast manager
            SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            // BlockManager bm = InstanceManager.getDefault(BlockManager.class);

            JUnitUtil.setBeanStateAndWait(sm.getSensor("Occ South Block"), Sensor.ACTIVE);

            // *******************************************************************************
            //  Here starts South going to west and back
            // *******************************************************************************
            d.loadTrainFromTrainInfo("SMLStopManualAuto.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();
            
            JFrameOperator atw = new JFrameOperator(Bundle.getMessage("TitleAutoTrains"));

            // trains loads and runs, 2 allocated sections, the one we are in and 1 ahead.
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==2);
            },"Allocated sections should be 2");

            JUnitUtil.waitFor(() -> {
                return smm.getSignalMast("South To West").getAspect().equals("Approach");
            }, "Signal South To West now Approach");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Failed To Start - Stop / Resume");
            
            // ****************************
            // Stop train with stop button, then resume
            // ****************************
            JButtonOperator boStop = new JButtonOperator(atw, Bundle.getMessage("StopButton"));
            boStop.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Speed goes to zero.");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            JButtonOperator boResume = new JButtonOperator(atw, Bundle.getMessage("ResumeButton"));
            boResume.push();
 
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Speed goes to medium");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            // ********************************
            // Go To Manual mode then back to auto
            // ********************************
            JButtonOperator boToManual = new JButtonOperator(atw, Bundle.getMessage("ToManualButton"));
            boToManual.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Failed to stop going to manual mode");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            JButtonOperator boToAuto = new JButtonOperator(atw, Bundle.getMessage("ToAutoButton"));
            boToAuto.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Failed to resume after auto");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            // ********************************
            // Go to manual, go to forward, back to auto
            // Direction should return to rev
            // ********************************** 
            boToManual.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Failed to going to Manual - for forward test");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            JRadioButtonOperator boFwd = new JRadioButtonOperator(atw, Bundle.getMessage("ForwardRadio"));
            boFwd.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "speed should still be zero when fwd button pushed");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getIsForward() == true;
                }, "Throttle should be in forwards");
            
            boToAuto.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Failed to resume after auto");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getIsForward() == false;
                }, "Throttle should have reverted to prior state");

            // ********************************
            // Go to manual, go to full throttle, back to auto
            // speed should go back to medium
            // ********************************** 
            boToManual.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Failed to go to manual for slider test");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            JSliderOperator sliderSpeed = new JSliderOperator(atw);
            sliderSpeed.setValue(100);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 1.0f;
                }, "Throttle should be foot to the floor");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");
            
            boToAuto.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "speed to should have reverted to medium after auto");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle not in Forward");

            // Now move train into next section. It will immediately reverse
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Occ West Block"), Sensor.ACTIVE);
            JUnitUtil.waitFor(() -> {
                return smm.getSignalMast("South To West").getAspect().equals("Stop");
            }, "Signal South To West now Stop");
            
            JUnitUtil.setBeanStateAndWait(sm.getSensor("Occ South Block"), Sensor.INACTIVE);
            
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Failed to stop at end");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getIsForward() == true;
                }, "Train reverses, by going into fwds at end");
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "And goes to medium");
        
            // ****************************
            // In Reverse transit Stop train with stop button, then resume
            // ****************************
            boStop.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Reverse Transit failed to stop");
            assertEquals(true, aat.getThrottle().getIsForward(),"Reverse Transit Throttle should be in Forward");

            boResume.push();
 
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Failed to resume after stop");
            assertEquals(true, aat.getThrottle().getIsForward(),"Reverse Transit Throttle should be in Forward");

            // ********************************
            // In reverse transit Go To Manual mode then back to auto
            // ********************************
            boToManual.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Stop going to manualmode");
            assertEquals(true, aat.getThrottle().getIsForward(),"Throttle should be in forward");

            boToAuto.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Resume after stop");
            assertEquals(true, aat.getThrottle().getIsForward(),"Throttle should be in forward");

            // ********************************
            // In reverse transit Go To Manual mode change gear to Reverse and then foot to the floor then back to auto
            // ********************************
            boToManual.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 0.0f;
                }, "Failed to stop going to manualmode");
            assertEquals(true, aat.getThrottle().getIsForward(),"Throttle should be in forward");

            JRadioButtonOperator boRev = new JRadioButtonOperator(atw, Bundle.getMessage("ReverseRadio"));
            boRev.push();
            sliderSpeed.setValue(100);
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == 1.0f;
                }, "Throttle should be foot to the floor");
            assertEquals(false, aat.getThrottle().getIsForward(),"Throttle should be in reverse");

            boToAuto.push();
            JUnitUtil.waitFor(() -> {
                return aat.getThrottle().getSpeedSetting() == speedMedium;
                }, "Failed to resume after stop");
            assertEquals(true, aat.getThrottle().getIsForward(),"Throttle should be in fwd");

            // cancel (terminate) the train. The train is set not to terminate at end
            // as we dont see the throttle go to zero if we do that.

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
                            "SMLStopManualAuto.xml").toPath();
                    outPathTrainInfo1 = new File(outBaseTrainInfo, "SMLStopManualAuto.xml").toPath();
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
            JUnitUtil.clearShutDownManager();
            JUnitUtil.resetWindows(false,false);
            JUnitUtil.resetFileUtilSupport();
            JUnitUtil.tearDown();
        }
    }
