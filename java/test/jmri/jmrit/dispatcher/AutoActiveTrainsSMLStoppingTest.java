package jmri.jmrit.dispatcher;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.Block;
import jmri.BlockManager;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author SG 2020
 *
 * Tests Auto Active Trains Stopping
 * There are four reasons trains stop normally.
 *     The track ahead is not allocated to the train.
 *     Theyve come to the end of the transit.
 *     The controlling signal is Zero speed (not tested here)
 *     They are paused by train action (not tested here)
 *     They are sent to manual mode. (not test here)
 * There is one abnormal reason (not test here)
 *     The train has no occupied section that has been allocated to it.
 * Stopping is cancelled when the train is stopping for a zero speed which then goes to a greater than zero speed before the train has
 *   completely stopped.
 *
 * This test is using SML. The loco is not speed profiled.
 * We test stopping by sensor, stopping by previous block going vacant (train fits)
 *   and stopping by entering block (train dont fit)
 * We test start to end no stops, start to end with a stop,
 *   and start to end with a stop thats aborted dues to conditions ahead changing.
 *
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class AutoActiveTrainsSMLStoppingTest {

    // Only one aat at a time
    private AutoActiveTrain aat = null;

    @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
    @Test
    public void testShowAndClose() throws Exception {
//        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLStoppingLayout3.xml");
        cm.load(f);

        // insure logix etc fire up (This is an SSL Layout)
        // InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // signal mast manager
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

        checkAndSetSpeedsSSL();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        //set sensors inactive
        //for (Sensor s : sm.getNamedBeanSet()) {
        //    s.setState( Block.UNOCCUPIED);
        //}
        // place train on layout
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);

        // *******************************************************************************
        //  Here start South - West - North -East South - Stop - using stopping sensors
        // *******************************************************************************
        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==4);
        },"Allocated sections should be 4");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
            }, "Failed to Start  South - West - North -East South - Stop - using stopping sensors");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"),Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Clear");
        }, "West-North Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        //JUnitUtil.setBeanStateAndWait(sm.getBlock("South 1"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-East-Cont").getAspect().equals("Stop");
        }, "Signal Sw-East-Cont Should go red");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - end - stop - stopping sensors.");
        // Set Stop Sensor
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - stop - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();


        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - using stopping sensors
        // *******************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        // trains loads and runs, 3 (east blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");
        assertEquals(sm.getSensor("Dir West Fwd").getState(),Sensor.ACTIVE);
        assertEquals(sm.getSensor("Dir West Rev").getState(),Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
            }, "Failed to Start South - West - North - Stop -East South - Stop - using stopping sensors");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.INACTIVE);  // and stopping sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");
        // set stopping sensor
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop North Rev"), Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. end - North stop - end - stopping sensors.");
        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED); // tail moves out
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop North Rev"), Sensor.INACTIVE); // and stopping sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-East-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Yellow");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        assertEquals(bm.getBlock("Block South 1").getState(),Block.OCCUPIED);
        assertEquals(bm.getBlock("Block East Switch").getState(),Block.UNOCCUPIED);
        
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - end - no stop - stopping sensors.");
        // Set Stop Sensor
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"),Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - using stopping sensors
        // *********************************************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Clear");

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 (East Blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");
        assertEquals(sm.getSensor("Dir West Fwd").getState(),Sensor.ACTIVE);
        assertEquals(sm.getSensor("Dir West Rev").getState(),Sensor.INACTIVE);

        JUnitUtil.waitFor(100);

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
            }, Float.toString(aat.getThrottle().getSpeedSetting()) + ":" + Float.toString(speedNormal));
            // "Failed to Start South - West - North - Start Stop - Cancel Stop -East South - Stop - using stopping sensors[{}]",
            // aat.getThrottle().getSpeedSetting());

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.INACTIVE);  // and stopping sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");

        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Approach");
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED); // tail moves out
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop North Rev"), Sensor.INACTIVE); // and stopping sensor
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Cont now red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - end - no stop - stopping sensors.");
        // Set Stop Sensor
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // Clear the stopping sensors from the blocks. - TRain will fit so speed goes to zero when previous block goes  inactive

        InstanceManager.getDefault(jmri.SectionManager.class).getSection("Section South 1").setReverseStoppingSensorName("");
        InstanceManager.getDefault(jmri.SectionManager.class).getSection("Section South 1").setForwardStoppingSensorName("");
        InstanceManager.getDefault(jmri.SectionManager.class).getSection("Section North").setReverseStoppingSensorName("");
        InstanceManager.getDefault(jmri.SectionManager.class).getSection("Section North").setForwardStoppingSensorName("");

        // *******************************************************************************
        //  Here start South - West - North -East South - Stop - using stop by previous block going inactive
        // *******************************************************************************
        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==4);
        },"Allocated sections should be 4");

        // set up loco address
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear begin - end - no stops - prev block inactive.");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to Start begin - begin - end - no stops - prev block inactive.");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Clear");
        }, "West-North Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - end - no stops - prev block inactive.");
        // clear penultimate Section.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - prev block inactive.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - using  prev block inactive.
        // *******************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 (east Blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "DFailed to Start begin - stop north - go - end - stop");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.INACTIVE);  // and stopping sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering north. begin - stop north - go - end - prev block inactive.");
        // free previous block
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. begin - North stop - go - end  prev block inactive.");

        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Approach");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to restart in north. begin - stop north - go - end - prev block inactive.");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Cont is Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - North stop - go - end  prev block inactive.");
        // Set Stop Sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - North stop - go - end  prev block inactive.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - using previous block
        // *********************************************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 (east Blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear begin - start stop north - go - end - stop on previous block");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to Start begin - start stop north - go - end - stop");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedMedium;
        }, "Failed to slow for yellow entering north. begin - start stop north - go - end - stop on previous block");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering north. begin - start stop north - go - end - stop on previous block");

        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Clear");
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to accelerate. begin - start stop north - go - end - stop on previous block");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);  // tail moves out
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div is Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedStopping;
        }, "Failed to slow entering south. begin - start stop north - go - end - stop on previous block");
        // Set Stop Sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end -begin - start stop north - go - end - stop on previous block");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        // *******************************************************************************
        //  Here start South - West - North -East South - Stop - - on entry train dont fit
        // *******************************************************************************

        // make all blocks zero length
        for (Block b : bm.getNamedBeanSet()) {
            b.setLength(0.0f);
        }

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==4);
        },"Allocated sections should be 4");

        // set up loco address
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to Start begin - begin - end - no stops - train dont fit.");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Clear");
        }, "West-North Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Clear");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End cont red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - train dont fit.");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED);

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - train dont fit
        // *******************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 (East Blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to start South - West - North - Stop -East South - Stop - train dont fit");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Stop South 1 Rev"), Sensor.INACTIVE);  // and stopping sensor
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
       JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. begin - North stop - go - end  train dont fit.");

        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Clear");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedMedium;
        }, "Failed to restart in north. begin - stop north - go - end - train dont fit.");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED); // free block behind
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Yellow");        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting()  == 0.0f;
        }, "Failed to Stop at end - begin - North stop - go - end  train dont fit");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED); // clean up to add next go round

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();
        // wait for cleanup to finish
        JUnitUtil.waitFor(200);

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - on entry train dont fit
        // *********************************************************************************************************

        // block track at East.
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 3 (east Blocked) allocated sections, the one we are in and 2 ahead.
        JUnitUtil.waitFor(() -> {
            return(d.getAllocatedSectionsList().size()==3);
        },"Allocated sections should be 3");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear");

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Clear");
        }, "Signal West End Div now Clear begin - start stop north - go - end - stop on previous block");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to Start begin - start stop north - go - end - stop");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Sw-West-Cont no red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == speedMedium;
        }, "Failed to slow for yellow - start stop north - go - end - Stop - on entry train dont fit");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West Switch"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West-North").getAspect().equals("Approach");
        }, "West-North Yellow");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to stop - start stop north - go - end - Stop - on entry train dont fit");

        // free up block ahead
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Approach");
        }, "Signal West End Div now Approach");
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() >= speedMedium;
        }, "Failed to accelerate. begin - start stop north - go - end - Stop - on entry train dont fit");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block West"), Block.UNOCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block North"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("North-East").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.OCCUPIED);
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East"), Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("Sw-West-Cont").getAspect().equals("Stop");
        }, "Signal West End Div now Red");
        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block South 1"), Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end -begin - start stop north - go - end - Stop - on entry train dont fit");

        JUnitUtil.setBeanStateAndWait(bm.getBlock("Block East Switch"), Block.UNOCCUPIED); // clean up

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

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

    private void checkAndSetSpeedsSSL() {
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
                        "SSL3TestTrain.xml").toPath();
                outPathTrainInfo1 = new File(outBaseTrainInfo, "SSL3TestTrain.xml").toPath();
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
