package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check that a speed measured during a warrant run is merged once into the
 * session speed profile.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SpeedUtilMergeTest {

    private SpeedUtil speedUtil;

    @Test
    public void testMeasurementMergedIntoNearbyStep() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setForwardSpeed(0.5f, 200f);

        // throttle 0.502 is within one 128-step increment of the 0.5 entry
        speedUtil.setSpeedProfile(profile, 0.502f, 220f, true);

        TreeMap<Integer, SpeedStep> speeds = profile.getProfileSpeeds();
        assertEquals(1, speeds.size(), "measurement merged into the existing step, no new step");
        assertEquals(210f, speeds.get(500).getForwardSpeed(), 0.01f,
                "existing step averaged once with the measured speed");
    }

    @Test
    public void testReverseMeasurementMergedIntoNearbyStep() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setForwardSpeed(0.5f, 200f);
        profile.setReverseSpeed(0.5f, 180f);

        speedUtil.setSpeedProfile(profile, 0.497f, 200f, false);

        TreeMap<Integer, SpeedStep> speeds = profile.getProfileSpeeds();
        assertEquals(1, speeds.size(), "measurement merged into the existing step, no new step");
        assertEquals(190f, speeds.get(500).getReverseSpeed(), 0.01f,
                "existing reverse speed averaged once with the measured speed");
        assertEquals(200f, speeds.get(500).getForwardSpeed(), 0.01f, "forward speed unchanged");
    }

    @Test
    public void testFirstReverseMeasurementSetOnNearbyStep() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setForwardSpeed(0.5f, 200f);

        // nearby step has no reverse speed yet
        speedUtil.setSpeedProfile(profile, 0.502f, 190f, false);

        TreeMap<Integer, SpeedStep> speeds = profile.getProfileSpeeds();
        assertEquals(1, speeds.size(), "reverse speed set on the existing step, no new step");
        assertEquals(190f, speeds.get(500).getReverseSpeed(), 0.01f, "measured reverse speed set");
        assertEquals(200f, speeds.get(500).getForwardSpeed(), 0.01f, "forward speed unchanged");
        // the profile must know it now has reverse speeds, or it reports 0 for them
        assertTrue(profile.hasReverseSpeeds(), "profile records that it has reverse speeds");
        assertEquals(190f, profile.getReverseSpeed(0.5f), 0.01f, "reverse speed readable from the profile");
    }

    @Test
    public void testFirstForwardMeasurementSetOnNearbyStep() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setReverseSpeed(0.5f, 180f);

        // nearby step has no forward speed yet
        speedUtil.setSpeedProfile(profile, 0.498f, 210f, true);

        assertEquals(1, profile.getProfileSpeeds().size(), "forward speed set on the existing step, no new step");
        assertTrue(profile.hasForwardSpeeds(), "profile records that it has forward speeds");
        assertEquals(210f, profile.getForwardSpeed(0.5f), 0.01f, "forward speed readable from the profile");
    }

    @Test
    public void testMeasurementAtStepKeyAveragedOnce() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setForwardSpeed(0.5f, 200f);

        // exactly on the step: the step is both floor and ceiling entry
        speedUtil.setSpeedProfile(profile, 0.5f, 220f, true);

        assertEquals(210f, profile.getProfileSpeeds().get(500).getForwardSpeed(), 0.01f,
                "step averaged once with the measured speed");
    }

    @Test
    public void testMeasurementAwayFromStepsAddsStep() {
        RosterSpeedProfile profile = new RosterSpeedProfile(null);
        profile.setForwardSpeed(0.5f, 200f);

        speedUtil.setSpeedProfile(profile, 0.3f, 120f, true);

        TreeMap<Integer, SpeedStep> speeds = profile.getProfileSpeeds();
        assertEquals(2, speeds.size(), "new step added");
        assertEquals(200f, speeds.get(500).getForwardSpeed(), 0.01f, "existing step unchanged");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        speedUtil = new SpeedUtil();
        speedUtil.setThrottle(new DebugThrottle(new DccLocoAddress(3, false),
                new InternalSystemConnectionMemo(false)));
    }

    @AfterEach
    public void tearDown() {
        speedUtil = null;
        ShutDownManager sm = InstanceManager.getDefault(ShutDownManager.class);
        List<ShutDownTask> tasks = new ArrayList<>();
        for (Runnable r : sm.getRunnables()) {
            if (r instanceof WarrantShutdownTask) {
                tasks.add((ShutDownTask) r);
            }
        }
        tasks.forEach(sm::deregister);
        JUnitUtil.tearDown();
    }

}
