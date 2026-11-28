package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Trains train tracking knows, drawn on a Layout Editor panel.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class TrainFootprintsTest {

    private LayoutEditor le;
    private Block b1;
    private Block b2;

    // A straight line of three Blocks, B1 from x 100 to 200, B2 to 300 and B3 to 400, at y 100; the trains use B1 and B2.
    private void threeBlocksInALine() {
        PositionablePoint[] p = new PositionablePoint[4];
        for (int i = 0; i < 4; i++) {
            p[i] = new PositionablePoint("A" + i, PositionablePoint.PointType.ANCHOR, le);
            le.addLayoutTrack(p[i], new PositionablePointView(p[i], new Point2D.Double(100.0 + 100 * i, 100.0), le));
        }
        Block[] blocks = new Block[3];
        for (int i = 0; i < 3; i++) {
            TrackSegment ts = new TrackSegment("T" + i, p[i], HitPointType.POS_POINT, p[i + 1],
                    HitPointType.POS_POINT, true, le);
            le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
            LayoutBlock lb = java.util.Objects.requireNonNull(InstanceManager.getDefault(LayoutBlockManager.class)
                    .createNewLayoutBlock("ILB" + (i + 1), "B" + (i + 1)));
            lb.initializeLayoutBlock();
            ts.setLayoutBlock(lb);
            blocks[i] = lb.getBlock();
        }
        b1 = blocks[0];
        b2 = blocks[1];
    }

    private TrainRecord train(String name, List<Block> blocks) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord(name);
            r.place(blocks, true);
            return r;
        });
    }

    private BufferedImage draw() {
        BufferedImage image = new BufferedImage(500, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 500, 200);
        ThreadingUtil.runOnGUI(() -> le.getTrainFootprints().paint(g2));
        g2.dispose();
        return image;
    }

    private static boolean drawnAt(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) != Color.WHITE.getRGB();
    }

    @Test
    public void testDrawnOnlyWhenChosen() {
        threeBlocksInALine();
        train("Mail", List.of(b2, b1));

        assertFalse(le.getTrainFootprints().isShown(), "off unless chosen");
        BufferedImage image = draw();
        assertFalse(drawnAt(image, 150, 100), "not drawn");

        le.getTrainFootprints().setShown(true);
        assertTrue(le.getTrainFootprints().isShown(), "the choice is kept");
        image = draw();
        assertTrue(drawnAt(image, 150, 100), "drawn in B1");
        assertTrue(drawnAt(image, 250, 100), "drawn in B2");
        assertFalse(drawnAt(image, 350, 100), "not in B3");
    }

    @Test
    public void testFrontAndTheWayItFaces() {
        threeBlocksInALine();
        TrainFootprints.Track track = ThreadingUtil.runOnGUIwithReturn(() -> new TrainFootprints.Track(le));

        // front in B2, B1 behind: the front is where B2 meets B3, facing towards B3
        TrainFootprints.Front front = track.front(List.of(b2, b1), b1);
        assertNotNull(front);
        assertEquals(300.0, front.at.getX(), 0.01);
        assertEquals(100.0, front.at.getY(), 0.01);
        assertEquals(1.0, front.dx, 0.001);
        assertEquals(0.0, front.dy, 0.001);

        // front in B1, B2 behind: B1 meets no other Block, so the front is the end of the track
        front = track.front(List.of(b1, b2), b2);
        assertNotNull(front);
        assertEquals(100.0, front.at.getX(), 0.01);
        assertEquals(-1.0, front.dx, 0.001);

        // nothing known behind: the way it faces isn't known
        assertNull(track.front(List.of(b2), null));
    }

    @Test
    public void testATrainPlacedInOneBlockShowsItsWayIsNotKnown() {
        threeBlocksInALine();
        le.getTrainFootprints().setShown(true);
        train("Mail", List.of(b2));

        BufferedImage image = draw();
        assertTrue(drawnAt(image, 250, 100), "drawn in B2");
        // placed with nothing behind it: no arrowhead at either end, "?" by its name instead
        assertFalse(drawnAt(image, 312, 100), "no arrowhead beyond B3's end");
        assertFalse(drawnAt(image, 188, 100), "nor beyond B1's");
    }

    @Test
    public void testCrossoverLegsAreInTheirOwnBlocks() {
        LayoutTurnout t = new LayoutRHXOver("TO1", le);
        LayoutTurnoutView v = new LayoutRHXOverView((LayoutRHXOver) t, new Point2D.Double(200.0, 100.0), 0.0, 1.0, 1.0, le);
        le.addLayoutTrack(t, v);
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        LayoutBlock a = java.util.Objects.requireNonNull(lbm.createNewLayoutBlock("ILB11", "TA"));
        a.initializeLayoutBlock();
        LayoutBlock c = java.util.Objects.requireNonNull(lbm.createNewLayoutBlock("ILB12", "TC"));
        c.initializeLayoutBlock();
        t.setLayoutBlockByName("TA");
        t.setLayoutBlockCByName("TC");

        TrainFootprints.Track track = ThreadingUtil.runOnGUIwithReturn(() -> new TrainFootprints.Track(le));
        assertEquals(4, track.legs.size(), "a leg to each of A, B, C and D");
        assertEquals(1, track.legs.stream().filter(l -> l.block == c.getBlock()).count(), "C's leg in its own Block");
        assertEquals(v.getCoordsC(), track.legs.stream().filter(l -> l.block == c.getBlock()).findFirst()
                .orElseThrow().end2);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Train Footprints Test"));
    }

    @AfterEach
    public void tearDown() {
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
