package jmri.jmrit.dispatcher;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastManager;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Steve Gigiel 2018
 *
 * Tests the LoadAtStartUp function for Dispatcher In addition it tests auto
 * running of a train.
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LoadAtStartUpTest {

    @Test
    public void testShowAndClose() throws Exception {
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
        FileUtil.setUserFilesPath(ProfileManager.getDefault().getActiveProfile(), "java/test/jmri/jmrit");
        // we need a throttle manager
        ThrottleManager m = InstanceManager.getDefault(ThrottleManager.class);
        // signal mast manager
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);

        //set sensors inactive
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (Sensor s : sm.getNamedBeanSet()) {
            s.setState(Sensor.INACTIVE);
        }
        // place train on layout
        sm.getSensor("Occ South Platform").setState(Sensor.ACTIVE);

        // and load. only one of 2 trains will load
        d.loadAtStartup();

        assertThat(d.getActiveTrainsList().size()).withFailMessage("Train Loaded").isEqualTo(1);

        // trains loads and runs, 4 allocated sections, the one we are in and 3 ahead.
        assertThat(d.getAllocatedSectionsList()).withFailMessage("Allocated sections 4").hasSize(4);
        // set up loco address
        DccLocoAddress addr = new DccLocoAddress(1000, true);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West End Div").getAspect().equals("Clear");
        }, "Signal West End Div now green");
        // check signals and speed
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("1 West End Div Signal Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("1 West To South  Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("1 South To East Signal Green").isEqualTo("Approach");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("1 East End Throat Signal Green").isEqualTo("Stop");
        float speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ West Platform Switch").setState(Sensor.ACTIVE);

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West End Div").getAspect().equals("Stop");
        }, "Signal Just passed stop");
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("2 West End Div Signal Stop").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("2 West To South  Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("2 South To East Signal Green").isEqualTo("Approach");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("2 East End Throat Signal Green").isEqualTo("Stop");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ West Block").setState(Sensor.ACTIVE);
        sm.getSensor("Occ South Platform").setState(Sensor.INACTIVE);
        sm.getSensor("Occ West Platform Switch").setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("South To East").getAspect().equals("Clear");
        }, "Signal South To East now Clear");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("3 West To South  Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("3 South To East Signal Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("3 East End Throat Signal Approach").isEqualTo("Approach");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ South Block").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("West To South").getAspect().equals("Stop");
        }, "Signal Just passed West To South now stop");
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("4 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("4 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("4 South To East Signal Green").isEqualTo("Clear");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("4 East End Throat Signal Green").isEqualTo("Approach");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.59f,0.61f);

        sm.getSensor("Occ West Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Block").setState(Sensor.ACTIVE);

        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("South To East").getAspect().equals("Stop");
        }, "Signal Just passed south to east now stop");
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("5 West End Div Signal Green").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("5 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("5 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("5 East End Throat Signal yellow").isEqualTo("Approach");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ South Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> {
            return smm.getSignalMast("East End Throat").getAspect().equals("Stop");
        }, "Signal Just passed east end throat now stop");
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("6 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("6 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("6 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("6 East End Throat Signal Red").isEqualTo("Stop");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ East Platform Switch").setState(Sensor.ACTIVE);
        // No change
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("7 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("7 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("7 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("7 East End Throat Signal Red").isEqualTo("Stop");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ South Platform").setState(Sensor.ACTIVE);
        // signals no change, speed changes
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("8 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("8 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("8 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("8 East End Throat Signal Red").isEqualTo("Stop");
        speed = (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING);
        assertThat(speed).isBetween(0.14f,0.16f);

        sm.getSensor("Occ East Block").setState(Sensor.INACTIVE);
        sm.getSensor("Occ East Platform Switch").setState(Sensor.INACTIVE);
        // signals no change, speed changes to stop
        assertThat(smm.getSignalMast("West End Div").getAspect()).withFailMessage("9 West End Div Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("West To South").getAspect()).withFailMessage("9 West To South  Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("South To East").getAspect()).withFailMessage("9 South To East Signal Red").isEqualTo("Stop");
        assertThat(smm.getSignalMast("East End Throat").getAspect()).withFailMessage("9 East End Throat Signal Red").isEqualTo("Stop");

        // train slows to stop
        JUnitUtil.waitFor(() -> {
            return (float) m.getThrottleInfo(addr, Throttle.SPEEDSETTING) == 0.0;
        }, "Signal Just passed east end throat now stop");

        // cancel (terminate) the train.
        JButtonOperator bo = new JButtonOperator(dw, Bundle.getMessage("TerminateTrain"));
        bo.push();

        assertThat((d.getActiveTrainsList().isEmpty())).withFailMessage("All trains terminated").isTrue();

        JFrameOperator aw = new JFrameOperator("AutoTrains");

        aw.requestClose();
        dw.requestClose();

        // cleanup window
        JUnitUtil.dispose(d);
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
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
