package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Timebase;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that redrawing only the part of a Layout Editor panel drawn in a
 * layout block's colours, after that block changes, gives exactly the picture
 * that redrawing the whole panel gives.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutEditorBlockRepaintTest {

    private ConfigXmlManager cm = null;

    /**
     * For every layout block on every panel of a real layout file, in
     * operating mode and at two zooms: draw the panel, change the block's
     * colours, redraw only the area for that block over the old picture, and
     * compare with the whole panel redrawn. The file has turnouts, slips,
     * crossovers, crossings and a turntable whose rays join tracks in other
     * blocks; first, the legs of its slips and crossovers, and the second
     * track of each crossing, are given blocks of their own, and the
     * turntable is given a block and turnouts and set to a ray, so that its
     * bridge is drawn, its short end pointing away from all the rays. Done
     * twice: as that, and with some of the straight track made into circular
     * and elliptical arcs. Outside the area the pictures must be identical;
     * inside it they may differ a little where curves cross its edge (see
     * {@code CURVE_LEVELS}).
     *
     * @throws JmriException if the layout file can't be loaded
     */
    @Test
    @DisabledIfHeadless
    public void testRedrawingABlocksAreaMatchesRedrawingTheWholePanel() throws JmriException {
        assertTrue(cm.load(new File("java/test/jmri/jmrit/display/layoutEditor/load/GeoPanelsAnon.xml")));
        stopWhatChangesByItself();
        Set<LayoutEditor> panels = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class);
        assertEquals(4, panels.size(), "panels in the file");

        for (LayoutEditor le : panels) {
            ThreadingUtil.runOnGUI(() -> giveLegsAndCrossingsBlocksOfTheirOwn(le));
            setTurntableBridges(le);
        }
        int checks = 0;
        int changedPicture = 0;
        for (boolean arcs : new boolean[]{false, true}) {
            for (LayoutEditor le : panels) {
                ThreadingUtil.runOnGUI(() -> {
                    le.setAllEditable(false);
                    if (arcs) {
                        makeSomeArcs(le);
                    }
                });
                for (LayoutBlock lb : blocksOn(le)) {
                    for (double zoom : new double[]{0.6, 1.3}) {
                        if (checkBlock(le, lb, zoom)) {
                            changedPicture++;
                        }
                        checks++;
                    }
                }
            }
        }
        // a block whose track is outside the visible part of the panel at a
        // zoom isn't drawn, so its change can't show; most must show
        assertTrue(checks > 160, "checks made: " + checks);
        assertTrue(changedPicture > checks / 2, "checks where the block's change showed: " + changedPicture + " of " + checks);

        for (LayoutEditor le : panels) {
            new EditorFrameOperator(le.getName()).closeFrameWithConfirmations();
        }
    }

    /**
     * In operating mode a block change asks Swing to redraw only that block's
     * area of the panel; in edit mode, the whole panel.
     *
     * @throws JmriException if the layout file can't be loaded
     */
    @Test
    @DisabledIfHeadless
    public void testABlockChangeRedrawsOnlyItsArea() throws JmriException {
        assertTrue(cm.load(new File("java/test/jmri/jmrit/display/layoutEditor/load/GeoPanelsAnon.xml")));
        stopWhatChangesByItself();
        LayoutEditor le = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class).first();
        LayoutBlock lb = blocksOn(le).get(0);
        JComponent targetPanel = le.getTargetPanel();
        assertNotNull(targetPanel);

        RecordingRepaintManager recorder = new RecordingRepaintManager(targetPanel);
        RepaintManager original = RepaintManager.currentManager(targetPanel);
        try {
            ThreadingUtil.runOnGUI(() -> {
                le.setAllEditable(false);
                RepaintManager.setCurrentManager(recorder);
            });
            Rectangle expected = ThreadingUtil.runOnGUIwithReturn(() -> le.getLayoutBlockRepaintArea(lb));
            assertNotNull(expected);

            ThreadingUtil.runOnGUI(() -> recorder.record(lb::redrawLayoutBlockPanels));
            assertEquals(List.of(expected), recorder.regions(), "operating mode: only the block's area");
            assertTrue(expected.width < targetPanel.getWidth() || expected.height < targetPanel.getHeight(),
                    "the block's area is less than the whole panel");

            ThreadingUtil.runOnGUI(() -> le.setAllEditable(true));
            ThreadingUtil.runOnGUI(() -> recorder.record(lb::redrawLayoutBlockPanels));
            assertEquals(List.of(new Rectangle(0, 0, targetPanel.getWidth(), targetPanel.getHeight())),
                    recorder.regions(), "edit mode: the whole panel");
        } finally {
            ThreadingUtil.runOnGUI(() -> RepaintManager.setCurrentManager(original));
        }
        new EditorFrameOperator(le.getName()).closeFrameWithConfirmations();
    }

    /**
     * Check one block at one zoom.
     *
     * @return true if changing the block's colours changed the picture
     */
    private boolean checkBlock(LayoutEditor le, LayoutBlock lb, double zoom) {
        ThreadingUtil.runOnGUI(() -> le.setZoom(zoom));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> isZoomedTo(le, zoom)), "zoomed to " + zoom);

        BufferedImage before = draw(le, null, null);

        Color[] old = {lb.getBlockTrackColor(), lb.getBlockOccupiedColor(), lb.getBlockExtraColor()};
        Color marked = new Color(17, 201, 93);
        ThreadingUtil.runOnGUI(() -> {
            lb.setBlockTrackColor(marked);
            lb.setBlockOccupiedColor(marked);
            lb.setBlockExtraColor(marked);
        });
        Rectangle area = ThreadingUtil.runOnGUIwithReturn(() -> le.getLayoutBlockRepaintArea(lb));
        BufferedImage whole = draw(le, null, null);
        BufferedImage part = (area == null) ? before : draw(le, area, before);

        String what = "panel '" + le.getLayoutName() + "' block '" + lb.getDisplayName() + "' zoom " + zoom;
        assertNothingChangedOutside(area, before, whole, what + ": the change");
        assertNothingChangedOutside(area, before, part, what + ": redrawing the area");
        assertSameInside(area, whole, part, what);
        boolean changed = !samePicture(before, whole);

        ThreadingUtil.runOnGUI(() -> {
            lb.setBlockTrackColor(old[0]);
            lb.setBlockOccupiedColor(old[1]);
            lb.setBlockExtraColor(old[2]);
        });
        return changed;
    }

    /**
     * Nothing on the panels may change by itself between two pictures, or
     * while repaints are recorded: the file starts its fast clock, whose
     * hands move each minute, so stop it; and no signal head may flash.
     */
    private static void stopWhatChangesByItself() {
        ThreadingUtil.runOnGUI(() -> InstanceManager.getDefault(Timebase.class).setRun(false));
        int flashing = SignalHead.FLASHRED | SignalHead.FLASHYELLOW | SignalHead.FLASHGREEN | SignalHead.FLASHLUNAR;
        for (SignalHead head : InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet()) {
            assertEquals(0, head.getAppearance() & flashing, "signal head " + head.getDisplayName() + " flashes");
        }
    }

    private static boolean isZoomedTo(LayoutEditor le, double zoom) {
        return Math.abs(le.getZoom() - zoom) < 1e-9;
    }

    /**
     * Draw the panel as Swing would, into a new picture, or, with an area,
     * only that area over a copy of an earlier picture. As Swing does, the
     * area is first filled with the background of the panel, or, where the
     * panel has none, of the first opaque component behind it.
     */
    private static BufferedImage draw(LayoutEditor le, Rectangle area, BufferedImage over) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            JComponent targetPanel = le.getTargetPanel();
            int w = targetPanel.getWidth();
            int h = targetPanel.getHeight();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            if (over != null) {
                g.drawImage(over, 0, 0, null);
            }
            Rectangle clip = (area != null) ? area : new Rectangle(0, 0, w, h);
            g.setClip(clip);
            g.setColor(backgroundBehind(targetPanel));
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
            targetPanel.paint(g);
            g.dispose();
            return image;
        });
    }

    private static Color backgroundBehind(Component c) {
        for (Component p = c; p != null; p = p.getParent()) {
            if (p.isOpaque()) {
                return p.getBackground();
            }
        }
        return Color.white;
    }

    private static boolean samePicture(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Outside the area, a picture is the same as before: used to check that
     * every pixel the block change alters is inside the area, and that
     * redrawing the area draws nothing outside it.
     */
    private static void assertNothingChangedOutside(Rectangle area, BufferedImage before, BufferedImage after, String what) {
        int changed = 0;
        String first = null;
        for (int y = 0; y < after.getHeight(); y++) {
            for (int x = 0; x < after.getWidth(); x++) {
                if (((area == null) || !area.contains(x, y)) && (before.getRGB(x, y) != after.getRGB(x, y))) {
                    if (first == null) {
                        first = "(" + x + ", " + y + ")";
                    }
                    changed++;
                }
            }
        }
        assertEquals(0, changed, what + ": pixels changed outside the area, the first at " + first);
    }

    /**
     * Inside the area, redrawing only the area gives the same picture as
     * redrawing the whole panel, to within {@link #CURVE_LEVELS} in each
     * colour.
     */
    private static void assertSameInside(Rectangle area, BufferedImage whole, BufferedImage part, String what) {
        if (area == null) {
            return;
        }
        int differing = 0;
        String first = null;
        for (int y = Math.max(0, area.y); y < Math.min(whole.getHeight(), area.y + area.height); y++) {
            for (int x = Math.max(0, area.x); x < Math.min(whole.getWidth(), area.x + area.width); x++) {
                int a = whole.getRGB(x, y);
                int b = part.getRGB(x, y);
                if (channelDifference(a, b) > CURVE_LEVELS) {
                    if (first == null) {
                        first = "(" + x + ", " + y + ") " + Integer.toHexString(a & 0xffffff) + " not " + Integer.toHexString(b & 0xffffff);
                    }
                    differing++;
                }
            }
        }
        assertEquals(0, differing, what + ": pixels in the area that differ from a whole redraw, the first at " + first);
    }

    /**
     * How many levels, in any one colour, a pixel inside the area may differ
     * by. Where a curve (an arc, a turntable's pit) crosses the edge of the
     * area being drawn, Java2D cuts the curve there, and so draws its
     * antialiased edges slightly differently along it, as it does for any
     * partial repaint Swing makes; with straight track only the pictures are
     * identical. On this file the most seen is 24 levels, on one pixel (most
     * differ by 1 or 2); this allows twice that. A track left in its old
     * colour, or not drawn, differs from the colour this test sets by over 100
     * levels.
     */
    private static final int CURVE_LEVELS = 48;

    private static int channelDifference(int a, int b) {
        int d = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            d = Math.max(d, Math.abs(((a >> shift) & 0xff) - ((b >> shift) & 0xff)));
        }
        return d;
    }

    /**
     * The layout blocks on a panel, in a fixed order, as the panel records
     * them (not as the code under test finds them).
     */
    private static List<LayoutBlock> blocksOn(LayoutEditor le) {
        List<LayoutBlock> blocks = new ArrayList<>();
        for (LayoutBlock lb : InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet()) {
            if (lb.isOnPanel(le)) {
                blocks.add(lb);
            }
        }
        return blocks;
    }

    /**
     * Give the B, C and D legs of every slip and crossover, and the BD track of
     * every crossing, blocks of their own, so that each is drawn in more than
     * one block's colours.
     */
    private static void giveLegsAndCrossingsBlocksOfTheirOwn(LayoutEditor le) {
        for (LayoutTurnoutView view : le.getLayoutTurnoutAndSlipViews()) {
            LayoutTurnout lt = view.getLayoutTurnout();
            if (lt.isTurnoutTypeXover() || lt.isTurnoutTypeSlip()) {
                String name = le.getLayoutName() + " " + lt.getName();
                lt.setLayoutBlockBByName(newLayoutBlock(name + " B"));
                lt.setLayoutBlockCByName(newLayoutBlock(name + " C"));
                lt.setLayoutBlockDByName(newLayoutBlock(name + " D"));
            }
        }
        for (LevelXingView view : le.getLevelXingViews()) {
            LevelXing lx = view.getLevelXing();
            lx.setLayoutBlockBD(le.provideLayoutBlock(newLayoutBlock(le.getLayoutName() + " " + lx.getName() + " BD")));
        }
    }

    private static int turnoutNumber = 0;

    /**
     * Give each turntable on a panel a block of its own, a radius of 120 and a
     * turnout for each ray, and turn its bridge to its first ray.
     */
    private static void setTurntableBridges(LayoutEditor le) {
        for (LayoutTurntableView view : le.getLayoutTurntableViews()) {
            LayoutTurntable tt = view.getTurntable();
            int first = tt.getRayTrackList().get(0).getConnectionIndex();
            ThreadingUtil.runOnGUI(() -> {
                tt.setLayoutBlock(le.provideLayoutBlock(newLayoutBlock(le.getLayoutName() + " " + tt.getName())));
                // large enough that the bridge's short end reaches well past
                // the box round the centre and the rays' ends
                tt.setRadius(120.0);
                tt.setTurnoutControlled(true);
                for (LayoutTurntable.RayTrack ray : tt.getRayTrackList()) {
                    String name = "ITTT" + (++turnoutNumber);
                    InstanceManager.getDefault(TurnoutManager.class).provideTurnout(name);
                    tt.setRayTurnout(ray.getConnectionIndex(), name, Turnout.THROWN);
                }
                tt.setPosition(first);
            });
            JUnitUtil.waitFor(() -> tt.getPosition() == first, "turntable " + tt.getName() + " at ray " + first);
        }
    }

    /** Make a layout block, with its block, and return its name. */
    private static String newLayoutBlock(String name) {
        assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, name), name);
        return name;
    }

    /** Make every fifth plain track segment with a block an arc: alternately circular (180 degrees) and elliptical. */
    private static void makeSomeArcs(LayoutEditor le) {
        int n = 0;
        for (TrackSegmentView tsv : le.getTrackSegmentViews()) {
            if (!tsv.isArc() && !tsv.isBezier() && (tsv.getLayoutBlock() != null)
                    && (tsv.getConnect1() != null) && (tsv.getConnect2() != null)) {
                if ((n % 5) == 0) {
                    tsv.setArc(true);
                    boolean circle = (n % 10) == 0;
                    tsv.setCircle(circle);
                    tsv.setAngle(circle ? 180.0 : 90.0);
                }
                n++;
            }
        }
    }

    /**
     * Records the areas Swing is asked to repaint on one component while an
     * action runs, so that repaints asked for by anything else, at other
     * times, are not counted.
     */
    private static class RecordingRepaintManager extends RepaintManager {

        private final JComponent watched;
        private final List<Rectangle> regions = new ArrayList<>();
        private boolean recording = false;

        RecordingRepaintManager(JComponent watched) {
            this.watched = watched;
        }

        @Override
        public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            if (recording && (c == watched)) {
                regions.add(new Rectangle(x, y, w, h));
            }
            super.addDirtyRegion(c, x, y, w, h);
        }

        /**
         * Run an action on this thread, recording only the repaints it asks for.
         *
         * @param action what to run
         */
        synchronized void record(Runnable action) {
            regions.clear();
            recording = true;
            try {
                action.run();
            } finally {
                recording = false;
            }
        }

        synchronized List<Rectangle> regions() {
            return new ArrayList<>(regions);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        cm = new ConfigXmlManager() {
        };
    }

    @AfterEach
    public void tearDown() {
        cm = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.tearDown();
    }
}
