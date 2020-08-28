package jmri.jmrit.dispatcher;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;

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
 * This test is using SSL. The loco is not speed profiled.
 * We test stopping by sensor, stopping by previous block going vacant (train fits)
 *   and stopping by entering block (train dont fit)
 * We test start to end no stops, start to end with a stop,
 *   and start to end with a stop thats aborted dues to conditions ahead changing.
 *
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class AutoActiveTrainsStoppingTest {

    // Only one aat at a time
    private AutoActiveTrain aat = null;
    @SuppressWarnings("null")  // spec says cannot happen, everything defined in test data.
    @Test
    public void testShowAndClose() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load layout file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSSLLayout.xml");
        cm.load(f);

        // insure logix etc fire up (This is an SSL Layout)
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherSSLOptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // signal head manager
        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);

        //set sensors inactive
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (Sensor s : sm.getNamedBeanSet()) {
            s.setState(Sensor.INACTIVE);
        }
        // place train on layout
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);

        // *******************************************************************************
        //  Here start South - West - North -East South - Stop - using stopping sensors
        // *******************************************************************************
        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        ActiveTrain at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        assertThat(d.getAllocatedSectionsList()).withFailMessage("Section South 1").hasSize(4);
        // set up loco address
        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting()== 0.8f;
        }, "Failed to slow entering south. begin - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - end - stop - stopping sensors.");
        // Set Stop Sensor
        sm.getSensor("Stop South 1 Rev").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - stop - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();

        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - using stopping sensors
        // *******************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");
        // set stopping sensor
        sm.getSensor("Stop North Fwd").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. end - North stop - end - stopping sensors.");
        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);

        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Stop North Fwd").setState(Sensor.INACTIVE); // and stopping sensor
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - end - no stop - stopping sensors.");
        // Set Stop Sensor
        sm.getSensor("Stop South 1 Rev").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - using stopping sensors
        // *********************************************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");

        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.7f && aat.getThrottle().getSpeedSetting() < 0.9f;
        }, "Failed to slow entering north. begin - stop north - go - end - stop");

        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Stop North Rev").setState(Sensor.ACTIVE); // and stopping sensor
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - end - no stop - stopping sensors.");
        // Set Stop Sensor
        sm.getSensor("Stop South 1 Rev").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - stopping sensors.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

        bo.push();

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
        assertThat(d.getAllocatedSectionsList()).withFailMessage("Section South 1").hasSize(4);
        // set up loco address
        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green begin - end - no stops - prev block inactive.");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - begin - end - no stops - prev block inactive.");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - end - no stops - prev block inactive.");
        // clear penultimate Section.
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - prev block inactive.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();

        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - using  prev block inactive.
        // *******************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering north. begin - stop north - go - end - prev block inactive.");
        // free previous block
        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. begin - North stop - go - end  prev block inactive.");

        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to restart in north. begin - stop north - go - end - prev block inactive.");

        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - North stop - go - end  prev block inactive.");
        // Set Stop Sensor
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - North stop - go - end  prev block inactive.");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - using previous block
        // *********************************************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green begin - start stop north - go - end - stop on previous block");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - start stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering north. begin - start stop north - go - end - stop on previous block");

        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.7f && aat.getThrottle().getSpeedSetting() < 0.9f;
        }, "Failed to accelerate. begin - start stop north - go - end - stop on previous block");

        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.3f && aat.getThrottle().getSpeedSetting() < 0.4f;
        }, "Failed to slow entering south. begin - start stop north - go - end - stop on previous block");
        // Set Stop Sensor
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end -begin - start stop north - go - end - stop on previous block");

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

        bo.push();

        // *******************************************************************************
        //  Here start South - West - North -East South - Stop - - on entry train dont fit
        // *******************************************************************************

        // make all blocks zero length
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        for (Block b : bm.getNamedBeanSet()) {
            b.setLength(0.0f);
        }

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        assertThat(d.getAllocatedSectionsList()).withFailMessage("Section South 1").hasSize(4);
        // set up loco address
        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - begin - end - no stops - train dont fit.");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end - begin - end - no stops - train dont fit.");
        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE);

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();

        // *******************************************************************************
        //  Here start South - West - North - Stop -East South - Stop - train dont fit
        // *******************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);
        // slows to stopping speed
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at North. begin - North stop - go - end  train dont fit.");

        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to restart in north. begin - stop north - go - end - train dont fit.");
        sm.getSensor("Occ West").setState(Sensor.INACTIVE); // free block behind
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting()  == 0.0f;
        }, "Failed to Stop at end - begin - North stop - go - end  train dont fit");

        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE); // clean up to add next go round

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we dont see the throttle go to zero if we do that.

        bo.push();

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        // *********************************************************************************************************
        //  Here start South - West - North - Start Stop - Cancel Stop -East South - Stop - on entry train dont fit
        // *********************************************************************************************************

        // block track at East.
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);

        d.loadTrainFromTrainInfo("SSL3TestTrain.xml");

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        at = d.getActiveTrainsList().get(0);
        aat = at.getAutoActiveTrain();

        JUnitUtil.waitFor(() -> {
            return shm.getSignalHead("Sw-West-Throat-Conv").getAppearanceName().equals("Green");
        }, "Signal West End Div now green begin - start stop north - go - end - stop on previous block");

        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.8f;
        }, "Failed to Start begin - start stop north - go - end - stop");

        sm.getSensor("Occ West Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.INACTIVE);
        sm.getSensor("Stop South 1 Rev").setState(Sensor.INACTIVE);  // and stopping sensor
        sm.getSensor("Occ West").setState(Sensor.ACTIVE);
        sm.getSensor("Occ West Switch").setState(Sensor.INACTIVE);
        sm.getSensor("Occ North").setState(Sensor.ACTIVE);

        // free up block ahead
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        // Accelerates to
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() > 0.7f && aat.getThrottle().getSpeedSetting() < 0.9f;
        }, "Failed to accelerate. begin - start stop north - go - end - stop on previous block");

        sm.getSensor("Occ West").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East").setState(Sensor.ACTIVE);
        sm.getSensor("Occ North").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Switch").setState(Sensor.ACTIVE);
        sm.getSensor("Occ East").setState(Sensor.INACTIVE);
        sm.getSensor("Occ South 1").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return aat.getThrottle().getSpeedSetting() == 0.0f;
        }, "Failed to Stop at end -begin - start stop north - go - end - stop on previous block");

        sm.getSensor("Occ East Switch").setState(Sensor.INACTIVE); // clean up

        // cancel (terminate) the train. The train is set not to terminate at end
        // as we don't see the throttle go to zero if we do that.

        bo.push();

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();
        JFrameOperator aw = new JFrameOperator("AutoTrains");

        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
    }
    
    // Where in user space the "signals" file tree should live
    private static File path = null;

    // the file we create that we will delete
    private static Path outPath = null;

    @BeforeAll
    public static void doOnce() throws Exception {
        // set up users files in temp tst area
        path = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        try {
            FileUtil.createDirectory(path);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                        "SSL3TestTrain.xml").toPath();
                outPath = new File(path, "SSL3TestTrain.xml").toPath();
                Files.copy(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw e;
        }
    }
    
    @AfterAll
    public static void unDoOnce() {
        try {
            Files.delete(outPath);
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
