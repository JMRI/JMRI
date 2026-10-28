package jmri.implementation;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastLogicManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Check that a Signal Mast Logic does not lose a change that arrives while it
 * is working out an aspect.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicChangeDuringCheckTest {

    /**
     * A virtual mast that is released from held just after it has been set
     * to a given aspect, while the logic's aspect job is still running.
     */
    private static class ReleasedWhileSetMast extends VirtualSignalMast {

        volatile String releaseAfter = null;
        volatile Runnable afterRelease = null;

        ReleasedWhileSetMast(String systemName) {
            super(systemName);
        }

        @Override
        public void setAspect(@Nonnull String aspect) {
            super.setAspect(aspect);
            if (aspect.equals(releaseAfter)) {
                releaseAfter = null;
                setHeld(false);
                Runnable r = afterRelease;
                if (r != null) {
                    r.run();
                }
            }
        }
    }

    /**
     * A sensor that changes just after the logic's check has read it, as a
     * real sensor can between two lines of the check.
     */
    private static class ChangesAfterReadSensor extends AbstractSensor {

        /** The state to change to after the next read by the check; 0 for none. */
        volatile int changeAfterNextCheck = 0;

        /** The thread the check runs on: the layout thread, unless set. */
        volatile Thread checkThread = null;

        ChangesAfterReadSensor(String systemName) {
            super(systemName);
        }

        @Override
        public int getKnownState() {
            int state = super.getKnownState();
            int change = changeAfterNextCheck;
            Thread t = checkThread;
            boolean onCheckThread = (t == null) ? ThreadingUtil.isLayoutThread() : Thread.currentThread() == t;
            if (change != 0 && onCheckThread) {
                changeAfterNextCheck = 0;
                try {
                    setKnownState(change);
                } catch (JmriException e) {
                    throw new IllegalStateException(e);
                }
            }
            return state;
        }

        @Override
        public void requestUpdateFromLayout() {
            // nothing to ask for
        }
    }

    private VirtualSignalMast dest;
    private ChangesAfterReadSensor late;

    /**
     * Set up a logic from source to dest that needs IS1, IS2 and IS3 active.
     * IS2 is the sensor that can change during a check.
     */
    private DefaultSignalMastLogic logicNeedingThreeSensors(VirtualSignalMast source) {
        dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        late = new ChangesAfterReadSensor("IS2");
        sm.register(late);
        sm.provideSensor("IS1");
        sm.provideSensor("IS3");
        DefaultSignalMastLogic sml = (DefaultSignalMastLogic) InstanceManager.getDefault(SignalMastLogicManager.class)
            .newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        sml.addSensor("IS1", Sensor.ACTIVE, dest);
        sml.addSensor("IS2", Sensor.ACTIVE, dest);
        sml.addSensor("IS3", Sensor.ACTIVE, dest);
        return sml;
    }

    private void setSensor(String name, int state) {
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor(name);
        ThreadingUtil.runOnLayout(() -> s.setCommandedState(state));
    }

    /** Wait for anything already queued on the layout thread, such as the rest of a check, to finish. */
    private static void waitForLayoutThread() {
        ThreadingUtil.runOnLayout(() -> { });
    }

    @Test
    public void testReleaseWhileHeldAspectIsBeingSet() {
        ReleasedWhileSetMast source = new ReleasedWhileSetMast("IF$vsm:basic:one-searchlight($1)");
        dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");

        DefaultSignalMastLogic sml = (DefaultSignalMastLogic) InstanceManager.getDefault(SignalMastLogicManager.class)
            .newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source shows Approach behind a Stop");
        JUnitUtil.waitFor(() -> !sml.inWait, "initial aspect job finished");

        // the mast is released just as the held aspect has been set
        source.releaseAfter = "Stop";
        source.setHeld(true);
        JUnitUtil.waitFor(() -> source.releaseAfter == null, "held aspect set and mast released");
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect jobs finished");

        assertFalse(source.getHeld());
        assertEquals("Approach", source.getAspect(), "released mast follows the destination again");

        sml.dispose();
    }

    @Test
    public void testRepeatIsDroppedWhenDisposed() {
        ReleasedWhileSetMast source = new ReleasedWhileSetMast("IF$vsm:basic:one-searchlight($1)");
        dest = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");

        DefaultSignalMastLogic sml = (DefaultSignalMastLogic) InstanceManager.getDefault(SignalMastLogicManager.class)
            .newSignalMastLogic(source);
        sml.setDestinationMast(dest);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source shows Approach behind a Stop");
        JUnitUtil.waitFor(() -> !sml.inWait, "initial aspect job finished");

        // released while the job runs, so a repeat is asked for, then disposed before it can run;
        // a long delay keeps any repeat waiting, so that it shows in inWait
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(4000);
        source.releaseAfter = "Stop";
        source.afterRelease = sml::dispose;
        source.setHeld(true);
        JUnitUtil.waitFor(() -> source.releaseAfter == null, "held aspect set and mast released");
        waitForLayoutThread();
        assertFalse(sml.inWait, "no repeat is scheduled for a disposed logic");
        assertEquals("Stop", source.getAspect(), "disposed logic no longer sets the mast");
    }

    @Test
    public void testSensorChangeTowardsClearWhileRouteIsBeingChecked() {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        DefaultSignalMastLogic sml = logicNeedingThreeSensors(source);
        setSensor("IS1", Sensor.ACTIVE);
        setSensor("IS2", Sensor.INACTIVE);
        setSensor("IS3", Sensor.INACTIVE);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source at Stop while sensors are inactive");
        JUnitUtil.waitFor(() -> !sml.inWait, "initial aspect job finished");

        // IS3 going active starts a check; IS2 goes active just after the check has read it
        late.changeAfterNextCheck = Sensor.ACTIVE;
        setSensor("IS3", Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> late.getKnownState() == Sensor.ACTIVE, "IS2 went active during the check");

        JUnitUtil.waitFor(() -> "Approach".equals(source.getAspect()), "source clears once all sensors are active");
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect jobs finished");
        assertEquals("Approach", source.getAspect());

        sml.dispose();
    }

    @Test
    public void testSensorChangeAgainstRouteWhileRouteIsBeingChecked() {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        DefaultSignalMastLogic sml = logicNeedingThreeSensors(source);
        setSensor("IS1", Sensor.ACTIVE);
        setSensor("IS2", Sensor.ACTIVE);
        setSensor("IS3", Sensor.INACTIVE);
        sml.initialise();
        JUnitUtil.waitFor(() -> "Stop".equals(source.getAspect()), "source at Stop while IS3 is inactive");
        JUnitUtil.waitFor(() -> !sml.inWait, "initial aspect job finished");

        // IS3 going active starts a check; IS2 goes inactive just after the check has read it as active
        late.changeAfterNextCheck = Sensor.INACTIVE;
        setSensor("IS3", Sensor.ACTIVE);
        JUnitUtil.waitFor(() -> late.getKnownState() == Sensor.INACTIVE, "IS2 went inactive during the check");
        waitForLayoutThread();

        assertFalse(sml.isActive(dest), "route is not set with IS2 inactive");
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect jobs finished");
        assertEquals("Stop", source.getAspect(), "source stays at Stop with IS2 inactive");

        sml.dispose();
    }

    @Test
    public void testSensorChangeAgainstRouteWhileInitialising() {
        VirtualSignalMast source = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        DefaultSignalMastLogic sml = logicNeedingThreeSensors(source);
        setSensor("IS1", Sensor.ACTIVE);
        setSensor("IS2", Sensor.ACTIVE);
        setSensor("IS3", Sensor.ACTIVE);

        // initialise() runs the first check on this thread; IS2 goes inactive just after it has been read as active
        late.checkThread = Thread.currentThread();
        late.changeAfterNextCheck = Sensor.INACTIVE;
        sml.initialise();
        assertEquals(Sensor.INACTIVE, late.getKnownState(), "IS2 went inactive during the first check");

        assertFalse(sml.isActive(dest), "route is not set with IS2 inactive");
        JUnitUtil.waitFor(() -> !sml.inWait, "aspect jobs finished");
        waitForLayoutThread();
        assertFalse(sml.isActive(dest), "route is still not set after the check has run again");

        sml.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(20);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
