package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for setting the turnouts of an NX route on the layout thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DestinationPointsRouteSettingTest {

    private EntryExitPairs eep;
    private LayoutEditor panel;
    private SensorManager sm;
    private SignalMastLogicManager smlm;
    private SignalMast sourceMast;
    private SignalMast destMast;
    private Turnout leftTO;
    private DestinationPoints dp;

    private final AtomicBoolean commanded = new AtomicBoolean(false);
    private final AtomicBoolean commandedOnLayoutThread = new AtomicBoolean(false);
    private final AtomicBoolean returnedBeforeCommand = new AtomicBoolean(false);
    private final AtomicBoolean requestReturned = new AtomicBoolean(false);
    private final AtomicLong commandTime = new AtomicLong();

    /**
     * Records on which thread the turnout of the route is set, and whether
     * the request to set the route had returned by then.
     */
    private final PropertyChangeListener turnoutWatcher = e -> {
        if (Turnout.PROPERTY_COMMANDED_STATE.equals(e.getPropertyName()) && !commanded.get()) {
            commandTime.set(System.nanoTime());
            commandedOnLayoutThread.set(ThreadingUtil.isLayoutThread());
            returnedBeforeCommand.set(requestReturned.get());
            commanded.set(true);
        }
    };

    private boolean isRouteSet() {
        SignalMastLogic sml = smlm.getSignalMastLogic(sourceMast);
        return sml != null && sml.isDestinationValid(destMast) && !sml.getAutoBlocks(destMast).isEmpty();
    }

    private void requestRoute() {
        ThreadingUtil.runOnLayout(() -> {
            eep.setSingleSegmentRoute(dp.getSystemName());
            requestReturned.set(true);
        });
    }

    /**
     * Click an NX button: its sensor changes to the other state.
     *
     * @param name the name of the sensor
     */
    private void click(String name) {
        Sensor sensor = sm.getSensor(name);
        assertNotNull(sensor, name);
        sensor.setCommandedState(sensor.getKnownState() == Sensor.ACTIVE ? Sensor.INACTIVE : Sensor.ACTIVE);
    }

    /**
     * Make sure no NX button of the route is selected before the clicks of a test.
     */
    private void releaseButtons() {
        sm.getSensor("NX-Left-TO-A").setCommandedState(Sensor.INACTIVE);
        sm.getSensor("NX-Right-TO-B").setCommandedState(Sensor.INACTIVE);
    }

    @Test
    public void testTurnoutsSetOnLayoutThreadWithOldGuiBehavior() {
        eep.setSkipGuiFix(true);
        eep.turnoutSetDelay = 300;
        requestRoute();
        JUnitUtil.waitFor(commanded::get, "turnout commanded");
        JUnitUtil.waitFor(this::isRouteSet, "route set");
        long settingTime = System.nanoTime() - commandTime.get();

        assertTrue(commandedOnLayoutThread.get(), "turnout set on the layout thread");
        assertTrue(returnedBeforeCommand.get(), "the layout thread did not wait for the turnouts");
        assertEquals(Turnout.CLOSED, leftTO.getKnownState(), "turnout set for the route");
        // The next step is scheduled on a java.util.Timer, which counts in whole milliseconds of
        // the wall clock, so measured here it can come a fraction of a millisecond before the
        // 250 ms step plus the turnout delay. Without the turnout delay it would come after 250 ms.
        assertTrue(settingTime >= TimeUnit.MILLISECONDS.toNanos(eep.turnoutSetDelay),
                "route completed after the turnout delay: " + settingTime);
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        assertTrue(dp.isActive(), "route active");
    }

    @Test
    public void testTurnoutsSetOnLayoutThread() {
        eep.setSkipGuiFix(false);
        requestRoute();
        JUnitUtil.waitFor(commanded::get, "turnout commanded");
        JUnitUtil.waitFor(this::isRouteSet, "route set");

        assertTrue(commandedOnLayoutThread.get(), "turnout set on the layout thread");
        assertTrue(returnedBeforeCommand.get(), "the layout thread did not wait for the turnouts");
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        assertTrue(dp.isActive(), "route active");
    }

    @Test
    public void testTurnoutListenerOccupiesRouteBlockWithOldGuiBehavior() {
        // Something listening to the turnout, for example a Logix, reports the train in the
        // first block of the route as soon as the turnout is commanded
        eep.setSkipGuiFix(true);
        Sensor firstBlockSensor = sm.provideSensor("S-Left-TO");
        PropertyChangeListener trainMoves = e -> {
            if (Turnout.PROPERTY_COMMANDED_STATE.equals(e.getPropertyName())) {
                firstBlockSensor.setCommandedState(Sensor.ACTIVE);
            }
        };
        leftTO.addPropertyChangeListener(trainMoves);
        requestRoute();
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        leftTO.removePropertyChangeListener(trainMoves);

        assertTrue(requestReturned.get(), "request returned");
        assertEquals(Sensor.ACTIVE, firstBlockSensor.getKnownState(), "train in the first block");
        // Only the destination block is left, so the route is cleared down
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cleared down by the train");
    }

    @Test
    public void testClearDownWhileTurnoutsAreSet() {
        eep.setSkipGuiFix(false);
        eep.turnoutSetDelay = 500;
        PropertyChangeListener cancel = e -> {
            if (Turnout.PROPERTY_COMMANDED_STATE.equals(e.getPropertyName())) {
                ThreadingUtil.runOnLayoutEventually(() -> dp.cancelClearInterlock(EntryExitPairs.CANCELROUTE));
            }
        };
        leftTO.addPropertyChangeListener(cancel);
        requestRoute();
        JUnitUtil.waitFor(commanded::get, "turnout commanded");
        JUnitUtil.waitFor(() -> !dp.isActive(), "route cancelled");
        leftTO.removePropertyChangeListener(cancel);
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");

        // The rest of the route is not set up after it has been cancelled
        SignalMastLogic sml = smlm.getSignalMastLogic(sourceMast);
        assertTrue(sml == null || !sml.isDestinationValid(destMast), "no signal mast logic left for the cancelled route");
        assertTrue(sourceMast.getHeld(), "source mast still held");
    }

    @Test
    public void testMastHeldUntilTurnoutReportsItsState() throws JmriException {
        Sensor feedback = sm.provideSensor("IS-LEFT-TO-FB");
        feedback.setState(Sensor.ACTIVE);
        leftTO.provideFirstFeedbackSensor(feedback.getSystemName());
        leftTO.setFeedbackMode(Turnout.ONESENSOR);
        assertEquals(Turnout.THROWN, leftTO.getKnownState(), "turnout reports thrown");
        sourceMast.setHeld(true);

        requestRoute();
        JUnitUtil.waitFor(this::isRouteSet, "route set");
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        ThreadingUtil.runOnLayout(() -> { });
        assertEquals(Turnout.CLOSED, leftTO.getCommandedState(), "turnout commanded closed");
        assertEquals(Turnout.THROWN, leftTO.getKnownState(), "turnout has not moved yet");
        assertTrue(sourceMast.getHeld(), "mast held while the turnout moves");

        feedback.setState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> !sourceMast.getHeld(), "mast released when the turnout reports closed");
    }

    @Test
    public void testMastReleasedAfterTimeOut() throws JmriException {
        Sensor feedback = sm.provideSensor("IS-LEFT-TO-FB");
        feedback.setState(Sensor.ACTIVE);
        leftTO.provideFirstFeedbackSensor(feedback.getSystemName());
        leftTO.setFeedbackMode(Turnout.ONESENSOR);
        sourceMast.setHeld(true);
        dp.mastReleaseTimeout = 500;

        requestRoute();
        JUnitUtil.waitFor(this::isRouteSet, "route set");
        assertEquals(Turnout.THROWN, leftTO.getKnownState(), "turnout never reports its new state");
        JUnitUtil.waitFor(() -> !sourceMast.getHeld(), "mast released after the time out");
    }

    @Test
    public void testDoubleClickWithOldGuiBehavior() {
        // A second click on the destination is handled after the route has been set, so it does
        // not cancel the route
        eep.setSkipGuiFix(true);
        eep.setClearDownOption(EntryExitPairs.AUTOCANCEL);
        eep.turnoutSetDelay = 300;
        ThreadingUtil.runOnLayout(() -> {
            releaseButtons();
            click("NX-Left-TO-A");
            click("NX-Right-TO-B");
            click("NX-Right-TO-B");
        });
        JUnitUtil.waitFor(this::isRouteSet, "route set");
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        ThreadingUtil.runOnLayout(() -> { });
        ThreadingUtil.runOnLayout(() -> { });
        assertTrue(dp.isActive(), "route not cancelled by the second click");
        assertEquals(Sensor.ACTIVE, sm.getSensor("NX-Right-TO-B").getKnownState(), "second click handled");
    }

    @Test
    public void testHeldClickWritesSensorOnceWithOldGuiBehavior() throws JmriException {
        // A click on another NX button while a route is set is held back and handled when the
        // route is complete, without the sensor of that button being written again
        eep.setSkipGuiFix(true);
        eep.turnoutSetDelay = 300;
        Sensor other = sm.provideSensor("NX-Left-EB");
        other.setState(Sensor.INACTIVE);
        PointDetails otherPd = eep.getPointDetails(other, panel);
        assertNotNull(otherPd);
        List<Object> written = new CopyOnWriteArrayList<>();
        PropertyChangeListener counter = e -> {
            if (Sensor.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())) {
                written.add(e.getNewValue());
            }
        };
        other.addPropertyChangeListener(counter);
        ThreadingUtil.runOnLayout(() -> {
            releaseButtons();
            click("NX-Left-TO-A");
            click("NX-Right-TO-B");
            click("NX-Left-EB");    // held back until the route is complete
            assertEquals(EntryExitPairs.NXBUTTONINACTIVE, otherPd.getNXState(), "click held back");
        });
        JUnitUtil.waitFor(this::isRouteSet, "route set");
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        JUnitUtil.waitFor(() -> otherPd.getNXState() == EntryExitPairs.NXBUTTONSELECTED, "held click handled");
        other.removePropertyChangeListener(counter);
        assertEquals(List.of(Sensor.ACTIVE), written, "sensor written only by the click");
        ThreadingUtil.runOnLayout(() -> other.setCommandedState(Sensor.INACTIVE));
    }

    @Test
    public void testErrorWhileCompletingRouteWithOldGuiBehavior() throws JmriException {
        // Something listening to the NX buttons fails when the route updates them, both when
        // the route is completed and again when the failure is handled
        eep.setSkipGuiFix(true);
        eep.turnoutSetDelay = 300;
        AtomicBoolean armed = new AtomicBoolean(false);
        AtomicInteger failures = new AtomicInteger();
        PropertyChangeListener failing = e -> {
            if (armed.get() && Sensor.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())
                    && Integer.valueOf(Sensor.INACTIVE).equals(e.getNewValue()) && failures.get() < 2) {
                failures.incrementAndGet();
                throw new IllegalStateException("listener failed");
            }
        };
        Sensor srcSensor = sm.getSensor("NX-Left-TO-A");
        Sensor destSensor = sm.getSensor("NX-Right-TO-B");
        Sensor other = sm.provideSensor("NX-Left-EB");
        other.setState(Sensor.INACTIVE);
        PointDetails otherPd = eep.getPointDetails(other, panel);
        assertNotNull(otherPd);
        srcSensor.addPropertyChangeListener(failing);
        destSensor.addPropertyChangeListener(failing);
        ThreadingUtil.runOnLayout(() -> {
            releaseButtons();
            click("NX-Left-TO-A");
            click("NX-Right-TO-B");
            click("NX-Left-EB");    // held back until the route is complete
            armed.set(true);
        });
        JUnitUtil.waitFor(() -> failures.get() == 2, () -> "both updates failed: " + failures.get() + " " + srcSensor.getKnownState() + " " + destSensor.getKnownState());
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting reported as finished");
        armed.set(false);
        // The held click is handled even though the route failed
        JUnitUtil.waitFor(() -> otherPd.getNXState() == EntryExitPairs.NXBUTTONSELECTED, "held click handled");
        srcSensor.removePropertyChangeListener(failing);
        destSensor.removePropertyChangeListener(failing);
        ThreadingUtil.runOnLayout(() -> other.setCommandedState(Sensor.INACTIVE));
        JUnitAppender.assertErrorMessage("An error occurred while setting the route");
    }

    @Test
    public void testDoubleClickCancelsRoute() {
        // Without the old GUI behavior a second click on the destination while the route is set
        // cancels the route
        eep.setSkipGuiFix(false);
        eep.setClearDownOption(EntryExitPairs.AUTOCANCEL);
        eep.turnoutSetDelay = 300;
        ThreadingUtil.runOnLayout(() -> {
            releaseButtons();
            click("NX-Left-TO-A");
            click("NX-Right-TO-B");
            click("NX-Right-TO-B");
        });
        JUnitUtil.waitFor(() -> !eep.isSettingRoutes(), "route setting finished");
        assertFalse(dp.isActive(), "route cancelled by the second click");
        assertFalse(isRouteSet(), "route not set");
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        // The list of NX points is static, so remove the points of earlier tests: a button
        // still selected there would take part in the routes of this test
        EntryExitPairs.pointDetails.clear();

        ConfigureManager cm = InstanceManager.getDefault(ConfigureManager.class);
        java.io.File f = new java.io.File("java/test/jmri/jmrit/logixng/tools/LogixNG_Test_Dave_Sand.xml");
        assertTrue(cm.load(f));
        panel = InstanceManager.getDefault(EditorManager.class).get(LayoutEditor.class, "My Layout");
        assertNotNull(panel);

        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        lbm.initializeLayoutBlockPaths();
        JUnitUtil.waitFor(lbm::routingStablised, "routing stable");
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        smlm = InstanceManager.getDefault(SignalMastLogicManager.class);

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        sourceMast = smm.getSignalMast("Left-TO-A");
        destMast = smm.getSignalMast("Right-TO-B");
        assertNotNull(sourceMast);
        assertNotNull(destMast);

        // Use a temporary signal mast logic, as created by NX when no stored logic exists
        SignalMastLogic stored = smlm.getSignalMastLogic(sourceMast);
        assertNotNull(stored);
        ThreadingUtil.runOnGUI(() -> smlm.removeSignalMastLogic(stored));
        assertNull(smlm.getSignalMastLogic(sourceMast));

        for (String name : new String[]{"NX-Left-TO-A", "NX-Right-TO-B", "S-Left-Approach", "S-Left-TO", "S-Main", "S-Side", "S-Right-TO", "S-Right-Approach"}) {
            sm.provideSensor(name).setState(Sensor.INACTIVE);
        }

        // The route needs the turnout closed, so it will be moved
        leftTO = InstanceManager.getDefault(TurnoutManager.class).getTurnout("Left TO");
        assertNotNull(leftTO);
        leftTO.setCommandedState(Turnout.THROWN);

        PointDetails srcPd = eep.getPointDetails(sm.getSensor("NX-Left-TO-A"), panel);
        PointDetails destPd = eep.getPointDetails(sm.getSensor("NX-Right-TO-B"), panel);
        assertNotNull(srcPd);
        assertNotNull(destPd);
        dp = eep.getSourceForPoint(srcPd).getDestForPoint(destPd);
        assertNotNull(dp);
        leftTO.addPropertyChangeListener(turnoutWatcher);
    }

    @AfterEach
    public void tearDown() {
        if (leftTO != null) {
            leftTO.removePropertyChangeListener(turnoutWatcher);
        }
        if (panel != null) {
            JUnitUtil.dispose(panel);
        }
        panel = null;
        eep = null;
        dp = null;
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
