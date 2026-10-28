package jmri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the value a Block takes when it becomes occupied next to more
 * than one occupied neighbour. The result must not depend on the order in
 * which the paths of the Block were defined.
 * <p>
 * Block Y sits between X and Z, with no directions set on the Blocks, unless
 * a test says otherwise.
 */
public class BlockNeighbourValuesTest {

    private Block w;
    private Block x;
    private Block y;
    private Block z;
    private Block v;

    private static Path link(Block from, Block to, int fromDirection) {
        Path p = new Path();
        p.setBlock(to);
        p.setFromBlockDirection(fromDirection);
        from.addPath(p);
        return p;
    }

    /**
     * Link Y to its neighbours, in the order given.
     *
     * @param xFirst true to define the path to X first
     */
    private void linkY(boolean xFirst) {
        if (xFirst) {
            link(y, x, Path.EAST);
            link(y, z, Path.WEST);
        } else {
            link(y, z, Path.WEST);
            link(y, x, Path.EAST);
        }
    }

    private static void occupy(Block b, Object value) {
        b.goingActive();
        b.setValue(value);
    }

    private void unidentifiedNextToIdentified(boolean xFirst) {
        linkY(xFirst);
        occupy(x, null);        // a train that has not been identified
        occupy(z, "TrainB");
        y.goingActive();
        assertEquals("TrainB", y.getValue(), "Y takes the only value next to it, x first = " + xFirst);
        assertEquals(Path.WEST, y.getDirection(), "direction of the path to the identified train");
    }

    @Test
    public void testUnidentifiedNeighbourPathDefinedFirst() {
        unidentifiedNextToIdentified(true);
    }

    @Test
    public void testIdentifiedNeighbourPathDefinedFirst() {
        unidentifiedNextToIdentified(false);
    }

    private void twoDifferentTrains(boolean xFirst) {
        linkY(xFirst);
        occupy(x, "TrainA");
        occupy(z, "TrainB");
        y.goingActive();
        JUnitAppender.assertWarnMessageStartsWith("count of 2 ACTIVE neighbors");
        assertNull(y.getValue(), "Y can't decide between two trains, x first = " + xFirst);
    }

    @Test
    public void testTwoDifferentTrainsPathToXFirst() {
        twoDifferentTrains(true);
    }

    @Test
    public void testTwoDifferentTrainsPathToZFirst() {
        twoDifferentTrains(false);
    }

    /**
     * Y takes Train B's value while it can't tell which neighbour entered, and
     * keeps both as possible entrants until one of them empties.
     */
    private void unidentifiedAndTrainBThenOneLeaves(Block leaves) {
        linkY(false);
        link(x, y, Path.WEST);
        link(z, y, Path.EAST);
        occupy(x, null);
        occupy(z, "TrainB");
        y.goingActive();
        assertEquals("TrainB", y.getValue(), "Y takes the only value next to it");
        leaves.goingInactive();
    }

    @Test
    public void testTrainBLeavingItsBlockConfirmsTheValue() {
        unidentifiedAndTrainBThenOneLeaves(z);
        assertEquals("TrainB", y.getValue(), "Train B left Z, so Train B entered Y");
        assertEquals(Path.WEST, y.getDirection(), "direction of the path to Z");
    }

    @Test
    public void testUnidentifiedTrainLeavingItsBlockCorrectsTheValue() {
        unidentifiedAndTrainBThenOneLeaves(x);
        assertNull(y.getValue(), "the unidentified train left X, so it entered Y, not Train B");
        assertEquals(Path.EAST, y.getDirection(), "direction of the path to X");
    }

    @Test
    public void testNoNeighbourIdentified() {
        linkY(false);
        occupy(x, null);
        occupy(z, null);
        y.goingActive();
        assertNull(y.getValue(), "no value to take");
        assertEquals(Block.OCCUPIED, y.getState(), "Y is occupied");
    }

    /*
     * A neighbour becoming unoccupied may be its train leaving the other way
     * rather than entering Y. These use a line W - X - Y - Z - V, with
     * Block directions: travelling east goes from W towards V.
     */

    /**
     * Link the line W - X - Y - Z - V.
     *
     * @param yFromZ the direction of a train entering Y from Z: WEST on
     *               straight track; at a bend, a direction that may overlap
     *               that of a train leaving Z
     */
    private void linkLine(int yFromZ) {
        link(w, x, Path.WEST);
        link(x, w, Path.EAST);
        link(x, y, Path.WEST);
        link(y, x, Path.EAST);
        link(y, z, yFromZ);
        link(z, y, Path.EAST);
        link(z, v, Path.WEST);
        link(v, z, Path.EAST);
    }

    private static void occupy(Block b, Object value, int direction) {
        occupy(b, value);
        b.setDirection(direction);
    }

    /**
     * Two trains meet on single track, so Y can't tell which entered it.
     */
    private void trainsMeetAtY() {
        linkLine(Path.WEST);
        occupy(x, "TrainA", Path.EAST);
        occupy(z, "TrainB", Path.WEST);
        y.goingActive();
        JUnitAppender.assertWarnMessageStartsWith("count of 2 ACTIVE neighbors");
        assertNull(y.getValue(), "Y can't tell which train entered");
    }

    @Test
    public void testTrainBackingAwayDidNotEnter() {
        trainsMeetAtY();
        w.goingActive();    // Train A backs up into W
        x.goingInactive();  // and clears X
        assertEquals("TrainB", y.getValue(), "Train A left the other way, so Train B entered Y");
        assertEquals(Path.WEST, y.getDirection(), "direction of the path to Z");
    }

    @Test
    public void testLongTrainBackingAwayDidNotEnter() {
        linkLine(Path.WEST);
        occupy(w, "TrainA", Path.EAST);
        occupy(x, "TrainA", Path.EAST);
        occupy(z, "TrainB", Path.WEST);
        y.goingActive();
        JUnitAppender.assertWarnMessageStartsWith("count of 2 ACTIVE neighbors");
        x.goingInactive();  // Train A, also in W, backs off X
        assertEquals("TrainB", y.getValue(), "Train A is still in W, so Train B entered Y");
    }

    @Test
    public void testTrainEnteringFromAMeetIsTaken() {
        trainsMeetAtY();
        x.goingInactive();  // Train A's tail leaves X, and W is empty: it is in Y
        assertEquals("TrainA", y.getValue(), "Train A left X and went nowhere else, so it entered Y");
        assertEquals(Path.EAST, y.getDirection(), "direction of the path to X");
    }

    @Test
    public void testTrainAheadMovingOnAtABendDidNotEnter() {
        // at the bend, the direction of a train entering Y from Z overlaps that
        // of a train leaving Z, so the direction check can't rule Train B out
        linkLine(Path.WEST | Path.NORTH);
        occupy(x, "TrainA", Path.EAST);
        occupy(z, "TrainB", Path.NORTH);
        y.goingActive();    // Train A, following Train B, enters Y
        JUnitAppender.assertWarnMessageStartsWith("count of 2 ACTIVE neighbors");
        v.goingActive();    // Train B moves on into V
        z.goingInactive();  // and clears Z
        assertEquals("TrainA", y.getValue(), "Train B moved on, so Train A entered Y");
        assertEquals(Path.EAST, y.getDirection(), "direction of the path to X");
    }

    @Test
    public void testUnidentifiedStockMovingAwayKeepsTheValue() {
        // without Block directions, any occupied neighbour could have entered
        linkLine(Path.WEST);
        occupy(x, null);
        occupy(z, "TrainB");
        y.goingActive();
        assertEquals("TrainB", y.getValue(), "Y takes the only value next to it");
        w.goingActive();    // the unidentified stock is moved away into W
        x.goingInactive();
        assertEquals("TrainB", y.getValue(), "the stock in X left the other way, so Train B entered Y");
    }

    @Test
    public void testRulingOneOutOfThreeKeepsTheOtherTwo() {
        Block j = new Block("IB_J");
        linkLine(Path.WEST);
        link(y, j, Path.NORTH);
        link(j, y, Path.SOUTH);
        occupy(x, "TrainA", Path.EAST);
        occupy(z, "TrainB", Path.WEST);
        occupy(j, "TrainC", Path.NORTH);
        y.goingActive();
        JUnitAppender.assertWarnMessageStartsWith("count of 3 ACTIVE neighbors");
        w.goingActive();    // Train A backs away
        x.goingInactive();
        assertNull(y.getValue(), "Train B or Train C entered Y");
        z.goingInactive();  // Train B's tail leaves Z, and V is empty
        assertEquals("TrainB", y.getValue(), "Train B left Z and went nowhere else");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        w = new Block("IB_W");
        x = new Block("IB_X");
        y = new Block("IB_Y");
        z = new Block("IB_Z");
        v = new Block("IB_V");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
