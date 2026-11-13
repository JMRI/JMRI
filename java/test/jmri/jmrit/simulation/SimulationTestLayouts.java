package jmri.jmrit.simulation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.jmrit.display.layoutEditor.HitPointType;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.jdom2.JDOMException;

/**
 * Standard layouts and locos for tests with simulated trains, in any package.
 * <p>
 * Each layout is drawn on its own Layout Editor panel, with Layout Blocks,
 * internal sensors and turnouts, and real lengths in millimetres, and has the
 * Block paths JMRI makes after loading a panel (see
 * {@link SimulationLayoutBuilder} for how things are named). All turnouts
 * start closed. The layouts are:
 * <ul>
 * <li>{@link #loopWithPassingLoop()}: an oval with a station on it, where a
 * main platform road and a loop road run between two turnouts;
 * <li>{@link #terminus()}: an approach, a throat turnout and two platform
 * roads ending at end bumpers;
 * <li>{@link #junctionWithCrossover()}: double track with a crossover between
 * the lines, and a junction where a branch line leaves one of them.
 * </ul>
 * The locos ({@link Loco}) are made up, not anyone's real ones. Each is
 * written as a roster file in the format DecoderPro uses, with CV3, CV4 and
 * CV29 and a speed profile, and loaded as a roster entry by
 * {@link #rosterEntry(Loco)}.
 * <p>
 * Typical use:
 * <pre>
 * &#64;BeforeEach
 * public void setUp(&#64;TempDir File folder) {
 *     JUnitUtil.setUp();
 *     SimulationTestLayouts.setUpProfile(folder);
 *     layout = SimulationTestLayouts.terminus();
 * }
 *
 * &#64;Test
 * public void testIntoPlatform2() throws IOException {
 *     layout.turnout("Throat").setCommandedState(Turnout.THROWN);
 *     RosterEntry tank = SimulationTestLayouts.rosterEntry(SimulationTestLayouts.Loco.TANK);
 *     VirtualTrain t = VirtualTrain.builder("Local")
 *             .loco(SimulatedLoco.builder(tank).length(120).build()).length(200).build();
 *     SimulationTestUtil.place(t, layout.block("Approach"), null, 1200);
 *     SimulationTestUtil.throttle(3).setSpeedSetting(0.8f);
 *     SimulationTestUtil.waitForFrontIn(t, layout.block("Platform2"));
 *     ...
 * }
 *
 * &#64;AfterEach
 * public void tearDown() {
 *     SimulationTestUtil.removeTrains();
 *     layout.dispose();
 *     JUnitUtil.deregisterBlockManagerShutdownTask();
 *     JUnitUtil.tearDown();
 * }
 * </pre>
 * The panels need a display, so tests using them are marked
 * {@code @DisabledIfHeadless}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class SimulationTestLayouts {

    private SimulationTestLayouts() {
        // static methods only
    }

    /**
     * Set up a profile for the standard layouts and locos, after
     * {@code JUnitUtil.setUp()}: a profile in a folder of the test's own, a
     * configuration manager, the fully simulated profile of
     * {@link SimulationTestUtil#setUpSimulatedProfile()}, a Layout Block
     * manager, and a roster in the profile.
     *
     * @param folder an empty folder for the profile, such as one from
     *               {@code @TempDir}
     */
    public static void setUpProfile(@Nonnull File folder) {
        JUnitUtil.resetProfileManager(folder);
        JUnitUtil.initConfigureManager();
        SimulationTestUtil.setUpSimulatedProfile();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initRosterConfigManager();
    }

    /**
     * An oval of plain line, with a station where a main platform road and a
     * loop road run between two turnouts. A train running from Line1 to
     * Line2 goes round Line3, Line4, West, then Main or Loop, then East, and
     * back to Line1.
     * <pre>
     *        Line3 ------------ Line2
     *         |                   |
     *       Line4                Line1
     *         |                   |
     *        West ---- Main ---- East
     *           \                 /
     *            ----- Loop ------
     * </pre>
     * Blocks and lengths: Line1 900 mm, Line2 700, Line3 900, Line4 700, West
     * 250, Main 1200, Loop 1250, East 250. Turnouts: West (in Block West) and
     * East (in Block East); closed for Main, thrown for Loop.
     *
     * @return the layout
     */
    @Nonnull
    public static SimulationTestLayout loopWithPassingLoop() {
        SimulationLayoutBuilder b = new SimulationLayoutBuilder("Loop with a passing loop");
        b.block("Line1", 900);
        b.block("Line2", 700);
        b.block("Line3", 900);
        b.block("Line4", 700);
        b.block("West", 250);
        b.block("Main", 1200);
        b.block("Loop", 1250);
        b.block("East", 250);
        LayoutTurnout west = b.turnout("West", "West", true, 300, 400, 0);
        LayoutTurnout east = b.turnout("East", "East", false, 900, 400, 180);
        b.track(west, HitPointType.TURNOUT_B, east, HitPointType.TURNOUT_B, "Main");
        b.track(west, HitPointType.TURNOUT_C, east, HitPointType.TURNOUT_C, "Loop");
        PositionablePoint a1 = b.anchor(1100, 250);
        PositionablePoint a2 = b.anchor(600, 100);
        PositionablePoint a3 = b.anchor(100, 250);
        b.track(east, HitPointType.TURNOUT_A, a1, HitPointType.POS_POINT, "Line1");
        b.track(a1, HitPointType.POS_POINT, a2, HitPointType.POS_POINT, "Line2");
        b.track(a2, HitPointType.POS_POINT, a3, HitPointType.POS_POINT, "Line3");
        b.track(a3, HitPointType.POS_POINT, west, HitPointType.TURNOUT_A, "Line4");
        return b.build();
    }

    /**
     * A terminus: an approach from an end bumper, a throat turnout, and two
     * platform roads ending at end bumpers.
     * <pre>
     *   |- Approach ---- Throat ---- Platform1 -|
     *                          \
     *                           --- Platform2 -|
     * </pre>
     * Blocks and lengths: Approach 1500 mm, Throat 250, Platform1 1200,
     * Platform2 1100. Turnout: Throat (in Block Throat); closed for
     * Platform1, thrown for Platform2.
     *
     * @return the layout
     */
    @Nonnull
    public static SimulationTestLayout terminus() {
        SimulationLayoutBuilder b = new SimulationLayoutBuilder("Terminus");
        b.block("Approach", 1500);
        b.block("Throat", 250);
        b.block("Platform1", 1200);
        b.block("Platform2", 1100);
        LayoutTurnout throat = b.turnout("Throat", "Throat", true, 700, 300, 0);
        b.track(b.endBumper(100, 300), HitPointType.POS_POINT, throat, HitPointType.TURNOUT_A, "Approach");
        b.track(throat, HitPointType.TURNOUT_B, b.endBumper(1300, 300), HitPointType.POS_POINT, "Platform1");
        b.track(throat, HitPointType.TURNOUT_C, b.endBumper(1300, 380), HitPointType.POS_POINT, "Platform2");
        return b.build();
    }

    /**
     * Double track with a right-hand crossover between the lines, each line's
     * half of it in a Block of its own, and a junction turnout on the Up line
     * leading to a branch line. All lines end at end bumpers.
     * <pre>
     *   |- Up1 -- CrossoverUp ---- Up2 ---- Junction ---- Up3 -|
     *                   \                         \
     *                    \                         --- Branch -|
     *   |- Down1 - CrossoverDown -- Down2 -|
     * </pre>
     * The crossover's A-B is the Up line (Up1 to Up2) and its D-C the Down
     * line (Down1 to Down2); thrown, it joins Up1 to Down2. With it closed,
     * a train can use each line at once. Blocks and lengths: Up1 1200 mm,
     * CrossoverUp 400, Up2 900, Junction 250, Up3 1000, Branch 1000, Down1
     * 1200, CrossoverDown 400, Down2 1500. Turnouts: Crossover (in Blocks
     * CrossoverUp and CrossoverDown) and Junction (in Block Junction; closed
     * for Up3, thrown for Branch).
     *
     * @return the layout
     */
    @Nonnull
    public static SimulationTestLayout junctionWithCrossover() {
        SimulationLayoutBuilder b = new SimulationLayoutBuilder("Junction with a crossover");
        b.block("Up1", 1200);
        b.block("CrossoverUp", 400);
        b.block("Up2", 900);
        b.block("Junction", 250);
        b.block("Up3", 1000);
        b.block("Branch", 1000);
        b.block("Down1", 1200);
        b.block("CrossoverDown", 400);
        b.block("Down2", 1500);
        LayoutTurnout xo = b.crossover("Crossover", "CrossoverUp", "CrossoverDown", 500, 250);
        LayoutTurnout junction = b.turnout("Junction", "Junction", false, 1100, 200, 0);
        b.track(b.endBumper(100, 200), HitPointType.POS_POINT, xo, HitPointType.TURNOUT_A, "Up1");
        b.track(xo, HitPointType.TURNOUT_B, junction, HitPointType.TURNOUT_A, "Up2");
        b.track(junction, HitPointType.TURNOUT_B, b.endBumper(1600, 200), HitPointType.POS_POINT, "Up3");
        b.track(junction, HitPointType.TURNOUT_C, b.endBumper(1600, 80), HitPointType.POS_POINT, "Branch");
        b.track(b.endBumper(100, 300), HitPointType.POS_POINT, xo, HitPointType.TURNOUT_D, "Down1");
        b.track(xo, HitPointType.TURNOUT_C, b.endBumper(1600, 300), HitPointType.POS_POINT, "Down2");
        return b.build();
    }

    /**
     * The throttle settings at which the locos' speeds are given: speed steps
     * 10, 20 ... 120 and 126 of 126, in thousandths of full throttle, as a
     * roster speed profile keeps them.
     */
    private static final int[] PROFILE_STEPS = {79, 159, 238, 317, 397, 476, 556, 635, 714, 794, 873, 952, 1000};

    /**
     * Made-up locos for tests, each with a roster file holding CVs and a speed
     * profile. Speeds are in mm/s at the throttle settings of the profile.
     */
    public enum Loco {

        /**
         * "SimTank", short address 3: light momentum (CV3 3, CV4 2), CV29 6,
         * a little slower in reverse; 247 mm/s at full throttle.
         */
        TANK("SimTank", 3, false, new int[]{1, 3, 3, 3, 4, 2, 29, 6},
                new float[]{22, 45, 68, 91, 113, 134, 154, 173, 191, 208, 224, 239, 247},
                new float[]{21, 43, 65, 87, 108, 128, 147, 165, 182, 198, 213, 227, 235}),

        /**
         * "SimDiesel", long address 4321: more momentum (CV3 6, CV4 4), CV29
         * 34 (long address), a little slower in reverse; 405 mm/s at full
         * throttle.
         */
        DIESEL("SimDiesel", 4321, true, new int[]{3, 6, 4, 4, 17, 208, 18, 225, 29, 34},
                new float[]{35, 72, 110, 147, 183, 218, 252, 284, 314, 342, 368, 392, 405},
                new float[]{33, 69, 105, 141, 176, 210, 243, 274, 303, 330, 355, 378, 391}),

        /**
         * "SimShunter", short address 7: no momentum (CV3 and CV4 0), CV29 6,
         * the same speed both ways; 134 mm/s at full throttle.
         */
        SHUNTER("SimShunter", 7, false, new int[]{1, 7, 3, 0, 4, 0, 29, 6},
                new float[]{12, 25, 38, 50, 62, 73, 84, 94, 104, 113, 122, 130, 134},
                new float[]{12, 25, 38, 50, 62, 73, 84, 94, 104, 113, 122, 130, 134});

        private final String id;
        private final int address;
        private final boolean longAddress;
        private final SortedMap<Integer, Integer> cvs = new TreeMap<>();
        private final float[] forward;
        private final float[] reverse;

        Loco(String id, int address, boolean longAddress, int[] cvPairs, float[] forward, float[] reverse) {
            this.id = id;
            this.address = address;
            this.longAddress = longAddress;
            for (int i = 0; i < cvPairs.length; i += 2) {
                cvs.put(cvPairs[i], cvPairs[i + 1]);
            }
            this.forward = forward;
            this.reverse = reverse;
        }

        /**
         * @return the roster id, also the roster file's name without ".xml"
         */
        @Nonnull
        public String getId() {
            return id;
        }

        /**
         * @return the loco's address
         */
        @Nonnull
        public DccLocoAddress getAddress() {
            return new DccLocoAddress(address, longAddress);
        }

        /**
         * @return the CV values in the roster file, by CV number
         */
        @Nonnull
        public Map<Integer, Integer> getCvs() {
            return Collections.unmodifiableSortedMap(cvs);
        }

        /**
         * @return the profile's speeds, by throttle setting in thousandths
         *         of full throttle, each as {forward, reverse} in mm/s
         */
        @Nonnull
        public SortedMap<Integer, float[]> getSpeeds() {
            SortedMap<Integer, float[]> speeds = new TreeMap<>();
            for (int i = 0; i < PROFILE_STEPS.length; i++) {
                speeds.put(PROFILE_STEPS[i], new float[]{forward[i], reverse[i]});
            }
            return speeds;
        }

        /**
         * @return the roster file's contents, as DecoderPro writes them
         */
        @Nonnull
        String rosterXml() {
            StringBuilder s = new StringBuilder();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            s.append("<?xml-stylesheet href=\"/xml/XSLT/locomotive.xsl\" type=\"text/xsl\"?>\n");
            s.append("<locomotive-config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            s.append(" xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/locomotive-config.xsd\">\n");
            s.append("  <locomotive id=\"").append(id).append("\" fileName=\"").append(id).append(".xml\"");
            s.append(" roadNumber=\"\" roadName=\"\" mfg=\"\" owner=\"\" model=\"\" dccAddress=\"").append(address);
            s.append("\" comment=\"A made-up loco for tests with simulated trains\">\n");
            s.append("    <decoder model=\"\" family=\"\" comment=\"\" />\n");
            s.append("    <locoaddress>\n");
            s.append("      <dcclocoaddress number=\"").append(address).append("\" longaddress=\"")
                    .append(longAddress ? "yes" : "no").append("\" />\n");
            s.append("      <number>").append(address).append("</number>\n");
            s.append("      <protocol>").append(longAddress ? "dcc_long" : "dcc_short").append("</protocol>\n");
            s.append("    </locoaddress>\n");
            s.append("    <speedprofile>\n");
            s.append("      <overRunTimeForward>0.0</overRunTimeForward>\n");
            s.append("      <overRunTimeReverse>0.0</overRunTimeReverse>\n");
            s.append("      <speeds>\n");
            for (int i = 0; i < PROFILE_STEPS.length; i++) {
                s.append("        <speed>\n");
                s.append("          <step>").append(PROFILE_STEPS[i]).append("</step>\n");
                s.append("          <forward>").append(Float.toString(forward[i])).append("</forward>\n");
                s.append("          <reverse>").append(Float.toString(reverse[i])).append("</reverse>\n");
                s.append("        </speed>\n");
            }
            s.append("      </speeds>\n");
            s.append("    </speedprofile>\n");
            s.append("    <values>\n");
            for (Map.Entry<Integer, Integer> cv : cvs.entrySet()) {
                s.append("      <CVvalue name=\"").append(cv.getKey()).append("\" value=\"").append(cv.getValue())
                        .append("\" />\n");
            }
            s.append("    </values>\n");
            s.append("  </locomotive>\n");
            s.append("</locomotive-config>\n");
            return s.toString();
        }
    }

    /**
     * Write a loco's roster file in the profile's roster folder, load it as a
     * roster entry, and add the entry to the roster.
     *
     * @param loco the loco
     * @return its roster entry
     * @throws IOException if the file can't be written or read
     */
    @Nonnull
    public static RosterEntry rosterEntry(@Nonnull Loco loco) throws IOException {
        File folder = new File(Roster.getDefault().getRosterFilesLocation());
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("can't make the roster folder " + folder);
        }
        File file = new File(folder, loco.getId() + ".xml");
        Files.write(file.toPath(), loco.rosterXml().getBytes(StandardCharsets.UTF_8));
        RosterEntry entry;
        try {
            entry = RosterEntry.fromFile(file);
        } catch (JDOMException e) {
            throw new IOException("can't read " + file, e);
        }
        Roster.getDefault().addEntry(entry);
        return entry;
    }
}
