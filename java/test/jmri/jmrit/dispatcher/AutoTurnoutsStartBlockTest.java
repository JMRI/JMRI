package jmri.jmrit.dispatcher;

import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Turnouts in the Block a train is standing in when Dispatcher allocates its
 * first Section. The train may be standing on them, so they are checked, not
 * moved: a train whose route needs one set the other way waits until it is set.
 * <p>
 * Uses the West Platform Switch of DispatcherSMLLayout.xml: the West Block on
 * one side of turnout "Station West End", the two platforms on the other.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class AutoTurnoutsStartBlockTest {

    private DispatcherFrame d;
    private Block westSwitch;
    private Block westBlock;
    private Block northPlatform;
    private Block southPlatform;
    private Turnout stationWestEnd;

    // facing the turnout: from the West Block, through the switch, to the North Platform
    @Test
    public void testTrainWaitsWhileTurnoutInItsBlockIsSetAgainstIt() {
        Section sw = switchSection();
        Transit t = transit("Switch to North", sw, sectionNextTo(northPlatform));
        stationWestEnd.setCommandedState(otherState(stateTo(northPlatform)));
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        waitForWarning();
        assertTrue(d.getAllocatedSectionsList().isEmpty(), "nothing allocated while the turnout is set against the train");
        assertEquals(otherState(stateTo(northPlatform)), stationWestEnd.getKnownState(), "turnout under the train not moved");
        ActiveTrain at = d.getActiveTrainsList().get(0);
        assertEquals("WAITING: turnout Station West End", at.getStatusText(), "its status says what it is waiting for");

        // set by hand: the train is given its Section
        stationWestEnd.setCommandedState(stateTo(northPlatform));
        JUnitUtil.waitFor(() -> allocated(sw), "Section allocated once the turnout is set");
        assertNull(at.getWaitingFor(), "no longer waiting for the turnout");
        assertFalse(at.getStatusText().contains("Station West End"), "nor does its status say so: " + at.getStatusText());
    }

    @Test
    public void testTrainGoesWhenTurnoutInItsBlockIsAlreadySet() {
        Section sw = switchSection();
        Transit t = transit("Switch to North", sw, sectionNextTo(northPlatform));
        stationWestEnd.setCommandedState(stateTo(northPlatform));
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        JUnitUtil.waitFor(() -> allocated(sw), "Section allocated");
        assertEquals(stateTo(northPlatform), stationWestEnd.getKnownState(), "turnout unchanged");
    }

    // a train ended while it waits: setting the turnout afterwards does nothing more
    @Test
    public void testTrainEndedWhileWaiting() {
        Section sw = switchSection();
        Transit t = transit("Switch to North", sw, sectionNextTo(northPlatform));
        stationWestEnd.setCommandedState(otherState(stateTo(northPlatform)));
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        waitForWarning();
        d.terminateActiveTrain(d.getActiveTrainsList().get(0), true, false);
        JUnitUtil.waitFor(() -> d.getActiveTrainsList().isEmpty(), "train ended");

        stationWestEnd.setCommandedState(stateTo(northPlatform));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        assertTrue(d.getAllocatedSectionsList().isEmpty(), "nothing allocated for the ended train");
    }

    // a Section with no way in, in the direction of travel (the train leaves a dead end): nothing to check, as before
    @Test
    public void testNoWayInNothingChecked() {
        Section sw = InstanceManager.getDefault(SectionManager.class).createNewSection("Test Switch No Entry");
        assertNotNull(sw);
        sw.addBlock(westSwitch);
        sw.addToReverseList(new EntryPoint(westSwitch, northPlatform, "East"));
        sw.addToReverseList(new EntryPoint(westSwitch, southPlatform, "East"));
        Transit t = transit("Switch to North, no entry", sw, sectionNextTo(northPlatform));
        stationWestEnd.setCommandedState(otherState(stateTo(northPlatform)));
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        JUnitUtil.waitFor(() -> allocated(sw), "Section allocated");
        assertEquals(otherState(stateTo(northPlatform)), stationWestEnd.getKnownState(), "turnout unchanged");
    }

    // trailing the turnout: the train may be on either platform's leg, so the turnout is not checked, as before
    @Test
    public void testTurnoutBehindTrainOnEitherLegIsNotChecked() {
        Section sw = switchSection();
        Section wb = InstanceManager.getDefault(SectionManager.class).createNewSection("Test West Block");
        assertNotNull(wb);
        wb.addBlock(westBlock);
        wb.addToReverseList(new EntryPoint(westBlock, westSwitch, "East"));
        Transit t = transit("Switch to West", sw, wb);
        stationWestEnd.setCommandedState(Turnout.CLOSED);
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        JUnitUtil.waitFor(() -> allocated(sw), "Section allocated");
        assertEquals(Turnout.CLOSED, stationWestEnd.getKnownState(), "turnout unchanged");
    }

    // a train standing in the first Block of a two Block Section, facing the turnout
    @Test
    public void testTwoBlockSectionStartingInSwitchBlock() {
        Section west = InstanceManager.getDefault(SectionManager.class).getSection("West");
        assertNotNull(west);
        Transit t = transit("West to North", west, sectionNextTo(northPlatform));
        stationWestEnd.setCommandedState(otherState(stateTo(northPlatform)));
        occupy(westSwitch);

        loadTrain(t, westSwitch);
        waitForWarning();
        assertTrue(d.getAllocatedSectionsList().isEmpty(), "nothing allocated while the turnout is set against the train");

        stationWestEnd.setCommandedState(stateTo(northPlatform));
        JUnitUtil.waitFor(() -> allocated(west), "Section allocated once the turnout is set");
    }

    /**
     * A Section of the West Platform Switch alone: entered forward from the
     * West Block, in reverse from either platform.
     */
    private Section switchSection() {
        Section s = InstanceManager.getDefault(SectionManager.class).createNewSection("Test West Switch");
        assertNotNull(s);
        s.addBlock(westSwitch);
        s.addToForwardList(new EntryPoint(westSwitch, westBlock, "West"));
        s.addToReverseList(new EntryPoint(westSwitch, northPlatform, "East"));
        s.addToReverseList(new EntryPoint(westSwitch, southPlatform, "East"));
        return s;
    }

    /** The layout's Section holding a Block and entered from the switch. */
    private Section sectionNextTo(Block to) {
        Section found = null;
        for (Section s : InstanceManager.getDefault(SectionManager.class).getNamedBeanSet()) {
            if (s.getBlockList().contains(to) && s.connectsToBlock(westSwitch)) {
                found = s;
            }
        }
        assertNotNull(found, "a Section next to the switch holds " + to.getDisplayName());
        return found;
    }

    /**
     * A Transit of two Sections, each direction taken from the Sections'
     * entry points.
     */
    private Transit transit(String name, Section first, Section second) {
        Transit t = InstanceManager.getDefault(TransitManager.class).createNewTransit(name);
        assertNotNull(t);
        int secondDir = (second.getEntryPointFromBlock(westSwitch, Section.FORWARD) != null) ? Section.FORWARD : Section.REVERSE;
        int firstDir = (first.getExitPointToSection(second, Section.FORWARD) != null) ? Section.FORWARD : Section.REVERSE;
        t.addTransitSection(new TransitSection(first, 1, firstDir));
        t.addTransitSection(new TransitSection(second, 2, secondDir));
        return t;
    }

    private void loadTrain(Transit t, Block start) {
        TrainInfo info = new TrainInfo();
        info.setTransitName(t.getDisplayName());
        info.setTransitId(t.getDisplayName());
        info.setTrainsFrom(DispatcherFrame.TrainsFrom.TRAINSFROMUSER);
        info.setTrainName("T1");
        info.setTrainUserName("T1");
        info.setDccAddress("3");
        info.setTrainInTransit(true);
        info.setStartBlockName(start.getDisplayName() + "-1");
        info.setStartBlockId(start.getDisplayName());
        info.setStartBlockSeq(1);
        List<Block> dest = t.getDestinationBlocksList(start, true);
        Block end = dest.get(dest.size() - 1);
        info.setDestinationBlockName(end.getDisplayName() + "-2");
        info.setDestinationBlockId(end.getDisplayName());
        info.setDestinationBlockSeq(2);
        info.setAutoRun(false);
        info.setAllocationMethod(ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
        assertEquals(0, d.loadTrainFromTrainInfo(info), "train loaded");
        JUnitUtil.waitFor(() -> d.getActiveTrainsList().size() == 1, "train created");
        // let Dispatcher make its first allocation attempt
        new org.netbeans.jemmy.QueueTool().waitEmpty();
    }

    /** Dispatcher's warning that the turnout under the train is set against it. */
    private void waitForWarning() {
        String text = "T1: turnout Station West End (IT2) in West Platform Switch (IB:AUTO:0007), where the train is standing,"
            + " is not set " + (stateTo(northPlatform) == Turnout.CLOSED
                ? InstanceManager.turnoutManagerInstance().getClosedText()
                : InstanceManager.turnoutManagerInstance().getThrownText())
            + " for its route; set it by hand to let the train go";
        JUnitUtil.waitFor(() -> JUnitAppender.getBacklog().stream()
            .anyMatch(e -> text.equals(e.getMessage().getFormattedMessage())), "warning: " + text);
        JUnitAppender.assertWarnMessage(text);
        closeWaitingWindow();
    }

    private static javax.swing.JOptionPane optionPane(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JOptionPane) {
                return (javax.swing.JOptionPane) child;
            }
            if (child instanceof java.awt.Container) {
                javax.swing.JOptionPane found = optionPane((java.awt.Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** The window that tells whoever is at Dispatcher which turnout to set: check what it says, and close it. */
    private void closeWaitingWindow() {
        String state = stateTo(northPlatform) == Turnout.CLOSED
            ? InstanceManager.turnoutManagerInstance().getClosedText()
            : InstanceManager.turnoutManagerInstance().getThrownText();
        org.netbeans.jemmy.operators.JDialogOperator window
            = new org.netbeans.jemmy.operators.JDialogOperator(Bundle.getMessage("TurnoutUnderTrainTitle"));
        assertFalse(window.isModal(), "it doesn't hold Dispatcher up");
        String said = String.valueOf(optionPane(window.getContentPane()).getMessage());
        assertEquals("T1 can't leave West Platform Switch: turnout Station West End is not set " + state
            + " for its route.\nSet it by hand and the train will go.", said);
        new org.netbeans.jemmy.operators.JButtonOperator(window, 0).doClick();
        window.waitClosed();
    }

    private boolean allocated(Section s) {
        for (AllocatedSection as : d.getAllocatedSectionsList()) {
            if (as.getSection() == s) {
                return true;
            }
        }
        return false;
    }

    /** The state of Station West End for the switch's path to a Block. */
    private int stateTo(Block b) {
        for (Path p : westSwitch.getPaths()) {
            if (p.getBlock() == b) {
                return p.getSettings().get(0).getSetting();
            }
        }
        fail("no path to " + b.getDisplayName());
        return 0;
    }

    private static int otherState(int state) {
        return state == Turnout.CLOSED ? Turnout.THROWN : Turnout.CLOSED;
    }

    private void occupy(Block b) {
        JUnitUtil.setBeanStateAndWait(b.getSensor(), Sensor.ACTIVE);
        assertEquals(Block.OCCUPIED, b.getState());
    }

    @BeforeEach
    public void setUp() throws JmriConfigureXmlException {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        assertTrue(cm.load(new java.io.File("java/test/jmri/jmrit/dispatcher/DispatcherSMLLayout.xml")));
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        // Signal Mast Logic is set up when routing settles; wait, so it is not left to a later test
        JUnitUtil.waitFor(() -> InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)
            .routingStablised(), "Routing Stabilised");
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.setSignalType(DispatcherFrame.SECTIONSALLOCATED); // these tests are about turnouts, not signals

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        westSwitch = bm.getBlock("West Platform Switch");
        westBlock = bm.getBlock("West Block");
        northPlatform = bm.getBlock("North Platform West End");
        southPlatform = bm.getBlock("South Platform");
        stationWestEnd = InstanceManager.getDefault(TurnoutManager.class).getTurnout("Station West End");
        assertNotNull(westSwitch);
        assertNotNull(westBlock);
        assertNotNull(northPlatform);
        assertNotNull(southPlatform);
        assertNotNull(stationWestEnd);
        for (Sensor s : InstanceManager.getDefault(SensorManager.class).getNamedBeanSet()) {
            JUnitUtil.setBeanStateAndWait(s, Sensor.INACTIVE);
        }
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            for (ActiveTrain at : new java.util.ArrayList<>(d.getActiveTrainsList())) {
                d.terminateActiveTrain(at, true, false);
            }
        }
        d = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }
}
