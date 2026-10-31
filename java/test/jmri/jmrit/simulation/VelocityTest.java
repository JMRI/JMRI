package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link Velocity}: loco and train velocities, against values worked by hand.
 * These check the simulator's code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class VelocityTest {

    private static final double EPS = 1e-9;

    private static Velocity constant(double v, double t0, double t1) {
        List<Velocity.Part> p = new ArrayList<>();
        p.add(new Velocity.Part(t0, t1, v, v));
        return new Velocity(p);
    }

    @Test
    public void testSlowestLocoSetsSpeed() {
        List<Velocity> locos = new ArrayList<>();
        List<Velocity.Part> ramp = new ArrayList<>();
        ramp.add(new Velocity.Part(0, 1, 0, 100));
        locos.add(new Velocity(ramp));
        locos.add(constant(50, 0, 1));
        Velocity train = Velocity.slowest(locos, 0, 1);
        assertEquals(25.0, train.after(0.25), EPS, "ramping loco is slower first");
        assertEquals(50.0, train.after(0.75), EPS, "then the steady loco");
        // opposed locos: the train does not move
        locos.set(1, constant(-50, 0, 1));
        assertEquals(0.0, Velocity.slowest(locos, 0, 1).after(0.5), EPS);
    }

    @Test
    public void testLocoVelocityFollowsTable() {
        DecoderModel d = new DecoderModel(10, 10);
        List<DecoderModel.Ramp> ramps = d.advance(1.0, 0, 8.96); // 0 to full in 8.96 s
        SpeedTable fwd = SpeedTable.measured(new java.util.TreeMap<>(java.util.Map.of(0.5, 100.0, 1.0, 400.0)));
        Velocity v = Velocity.ofLoco(ramps, fwd, fwd, 1.0);
        assertEquals(50.0, v.after(2.24), EPS, "setting 0.25: half of 100");
        assertEquals(250.0, v.after(6.72), EPS, "setting 0.75: half way from 100 to 400");
        Velocity back = Velocity.ofLoco(ramps, fwd, fwd, -1.0);
        assertEquals(-50.0, back.after(2.24), EPS, "loco facing the rear of the train");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
