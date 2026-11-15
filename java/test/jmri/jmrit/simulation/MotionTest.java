package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link Motion}: the simulator's motion arithmetic, against values worked by hand.
 * These check the simulator's code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class MotionTest {

    private static final double EPS = 1e-9;

    private static Velocity constant(double v, double t0, double t1) {
        List<Velocity.Part> p = new ArrayList<>();
        p.add(new Velocity.Part(t0, t1, v, v));
        return new Velocity(p);
    }

    @Test
    public void testWorkedAnswerNextBlock() {
        // head at the entry of a 450 mm Block at t = 0, 100 mm/s: next Block at 4.5 s
        Motion m = Motion.of(constant(100, 0, 5), 0, -1e9, 1e9);
        List<Motion.Crossing> c = m.crossings(0, 450, true);
        assertEquals(1, c.size());
        assertEquals(4.5, c.get(0).time, EPS);
        assertEquals(true, c.get(0).inside);
        // rear of a 200 mm train leaves the Block behind (boundary 0) at 2.0 s
        c = m.crossings(-200, 0, false);
        assertEquals(2.0, c.get(0).time, EPS);
        assertEquals(false, c.get(0).inside);
    }

    @Test
    public void testAcceleratingAndLimit() {
        List<Velocity.Part> p = new ArrayList<>();
        p.add(new Velocity.Part(0, 2, 0, 100)); // 50 mm/s/s
        Velocity v = new Velocity(p);
        Motion free = Motion.of(v, 0, -1e9, 1e9);
        assertEquals(25.0, free.position(1.0), EPS);
        assertEquals(1.0, free.crossings(0, 25, true).get(0).time, EPS);
        Motion held = Motion.of(v, 0, -1e9, 30);
        // 25 t^2 = 30
        assertEquals(Math.sqrt(1.2), held.crossings(0, 30 - 1e-9, true).get(0).time, 1e-6);
        assertEquals(30.0, held.endPosition(), EPS);
    }

    @Test
    public void testReversingWithinStep() {
        List<Velocity.Part> p = new ArrayList<>();
        p.add(new Velocity.Part(0, 2, 100, -100)); // forward 50 mm, then back 50 mm
        Motion m = Motion.of(new Velocity(p), 0, -1e9, 1e9);
        assertEquals(50.0, m.position(1.0), EPS);
        assertEquals(0.0, m.endPosition(), EPS);
        List<Motion.Crossing> c = m.crossings(0, 25, true);
        assertEquals(2, c.size(), "passes 25 going and coming back");
        // position 100 t - 50 t^2 = 25 -> t = 1 - sqrt(0.5) and 1 + sqrt(0.5)
        assertEquals(1 - Math.sqrt(0.5), c.get(0).time, 1e-9);
        assertEquals(1 + Math.sqrt(0.5), c.get(1).time, 1e-9);
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
