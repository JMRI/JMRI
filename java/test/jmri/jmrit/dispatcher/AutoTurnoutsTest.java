package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrackExpectedState;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    // The Block a train is standing in, with a slip in it: both of the slip's turnouts must be right.
    @Test
    public void testStandingBlockSlipBothTurnoutsChecked() {
        Turnout a = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout b = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        LayoutSlip slip = mock(LayoutSlip.class);
        when(slip.getTurnout()).thenReturn(a);
        when(slip.getTurnoutB()).thenReturn(b);
        when(slip.getTurnoutBState(LayoutTurnout.STATE_AC)).thenReturn(Turnout.THROWN);
        LayoutTrackExpectedState<LayoutTurnout> tes = new LayoutTrackExpectedState<>(slip, LayoutTurnout.STATE_AC);
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("T1");
        Block blk = new Block("IB1", "Slip Block");
        blk.setState(Block.OCCUPIED);
        Section s = mock(Section.class);
        when(s.getState()).thenReturn(Section.FREE);

        AutoTurnouts t = new AutoTurnouts(null);
        a.setCommandedState(Turnout.CLOSED);
        b.setCommandedState(Turnout.THROWN);
        assertTrue(t.standingBlockTurnoutOK(tes, Turnout.CLOSED, true, s, blk, at, false), "both right");

        b.setCommandedState(Turnout.CLOSED);
        assertFalse(t.standingBlockTurnoutOK(tes, Turnout.CLOSED, true, s, blk, at, false), "second turnout wrong");
        JUnitAppender.assertWarnMessage("T1: turnout IT2 in Slip Block (IB1), where the train is standing, is not set "
            + InstanceManager.turnoutManagerInstance().getThrownText() + " for its route; set it by hand to let the train go");
        assertEquals(Turnout.CLOSED, b.getKnownState(), "not moved: the Block is occupied");
    }

    // With the Block not occupied (a layout without occupancy detection), a wrong turnout is set.
    @Test
    public void testStandingBlockNotOccupiedTurnoutSet() {
        Turnout a = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        LayoutTurnout lt = mock(LayoutTurnout.class);
        when(lt.getTurnout()).thenReturn(a);
        LayoutTrackExpectedState<LayoutTurnout> tes = new LayoutTrackExpectedState<>(lt, Turnout.THROWN);
        ActiveTrain at = mock(ActiveTrain.class);
        when(at.getTrainName()).thenReturn("T1");
        Block blk = new Block("IB1", "Points Block");
        Section s = mock(Section.class);
        when(s.getState()).thenReturn(Section.FREE);
        AutoTurnouts t = new AutoTurnouts(null);

        a.setCommandedState(Turnout.CLOSED);
        assertTrue(t.standingBlockTurnoutOK(tes, Turnout.THROWN, true, s, blk, at, false), "commanded");
        assertEquals(Turnout.THROWN, a.getCommandedState(), "set");

        a.setCommandedState(Turnout.CLOSED);
        assertTrue(t.standingBlockTurnoutOK(tes, Turnout.THROWN, true, s, blk, at, true), "commanded with delay");
        JUnitUtil.waitFor(() -> a.getCommandedState() == Turnout.THROWN, "set after the connection delay");

        a.setCommandedState(Turnout.CLOSED);
        assertFalse(t.standingBlockTurnoutOK(tes, Turnout.THROWN, false, s, blk, at, false), "check only: not set");
        JUnitAppender.assertWarnMessageStartingWith("T1: turnout IT1 in Points Block (IB1), where the train is standing");
        assertEquals(Turnout.CLOSED, a.getCommandedState(), "check only: not moved");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(AutoTurnoutsTest.class);

}
