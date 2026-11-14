package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jmri.Block;
import jmri.Section;
import jmri.jmrit.dispatcher.DeadlockCheck.Move;
import jmri.jmrit.dispatcher.DeadlockCheck.Option;
import jmri.jmrit.dispatcher.DeadlockCheck.Position;
import jmri.jmrit.dispatcher.DeadlockCheck.Result;
import jmri.jmrit.dispatcher.DeadlockCheck.Route;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DeadlockCheck}.
 * <p>
 * Each layout is made of Sections of one Block each. A step that is a place is
 * written "Name*", one that isn't "Name"; alternatives are separated by "|".
 */
public class DeadlockCheckTest {

    private final Map<String, Section> sections = new LinkedHashMap<>();

    private Section section(String name, int length) {
        Section s = mock(Section.class);
        Block b = mock(Block.class);
        when(s.getDisplayName()).thenReturn(name);
        when(s.getBlockList()).thenReturn(Collections.singletonList(b));
        when(s.getActualLength()).thenReturn(length);
        sections.put(name, s);
        return s;
    }

    private Section s(String name) {
        Section x = sections.get(name);
        if (x == null) {
            throw new IllegalArgumentException(name);
        }
        return x;
    }

    /*
     * A route from a description such as "A* S1 S2 B1*|B2*"; each word is a step.
     */
    private Route route(String name, boolean looping, long length, String steps) {
        List<List<Option>> list = new ArrayList<>();
        for (String step : steps.trim().split("\\s+")) {
            List<Option> options = new ArrayList<>();
            for (String o : step.split("\\|")) {
                boolean place = o.endsWith("*");
                options.add(new Option(s(place ? o.substring(0, o.length() - 1) : o), Section.FORWARD, place));
            }
            list.add(options);
        }
        return new Route(name, list, looping, length);
    }

    private static Position at(int progress, Section... held) {
        return new Position(progress, Arrays.asList(held));
    }

    private static Result check(Map<Route, Position> state) {
        return new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET).check(state);
    }

    /*
     * Two loops joined by a single line of three Sections. Each train shuttles
     * between the loops and may use either road of each.
     */
    private Route east;
    private Route west;

    private void twoLoops() {
        for (String n : new String[]{"A1", "A2", "S1", "S2", "S3", "B1", "B2"}) {
            section(n, 1000);
        }
        // from loop A to loop B and back
        east = route("East", true, 800, "A1*|A2* S1 S2 S3 B1*|B2* S3 S2 S1");
        // from loop B to loop A and back
        west = route("West", true, 800, "B1*|B2* S3 S2 S1 A1*|A2* S1 S2 S3");
    }

    @Test
    public void testTrainsInTheLoopsCanRun() {
        twoLoops();
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(east, at(0, s("A1")));
        state.put(west, at(0, s("B1")));
        assertEquals(Result.SAFE, check(state));
    }

    @Test
    public void testGrantingTheWholeLineIsSafe() {
        twoLoops();
        Map<Route, Position> state = new LinkedHashMap<>();
        // East has been given the line and the free road in loop B
        state.put(east, at(4, s("B2")));
        state.put(west, at(0, s("B1")));
        assertEquals(Result.SAFE, check(state));
    }

    @Test
    public void testEachHoldingHalfTheLineIsDeadlock() {
        twoLoops();
        Map<Route, Position> state = new LinkedHashMap<>();
        // allocation by a number of Sections: each has the start of the other's way
        state.put(east, at(2, s("S2"), s("S1")));
        state.put(west, at(1, s("S3")));
        assertEquals(Result.UNSAFE, check(state));
    }

    @Test
    public void testNoFreeRoadAtTheFarEndIsRefused() {
        twoLoops();
        Route parked = route("Parked", false, 800, "B2*");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(east, at(0, s("A1")));
        state.put(west, at(0, s("B1")));
        state.put(parked, at(0, s("B2")));
        // with both roads of loop B full, East can't be given the line
        List<Move> moves = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET)
                .moves(east, state.get(east), state);
        assertTrue(moves.isEmpty(), "no move to loop B while both its roads are held");
    }

    @Test
    public void testObstaclesAreBlocks() {
        twoLoops();
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(east, at(0, s("A1")));
        // a Block outside the routes, occupied as by a Section that overlaps them
        // (Signal Mast Logic makes its own Sections over the same Blocks), is in nobody's way
        Block elsewhere = mock(Block.class);
        assertEquals(2, new DeadlockCheck(Collections.singletonList(elsewhere), DeadlockCheck.DEFAULT_BUDGET)
                .moves(east, state.get(east), state).size(), "to B1 or B2 over the free line");
        // one road's Block occupied: only the other road
        List<Move> moves = new DeadlockCheck(Collections.singletonList(s("B2").getBlockList().get(0)),
                DeadlockCheck.DEFAULT_BUDGET).moves(east, state.get(east), state);
        assertEquals(1, moves.size());
        assertEquals(s("B1"), moves.get(0).getPath().get(moves.get(0).getPath().size() - 1));
    }

    /*
     * A circuit with one passing loop: West, Main|Loop, East, then plain line
     * Line1 to Line4 back to West. The turnout Sections are too short to stand
     * in. Clockwise trains take Main or Loop, as do anticlockwise ones.
     */
    private Route clockwise(String name) {
        return route(name, true, 600, "West Main*|Loop* East Line1* Line2* Line3* Line4*");
    }

    private Route anticlockwise(String name) {
        return route(name, true, 600, "East Loop*|Main* West Line4* Line3* Line2* Line1*");
    }

    private void passingLoop() {
        section("West", 250);
        section("East", 250);
        section("Main", 1200);
        section("Loop", 1250);
        section("Line1", 900);
        section("Line2", 700);
        section("Line3", 900);
        section("Line4", 700);
    }

    @Test
    public void testMeetAtThePassingLoop() {
        passingLoop();
        Route a = clockwise("A");
        Route b = anticlockwise("B");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(a, at(3, s("Line1")));    // clockwise, leaving the loop behind it
        state.put(b, at(1, s("Loop")));     // anticlockwise, waiting in the loop
        assertEquals(Result.SAFE, check(state));
        // A going on round towards the loop is safe
        state.put(a, at(4, s("Line2")));
        assertEquals(Result.SAFE, check(state));
    }

    @Test
    public void testLeavingTheLoopTowardsAnOncomingTrainIsRefused() {
        passingLoop();
        Route a = clockwise("A");
        Route b = anticlockwise("B");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(a, at(3, s("Line1")));
        // B leaves the loop for Line4 while A is coming round: they will meet on the plain line
        state.put(b, at(3, s("Line4")));
        assertEquals(Result.UNSAFE, check(state));
        // looking one move ahead would have allowed it: each can still make its next move
        DeadlockCheck one = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
        assertFalse(one.moves(a, state.get(a), state).isEmpty(), "A can move to Line2");
        assertFalse(one.moves(b, state.get(b), state).isEmpty(), "B can move to Line3");
    }

    @Test
    public void testThreeFollowingRoundTheCircuit() {
        passingLoop();
        Route a = clockwise("A");
        Route c = clockwise("C");
        Route d = clockwise("D");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(a, at(3, s("Line1")));
        state.put(c, at(5, s("Line3")));
        state.put(d, at(1, s("Main")));
        assertEquals(Result.SAFE, check(state));
    }

    @Test
    public void testCacheGivesTheSameAnswersAsAFreshSearch() {
        passingLoop();
        Route a = clockwise("A");
        Route c = clockwise("C");
        Route b = anticlockwise("B");
        List<Map<Route, Position>> arrangements = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    Section sa = a.optionsAt(i).get(0).getSection();
                    Section sc = c.optionsAt(j).get(0).getSection();
                    Section sb = b.optionsAt(k).get(0).getSection();
                    if (sa == sc || sa == sb || sb == sc) {
                        continue;
                    }
                    Map<Route, Position> state = new LinkedHashMap<>();
                    state.put(a, at(i, sa));
                    state.put(c, at(j, sc));
                    state.put(b, at(k, sb));
                    arrangements.add(state);
                    if (k == 0) {
                        Map<Route, Position> two = new LinkedHashMap<>();   // following each other
                        two.put(a, at(i, sa));
                        two.put(c, at(j, sc));
                        arrangements.add(two);
                    }
                    if (j == 0) {
                        Map<Route, Position> meeting = new LinkedHashMap<>();   // meeting at the loop
                        meeting.put(a, at(i, sa));
                        meeting.put(b, at(k, sb));
                        arrangements.add(meeting);
                    }
                }
            }
        }
        // in both orders, so answers found early are built on by later checks, and the other way round
        for (boolean reversed : new boolean[]{false, true}) {
            List<Map<Route, Position>> order = new ArrayList<>(arrangements);
            if (reversed) {
                Collections.reverse(order);
            }
            DeadlockCheck.ArrangementCache cache = new DeadlockCheck.ArrangementCache();
            DeadlockCheck check = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
            for (Map<Route, Position> state : order) {
                assertEquals(check(state), check.check(state, cache), state.toString());
            }
            int known = cache.size();
            for (Map<Route, Position> state : order) {
                assertEquals(check(state), check.check(state, cache), state.toString());
            }
            assertEquals(known, cache.size(), "asked again, every answer is already known");
        }
        // the arrangements include both answers: two clockwise and one anticlockwise can never all
        // run round one loop, two can, unless they are already stuck facing each other
        int safe = 0;
        int unsafe = 0;
        for (Map<Route, Position> state : arrangements) {
            Result r = check(state);
            if (r == Result.SAFE) {
                safe++;
            } else if (r == Result.UNSAFE) {
                unsafe++;
            }
            if (state.size() == 3) {
                assertEquals(Result.UNSAFE, r, state.toString());
            }
        }
        assertTrue(safe > 10, "some arrangements of two can run: " + safe);
        assertTrue(unsafe > 10, "some can't: " + unsafe);
    }

    @Test
    public void testCacheBuildsOnEarlierChecks() {
        passingLoop();
        Route a = clockwise("A");
        Route b = anticlockwise("B");
        DeadlockCheck.ArrangementCache cache = new DeadlockCheck.ArrangementCache();
        DeadlockCheck check = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(a, at(3, s("Line1")));
        state.put(b, at(1, s("Loop")));
        assertEquals(Result.SAFE, check.check(state, cache));
        int known = cache.size();
        // every arrangement they can go on to is already known
        state.put(a, at(4, s("Line2")));
        assertEquals(Result.SAFE, check.check(state, cache));
        state.put(b, at(3, s("Line4")));
        assertEquals(Result.UNSAFE, check.check(state, cache));
        assertTrue(cache.size() >= known);
    }

    @Test
    public void testTrainsOnSeparateTrackAreCheckedApart() {
        passingLoop();
        section("Q1", 1000);
        section("Q2", 1000);
        section("Q3", 1000);
        section("Q4", 1000);
        String circuit = "Q1* Q2* Q3* Q4*";
        Map<Route, Position> state = new LinkedHashMap<>();
        for (int i = 0; i < 3; i++) {
            state.put(route("T" + i, true, 800, circuit), at(i, s("Q" + (i + 1))));
        }
        Route a = clockwise("A");
        Route b = anticlockwise("B");
        state.put(a, at(3, s("Line1")));
        state.put(b, at(1, s("Loop")));
        DeadlockCheck.ArrangementCache cache = new DeadlockCheck.ArrangementCache();
        DeadlockCheck check = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
        assertEquals(Result.SAFE, check.check(state, cache), "both parts can run");
        state.put(b, at(3, s("Line4")));
        assertEquals(Result.UNSAFE, check.check(state, cache), "A and B will meet on the plain line");
        state.put(b, at(1, s("Loop")));
        state.put(route("T3", true, 800, circuit), at(3, s("Q4")));
        assertEquals(Result.UNSAFE, check.check(state, cache), "the circuit is full");
    }

    @Test
    public void testTwoEachWayRoundOneLoopCanNeverRun() {
        passingLoop();
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(clockwise("A"), at(3, s("Line1")));
        state.put(clockwise("C"), at(5, s("Line3")));
        state.put(anticlockwise("B"), at(1, s("Loop")));
        assertEquals(Result.UNSAFE, check(state));
    }

    @Test
    public void testFullCircuitOfOneTrainPerSection() {
        section("Q1", 1000);
        section("Q2", 1000);
        section("Q3", 1000);
        section("Q4", 1000);
        String circuit = "Q1* Q2* Q3* Q4*";
        Map<Route, Position> four = new LinkedHashMap<>();
        Map<Route, Position> three = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            Route r = route("T" + i, true, 800, circuit);
            four.put(r, at(i, s("Q" + (i + 1))));
            if (i < 3) {
                three.put(r, at(i, s("Q" + (i + 1))));
            }
        }
        assertEquals(Result.UNSAFE, check(four), "no Section is free, so none can start");
        assertEquals(Result.SAFE, check(three), "the free Section goes round");
    }

    @Test
    public void testTrainStartingBetweenPlacesCanRun() {
        // a circuit of three Sections; the 1200 mm train can stand only in the two longer ones
        section("P", 820);
        section("Q", 1660);
        section("R", 1250);
        Route r = route("Long", true, 1200, "P Q* R*");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(r, at(0, s("P")));    // it starts in P, which isn't a place
        assertEquals(Result.SAFE, check(state), "counted from the first place it reaches");
        state.put(r, at(3, s("P"), s("R")));     // its head given P again, its tail in R
        assertEquals(Result.SAFE, check(state));
    }

    /*
     * A terminus: Approach, Throat, then Platform1 or Platform2. Two shuttles,
     * one to each platform.
     */
    @Test
    public void testTerminusShuttles() {
        section("Approach", 1500);
        section("Throat", 250);
        section("Platform1", 1200);
        section("Platform2", 1100);
        Route p1 = route("P1", true, 900, "Approach* Throat Platform1* Throat");
        Route p2 = route("P2", true, 900, "Platform2* Throat Approach* Throat");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(p1, at(0, s("Approach")));
        state.put(p2, at(0, s("Platform2")));
        assertEquals(Result.SAFE, check(state));
        // P2 out on the approach while P1 is on it would be a collision, so the move isn't offered
        DeadlockCheck dc = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
        assertTrue(dc.moves(p2, state.get(p2), state).isEmpty());
    }

    @Test
    public void testTerminatingTrainsHeadOnOverASingleLine() {
        for (int i = 1; i <= 5; i++) {
            section("L" + i, 600);
        }
        Route up = route("Up", false, 200, "L1* L2* L3* L4* L5*");
        Route down = route("Down", false, 200, "L5* L4* L3* L2* L1*");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(up, at(0, s("L1")));
        state.put(down, at(0, s("L5")));
        assertEquals(Result.UNSAFE, check(state), "neither can reach its end");
        Map<Route, Position> one = new LinkedHashMap<>();
        one.put(up, at(0, s("L1")));
        assertEquals(Result.SAFE, check(one));
    }

    @Test
    public void testLongTrainHoldsTheSectionBehind() {
        section("Long", 1000);
        section("Short", 300);
        section("Next", 1000);
        Route r = route("R", false, 800, "Long* Short* Next*");
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(r, at(0, s("Long")));
        List<Move> moves = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET)
                .moves(r, state.get(r), state);
        assertEquals(1, moves.size());
        assertEquals(Arrays.asList(s("Short"), s("Long")), moves.get(0).getTo().getHeld(),
                "an 800 mm train stopped in a 300 mm Section still covers the one behind");
    }

    @Test
    public void testSectionsSharingABlockConflict() {
        Section a = section("A", 500);
        Section b = section("B", 500);
        Block shared = mock(Block.class);
        when(a.getBlockList()).thenReturn(Arrays.asList(shared));
        when(b.getBlockList()).thenReturn(Arrays.asList(mock(Block.class), shared));
        DeadlockCheck dc = new DeadlockCheck(Collections.emptyList(), DeadlockCheck.DEFAULT_BUDGET);
        assertTrue(dc.conflicts(a, b));
        assertFalse(dc.conflicts(a, section("C", 500)));
    }

    @Test
    public void testObstacleBlocksTheWay() {
        twoLoops();
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(east, at(0, s("A1")));
        DeadlockCheck dc = new DeadlockCheck(s("S2").getBlockList(), DeadlockCheck.DEFAULT_BUDGET);
        assertTrue(dc.moves(east, state.get(east), state).isEmpty(), "S2's Block is held by something with no route");
    }

    @Test
    public void testGivesUpWhenTheBudgetRunsOut() {
        passingLoop();
        Map<Route, Position> state = new LinkedHashMap<>();
        state.put(clockwise("A"), at(3, s("Line1")));
        state.put(anticlockwise("B"), at(1, s("Loop")));
        assertEquals(Result.GAVE_UP, new DeadlockCheck(Collections.emptyList(), 2).check(state));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
