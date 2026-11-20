package jmri.implementation;

import static org.junit.jupiter.api.Assertions.*;

import jmri.Meter;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * A meter shown in two places stays polled until both have let it go.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class MeterUpdateTaskSharedTest {

    private MeterUpdateTask task;
    private Meter meter;

    @Test
    public void testPolledUntilEveryUserLetsGo() {
        task.enable(meter);   // first window
        task.enable(meter);   // second window on the same meter
        assertTrue(task._enabled, "polling");
        task.disable(meter);  // first window closed
        assertTrue(task._enabled, "still polled for the second window");
        task.disable(meter);  // second window closed
        assertFalse(task._enabled, "no longer polled");
        task.disable(meter);  // one disable too many does no harm
        assertFalse(task._enabled);
        task.enable(meter);
        assertTrue(task._enabled, "polled again");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        task = new MeterUpdateTask(-1) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        meter = new DefaultMeter.DefaultVoltageMeter("IVVoltageMeter", Meter.Unit.Milli, 0.0, 1000.0, 1.0, task);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
