package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A Signal Mast Logic sets its source mast in a job that runs after the
 * Signal Mast Logic delay. A test that ends with such a job waiting must not
 * leave it to act in the next test.
 * <p>
 * Two logics in a row: A from mast 1 to mast 2, and B from mast 0 to mast 1.
 * When A's job sets mast 1, B schedules its own job, and looks up the Signal
 * Mast Logic Manager to do so. If the InstanceManager has been reset by then,
 * as the next test's set up does, that lookup creates a new Signal Mast Logic
 * Manager, with a Signal Mast Manager and (through the Layout Block Manager) a
 * Memory Manager, before the next test has its ConfigureManager: they are not
 * registered to be stored, and a later store leaves out everything loaded into
 * them (as in issue #7898).
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultSignalMastLogicDisposeTest {

    private static final int DELAY = 200; // each job runs after half of this

    /**
     * Wait until anything scheduled on the layout thread with a shorter
     * delay, such as a pending aspect job, has run: timer jobs run in the
     * order they are due.
     */
    private static void waitForDelayedJobs() {
        AtomicBoolean done = new AtomicBoolean(false);
        ThreadingUtil.runOnLayoutDelayed(() -> done.set(true), DELAY * 2);
        JUnitUtil.waitFor(done::get, "delayed jobs have run");
    }

    private VirtualSignalMast mast0;
    private VirtualSignalMast mast1;
    private VirtualSignalMast mast2;

    /** Set up logics A (mast 1 to mast 2) and B (mast 0 to mast 1), settled. */
    private void setUpTwoLogics() {
        SignalMastLogicManager smlm = InstanceManager.getDefault(SignalMastLogicManager.class);
        mast0 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($0)");
        mast1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        mast2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        ThreadingUtil.runOnLayout(() -> mast2.setAspect("Stop"));
        DefaultSignalMastLogic a = (DefaultSignalMastLogic) smlm.newSignalMastLogic(mast1);
        a.setDestinationMast(mast2);
        DefaultSignalMastLogic b = (DefaultSignalMastLogic) smlm.newSignalMastLogic(mast0);
        b.setDestinationMast(mast1);
        a.initialise();
        b.initialise();
        waitForDelayedJobs();
        assertEquals("Approach", mast1.getAspect(), "A in use: mast 1 shows Approach to mast 2 at Stop");
        assertEquals("Clear", mast0.getAspect(), "B in use: mast 0 shows Clear to mast 1 at Approach");
    }

    // Shows the set up gives a job that acts: with the logics in use, a change
    // of mast 2 sets mast 1 when A's job runs. So the next test cannot pass
    // just because no job was waiting.
    @Test
    public void testPendingJobSetsMast() {
        setUpTwoLogics();
        ThreadingUtil.runOnLayout(() -> mast2.setAspect("Clear"));
        waitForDelayedJobs();
        assertEquals("Clear", mast1.getAspect(), "A's job set mast 1");
    }

    // A job is waiting when the test ends. After the clean up, the next
    // test's reset of the InstanceManager gets nothing created behind it.
    @Test
    public void testNothingCreatedAfterResetWhenCleanedUp() {
        setUpTwoLogics();
        ThreadingUtil.runOnLayout(() -> mast2.setAspect("Clear")); // A's job now waiting

        JUnitUtil.clearSignalMastLogic(); // as at the end of a test
        JUnitUtil.resetInstanceManager(); // as at the start of the next
        waitForDelayedJobs();

        assertFalse(InstanceManager.containsDefault(SignalMastLogicManager.class), "no Signal Mast Logic Manager created");
        assertFalse(InstanceManager.containsDefault(SignalMastManager.class), "no Signal Mast Manager created");
        assertFalse(InstanceManager.containsDefault(MemoryManager.class), "no Memory Manager created");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(DELAY);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearSignalMastLogic();
        JUnitUtil.tearDown();
    }

}
