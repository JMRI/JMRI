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
 * Block Y sits between X and Z, with no directions set on the Blocks.
 */
public class BlockNeighbourValuesTest {

    private Block x;
    private Block y;
    private Block z;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        x = new Block("IB_X");
        y = new Block("IB_Y");
        z = new Block("IB_Z");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
