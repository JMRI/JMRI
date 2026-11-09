package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link TrackSnapshot}: what it copies, and that bean states reach
 * it through listeners it removes when disposed.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrackSnapshotTest {

    @Test
    public void testCopiesAndFollowsStates() {
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setCommandedState(Turnout.CLOSED);
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");
        a.setLength(450);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        a.setSensor("IS1");
        a.addPath(new Path(b, 0, 0, new BeanSetting(to, "IT1", Turnout.CLOSED)));
        int before = to.getNumPropertyChangeListeners();

        TrackSnapshot snap = TrackSnapshot.take(List.of(new InternalSensorOutput()));
        assertEquals(before + 1, to.getNumPropertyChangeListeners(), "listening to the turnout");
        assertEquals(450f, Objects.requireNonNull(snap.block(a), "Block 1 in the snapshot").length, 1e-6);
        assertEquals(s, snap.sensor(a));
        assertTrue(snap.output(a).isPresent(), "an internal sensor has an output");
        assertTrue(snap.unreported().isEmpty());
        assertNull(snap.sensor(b), "a dark Block");
        TrackSnapshot.BlockPath path = Objects.requireNonNull(snap.block(a), "Block 1 in the snapshot").paths.get(0);
        assertEquals(b, path.neighbour);
        assertTrue(snap.met(path.settings));

        // a later change of the configuration is not seen; a change of state is
        a.setLength(600);
        assertEquals(450f, Objects.requireNonNull(snap.block(a), "Block 1 in the snapshot").length, 1e-6);
        to.setCommandedState(Turnout.THROWN);
        assertEquals(Turnout.THROWN, snap.state(to));
        assertTrue(!snap.met(path.settings));

        snap.dispose();
        JUnitUtil.waitFor(() -> to.getNumPropertyChangeListeners() == before, "listener removed on the layout thread");
    }

    @Test
    public void testSensorWithNoOutput() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        a.setSensor(InstanceManager.getDefault(SensorManager.class).provideSensor("IS1").getSystemName());
        TrackSnapshot snap = TrackSnapshot.take(List.of());
        assertEquals("IB1", snap.unreported().get(a.getSensor()));
        assertTrue(snap.output(a).isEmpty());
        snap.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
