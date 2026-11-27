package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.Transit;
import jmri.jmrit.simulation.SimulationTestLayouts;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Scenarios for ghost Blocks, as the Block Table help describes them: a Block
 * with no real occupancy sensor, such as a short turnout Block, given an
 * internal sensor that only Dispatcher sets. An automatic train runs round
 * {@link SimulationTestLayouts#loopWithPassingLoop()} from Line1 to Main, with
 * the turnout Block West as the ghost.
 * <p>
 * These check what Dispatcher does today, so that a change near ghost Blocks
 * shows up here.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
@Timeout(120)
public class GhostBlockScenarioTest {

    private DispatcherScenario s;

    private Transit roundToMain() throws NamedBean.BadNameException {
        Section line12 = s.section("Line1-2", null, "Line1", "Line2");
        Section line34 = s.section("Line3-4", null, "Line3", "Line4");
        Section west = s.section("West", "Line4", "West");
        Section main = s.section("Main", "West", "Main");
        return s.transit("Round to Main", line12, line34, west, main);
    }

    /**
     * Make a Block one Dispatcher must track a train through without a
     * detector: a ghost Block, or a dark Block with no sensor.
     */
    private void undetected(boolean dark, String block) {
        if (dark) {
            s.block(block).setSensor(null);
        } else {
            s.block(block).setIsGhost(true);
        }
    }

    /**
     * Turn the Dark Blocks option on for a dark Block, once its Sections exist.
     */
    private void option(boolean dark) {
        if (dark) {
            s.dispatcher().setUseDarkBlocks(true);
        }
    }

    private static int indexOf(List<String> events, String start) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).startsWith(start)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void testDispatcherDrivesTheGhostBlocksSensor() throws IOException, NamedBean.BadNameException {
        s.block("West").setIsGhost(true);
        Transit t = roundToMain();
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);
        List<String> events = s.events();

        int westOn = indexOf(events, "West occupied");
        assertTrue(westOn >= 0, "Dispatcher set the ghost Block's sensor: " + events);
        assertTrue(events.get(westOn).contains("[Goods in Line4"),
                "before the train reached it, with its front still in Line4: " + events.get(westOn));
        assertTrue(indexOf(events, "Line4 unoccupied") >= 0, "the Block before it emptied: " + events);
        assertEquals(-1, indexOf(events, "West unoccupied"),
                "but the ghost is held while the train may still be on it: " + events);
        assertEquals(s.block("Main"), SimulationTestUtil.truth(train).getFrontBlock(),
                "stopped in its destination: " + events);
        assertTrue(SimulationTestUtil.truth(train).getBlocks().contains(s.block("West")),
                "with its tail on the ghost, as there is nothing to say when it has left: " + where());
        assertEquals(Block.OCCUPIED, s.block("West").getState(), "so the ghost Block shows occupied");
    }

    /**
     * A ghost Block of no length, a join between two turnouts as ghost Blocks
     * were designed for, is cleared when the Block before it empties, as
     * before: the train is past it then.
     */
    @Test
    public void testGhostOfNoLengthClearedWhenTheBlockBeforeEmpties() throws IOException, NamedBean.BadNameException {
        s.block("West").setIsGhost(true);
        s.block("West").setLength(0);
        Transit t = roundToMain();
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);
        List<String> events = s.events();
        int westOn = indexOf(events, "West occupied");
        assertTrue(westOn >= 0, "Dispatcher set the ghost Block's sensor: " + events);
        int line4Off = indexOf(events, "Line4 unoccupied");
        int westOff = indexOf(events, "West unoccupied");
        assertTrue(line4Off >= 0 && westOff == line4Off + 1,
                "and cleared it when the Block before it emptied: " + events);
        assertEquals(s.block("Main"), SimulationTestUtil.truth(train).getFrontBlock(), "stopped in Main: " + events);
        assertEquals(Block.UNOCCUPIED, s.block("West").getState(), "the ghost Block shows empty");
        // the simulated train still needs track to move through
        jmri.util.JUnitAppender.assertWarnMessage("Block \"West\" has no length; simulated trains take it as 300 mm.");
    }

    /**
     * The help says never to have two ghost Blocks in a row. If there are,
     * Dispatcher logs an error when the train is in the first and the second
     * is next, and stops the train, before the second. Before, it only logged
     * the error, cleared the first ghost and lost the train in the second.
     */
    @Test
    public void testTwoGhostBlocksInARow() throws IOException, NamedBean.BadNameException {
        twoGhostBlocksInARow(false);
    }

    /**
     * The same with a dark Block and the Dark Blocks option.
     */
    @Test
    public void testTwoDarkBlocksInARow() throws IOException, NamedBean.BadNameException {
        twoGhostBlocksInARow(true);
    }

    private void twoGhostBlocksInARow(boolean dark) throws IOException, NamedBean.BadNameException {
        undetected(dark, "West");
        undetected(dark, "Main");
        Transit t = roundToMain();
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        option(dark);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));

        s.waitFor(() -> at.getStatus() == ActiveTrain.STOPPED, "train stopped");
        s.waitForStop(at, train);
        assertNotNull(JUnitAppender.checkForMessageStartingWith(
                "Stopping due to two consecutive no sensor blocks [West], [Main]"), "the two ghost Blocks reported");
        List<String> events = s.events();
        assertEquals(-1, indexOf(events, "Main occupied"), "Dispatcher never set the second one's sensor: " + events);
        assertTrue(!SimulationTestUtil.truth(train).getBlocks().contains(s.block("Main")),
                "stopped before the second ghost Block: " + where());
    }

    /**
     * The ghost is held until the train has entered the Block after it and
     * that Block has emptied, so Auto Release does not free the ghost's
     * Section while the end of the train (300 mm, the ghost 250 mm) may still
     * be in it. Before, the ghost cleared when the Block before it emptied,
     * and its Section was freed under the train.
     */
    @Test
    public void testGhostSectionHeldWhileTheTrainIsStillInIt() throws IOException, NamedBean.BadNameException {
        s.block("West").setIsGhost(true);
        Transit t = roundToMain();
        Section west = InstanceManager.getDefault(SectionManager.class).getSection("West");
        List<String> whenFreed = watchFreed(west);
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));
        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);

        assertTrue(whenFreed.isEmpty(), "the ghost's Section not freed under the train: " + whenFreed
                + " " + s.events());
    }

    /**
     * The same, with a second train waiting for the ghost's Section to take
     * the loop road through it the other way. Before, the second train was
     * given the Section, and its turnout set for the loop road, while the end
     * of the first train was still on it, which derailed the first train.
     * Now the ghost is held, so the second train waits, still once the first
     * has been terminated standing on it.
     */
    @Test
    public void testGhostSectionNotGivenToAnotherTrainUnderTheFirst() throws IOException, NamedBean.BadNameException {
        ghostSectionNotGivenToAnotherTrainUnderTheFirst(false);
    }

    /**
     * The same with a dark Block and the Dark Blocks option.
     */
    @Test
    public void testDarkSectionNotGivenToAnotherTrainUnderTheFirst() throws IOException, NamedBean.BadNameException {
        ghostSectionNotGivenToAnotherTrainUnderTheFirst(true);
    }

    private void ghostSectionNotGivenToAnotherTrainUnderTheFirst(boolean dark) throws IOException, NamedBean.BadNameException {
        undetected(dark, "West");
        Transit round = roundToMain();
        Section west = InstanceManager.getDefault(SectionManager.class).getSection("West");
        Section loop = s.section("Loop", "East", "Loop");
        Section line34 = InstanceManager.getDefault(SectionManager.class).getSection("Line3-4");
        Transit back = s.transit("Loop to Line3", new int[]{Section.FORWARD, Section.REVERSE, Section.REVERSE},
                loop, west, line34);
        List<String> turnoutSet = new ArrayList<>();
        jmri.Turnout westTurnout = s.layout().turnout("West");
        westTurnout.addPropertyChangeListener(e -> {
            if ("CommandedState".equals(e.getPropertyName())) {
                turnoutSet.add((westTurnout.getCommandedState() == jmri.Turnout.THROWN ? "thrown" : "closed")
                        + where());
            }
        });
        VirtualTrain goods = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        VirtualTrain local = s.place("Local", SimulationTestLayouts.Loco.TANK, 200, "Loop", "East", 700);
        option(dark);
        ActiveTrain goodsAt = s.run(s.trainInfo(round, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));
        s.waitFor(() -> west.getState() != Section.FREE, "the ghost's Section allocated to Goods");
        s.run(s.trainInfo(back, SimulationTestLayouts.Loco.TANK, 200, "Loop", "Line3"));

        s.waitFor(() -> SimulationTestUtil.truth(goods).getFrontBlock() == s.block("Main"), "Goods into Main");
        s.waitForStop(goodsAt, goods);
        JUnitUtil.waitFor(5000);
        assertTrue(where().contains("[Goods Main West]"), "Goods stands with its end on the ghost: " + where());
        turnoutSet.forEach(x -> assertTrue(!x.startsWith("thrown"),
                "the ghost's turnout never set for Local: " + turnoutSet));
        assertTrue(!SimulationTestUtil.truth(goods).isDerailed(), "Goods not derailed: " + s.events());
        assertEquals(SimulationTestUtil.truth(local).getFrontBlock(), s.block("Loop"), "Local waits in Loop");
        // Goods ended with its end still on the ghost: the ghost stays held
        s.dispatcher().terminateActiveTrain(goodsAt, true, false);
        JUnitUtil.waitFor(3000);
        assertEquals(Block.OCCUPIED, s.block("West").getState(), "the ghost still held once Goods has ended");
        ActiveTrain localAt = s.dispatcher().getActiveTrainsList().get(0);
        assertEquals(1, localAt.getAllocatedSectionList().size(), "Local still not given the ghost's Section");
        assertTrue(!turnoutSet.stream().anyMatch(x -> x.startsWith("thrown")), "nor its turnout: " + turnoutSet);
        s.dispatcher().terminateActiveTrain(localAt, true, false);
    }

    /**
     * Records the Blocks every train is really in each time a Section is
     * freed.
     */
    private static List<String> watchFreed(Section section) {
        List<String> whenFreed = new ArrayList<>();
        section.addPropertyChangeListener(e -> {
            if ("state".equals(e.getPropertyName()) && section.getState() == Section.FREE) {
                whenFreed.add(where());
            }
        });
        return whenFreed;
    }

    /**
     * Rule G1, the ghost at the entry end of its Section: the Section of
     * West and Main, entered at West. The ghost's sensor still clears when
     * Line4 empties, but the Section is held by Main, and freed only after the
     * train has left Main, so off the turnout.
     */
    @Test
    public void testG1GhostFirstInItsSection() throws IOException, NamedBean.BadNameException {
        g1GhostFirstInItsSection(false);
    }

    /**
     * The same with a dark Block and the Dark Blocks option.
     */
    @Test
    public void testG1DarkFirstInItsSection() throws IOException, NamedBean.BadNameException {
        g1GhostFirstInItsSection(true);
    }

    private void g1GhostFirstInItsSection(boolean dark) throws IOException, NamedBean.BadNameException {
        undetected(dark, "West");
        Section line34 = s.section("Line3-4", null, "Line3", "Line4");
        Section westMain = s.section("West-Main", null, "West", "Main");
        Section east = s.section("East", "Main|Loop", "East");
        Section line12 = s.section("Line1-2", null, "Line1", "Line2");
        Transit t = s.transit("Round past the ghost", line34, westMain, east, line12);
        List<String> whenFreed = watchFreed(westMain);
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2", 400);
        option(dark);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Line2"), "train into Line2");
        s.waitForStop(at, train);
        assertTrue(s.events().stream().anyMatch(e -> e.startsWith("West unoccupied")),
                "Dispatcher still set and cleared the ghost's sensor: " + s.events());
        assertEquals(1, whenFreed.size(), "the ghost's Section freed once: " + s.events());
        assertTrue(!whenFreed.get(0).contains(" West"), "only once the train was off the ghost: " + whenFreed);
    }

    /**
     * Rule G1, the ghost in the middle of its Section: Line4, West, Main.
     */
    @Test
    public void testG1GhostInTheMiddleOfItsSection() throws IOException, NamedBean.BadNameException {
        g1GhostInTheMiddleOfItsSection(false);
    }

    /**
     * The same with a dark Block and the Dark Blocks option.
     */
    @Test
    public void testG1DarkInTheMiddleOfItsSection() throws IOException, NamedBean.BadNameException {
        g1GhostInTheMiddleOfItsSection(true);
    }

    private void g1GhostInTheMiddleOfItsSection(boolean dark) throws IOException, NamedBean.BadNameException {
        undetected(dark, "West");
        Section line3 = s.section("Line3", "Line2", "Line3");
        Section mid = s.section("Line4-West-Main", null, "Line4", "West", "Main");
        Section east = s.section("East", "Main|Loop", "East");
        Section line12 = s.section("Line1-2", null, "Line1", "Line2");
        Transit t = s.transit("Round past the ghost", line3, mid, east, line12);
        List<String> whenFreed = watchFreed(mid);
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2", 400);
        option(dark);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Line2"), "train into Line2");
        s.waitForStop(at, train);
        assertEquals(1, whenFreed.size(), "the ghost's Section freed once: " + s.events());
        assertTrue(!whenFreed.get(0).contains(" West"), "only once the train was off the ghost: " + whenFreed);
    }

    /**
     * Rule G1 for both directions through the turnout, with overlapping
     * Sections: Goods runs Line4, West, Main with West first in Section
     * West-Main; Local, waiting in the Loop, runs the other way through West
     * into Line4, with West first in its own Section (Line4-West, travelled
     * in reverse). The same as the derailment above, but Local is not given
     * West until Goods is off the turnout, and Goods is not derailed.
     */
    @Test
    public void testG1BothDirectionsWithOverlappingSections() throws IOException, NamedBean.BadNameException {
        g1BothDirectionsWithOverlappingSections(false);
    }

    /**
     * The same with a dark Block and the Dark Blocks option.
     */
    @Test
    public void testG1DarkBothDirectionsWithOverlappingSections() throws IOException, NamedBean.BadNameException {
        g1BothDirectionsWithOverlappingSections(true);
    }

    private void g1BothDirectionsWithOverlappingSections(boolean dark) throws IOException, NamedBean.BadNameException {
        undetected(dark, "West");
        Section line34 = s.section("Line3-4", null, "Line3", "Line4");
        Section westMain = s.section("West-Main", null, "West", "Main");
        Section east = s.section("East", "Main|Loop", "East");
        Section line12 = s.section("Line1-2", null, "Line1", "Line2");
        Transit round = s.transit("Round past the ghost", line34, westMain, east, line12);
        Section loop = s.section("Loop", "East", "Loop");
        Section line4West = s.section("Line4-West", null, "Line4", "West");
        Section line3 = s.section("Line3", "Line2", "Line3");
        Transit back = s.transit("Loop to Line3", new int[]{Section.FORWARD, Section.REVERSE, Section.REVERSE},
                loop, line4West, line3);
        List<String> turnoutSet = new ArrayList<>();
        jmri.Turnout westTurnout = s.layout().turnout("West");
        westTurnout.addPropertyChangeListener(e -> {
            if ("CommandedState".equals(e.getPropertyName())) {
                turnoutSet.add((westTurnout.getCommandedState() == jmri.Turnout.THROWN ? "thrown" : "closed")
                        + where());
            }
        });
        VirtualTrain goods = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2", 400);
        VirtualTrain local = s.place("Local", SimulationTestLayouts.Loco.TANK, 200, "Loop", "East", 700);
        option(dark);
        ActiveTrain goodsAt = s.run(s.trainInfo(round, SimulationTestLayouts.Loco.DIESEL, 300, "Line3", "Line2"));
        s.waitFor(() -> westMain.getState() != Section.FREE, "the ghost's Section allocated to Goods");
        ActiveTrain localAt = s.run(s.trainInfo(back, SimulationTestLayouts.Loco.TANK, 200, "Loop", "Line3"));

        s.waitFor(() -> SimulationTestUtil.truth(local).getFrontBlock() == s.block("Line3"), "Local into Line3");
        s.waitForStop(localAt, local);
        s.waitFor(() -> SimulationTestUtil.truth(goods).getFrontBlock() == s.block("Line2"), "Goods into Line2");
        s.waitForStop(goodsAt, goods);
        List<String> thrown = new ArrayList<>();
        turnoutSet.forEach(x -> {
            if (x.startsWith("thrown")) {
                thrown.add(x);
            }
        });
        assertTrue(!thrown.isEmpty(), "the turnout was set for Local: " + turnoutSet);
        thrown.forEach(x -> assertTrue(!x.matches(".*\\[Goods[^\\]]* West[ \\]].*"),
                "never set for Local with Goods on it: " + turnoutSet));
        assertTrue(!SimulationTestUtil.truth(goods).isDerailed(), "Goods not derailed: " + s.events());
        assertTrue(!SimulationTestUtil.truth(local).isDerailed(), "Local not derailed: " + s.events());
        // Local enters Line3 with Goods standing in Line2, so Line3 has two
        // occupied neighbours and Block tracking cannot pass a train on to it
        JUnitAppender.suppressWarnMessageStartsWith("count of 2 ACTIVE neighbors with proper direction can't be handled for block Line3");
    }

    /**
     * Rule G2: a train that ends its run with its tail on a ghost. The ghost
     * is first in the destination Section (rule G1 kept), but the train,
     * 1400 mm, is longer than Main, 1200 mm. While it stands there its Section
     * is held; once it is terminated, the Section is freed with the train
     * still on the turnout, but the ghost stays held and shows occupied.
     */
    @Test
    public void testG2TrainLeftStandingOnTheGhost() throws IOException, NamedBean.BadNameException {
        s.block("West").setIsGhost(true);
        Section approach = s.section("Line2-3-4", null, "Line2", "Line3", "Line4");
        Section westMain = s.section("West-Main", null, "West", "Main");
        Transit t = s.transit("Into Main", approach, westMain);
        List<String> whenFreed = watchFreed(westMain);
        VirtualTrain train = s.place("Long", SimulationTestLayouts.Loco.DIESEL, 1400, "Line3", "Line2", 850);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 1400, "Line3", "Main"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);
        String standing = where();
        assertTrue(whenFreed.isEmpty(), "held while the train stands there: " + whenFreed);
        s.dispatcher().terminateActiveTrain(at, true, false);
        s.waitFor(() -> westMain.getState() == Section.FREE, "Section freed on terminating");
        assertTrue(standing.contains(" West") && whenFreed.get(0).contains(" West"),
                "freed with the train still on the ghost: " + standing + " " + whenFreed);
        assertEquals(Block.OCCUPIED, s.block("West").getState(),
                "but the ghost is still held, so shows occupied and is not given to another train");
    }

    /**
     * A dark Block (no sensor at all) where the ghost would be, without the
     * Dark Blocks option: as before, the Block is undetected, so its
     * Section's occupancy is not known, and the train is stopped.
     */
    @Test
    public void testDarkBlockStopsTheTrainWithoutTheOption() throws IOException, NamedBean.BadNameException {
        s.block("West").setSensor(null);
        Transit t = roundToMain();
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);
        assertEquals(Block.UNDETECTED, s.block("West").getState(), "the dark Block is undetected");
        assertNotNull(JUnitAppender.checkForMessageStartingWith(
                "Occupancy of block West (IBWest) is not OCCUPIED or UNOCCUPIED"), "its Section's occupancy not known");
        assertNotNull(JUnitAppender.checkForMessageStartingWith(
                "SimDiesel / Round to Main:Block [Main] ahead has been entered illegally or next block [West] never entered."),
                "the train seen in Main without passing West");
        assertEquals(ActiveTrain.STOPPED, at.getStatus(), "the train stopped by Dispatcher: " + s.events());
        s.dispatcher().terminateActiveTrain(at, true, false);
        JUnitUtil.waitFor(3000);
        while (JUnitAppender.checkForMessageStartingWith(
                "Occupancy of block West (IBWest) is not OCCUPIED or UNOCCUPIED") != null) {
            // reported each time the Section's occupancy is read
        }
    }

    /**
     * The same with the Dark Blocks option: Dispatcher shows the dark Block
     * unoccupied, then occupied as the train comes to it, and holds it as it
     * holds a ghost Block until the train is clear of it.
     */
    @Test
    public void testDarkBlockTrackedWithTheOption() throws IOException, NamedBean.BadNameException {
        s.block("West").setSensor(null);
        Transit t = roundToMain();
        s.dispatcher().setUseDarkBlocks(true);
        assertEquals(Block.UNOCCUPIED, s.block("West").getState(), "the dark Block shows unoccupied");
        Section west = InstanceManager.getDefault(SectionManager.class).getSection("West");
        List<String> whenFreed = watchFreed(west);
        VirtualTrain train = s.place("Goods", SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "East", 400);
        ActiveTrain at = s.run(s.trainInfo(t, SimulationTestLayouts.Loco.DIESEL, 300, "Line1", "Main"));

        s.waitFor(() -> SimulationTestUtil.truth(train).getFrontBlock() == s.block("Main"), "train into Main");
        s.waitForStop(at, train);
        List<String> events = s.events();
        int westOn = indexOf(events, "West occupied");
        assertTrue(westOn >= 0 && events.get(westOn).contains("[Goods in Line4"),
                "occupied as the train came to it: " + events);
        assertTrue(indexOf(events, "Main occupied") > westOn, "the train tracked on into Main: " + events);
        assertEquals(s.block("Main"), SimulationTestUtil.truth(train).getFrontBlock(), "stopped in Main: " + events);
        assertEquals(Block.OCCUPIED, s.block("West").getState(), "held while the train's end may be on it");
        assertTrue(whenFreed.isEmpty(), "its Section not freed under the train: " + whenFreed);

        s.dispatcher().setUseDarkBlocks(false);
        assertEquals(Block.OCCUPIED, s.block("West").getState(), "an occupied dark Block left as it is");
    }

    private static String where() {
        StringBuilder sb = new StringBuilder();
        for (jmri.jmrit.simulation.SimulationTruth.TrainTruth t : SimulationTestUtil.truth().getTrains()) {
            sb.append(" [").append(t.getTrain().getName());
            t.getBlocks().forEach(b -> sb.append(' ').append(b.getUserName()));
            sb.append(']');
        }
        return sb.toString();
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        s = new DispatcherScenario(folder, SimulationTestLayouts::loopWithPassingLoop);
    }

    @AfterEach
    public void tearDown() {
        s.dispose();
        JUnitUtil.tearDown();
    }
}
