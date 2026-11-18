package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoTurnoutsTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        AutoTurnouts t = new AutoTurnouts(d);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(d);
    }

    // checkOrSetTurnout: one turnout of a route, checked or set as the Options say

    private AutoTurnouts at;
    private Turnout turnout;
    private Section section;
    private Block block;
    private ActiveTrain train;

    private void prepare(int known, boolean sectionFree) {
        at = new AutoTurnouts(null); // no crossover, so no Dispatcher is needed
        turnout = mock(Turnout.class);
        when(turnout.getKnownState()).thenReturn(known);
        section = mock(Section.class);
        when(section.getState()).thenReturn(sectionFree ? Section.FREE : Section.FORWARD); // FORWARD: allocated, so not free
        block = new Block("IB1");
        train = mock(ActiveTrain.class);
        when(train.getTrainName()).thenReturn("T1");
    }

    private boolean checkOrSet(boolean trust, boolean set) {
        return at.checkOrSetTurnout(mock(LayoutTurnout.class), turnout, Turnout.THROWN, trust, set,
                section, block, train, false);
    }

    @Test
    public void testUntrustedTurnoutThatCantBeSetIsNotReady() {
        // Trust Known Turnouts off, Section not free: the turnout can't be set,
        // so the route is not ready, even though it is already thrown
        prepare(Turnout.THROWN, false);
        assertFalse(checkOrSet(false, true), "turnout that can't be set");
        verify(turnout, never()).setCommandedState(anyInt());
    }

    @Test
    public void testUntrustedTurnoutIsCommandedWhateverItsKnownState() {
        prepare(Turnout.THROWN, true);
        assertTrue(checkOrSet(false, true), "turnout set");
        verify(turnout).setCommandedState(Turnout.THROWN);
    }

    @Test
    public void testTrustedTurnoutAlreadySetIsLeftAlone() {
        prepare(Turnout.THROWN, false);
        assertTrue(checkOrSet(true, true), "turnout already as needed");
        verify(turnout, never()).setCommandedState(anyInt());
    }

    @Test
    public void testTrustedTurnoutSetTheOtherWay() {
        prepare(Turnout.CLOSED, true);
        assertTrue(checkOrSet(true, true), "turnout set");
        verify(turnout).setCommandedState(Turnout.THROWN);

        prepare(Turnout.CLOSED, false);
        assertFalse(checkOrSet(true, true), "turnout that can't be set");
        verify(turnout, never()).setCommandedState(anyInt());
    }

    @Test
    public void testCheckingOnlyNeverCommands() {
        // as for a slip's second turnout, which used to be commanded even when only checking
        for (boolean trust : new boolean[]{false, true}) {
            prepare(Turnout.CLOSED, true);
            assertFalse(checkOrSet(trust, false), "not as needed, trust " + trust);
            verify(turnout, never()).setCommandedState(anyInt());

            prepare(Turnout.THROWN, true);
            assertTrue(checkOrSet(trust, false), "as needed, trust " + trust);
            verify(turnout, never()).setCommandedState(anyInt());
        }
    }

    @Test
    public void testConnectionDelayCommandsAtTheInterval() {
        prepare(Turnout.CLOSED, true);
        assertTrue(at.checkOrSetTurnout(mock(LayoutTurnout.class), turnout, Turnout.THROWN, false, true,
                section, block, train, true), "turnout set");
        verify(turnout).setCommandedStateAtInterval(Turnout.THROWN);
        verify(turnout, never()).setCommandedState(anyInt());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AutoTurnoutsTest.class);

}
