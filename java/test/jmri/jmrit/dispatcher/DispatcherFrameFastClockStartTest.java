package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests that trains waiting for a fast clock time start when the fast clock
 * moves on by more than one minute at a time.
 */
@DisabledIfHeadless
public class DispatcherFrameFastClockStartTest {

    private Timebase clock;
    private DispatcherFrame d;

    private static Date time(int day, int hr, int min) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2026, Calendar.MARCH, day, hr, min, 0);
        return cal.getTime();
    }

    private ActiveTrain delayedTrain(int hr, int min) {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("Delayed");
        when(at.getStarted()).thenReturn(false);
        when(at.getDelayedStart()).thenReturn(ActiveTrain.TIMEDDELAY);
        when(at.getDepartureTimeHr()).thenReturn(hr);
        when(at.getDepartureTimeMin()).thenReturn(min);
        d.addDelayedTrain(at, ActiveTrain.TIMEDDELAY, null, false);
        return at;
    }

    @Test
    public void testStartsAtDepartureMinute() {
        ActiveTrain at = delayedTrain(8, 0);
        clock.setTime(time(1, 7, 59));
        verify(at, never()).setStarted();
        clock.setTime(time(1, 8, 0));
        verify(at).setStarted();
    }

    @Test
    public void testStartsWhenClockPassesDepartureMinute() {
        ActiveTrain at = delayedTrain(8, 0);
        // the fast clock goes straight from 07:58 to 08:05
        clock.setTime(time(1, 8, 5));
        verify(at).setStarted();
    }

    @Test
    public void testStartsWhenClockPassesMidnight() {
        clock.setTime(time(1, 23, 55));
        ActiveTrain at = delayedTrain(0, 5);
        clock.setTime(time(2, 0, 10));
        verify(at).setStarted();
    }

    @Test
    public void testDoesNotStartEarly() {
        ActiveTrain at = delayedTrain(9, 0);
        clock.setTime(time(1, 8, 5));
        clock.setTime(time(1, 8, 59));
        verify(at, never()).setStarted();
    }

    @Test
    public void testDoesNotStartWhenClockSetBack() {
        ActiveTrain at = delayedTrain(7, 0);
        // 07:00 is behind the clock, so the train waits for the next 07:00
        clock.setTime(time(1, 6, 30));
        verify(at, never()).setStarted();
        clock.setTime(time(1, 7, 10));
        verify(at).setStarted();
    }

    @Test
    public void testRestartsWhenClockPassesRestartMinute() {
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("Restart");
        when(at.getStarted()).thenReturn(true);
        when(at.getDelayedStart()).thenReturn(ActiveTrain.NODELAY);
        when(at.getStatus()).thenReturn(ActiveTrain.READY);
        when(at.reachedRestartPoint()).thenReturn(true);
        when(at.getRestartDepartHr()).thenReturn(8);
        when(at.getRestartDepartMin()).thenReturn(2);
        d.addDelayedTrain(at, ActiveTrain.TIMEDDELAY, null, false);
        clock.setTime(time(1, 8, 4));
        verify(at).restart();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        clock = InstanceManager.getDefault(Timebase.class);
        clock.setRun(false);
        clock.setTime(time(1, 7, 58));
        d = InstanceManager.getDefault(DispatcherFrame.class);
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
