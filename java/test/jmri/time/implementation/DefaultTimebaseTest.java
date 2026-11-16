package jmri.time.implementation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DefaultTimebase, comparing against how SimpleTimebase behaves.
 */
public class DefaultTimebaseTest {

    private Timebase tb;
    private List<PropertyChangeEvent> events;

    private static Date at(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1957, Calendar.JUNE, 14, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static long secondsBetween(Date from, Date to) {
        return (to.getTime() - from.getTime()) / 1000;
    }

    @Test
    public void testSetTimeWhileStoppedIsKeptOnStart() {
        tb.setRun(false);
        tb.userSetTime(at(6, 0));
        tb.setRun(true);
        long diff = secondsBetween(at(6, 0), tb.getTime());
        assertTrue(diff >= 0 && diff < 60, "clock should still be near 06:00, was " + tb.getTime());
    }

    @Test
    public void testStoppedClockDoesNotMove() throws TimebaseRateException {
        tb.setRun(true);
        tb.userSetRate(60.0);
        tb.userSetTime(at(6, 0));
        tb.setRun(false);
        Date stoppedAt = tb.getTime();
        JUnitUtil.waitFor(1000);
        assertEquals(stoppedAt, tb.getTime(), "time should not change while stopped");
        tb.setRun(true);
        JUnitUtil.waitFor(300);
        long diff = secondsBetween(stoppedAt, tb.getTime());
        assertTrue(diff < 60, "time stopped should not be added on restart, gained " + diff + " s");
    }

    @Test
    public void testNewRateOnlyAppliesFromNow() throws TimebaseRateException {
        tb.userSetRate(1.0);
        tb.setRun(true);
        tb.userSetTime(at(8, 0));
        JUnitUtil.waitFor(1500);
        Date before = tb.getTime();
        tb.userSetRate(60.0);
        JUnitUtil.waitFor(300);
        long diff = secondsBetween(before, tb.getTime());
        assertTrue(diff < 60, "clock should not jump when the rate changes, jumped " + diff + " s");
    }

    @Test
    public void testMinuteEventSentWhenStopCrossesMinute() throws TimebaseRateException {
        tb.setRun(false);
        tb.userSetRate(60.0);
        tb.userSetTime(new Date(at(6, 0).getTime() + 59500));
        tb.addMinuteChangeListener(events::add);
        tb.setRun(true);
        JUnitUtil.waitFor(50);
        tb.setRun(false);
        assertFalse(events.isEmpty(), "a minute event should be sent when the clock passes 06:01");
        assertEquals(Double.valueOf(1), events.get(events.size() - 1).getNewValue());
    }

    @Test
    public void testRunListener() {
        tb.setRun(false);
        tb.addPropertyChangeListener("run", events::add);
        tb.setRun(true);
        tb.setRun(false);
        assertEquals(2, events.size());
        assertEquals("run", events.get(0).getPropertyName());
        assertEquals(Boolean.TRUE, events.get(0).getNewValue());
        assertEquals(Boolean.FALSE, events.get(1).getNewValue());
    }

    @Test
    public void testRateListener() throws TimebaseRateException {
        tb.addPropertyChangeListener("rate", events::add);
        tb.userSetRate(4.0);
        assertEquals(1, events.size());
        assertEquals("rate", events.get(0).getPropertyName());
        assertEquals(1.0, (Double) events.get(0).getOldValue(), 0.001);
        assertEquals(4.0, (Double) events.get(0).getNewValue(), 0.001);
    }

    @Test
    public void testMinuteChangeListener() {
        tb.setRun(false);
        tb.userSetTime(at(6, 0));
        tb.addMinuteChangeListener(events::add);
        tb.userSetTime(at(6, 5));
        assertEquals(1, events.size());
        assertEquals("minutes", events.get(0).getPropertyName());
        assertEquals(Double.valueOf(0), events.get(0).getOldValue());
        assertEquals(Double.valueOf(5), events.get(0).getNewValue());
    }

    @Test
    public void testTimeListener() {
        tb.setRun(false);
        tb.userSetTime(at(6, 0));
        tb.addPropertyChangeListener("time", events::add);
        tb.userSetTime(at(6, 5));
        assertEquals(1, events.size());
        assertEquals("time", events.get(0).getPropertyName());
        assertEquals(at(6, 0), events.get(0).getOldValue());
        assertEquals(at(6, 5), events.get(0).getNewValue());
    }

    @Test
    public void testUnknownPropertyName() {
        assertEquals(0, tb.getPropertyChangeListeners("nobody").length);
        assertDoesNotThrow(() -> tb.removePropertyChangeListener("nobody", events::add));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        tb = InstanceManager.getDefault(Timebase.class);
        events = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        tb = null;
        events = null;
        JUnitUtil.tearDown();
    }

}
