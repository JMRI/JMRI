package jmri.jmrit.roster.swing.speedprofile;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedProfilePanelTest {

    private static final float STEP = 1.0f / 126.0f;

    @Test
    public void testCTor() {
        SpeedProfilePanel t = new SpeedProfilePanel();
        Assert.assertNotNull("exists",t);
    }

    private SpeedProfilePanel panel;
    private Sensor start;
    private Sensor middle;
    private Sensor finish;

    /**
     * Set up a profile of steps 1 to 2 on a debug throttle, timed over a 500
     * mm Block.
     */
    private void setUpProfile(String repeatLowest, String repeatTimes) {
        JUnitUtil.initDebugThrottleManager();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        start = sm.provideSensor("IS1");
        middle = sm.provideSensor("IS2");
        finish = sm.provideSensor("IS3");
        RosterEntry re = new RosterEntry();
        re.setId("Profiled");
        re.setFileName("Profiled.xml");
        re.setDccAddress("1234");
        re.setProtocol(jmri.LocoAddress.Protocol.DCC_LONG);
        ThreadingUtil.runOnGUI(() -> {
            panel = new SpeedProfilePanel();
            panel.re = re;
            panel.throttleSpeedSteps = 128;
            panel.lengthField.setText("500");
            panel.sensorDelay.setText("");
            panel.sensorAPanel.setDefaultNamedBean(start);
            panel.sensorCPanel.setDefaultNamedBean(middle);
            panel.sensorBPanel.setDefaultNamedBean(finish);
            panel.speedStepFrom.setText("1");
            panel.speedStepTo.setText("2");
            panel.speedStepIncr.setText("1");
            panel.repeatLowestSteps.setText(repeatLowest);
            panel.repeatTimes.setText(repeatTimes);
        });
    }

    /**
     * Run the loco through the timed Block once, as the profiler expects:
     * the Block sensor goes active, then the finish sensor, then the Block
     * sensor goes inactive.
     *
     * @param forward the direction of the run
     * @return the throttle setting of the run
     */
    private float passThroughBlock(boolean forward) throws JmriException {
        JUnitUtil.waitFor(() -> {
            DccThrottle t = panel.t;
            return t != null && t.getIsForward() == forward && t.getSpeedSetting() > 0.0f
                    && panel.startListener != null && panel.finishSensor == (forward ? finish : start);
        }, "run " + (forward ? "forward" : "reverse") + " started");
        float speed = panel.t.getSpeedSetting();
        Sensor finishing = forward ? finish : start;
        middle.setState(Sensor.ACTIVE);
        JUnitUtil.waitFor(50);
        finishing.setState(Sensor.ACTIVE);
        middle.setState(Sensor.INACTIVE);
        finishing.setState(Sensor.INACTIVE);
        return speed;
    }

    private List<Float> runProfile(int runs) throws JmriException {
        ThreadingUtil.runOnGUI(() -> panel.profileButton.doClick());
        List<Float> settings = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            float fwd = passThroughBlock(true);
            float rev = passThroughBlock(false);
            assertEquals(fwd, rev, 1e-6f, "each run is made both ways at the same setting");
            settings.add(fwd);
        }
        JUnitUtil.waitFor(() -> panel.t == null && panel.profileButton.isEnabled(), "profile finished");
        return settings;
    }

    @Test
    public void testProfileWithoutRepeats() throws JmriException {
        setUpProfile("", "");
        List<Float> settings = runProfile(2);
        assertEquals(STEP, settings.get(0), 1e-4f);
        assertEquals(2 * STEP, settings.get(1), 1e-4f);
        assertEquals(2, panel.speeds.size(), "steps 1 and 2 measured");
        assertFalse(panel.repeats.hasRepeats());
        assertFalse(panel.viewRepeatsButton.isEnabled());
    }

    @Test
    public void testProfileRepeatsLowestStepAtEnd() throws JmriException {
        setUpProfile("1", "2");
        List<Float> settings = runProfile(4);
        assertEquals(STEP, settings.get(0), 1e-4f);
        assertEquals(2 * STEP, settings.get(1), 1e-4f);
        assertEquals(STEP, settings.get(2), 1e-4f, "step 1 repeated");
        assertEquals(STEP, settings.get(3), 1e-4f, "step 1 repeated again");

        assertTrue(panel.repeats.hasRepeats());
        assertEquals(1, panel.repeats.getRepeated().size(), "only step 1 repeated");
        SpeedStepRepeats.Entry e = panel.repeats.getRepeated().get(Math.round(STEP * 1000));
        assertNotNull(e);
        assertEquals(1, e.step);
        for (boolean forward : new boolean[]{true, false}) {
            SpeedStepRepeats.Samples s = e.get(forward);
            assertNotNull(s.getFirst(), "first run kept");
            assertEquals(2, s.getRepeats().size(), "repeated twice");
            SpeedProfilePanel.SpeedStep ss = panel.speeds.get(Math.round(STEP * 1000));
            assertEquals(s.mean(), forward ? ss.getForwardSpeed() : ss.getReverseSpeed(), 1e-4f,
                    "the profile is given the mean of the repeats");
        }
        assertTrue(panel.viewRepeatsButton.isEnabled());

        if (!GraphicsEnvironment.isHeadless()) {
            ThreadingUtil.runOnGUI(() -> panel.viewRepeats());
            assertNotNull(panel.repeatsTable);
            assertEquals(2, panel.repeatsTable.model.getRowCount());
            JUnitUtil.dispose(panel.repeatsTable);
        }

        ThreadingUtil.runOnGUI(() -> panel.clearNewData());
        assertFalse(panel.repeats.hasRepeats());
        assertFalse(panel.viewRepeatsButton.isEnabled());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        if (panel != null) {
            ThreadingUtil.runOnGUI(() -> panel.cancelButton());
            panel = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SpeedProfilePanelTest.class);

}
