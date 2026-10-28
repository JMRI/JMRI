package jmri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the late value of a Block entered from two occupied neighbours
 * holding different trains.
 * <p>
 * Blocks X, Y and Z are in a line, X - Y - Z, with no directions set.
 */
public class BlockEntryCandidatesTest {

    private Block x;
    private Block y;
    private Block z;

    private static void link(Block from, Block to) {
        Path p = new Path();
        p.setBlock(to);
        from.addPath(p);
    }

    private static void occupy(Block b, Object value) {
        b.goingActive();
        if (value != null) {
            b.setValue(value);
        }
    }

    /**
     * A train enters Y between the trains in X and Z: Y can't tell which one
     * it is.
     */
    private void enterBetween() {
        y.goingActive();
        JUnitAppender.assertWarnMessageStartsWith("count of 2 ACTIVE neighbors");
        assertNull(y.getValue(), "Y can't decide between TrainA and TrainB");
    }

    @Test
    public void testEmptiedBlockDoesNotTakeLateValue() {
        occupy(x, "TrainA");
        occupy(z, "TrainB");
        enterBetween();

        // that train leaves Y again
        y.goingInactive();
        // later TrainA leaves X
        x.goingInactive();

        assertEquals(Block.UNOCCUPIED, y.getState(), "Y is empty");
        assertNull(y.getValue(), "an empty Block is not given TrainA");
    }

    @Test
    public void testOccupiedBlockStillTakesLateValue() {
        occupy(x, "TrainA");
        occupy(z, "TrainB");
        enterBetween();

        // TrainA leaves X, so the train in Y is TrainA
        x.goingInactive();

        assertEquals(Block.OCCUPIED, y.getState(), "Y is occupied");
        assertEquals("TrainA", y.getValue(), "Y is given TrainA late");
    }

    @Test
    public void testNextTrainDoesNotInheritStaleValue() {
        occupy(x, "TrainA");
        occupy(z, "TrainB");
        enterBetween();
        y.goingInactive();
        x.goingInactive();
        z.goingInactive();

        // an unidentified train enters Y from nowhere a while later
        y.goingActive();
        assertNull(y.getValue(), "the next train in Y is not labelled TrainA");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        x = new Block("IB_X");
        y = new Block("IB_Y");
        z = new Block("IB_Z");
        link(x, y);
        link(y, x);
        link(y, z);
        link(z, y);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
