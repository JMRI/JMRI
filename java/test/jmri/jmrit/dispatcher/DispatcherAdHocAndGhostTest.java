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
    import jmri.util.JUnitUtil;

    import org.junit.jupiter.api.*;
    import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
    import org.netbeans.jemmy.operators.JButtonOperator;
    import org.netbeans.jemmy.operators.JFrameOperator;
    
    import static org.assertj.core.api.Assertions.assertThat;
    import static org.junit.jupiter.api.Assertions.assertEquals;

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")

public class DispatcherAdHocAndGhostTest {
    /**
     *
     * @author SG 2024
     *
     *
     */
        // Only one aat at a time
        private AutoActiveTrain aat = null;

        private static final double TOLERANCE = 0.0001;

        private static void increaseWaitForStep() {
            JUnitUtil.WAITFOR_DELAY_STEP = 20;
        }

        @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
        @Test
        public void testAdHocLoad() throws Exception {
             jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
            };
            // Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

            // more time for us less for the waitfor code...
            increaseWaitForStep();

            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

            // load layout file
            java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml");
            cm.load(f);

            InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

            // load dispatcher, with all the correct options
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
            // signal mast manager - but adhoc uses No Signals, number of sectionsallocated
            // SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

            checkAndSetSpeedsSML();
            SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            // BlockManager bm = InstanceManager.getDefault(BlockManager.class);

            JUnitUtil.setBeanStateAndWait(sm.provideSensor("Occ South Block"), Sensor.ACTIVE);

            d.loadTrainFromTrainInfo("SOUTH_EAST_SOUTHPLATORM.xml");

            assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

            ActiveTrain at = d.getActiveTrainsList().get(0);
            aat = at.getAutoActiveTrain();

            // trains loads and runs, 2 allocated sections, the one we are in and 1 ahead.
            JUnitUtil.waitFor(() -> {
                return(d.getAllocatedSectionsList().size()==2);
            },"Allocated sections should be 2");

            assertEquals(true, aat.getThrottle().getIsForward(),"Throttle should be in forward");
            JUnitUtil.waitFor(() -> {
                return (Math.abs(aat.getThrottle().getSpeedSetting() - speedNormal ) < TOLERANCE );
                }, "Failed To Start - Stop / Resume");

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
            speedStopping = InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName())/100.0f;
            assertEquals(0.1f, speedStopping, TOLERANCE);
            speedNormal = InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed("Normal")/100.0f;
            assertEquals(1.0f, speedNormal , TOLERANCE);
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
                            "SOUTH_EAST_SOUTHPLATORM.xml").toPath();
                    outPathTrainInfo1 = new File(outBaseTrainInfo, "SOUTH_EAST_SOUTHPLATORM.xml").toPath();
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
