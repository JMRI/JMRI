package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jmri.BeanSetting;
import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.XmlFile;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link SimulationTestLayouts}, {@link SimulationLayoutBuilder} and
 * {@link SimulationTestLayout}: each layout builds with its Blocks, lengths,
 * sensors and the Block paths JMRI makes from the drawing; the locos' roster
 * files load with their CVs and speed profiles; and a simulated train driven
 * by an Internal throttle runs on each layout. The runs assert the order of
 * events and the ground truth, never how long anything took. They set no
 * truth offset, so they check the layouts and JMRI's code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class SimulationTestLayoutsTest {

    private SimulationTestLayout layout;
    private final List<String> events = Collections.synchronizedList(new ArrayList<>());

    /**
     * The paths of a Block: for each neighbour, the turnout settings the path
     * needs, as "ITName closed" or "ITName thrown", or "" for none.
     */
    private static Map<String, String> paths(Block b) {
        Map<String, String> result = new TreeMap<>();
        for (Path p : b.getPaths()) {
            List<String> settings = new ArrayList<>();
            for (BeanSetting s : p.getSettings()) {
                String state = (s.getSetting() == Turnout.CLOSED) ? "closed"
                        : (s.getSetting() == Turnout.THROWN) ? "thrown" : Integer.toString(s.getSetting());
                settings.add(s.getBean().getSystemName() + " " + state);
            }
            Collections.sort(settings);
            String old = result.put(p.getBlock().getUserName(), String.join(", ", settings));
            assertEquals(null, old, "one path from " + b.getUserName() + " to " + p.getBlock().getUserName());
        }
        return result;
    }

    private static Sensor sensor(Block b) {
        Sensor s = b.getSensor();
        if (s == null) {
            return fail(b.getUserName() + " has no sensor");
        }
        return s;
    }

    private static SpeedTable table(RosterSpeedProfile profile, boolean forward) {
        SpeedTable t = SpeedTable.fromProfile(profile, forward);
        if (t == null) {
            return fail("no speed table");
        }
        return t;
    }

    private static Map<String, String> expect(String... neighbourAndSettings) {
        Map<String, String> result = new TreeMap<>();
        for (int i = 0; i < neighbourAndSettings.length; i += 2) {
            result.put(neighbourAndSettings[i], neighbourAndSettings[i + 1]);
        }
        return result;
    }

    /** Check the layout's Blocks, lengths, sensors and Layout Blocks. */
    private void checkBlocks(SimulationTestLayout l, String[] names, double[] lengths) {
        assertEquals(List.of(names), new ArrayList<>(l.getBlocks().keySet()), "the Blocks, in order");
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        for (int i = 0; i < names.length; i++) {
            Block b = l.block(names[i]);
            assertEquals("IB" + names[i], b.getSystemName());
            assertEquals(names[i], b.getUserName());
            assertEquals(lengths[i], b.getLengthMm(), 1e-3, names[i] + "'s length");
            assertEquals("IS" + names[i], sensor(b).getSystemName());
            assertEquals(Sensor.INACTIVE, sensor(b).getKnownState());
            LayoutBlock lb = lbm.getLayoutBlock(b);
            if (lb == null) {
                fail(names[i] + " has no Layout Block");
                return;
            }
            assertEquals("ILB" + names[i], lb.getSystemName());
            assertEquals("IS" + names[i], lb.getOccupancySensorName());
            assertTrue(lb.isOnPanel(l.getEditor()), names[i] + " is on the panel");
        }
        for (Map.Entry<String, Turnout> t : l.getTurnouts().entrySet()) {
            assertEquals("IT" + t.getKey(), t.getValue().getSystemName());
            assertEquals(Turnout.CLOSED, t.getValue().getKnownState(), t.getKey() + " starts closed");
        }
        assertThrows(IllegalArgumentException.class, () -> l.block("Nowhere"));
        assertThrows(IllegalArgumentException.class, () -> l.turnout("Nowhere"));
    }

    @Test
    public void testLoopWithPassingLoop() {
        layout = SimulationTestLayouts.loopWithPassingLoop();
        checkBlocks(layout, new String[]{"Line1", "Line2", "Line3", "Line4", "West", "Main", "Loop", "East"},
                new double[]{900, 700, 900, 700, 250, 1200, 1250, 250});
        assertEquals(List.of("West", "East"), new ArrayList<>(layout.getTurnouts().keySet()));
        // the oval
        assertEquals(expect("East", "", "Line2", ""), paths(layout.block("Line1")));
        assertEquals(expect("Line1", "", "Line3", ""), paths(layout.block("Line2")));
        assertEquals(expect("Line2", "", "Line4", ""), paths(layout.block("Line3")));
        assertEquals(expect("Line3", "", "West", ""), paths(layout.block("Line4")));
        // the station: each turnout's Block reaches Main closed and Loop thrown
        assertEquals(expect("Line4", "", "Main", "ITWest closed", "Loop", "ITWest thrown"),
                paths(layout.block("West")));
        assertEquals(expect("Line1", "", "Main", "ITEast closed", "Loop", "ITEast thrown"),
                paths(layout.block("East")));
        assertEquals(expect("West", "ITWest closed", "East", "ITEast closed"), paths(layout.block("Main")));
        assertEquals(expect("West", "ITWest thrown", "East", "ITEast thrown"), paths(layout.block("Loop")));
    }

    @Test
    public void testTerminus() {
        layout = SimulationTestLayouts.terminus();
        checkBlocks(layout, new String[]{"Approach", "Throat", "Platform1", "Platform2"},
                new double[]{1500, 250, 1200, 1100});
        assertEquals(List.of("Throat"), new ArrayList<>(layout.getTurnouts().keySet()));
        assertEquals(expect("Throat", ""), paths(layout.block("Approach")), "the approach starts at an end bumper");
        assertEquals(expect("Approach", "", "Platform1", "ITThroat closed", "Platform2", "ITThroat thrown"),
                paths(layout.block("Throat")));
        assertEquals(expect("Throat", "ITThroat closed"), paths(layout.block("Platform1")), "ends at a bumper");
        assertEquals(expect("Throat", "ITThroat thrown"), paths(layout.block("Platform2")), "ends at a bumper");
    }

    @Test
    public void testJunctionWithCrossover() {
        layout = SimulationTestLayouts.junctionWithCrossover();
        checkBlocks(layout, new String[]{"Up1", "CrossoverUp", "Up2", "Junction", "Up3", "Branch", "Down1",
                "CrossoverDown", "Down2"}, new double[]{1200, 400, 900, 250, 1000, 1000, 1200, 400, 1500});
        assertEquals(List.of("Crossover", "Junction"), new ArrayList<>(layout.getTurnouts().keySet()));
        assertEquals(expect("CrossoverUp", ""), paths(layout.block("Up1")));
        assertEquals(expect("CrossoverDown", ""), paths(layout.block("Down2")));
        // each line's half is its own Block, so closed, a train can use each line at once;
        // thrown, Up1 (A) is joined to Down2 (C) across the two halves
        assertEquals(expect("Up1", "", "Up2", "ITCrossover closed", "CrossoverDown", "ITCrossover thrown"),
                paths(layout.block("CrossoverUp")));
        assertEquals(expect("Down1", "ITCrossover closed", "Down2", "", "CrossoverUp", "ITCrossover thrown"),
                paths(layout.block("CrossoverDown")));
        assertEquals(expect("CrossoverUp", "ITCrossover closed", "Junction", ""), paths(layout.block("Up2")));
        assertEquals(expect("CrossoverDown", "ITCrossover closed"), paths(layout.block("Down1")));
        assertEquals(expect("Up2", "", "Up3", "ITJunction closed", "Branch", "ITJunction thrown"),
                paths(layout.block("Junction")));
        assertEquals(expect("Junction", "ITJunction closed"), paths(layout.block("Up3")));
        assertEquals(expect("Junction", "ITJunction thrown"), paths(layout.block("Branch")));
    }

    @Test
    public void testRosterEntries() throws IOException, JDOMException {
        for (SimulationTestLayouts.Loco loco : SimulationTestLayouts.Loco.values()) {
            String id = loco.getId();
            RosterEntry e = SimulationTestLayouts.rosterEntry(loco);
            assertSame(e, Roster.getDefault().getEntryForId(id), "in the roster");
            assertEquals(id + ".xml", e.getFileName());
            assertEquals(loco.getAddress(), e.getDccLocoAddress());
            File file = new File(Roster.getDefault().getRosterFilesLocation(), e.getFileName());
            assertTrue(file.isFile(), "written in the profile's roster folder");

            XmlFile xf = new XmlFile() {
            };
            if (!Boolean.getBoolean("jmri.skipschematests")) {
                xf.setValidate(XmlFile.Validate.CheckDtdThenSchema);
            }
            Element written = xf.rootFromFile(file).getChild("locomotive");
            // the speed profile is just as JMRI itself writes it
            XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
            assertEquals(out.outputString(e.store().getChild("speedprofile")),
                    out.outputString(written.getChild("speedprofile")), id + ": speed profile as JMRI writes it");

            // the CVs, as the simulator reads them
            Map<String, Integer> cvs = RosterCvs.read(e);
            for (int cv : new int[]{3, 4, 29}) {
                assertEquals(loco.getCvs().get(cv), cvs.get(Integer.toString(cv)), id + " CV" + cv);
            }
            assertEquals(loco.getCvs().size(), cvs.size(), id + ": every CV");

            SimulatedLoco sim = SimulatedLoco.builder(e).length(100).build();
            assertEquals(id, sim.getRosterId());
            assertEquals(loco.getAddress(), sim.getAddress());
            assertEquals(loco.getCvs().get(3), sim.getCv3());
            assertEquals(loco.getCvs().get(4), sim.getCv4());
            assertFalse(sim.isInConsist(), id + " is not in a consist");
            assertTrue(sim.hasMeasuredSpeeds(), id + " runs to its speed profile");

            // the profile, as the roster entry loaded it and as the simulator reads it
            RosterSpeedProfile profile = e.getSpeedProfile();
            assertNotNull(profile);
            assertEquals(loco.getSpeeds().keySet(), profile.getProfileSpeeds().keySet(), id + ": the steps");
            SpeedTable forward = table(profile, true);
            SpeedTable reverse = table(profile, false);
            for (Map.Entry<Integer, float[]> s : loco.getSpeeds().entrySet()) {
                RosterSpeedProfile.SpeedStep step = profile.getProfileSpeeds().get(s.getKey());
                assertEquals(s.getValue()[0], step.getForwardSpeed(), 1e-6);
                assertEquals(s.getValue()[1], step.getReverseSpeed(), 1e-6);
                assertEquals(s.getValue()[0], forward.speed(s.getKey() / 1000.0), 1e-3,
                        id + " forward at " + s.getKey());
                assertEquals(s.getValue()[1], reverse.speed(s.getKey() / 1000.0), 1e-3,
                        id + " reverse at " + s.getKey());
            }
        }
        assertEquals(34, SimulationTestLayouts.Loco.DIESEL.getCvs().get(29), "CV29 for a long address");
    }

    // Trains on the layouts, in real time

    /** Record each sensor change on the layout, as "Name active" or "Name inactive". */
    private void record() {
        for (Block b : layout.getBlocks().values()) {
            sensor(b).addPropertyChangeListener("KnownState", e -> events.add(b.getUserName()
                    + (Integer.valueOf(Sensor.ACTIVE).equals(e.getNewValue()) ? " active" : " inactive")));
        }
    }

    private List<String> became(String state) {
        List<String> result = new ArrayList<>();
        synchronized (events) {
            for (String e : events) {
                if (e.endsWith(" " + state)) {
                    result.add(e.substring(0, e.length() - state.length() - 1));
                }
            }
        }
        return result;
    }

    private Set<Block> occupied() {
        Set<Block> result = new HashSet<>();
        for (Block b : layout.getBlocks().values()) {
            if (sensor(b).getKnownState() == Sensor.ACTIVE) {
                result.add(b);
            }
        }
        return result;
    }

    /** Wait until the sensors report just the Blocks the train is really in. */
    private void waitForSensorsToAgree(VirtualTrain t) {
        JUnitUtil.waitFor(() -> occupied().equals(new HashSet<>(SimulationTestUtil.truth(t).getBlocks())),
                "sensors agree with where " + t.getName() + " really is");
    }

    private static void stop(DccThrottle throttle, VirtualTrain t) {
        throttle.setSpeedSetting(0.0f);
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0.0;
        }, t.getName() + " stopped");
    }

    private static VirtualTrain train(String name, RosterEntry loco, double locoLength, double length) {
        return VirtualTrain.builder(name).loco(SimulatedLoco.builder(loco).length(locoLength).build())
                .length(length).build();
    }

    @Test
    public void testTrainRoundTheLoopByTheLoopRoad() throws IOException {
        layout = SimulationTestLayouts.loopWithPassingLoop();
        layout.turnout("West").setCommandedState(Turnout.THROWN);
        layout.turnout("East").setCommandedState(Turnout.THROWN);
        record();
        VirtualTrain t = train("Express",
                SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.DIESEL), 200, 250);
        SimulationTestUtil.place(t, layout.block("Line2"), layout.block("Line1"), 300);
        JUnitUtil.waitFor(() -> became("active").equals(List.of("Line2")), "placement reported");

        DccThrottle throttle = SimulationTestUtil.throttle(4321);
        throttle.setSpeedSetting(0.8f);
        // wait in turn only for Blocks the train takes seconds to pass, so none is missed
        for (String name : new String[]{"Line4", "Loop", "Line1", "Line2"}) {
            SimulationTestUtil.waitForFrontIn(t, layout.block(name));
        }
        stop(throttle, t);
        waitForSensorsToAgree(t);

        assertEquals(List.of("Line2", "Line3", "Line4", "West", "Loop", "East", "Line1", "Line2"),
                became("active").subList(0, 8), "round the oval and through the loop road, in order");
        assertFalse(became("active").contains("Main"), "never on the main road");
        assertTrue(SimulationTestUtil.truth(t).getSpeedMmPerSecond() == 0.0);
    }

    @Test
    public void testTrainIntoTerminusPlatformAndBack() throws IOException {
        layout = SimulationTestLayouts.terminus();
        layout.turnout("Throat").setCommandedState(Turnout.THROWN);
        record();
        VirtualTrain t = train("Local", SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.TANK), 120, 200);
        SimulationTestUtil.place(t, layout.block("Approach"), null, 1200);

        DccThrottle throttle = SimulationTestUtil.throttle(3);
        throttle.setSpeedSetting(0.8f);
        SimulationTestUtil.waitForFrontIn(t, layout.block("Platform2"));
        stop(throttle, t);
        SimulationTruth.TrainTruth in = SimulationTestUtil.truth(t);
        assertEquals(layout.block("Platform2"), in.getFrontBlock(), "stopped in platform 2");
        assertTrue(in.getFrontPositionMm() < 1100, "short of the end bumper");
        waitForSensorsToAgree(t);
        assertEquals(List.of("Approach", "Throat", "Platform2"), became("active"), "in by the throat, in order");

        // back out, rear first, until the whole train is in the approach again
        throttle.setIsForward(false);
        throttle.setSpeedSetting(0.8f);
        List<Block> approach = List.of(layout.block("Approach"));
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getBlocks().equals(approach);
        }, "Local back in the approach");
        assertTrue(SimulationTestUtil.truth(t).getSpeedMmPerSecond() < 0, "moving rear first");
        stop(throttle, t);
        waitForSensorsToAgree(t);
        List<String> left = became("inactive");
        assertTrue(left.indexOf("Platform2") >= 0 && left.indexOf("Platform2") < left.lastIndexOf("Throat"),
                "left platform 2 before the throat: " + left);
        assertFalse(became("active").contains("Platform1"), "never in platform 1");
    }

    @Test
    public void testTrainAcrossTheCrossover() throws IOException {
        layout = SimulationTestLayouts.junctionWithCrossover();
        layout.turnout("Crossover").setCommandedState(Turnout.THROWN);
        record();
        VirtualTrain t = train("Shunt", SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.SHUNTER),
                100, 150);
        SimulationTestUtil.place(t, layout.block("Up1"), null, 1000);

        DccThrottle throttle = SimulationTestUtil.throttle(7);
        throttle.setSpeedSetting(1.0f);
        SimulationTestUtil.waitForFrontIn(t, layout.block("Down2"));
        stop(throttle, t);
        assertEquals(layout.block("Down2"), SimulationTestUtil.truth(t).getFrontBlock(), "crossed to the Down line");
        waitForSensorsToAgree(t);
        assertEquals(List.of("Up1", "CrossoverUp", "CrossoverDown", "Down2"), became("active"),
                "over the crossover, in order");
    }

    @Test
    public void testTrainOntoTheBranch() throws IOException {
        layout = SimulationTestLayouts.junctionWithCrossover();
        layout.turnout("Junction").setCommandedState(Turnout.THROWN);
        record();
        VirtualTrain t = train("Branch train", SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.TANK),
                120, 200);
        SimulationTestUtil.place(t, layout.block("Up1"), null, 1000);

        DccThrottle throttle = SimulationTestUtil.throttle(3);
        throttle.setSpeedSetting(0.8f);
        SimulationTestUtil.waitForFrontIn(t, layout.block("Up2"));
        SimulationTestUtil.waitForFrontIn(t, layout.block("Branch"));
        stop(throttle, t);
        assertEquals(layout.block("Branch"), SimulationTestUtil.truth(t).getFrontBlock(), "took the branch");
        waitForSensorsToAgree(t);
        assertEquals(List.of("Up1", "CrossoverUp", "Up2", "Junction", "Branch"), became("active"),
                "along the Up line and onto the branch, in order");
    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        JUnitUtil.setUp();
        SimulationTestLayouts.setUpProfile(folder);
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        if (layout != null) {
            layout.dispose();
            layout = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
