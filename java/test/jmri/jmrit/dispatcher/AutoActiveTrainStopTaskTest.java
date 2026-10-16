package jmri.jmrit.dispatcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.TransitSection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that the task for a stop runs once when the train stops, and not at
 * all when the stop is cancelled before the train stops.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoActiveTrainStopTaskTest {

    private DispatcherFrame dispatcher;
    private AutoActiveTrain aat;
    private volatile float throttleSpeed = 0.5f;

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field f = AutoActiveTrain.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(aat, value);
    }

    private void call(String name, Class<?>[] types, Object... args) throws ReflectiveOperationException {
        Method m = AutoActiveTrain.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        m.invoke(aat, args);
    }

    private void stopInCurrentSection() throws ReflectiveOperationException {
        call("stopInCurrentSection", new Class<?>[]{int.class}, AutoActiveTrain.NO_TASK);
    }

    private void cancelStopInCurrentSection() throws ReflectiveOperationException {
        call("cancelStopInCurrentSection", new Class<?>[]{});
    }

    @Test
    public void testStopTaskRunsWhenTrainStops() throws ReflectiveOperationException {
        stopInCurrentSection();
        throttleSpeed = 0.0f;
        verify(dispatcher, timeout(2000)).queueReleaseOfCompletedAllocations();
    }

    @Test
    public void testCancelledStopTaskDoesNotRunAtLaterStop() throws Exception {
        stopInCurrentSection();
        cancelStopInCurrentSection();  // signal cleared, the train runs on
        throttleSpeed = 0.0f;          // and later stops for some other reason
        Thread.sleep(500);
        verify(dispatcher, never()).queueReleaseOfCompletedAllocations();
    }

    @Test
    public void testCancelledEndTaskIsReported() throws Exception {
        call("stopInCurrentSection", new Class<?>[]{int.class}, AutoActiveTrain.END_REVERSAL);
        cancelStopInCurrentSection();
        JUnitAppender.assertWarnMessage("Train: stop cancelled before the train stopped, task[1] not run");
        throttleSpeed = 0.0f;
        Thread.sleep(500);
        verify(dispatcher, never()).queueReleaseOfCompletedAllocations();
    }

    @Test
    public void testTerminateEndsWaitForStop() throws Exception {
        stopInCurrentSection();
        // the train is now worked by hand, so it has no engineer to report it stopped
        setField("_autoEngineer", null);
        Assertions.assertTrue(isWaitForStopRunning(), "waiting for the train to stop");
        aat.terminate();
        JUnitUtil.waitFor(() -> !isWaitForStopRunning(), "wait for stop ended when the train was terminated");
        verify(dispatcher, never()).queueReleaseOfCompletedAllocations();
    }

    private boolean isWaitForStopRunning() {
        String name = "Wait for stop " + aat.getActiveTrain().getActiveTrainName();
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(t -> t.isAlive() && name.equals(t.getName()));
    }

    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        JUnitUtil.setUp();
        dispatcher = mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        aat = new AutoActiveTrain(at);

        // a moving train, its speed is set by the test
        DccThrottle throttle = mock(DccThrottle.class);
        when(throttle.getSpeedSetting()).thenAnswer(i -> throttleSpeed);
        AutoActiveTrain.AutoEngineer engineer = aat.new AutoEngineer(throttle, null);
        engineer.setSpeedLimits(0.0f, 1.0f, 1.0f);
        engineer.setSpeedImmediate(0.5f);

        // the train is in a Section too short to stop by occupancy, so it stops at once
        Section section = mock(Section.class);
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(section);
        when(as.getNextSection()).thenReturn(mock(Section.class));
        when(as.getTransitSection()).thenReturn(mock(TransitSection.class));

        setField("_autoEngineer", engineer);
        setField("_currentAllocatedSection", as);
        setField("_dispatcher", dispatcher);
    }

    @AfterEach
    public void tearDown() throws ReflectiveOperationException {
        cancelStopInCurrentSection();
        throttleSpeed = 0.0f;
        JUnitUtil.tearDown();
    }

}
