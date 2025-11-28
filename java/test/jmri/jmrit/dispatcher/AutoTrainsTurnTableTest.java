package jmri.jmrit.dispatcher;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author SG 2025
 *
 * Tests Multiblock stopping.
 * First move a too long train into a two block section. Stops immediately
 * Move a very short train in, stop when it gets to far end
 * Move a medium train
 *
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class AutoTrainsTurnTableTest {

    // Only one aat at a time
    private AutoActiveTrain aat = null;

    private static final double TOLERANCE = 0.0001;

    // Adjust this if timeouts on the server
    private static final int waitInterval = 200;

    @Test
    public void testML_T3() {
        /*
         * runs trail from outside across the turntable to stall, utilizing stopallocating sensor on turn table.
         */
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/DistpatcherTurnTable.xml");
        Assertions.assertDoesNotThrow(() -> {
            cm.load(f);
        });

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("ML"), Sensor.ACTIVE);

        //force turntable to unknown/ideterminate
        resetTurnTable(sm);

        d.loadTrainFromTrainInfo("TestTurnTable_ML_TT3.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        //reset turntable NB the real turntable sets its sensors automatically on getting a new position.
        resetTurnTable(sm);
         // trains loads and runs - stops allocating on stopallocating sensor in turntable.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");
        List<String> sectionsAllocated = Arrays.asList("ML", "TT2", "TT");
        JUnitUtil.waitFor(() -> {
            return(checkSections(at,sectionsAllocated));
        },"Allocated sections wrong");


        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("ML"), Sensor.INACTIVE);
         JUnitUtil.waitFor(waitInterval);
         JUnitUtil.waitFor(() -> {
             return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
             }, "Should have stop on block 2 inactive.");

        JUnitUtil.waitFor(waitInterval);

        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1502");
        JUnitUtil.waitFor(() -> {
            return (t.getCommandedState() == Turnout.THROWN);
        },"Turnout should have been requested");

        Assert.assertEquals("Currently turnout unknown state", Turnout.INCONSISTENT, t.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1502-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently turnout thrown state", Turnout.THROWN, t.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "TurnTable set "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.INACTIVE);

        //on stop transit sets the stop alloc inactive and TT3 is allocated.

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop waiting for turntable to go the ray3.");

        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==2);
        },"Allocated sections should be 2");
        JUnitUtil.waitFor(() -> {
            return(checkSections(at,Arrays.asList("TT","TT3")));
        },"Allocated after stopalloc inactive sections wrong");

        resetTurnTable(sm);
        JUnitUtil.waitFor(waitInterval);

         Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1503");
         JUnitUtil.waitFor(() -> {
             return (t2.getCommandedState() == Turnout.THROWN);
         },"Turnout Ray 3 should have been requested");

        Assert.assertEquals("Turnout should have been requested", Turnout.THROWN, t2.getCommandedState());
        Assert.assertEquals("Currently turnout unknown state", Turnout.INCONSISTENT, t2.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1503-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently turnout thrown state", Turnout.THROWN, t2.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "TurnTable set Ray 3 "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT3"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should have stop waiting for turntable to go the ray3.");

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
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testT1_ML_AndBack() {
        /*
         * runs trail from ray across the turntable to somewhere else and back..
         */
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/DistpatcherTurnTable.xml");
        Assertions.assertDoesNotThrow(() -> {
            cm.load(f);
        });

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        // trains fills one block
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT1"), Sensor.ACTIVE);

        //force turntable to unknown/ideterminate
        resetTurnTable(sm);

        d.loadTrainFromTrainInfo("TestTurnTable_TT1_ML.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        //reset turntable NB the real turntable sets its sensors automatically on getting a new position.
        resetTurnTable(sm);
         // trains loads and runs - stops allocating on stopallocating sensor in turntable.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==2);
        },"Allocated sections should be 2");
        List<String> sectionsAllocated = Arrays.asList("TT1", "TT");
        JUnitUtil.waitFor(() -> {
            return(checkSections(at,sectionsAllocated));
        },"Allocated sections wrong");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should waiting for turntable to go the ray1.");

        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1501");
        JUnitUtil.waitFor(() -> {
            return (t.getCommandedState() == Turnout.THROWN);
        },"Turnout ray 1 should have been requested");
        Assert.assertEquals("Currently Ray 1 turnout unknown state", Turnout.INCONSISTENT, t.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1501-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently Ray 1 turnout thrown state", Turnout.THROWN, t.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        resetTurnTable(sm);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT1"), Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
        }, "Should have stop in turntable.");

        JUnitUtil.waitFor(waitInterval);

        Turnout t2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1502");
        JUnitUtil.waitFor(() -> {
             return (t2.getCommandedState() == Turnout.THROWN);
        },"Turnout ray 2 should have been requested");
        Assert.assertEquals("Currently Ray 2 turnout unknown state", Turnout.INCONSISTENT, t2.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1502-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently Ray 2 turnout thrown state", Turnout.THROWN, t2.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "TurnTable set "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.INACTIVE);

        JUnitUtil.waitFor(waitInterval);

        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==2);
        },"Allocated sections should be 2");
        JUnitUtil.waitFor(() -> {
            return(checkSections(at,Arrays.asList("TT2","ML")));
        },"Allocated after stopalloc inactive sections wrong");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Keep Going "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("ML"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Stopping at end failed");

        JUnitUtil.waitFor(waitInterval);

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("Return"), Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Depart return trip "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("ML"), Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "keep going turntable left in correct state "+ speedSlow + " but was " + aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.ACTIVE);
        resetTurnTable(sm);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should waiting for turntable to go the ray1.");

        Turnout t3 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1501");
        JUnitUtil.waitFor(() -> {
            return (t3.getCommandedState() == Turnout.THROWN);
        },"Turnout ray 1 should have been requested");
        Assert.assertEquals("Currently Ray 1 turnout unknown state", Turnout.INCONSISTENT, t3.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1501-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently Ray 1 turnout thrown state", Turnout.THROWN, t3.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");

        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT1"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.INACTIVE);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Stopping at end of return failed");

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
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void test_TT_ML() {
        /*
         * Starts in turntable..
         */
         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        File f = new File("java/test/jmri/jmrit/dispatcher/DistpatcherTurnTable.xml");
        Assertions.assertDoesNotThrow(() -> {
            cm.load(f);
        });

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // running with no signals
        checkAndSetSpeedsSML();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        //reset turntable NB the real turntable sets its sensors automatically on getting a new position.
        resetTurnTable(sm);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1501-B"), Sensor.INACTIVE);

        d.loadTrainFromTrainInfo("TestTurnTable_TT_ML.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();


         // trains loads and runs - stops allocating on stopallocating sensor in turntable.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");
        List<String> sectionsAllocated = Arrays.asList("TT","TT2","ML");
        JUnitUtil.waitFor(() -> {
            return(checkSections(at,sectionsAllocated));
        },"Allocated sections wrong");

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
            }, "Should waiting for turntable to go the ray 2.");

        Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1502");
        JUnitUtil.waitFor(() -> {
            return (t1.getCommandedState() == Turnout.THROWN);
        },"Turnout ray 1 should have been requested");
        Assert.assertEquals("Currently Ray 1 turnout unknown state", Turnout.INCONSISTENT, t1.getKnownState());
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1502-B"), Sensor.INACTIVE);
        Assert.assertEquals("Currently Ray 1 turnout thrown state", Turnout.THROWN, t1.getKnownState());

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        resetTurnTable(sm);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT"), Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);

        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() - speedSlow ) < TOLERANCE );
            }, "Failed To Start - Stop / Resume");
        resetTurnTable(sm);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("ML"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("TT2"), Sensor.INACTIVE);
        JUnitUtil.waitFor(waitInterval);
        JUnitUtil.waitFor(() -> {
            return (Math.abs(aat.getThrottle().getSpeedSetting() ) < TOLERANCE );
        }, "Should have stop in turntable.");


        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");
        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }



    private void resetTurnTable(SensorManager sm) {
        //The real turntable that this is based upon does this before processing command
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1501-B"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1501-A"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1502-B"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1502-A"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1503-B"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.provideSensor("1503-A"), Sensor.ACTIVE);
    }



    private boolean checkSections(ActiveTrain at, List<String> sectionNames) {
        if (at.getAllocatedSectionList().size() != sectionNames.size()) {
            return false;
        }
        for (String sectionName : sectionNames) {
            boolean found = false;
            for (AllocatedSection as : at.getAllocatedSectionList()) {
                if (as.getSectionName().equals(sectionName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
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
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();
        // set up users files in temp tst area
        outBaseTrainInfo = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        outBaseSignal = new File(FileUtil.getUserFilesPath(), "signal");
        try {
            FileUtil.createDirectory(outBaseTrainInfo);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTurnTable_ML_TT3.xml").toPath();
                outPathTrainInfo1 = new File(outBaseTrainInfo, "TestTurnTable_ML_TT3.xml").toPath();
                Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);

                inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTurnTable_TT1_ML.xml").toPath();
                outPathTrainInfo1 = new File(outBaseTrainInfo, "TestTurnTable_TT1_ML.xml").toPath();
                Files.copy(inPath, outPathTrainInfo1, StandardCopyOption.REPLACE_EXISTING);

                inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "TestTurnTable_TT_ML.xml").toPath();
                outPathTrainInfo1 = new File(outBaseTrainInfo, "TestTurnTable_TT_ML.xml").toPath();
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

        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
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
