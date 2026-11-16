package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * A Section given to a train beyond the end of its Transit, with Allocate
 * Extra or {@link DispatcherFrame#extendActiveTrainsPath}, on the test panel
 * from JMRI/JMRI#15894: Block1 and turnout TO_L, Block2, Block3, Block4, then
 * turnout TO_R and Block5, in a line.
 * <p>
 * The Transit "Blocks REV" runs from Block5 to Block2, its Sections in their
 * reverse direction. The Section "Block1+Turnout" beyond its end can only be
 * entered from Block2 in its reverse direction. Automatic allocation is on, as
 * in the user's options.
 */
@DisabledIfHeadless
public class DispatcherFrameExtraSectionTest {

    private DispatcherFrame d;

    // The train is given the next Section in the direction it enters it; it
    // was refused, as only the forward direction was tried.
    @Test
    public void testExtendReverseRunningTrain() {
        ActiveTrain at = startTrain("Blocks REV", "Block5", "Block2", "Block5");
        assertTrue(extend(at, "Block1+Turnout"), "Block1+Turnout added to the train's path");
        assertEquals(Section.REVERSE, section("Block1+Turnout").getState(), "allocated in its reverse direction");
        assertEquals(Turnout.CLOSED, turnout("TRANSIT_TEST_L").getKnownState(), "TO_L set for the train");
    }

    // Allocate Extra gives a Section outside the Transit in the direction the
    // train enters it; it was always forward.
    @Test
    public void testAllocateExtraReverseRunningTrain() {
        ActiveTrain at = startTrain("Blocks REV", "Block5", "Block2", "Block5");
        ThreadingUtil.runOnGUI(() -> d.allocateExtraSection(at, section("Block1+Turnout"), null));
        assertEquals(Section.REVERSE, section("Block1+Turnout").getState(), "allocated in its reverse direction");
    }

    @Test
    public void testExtendForwardRunningTrain() {
        ActiveTrain at = startTrain("Blocks FWD", "Block1", "Block4", "Block1");
        assertTrue(extend(at, "Block5+Turnout"), "Block5+Turnout added to the train's path");
        assertEquals(Section.FORWARD, section("Block5+Turnout").getState());
        assertEquals(Turnout.CLOSED, turnout("TRANSIT_TEST_R").getKnownState(), "TO_R set for the train");
    }

    // A Section that has no entry point from the train's last Section, in
    // either direction, is still refused, and the train's Transit and the
    // Section are left as they were.
    @Test
    public void testExtendRefusesSectionNotAdjoining() {
        ActiveTrain at = startTrain("Blocks REV", "Block5", "Block2", "Block5");
        int transitLength = at.getTransit().getTransitSectionList().size();
        assertFalse(extend(at, "Block5"), "Block5 does not adjoin the train's last Section, Block2");
        assertEquals(transitLength, at.getTransit().getTransitSectionList().size(), "the Transit is unchanged");
        assertEquals(Section.FREE, section("Block5").getState(), "Block5 is not allocated");
    }

    // With signal masts, the Auto Allocator logged errors when it came across
    // the extra Section's request, and could fail on the request being
    // disposed of while it looked at it. tearDown fails on any error logged.
    @Test
    public void testExtraSectionsWithSignalMasts() {
        d.setSignalType(DispatcherFrame.SIGNALMAST);
        ActiveTrain at = startTrain("Blocks FWD", "Block1", "Block4", "Block1");
        assertTrue(extend(at, "Block5+Turnout"), "Block5+Turnout added to the train's path");
        ThreadingUtil.runOnGUI(() -> d.terminateActiveTrain(at, true, false));
        d.queueWaitForEmpty();

        InstanceManager.getDefault(SensorManager.class).getSensor("Block1").setCommandedState(Sensor.INACTIVE);
        ActiveTrain fwd = startTrain("DN:Blocks FWD", "Block5", "Block2", "Block5");
        ThreadingUtil.runOnGUI(() -> d.allocateExtraSection(fwd, section("DN:Block1+Turnout"), null));
        assertEquals(Section.FORWARD, section("DN:Block1+Turnout").getState());
    }

    private boolean extend(ActiveTrain at, String sectionName) {
        boolean[] ok = new boolean[1];
        ThreadingUtil.runOnGUI(() -> ok[0] = d.extendActiveTrainsPath(section(sectionName), at, null));
        return ok[0];
    }

    private ActiveTrain startTrain(String transit, String startBlock, String endBlock, String occupied) {
        InstanceManager.getDefault(SensorManager.class).getSensor(occupied).setCommandedState(Sensor.ACTIVE);
        ActiveTrain[] at = new ActiveTrain[1];
        ThreadingUtil.runOnGUI(() -> {
            at[0] = d.createActiveTrain(transit, "X", ActiveTrain.USER, startBlock, 1, endBlock, 4,
                    false, "3", 5, false, false, false, null, ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN);
            assertNotNull(at[0], "train created");
            at[0].setDispatcher(d);
            d.allocateNewActiveTrain(at[0]);
        });
        JUnitUtil.waitFor(() -> at[0].getEndBlockSection() != null
                && at[0].getEndBlockSection().getState() != Section.FREE, "the whole Transit allocated");
        d.queueWaitForEmpty();
        return at[0];
    }

    private static Section section(String name) {
        return InstanceManager.getDefault(SectionManager.class).getSection(name);
    }

    private static Turnout turnout(String name) {
        return InstanceManager.getDefault(TurnoutManager.class).getTurnout(name);
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
        InstanceManager.getDefault(ConfigureManager.class).load(
                new File("java/test/jmri/jmrit/dispatcher/ExtraSectionLayout.xml"));
        InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();
        // both turnouts thrown, as in the issue
        turnout("TRANSIT_TEST_L").setCommandedState(Turnout.THROWN);
        turnout("TRANSIT_TEST_R").setCommandedState(Turnout.THROWN);
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/ExtraSectionOptions.xml");
        d = InstanceManager.getDefault(DispatcherFrame.class);
        d.stopStartAutoAllocateRelease();
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            JUnitUtil.dispose(d);
        }
        JUnitUtil.closeAllPanels();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
