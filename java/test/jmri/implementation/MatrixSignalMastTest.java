package jmri.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the MatrixSignalMast implementation.
 *
 * @author Egbert Broerse Copyright (C) 2016, 2019, 2021
 */
public class MatrixSignalMastTest {

    Turnout it11;
    Turnout it12;
    Turnout it13;

    @Test
    public void testSetup() {
        Assertions.assertEquals(Turnout.UNKNOWN, it11.getCommandedState(), "it11 before");
        Assertions.assertEquals(Turnout.UNKNOWN, it12.getCommandedState(), "it12 before");
        Assertions.assertEquals(Turnout.UNKNOWN, it13.getCommandedState(), "it13 before");
    }

    @Test
    public void testCtor1() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        Assertions.assertEquals("IF$xsm:basic:one-low($0001)-3t", m.getSystemName(), "system name");
        Assertions.assertEquals("user", m.getUserName(), "user name");
        // log.debug(it11.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)); // debug
        Assertions.assertEquals("IT12", m.outputsToBeans.get("output2").getName(), "output2");
    }

    @Test
    public void testHeld() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assertions.assertFalse(m.getHeld());

        m.setHeld(true);
        Assertions.assertTrue(m.getHeld());

        m.setHeld(false);
        Assertions.assertFalse(m.getHeld());
    }

    @Test
    public void testMaxOutputs10() {
        int check = 10;
        for (int i = 1; i <= check; i++) InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout(""+i);

        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(check);
        StringBuilder clear = new StringBuilder();
        StringBuilder stop = new StringBuilder();

        for (int i = 1; i <= check; i++)  {
            m.setOutput("output"+i, "IT"+i); // Note: "IT" added to name by system
            clear.append("0");
            stop.append("1");
        }

        m.setBitstring("Clear", clear.toString());
        m.setBitstring("Stop", stop.toString());
        m.setBitstring("Unlit", stop.toString());

        m.setAllowUnLit(true);
        m.setUnLitBits(stop.toString());

        m.aspect = "Clear"; // define some initial aspect before setting any aspect
        // wait for outputs and outputbits to be set

        Assertions.assertTrue(m.getLit());

        m.setLit(false);
        Assertions.assertFalse(m.getLit());

        m.setLit(true);
        Assertions.assertTrue(m.getLit());

        Assertions.assertEquals(10, m.getOutputs().size());

    }

    @Test
    public void testLit() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11"); // Note: "IT" added to name by system
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        m.setBitstring("Clear", "111");
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001");
        m.setBitstring("Unlit", "000");

        m.setAllowUnLit(true);
        m.setUnLitBits("000");

        m.aspect = "Clear"; // define some initial aspect before setting any aspect
        // wait for outputs and outputbits to be set

        Assertions.assertTrue(m.getLit());

        m.setLit(false);
        Assertions.assertFalse(m.getLit());

        m.setLit(true);
        Assertions.assertTrue(m.getLit());
    }

    @Test
    public void testAspects() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        m.setBitstring("Clear", "111"); // used for test below
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001"); // used for test below
        m.setBitstring("Unlit", "000");

        m.setAllowUnLit(true);
        m.setUnLitBits("000");
        m.setAspectEnabled("Clear");
        m.setAspectEnabled("Approach");
        m.setAspectEnabled("Stop");
        m.setAspectEnabled("Unlit");
        m.resetPreviousStates(false);

        m.aspect = "Stop"; // define some initial aspect before setting any aspect
        m.setMatrixMastCommandDelay(0);
        // wait for outputs and outputbits to be set

        // log.debug(java.util.Arrays.toString(m.getBitsForAspect("Stop")));
        Assertions.assertEquals("[0, 0, 1]", java.util.Arrays.toString(m.getBitsForAspect("Stop")), "check bitarray for Stop");

        InstanceManager.getDefault(jmri.TurnoutManager.class).setOutputInterval(0); // default outputInterval = 250, set to 0 to speed up test
        m.setAspect("Clear");
        Assertions.assertEquals("Clear", m.getAspect(), "check Clear");
        JUnitUtil.waitFor( ()->{ return it11.getCommandedState() == Turnout.CLOSED; }, "it11 for Clear" );
        m.setAspect("Stop");
        Assertions.assertEquals("Stop", m.getAspect(), "check Stop");
        JUnitUtil.waitFor( ()->{ return it12.getCommandedState() == Turnout.THROWN; }, "it12 for Stop" );
        // it12 state is more fragile
    }

    @Test
    public void testAspectAttributes() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        Assertions.assertNotNull(m);
        m.setAspect("Clear");
        jmri.util.JUnitAppender.assertErrorMessage("Trying to set an aspect (Clear) on signal mast user which has not been configured");

        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");

        m.setBitstring("Clear", "111"); // used for test below
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001"); // used for test below
        m.setBitstring("Unlit", "000");
        Assertions.assertEquals("../../../resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-287.gif",
                m.getAppearanceMap().getProperty("Clear", "imagelink"));
    }

    @Test
    public void testAspectNotSet() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assertions.assertNull(m.getAspect(), "check null");
    }

    @Test
    public void testSetDelay() {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        Assertions.assertEquals(0, m.getMatrixMastCommandDelay(), "initial mast delay 0");
        m.setMatrixMastCommandDelay(150);
        Assertions.assertEquals(150, m.getMatrixMastCommandDelay(), "get new mast delay");
        m.setMatrixMastCommandDelay(0);
    }

    /**
     * A three output mast with Clear 111, Approach 100 and Stop 001.
     */
    private MatrixSignalMast threeOutputMast(boolean resetPreviousStates, int delay) {
        MatrixSignalMast m = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");
        m.setBitNum(3);
        m.setOutput("output1", "IT11");
        m.setOutput("output2", "IT12");
        m.setOutput("output3", "IT13");
        m.setBitstring("Clear", "111");
        m.setBitstring("Approach", "100");
        m.setBitstring("Stop", "001");
        m.setAspectEnabled("Clear");
        m.setAspectEnabled("Approach");
        m.setAspectEnabled("Stop");
        m.resetPreviousStates(resetPreviousStates);
        m.setMatrixMastCommandDelay(delay);
        InstanceManager.getDefault(jmri.TurnoutManager.class).setOutputInterval(0);
        return m;
    }

    /**
     * Wait for the commands already queued on the layout thread to run.
     */
    private static void flushLayoutThread() {
        ThreadingUtil.runOnLayout(() -> {});
    }

    /**
     * With "reset previous states", an output the Stop step turns off and the
     * new aspect turns on again must end up on. The aspect is set on the
     * layout thread, as Signal Mast Logic does, so the new aspect is worked
     * out before the Stop step's commands have been sent.
     */
    @Test
    public void testResetPreviousStatesThenAspect() {
        MatrixSignalMast m = threeOutputMast(true, 0);
        m.setAspect("Clear");
        flushLayoutThread();
        Assertions.assertEquals(Turnout.CLOSED, it11.getCommandedState(), "it11 for Clear");

        ThreadingUtil.runOnLayout(() -> m.setAspect("Approach"));
        flushLayoutThread();
        Assertions.assertEquals(Turnout.CLOSED, it11.getCommandedState(), "it11 for Approach");
        Assertions.assertEquals(Turnout.THROWN, it12.getCommandedState(), "it12 for Approach");
        Assertions.assertEquals(Turnout.THROWN, it13.getCommandedState(), "it13 for Approach");
    }

    /**
     * Two aspects set before the first one's commands are sent: the outputs
     * end at the second aspect.
     */
    @Test
    public void testAspectSetAgainBeforeOutputsSent() {
        MatrixSignalMast m = threeOutputMast(false, 0);
        m.setAspect("Clear");
        flushLayoutThread();

        ThreadingUtil.runOnLayout(() -> {
            m.setAspect("Stop");
            m.setAspect("Clear"); // before Stop's commands are sent
        });
        flushLayoutThread();
        Assertions.assertEquals(Turnout.CLOSED, it11.getCommandedState(), "it11 for Clear");
        Assertions.assertEquals(Turnout.CLOSED, it12.getCommandedState(), "it12 for Clear");
        Assertions.assertEquals(Turnout.CLOSED, it13.getCommandedState(), "it13 for Clear");
    }

    /**
     * The command delay spaces the outputs out without holding the thread
     * that sets the aspect, which is usually the layout thread.
     */
    @Test
    public void testCommandDelayDoesNotHoldCaller() {
        int delay = 500; // the old code slept 3 times this for 3 outputs
        MatrixSignalMast m = threeOutputMast(false, delay);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());
        List<Turnout> order = Collections.synchronizedList(new ArrayList<>());
        for (Turnout t : new Turnout[]{it11, it12, it13}) {
            t.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, e -> {
                order.add(t);
                times.add(System.nanoTime());
            });
        }
        long[] held = new long[1];
        ThreadingUtil.runOnLayout(() -> {
            long start = System.nanoTime();
            m.setAspect("Clear");
            held[0] = (System.nanoTime() - start) / 1_000_000;
        });
        // the fixed code returns in well under a millisecond: a margin of 500 ms
        Assertions.assertTrue(held[0] < delay, "setAspect held the layout thread for " + held[0] + " ms");

        JUnitUtil.waitFor(() -> order.size() == 3, "all three outputs sent");
        Assertions.assertEquals(List.of(it11, it12, it13), order, "outputs sent in order");
        for (int i = 1; i < 3; i++) {
            long gap = (times.get(i) - times.get(i - 1)) / 1_000_000;
            // the timer waits at least the delay; allow for its millisecond clock
            Assertions.assertTrue(gap >= delay - 2, "gap before output " + (i + 1) + " was " + gap + " ms");
        }
    }

    /**
     * Commands still waiting when the mast is deleted are not sent.
     */
    @Test
    public void testDisposeDropsWaitingCommands() {
        MatrixSignalMast m = threeOutputMast(false, 0);
        ThreadingUtil.runOnLayout(() -> {
            m.setAspect("Clear");
            m.dispose(); // before the commands are sent
        });
        flushLayoutThread();
        Assertions.assertEquals(Turnout.UNKNOWN, it11.getCommandedState(), "it11 not sent");
        Assertions.assertEquals(Turnout.UNKNOWN, it12.getCommandedState(), "it12 not sent");
        Assertions.assertEquals(Turnout.UNKNOWN, it13.getCommandedState(), "it13 not sent");
    }

    /**
     * An output marked 'n' in an aspect's pattern is left as it is.
     */
    @Test
    public void testUndefinedOutputLeftAlone() {
        MatrixSignalMast m = threeOutputMast(false, 0);
        m.setBitstring("Approach", "1n0");
        m.setAspect("Approach");
        flushLayoutThread();
        Assertions.assertEquals(Turnout.CLOSED, it11.getCommandedState(), "it11 on");
        Assertions.assertEquals(Turnout.UNKNOWN, it12.getCommandedState(), "it12 left alone");
        Assertions.assertEquals(Turnout.THROWN, it13.getCommandedState(), "it13 off");
    }

    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        // provide 3 turnouts:
        it11 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT11");
        it12 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT12");
        it13 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT13");
    }

    @AfterEach
    public void tearDown() {
        // has dumpd a bunch of stuff to AWT thread
        JUnitUtil.waitFor(20);

        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.tearDown();
    }

    //private static final Logger log = LoggerFactory.getLogger(MatrixSignalMastTest.class);

}
