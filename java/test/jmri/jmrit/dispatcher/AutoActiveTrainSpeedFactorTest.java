package jmri.jmrit.dispatcher;

import java.lang.reflect.Field;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Transit;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * The speed profile's planners send their settings straight to the throttle,
 * so the engineer gives them the throttle's actual settings: a train's speed
 * factor is applied to the speed it is asked for, as for any other speed
 * change, and not again, or inverted, by the planner.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoActiveTrainSpeedFactorTest {

    private DispatcherFrame dispatcher;
    private DebugThrottle throttle;
    private AutoActiveTrain aat;
    private AutoActiveTrain.AutoEngineer engineer;
    private RosterSpeedProfile profile;

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field f = AutoActiveTrain.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(aat, value);
    }

    @Test
    public void testStopByDistanceGivesActualSettings() {
        engineer.slowOverDistance(200.0f, true);
        Mockito.verify(profile).planApproachToMinOverDistance(throttle, 200.0f, 1.0f, false);
        engineer.slowOverDistance(150.0f, false);
        Mockito.verify(profile).planStopToZeroOverDistance(throttle, 150.0f, 1.0f, false);
    }

    @Test
    public void testStopByDistanceAsBeforeGivesSpeedFactor() {
        // Dispatcher's "Stop by Distance as in Earlier Versions" option
        Mockito.when(dispatcher.getStopByDistanceAsBefore()).thenReturn(true);
        engineer.slowOverDistance(200.0f, true);
        Mockito.verify(profile).planApproachToMinOverDistance(throttle, 200.0f, 0.8f, true);
        engineer.slowOverDistance(150.0f, false);
        Mockito.verify(profile).planStopToZeroOverDistance(throttle, 150.0f, 0.8f, true);
    }

    @Test
    public void testPhysicsRampAsBefore() throws Exception {
        Mockito.when(dispatcher.getStopByDistanceAsBefore()).thenReturn(true);
        engineer.setRamping(AutoActiveTrain.RAMP_PHYSICS, 1000, 100, 1);
        engineer.setTargetSpeed(0.5f);
        // as earlier versions: the speed asked for, and the speed factor for the profile to divide by
        Mockito.verify(profile, Mockito.timeout(10000)).runPhysicsAccelerationToTargetThrottle(
                ArgumentMatchers.eq(throttle), ArgumentMatchers.floatThat(f -> Math.abs(f - 0.5f) < 0.0001f),
                ArgumentMatchers.anyFloat(), ArgumentMatchers.anyFloat(), ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(), ArgumentMatchers.eq(0.8f));
    }

    @Test
    public void testPhysicsRampToTheActualSetting() throws Exception {
        engineer.setRamping(AutoActiveTrain.RAMP_PHYSICS, 1000, 100, 1);
        engineer.setTargetSpeed(0.5f);
        // the speed factor of 0.8 applied once: 0.5 x 0.8
        Mockito.verify(profile, Mockito.timeout(10000)).runPhysicsAccelerationToTargetThrottle(
                ArgumentMatchers.eq(throttle), ArgumentMatchers.floatThat(f -> Math.abs(f - 0.4f) < 0.0001f),
                ArgumentMatchers.anyFloat(), ArgumentMatchers.anyFloat(), ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(), ArgumentMatchers.eq(1.0f));
        Assertions.assertEquals(0.4f, engineer.getTargetSpeed(), 0.0001f, "target speed as for any other change");
    }

    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        JUnitUtil.setUp();
        dispatcher = Mockito.mock(DispatcherFrame.class);
        Mockito.when(dispatcher.getScale()).thenReturn(jmri.ScaleManager.getScale("HO"));
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        JUnitUtil.initDebugThrottleManager(memo);
        throttle = new DebugThrottle(new DccLocoAddress(3, false), memo);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        aat = new AutoActiveTrain(at);

        RosterEntry re = new RosterEntry();
        profile = Mockito.mock(RosterSpeedProfile.class);
        Mockito.when(profile.getRosterEntry()).thenReturn(re);
        Mockito.when(profile.hasForwardSpeeds()).thenReturn(true);
        Mockito.when(profile.hasReverseSpeeds()).thenReturn(true);
        re.setPhysicsWeightKg(80000.0f);
        re.setSpeedProfile(profile);
        setField("re", re);
        setField("_dispatcher", dispatcher);
        setField("_throttle", throttle);

        engineer = aat.new AutoEngineer(throttle, re);
        engineer.setSpeedLimits(0.0f, 1.0f, 0.8f); // speed factor 0.8
        setField("_autoEngineer", engineer);
    }

    @AfterEach
    public void tearDown() {
        engineer.abort();
        JUnitUtil.tearDown();
    }

}
