package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutEditorComponentTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        LayoutEditor le = new LayoutEditor("Layout Editor Component Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);
        assertNotNull(t, "exists");
        JUnitUtil.dispose(le);
    }

    /**
     * The clip held by the component is the visible part of the panel, used to
     * cull drawing. It must be intersected with the clip Swing supplied, not
     * substituted for it, so that a partial repaint stays partial.
     */
    @Test
    @DisabledIfHeadless
    public void testPaintStaysInsideRequestedClip() {
        LayoutEditor le = new LayoutEditor("Layout Editor Component Clip Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);

        // the visible area of the panel, as adjustClip() would set it
        t.setClip(new Rectangle2D.Double(0, 0, 400, 400));

        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            // the small area Swing asked to be repainted, e.g. one icon
            Rectangle requested = new Rectangle(100, 100, 20, 20);
            g2.setClip(requested);

            t.paint(g2);

            Rectangle after = g2.getClipBounds();
            assertNotNull(after, "clip still set after painting");
            assertTrue(requested.contains(after), "painting stayed inside the requested clip, was " + after);
        } finally {
            g2.dispose();
        }
        JUnitUtil.dispose(le);
    }

    /**
     * Zoomed out, in, or not at all, the track is drawn wherever it is on the
     * panel, not only in the part of the panel that the panel's size before
     * zooming would cover. A track near each corner of the layout is drawn
     * when that corner is in view: zoomed out, the whole layout is in view;
     * zoomed in, the view is scrolled to the far corner.
     */
    @Test
    @DisabledIfHeadless
    public void testTrackIsDrawnAcrossTheWholePanelAtEveryZoom() {
        // made on the GUI thread (Swing thread), as when a panel is loaded,
        // so that the panel's scroll bars get their listeners
        LayoutEditor le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Layout Editor Component Zoom Test Layout"));
        Point2D nearFrom = new Point2D.Double(100, 100);
        Point2D nearTo = new Point2D.Double(300, 100);
        Point2D farFrom = new Point2D.Double(1300, 1100);
        Point2D farTo = new Point2D.Double(1500, 1100);
        ThreadingUtil.runOnGUI(() -> {
            addTrack(le, "Near", nearFrom, nearTo);
            addTrack(le, "Far", farFrom, farTo);
            le.setAllEditable(false);
            le.setSize(1300, 1000);
            le.setVisible(true);
        });

        Color background = backgroundBehind(le.getTargetPanel());
        for (double zoom : new double[]{0.5, 0.8, 1.0, 2.0}) {
            ThreadingUtil.runOnGUI(() -> {
                le.setZoom(zoom);
                scrollTo(le, false);
            });
            BufferedImage picture = draw(le);
            assertTrue(isDrawn(nearFrom, nearTo, zoom, picture, background), "near track drawn at zoom " + zoom);
            // the far corner can be out of view until scrolled to: zoomed in, or with a window
            // smaller than asked for, as on a small screen
            ThreadingUtil.runOnGUI(() -> scrollTo(le, true));
            picture = draw(le);
            assertTrue(isDrawn(farFrom, farTo, zoom, picture, background), "far track drawn at zoom " + zoom);
        }
        JUnitUtil.dispose(le);
    }

    /** Add a plain track between two new anchors. */
    private static void addTrack(LayoutEditor le, String name, Point2D from, Point2D to) {
        PositionablePoint p1 = new PositionablePoint(name + "1", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p1, new PositionablePointView(p1, from, le));
        PositionablePoint p2 = new PositionablePoint(name + "2", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p2, new PositionablePointView(p2, to, le));
        TrackSegment ts = new TrackSegment(name, p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, le);
        le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
    }

    /** Scroll to the far (bottom right) corner of the panel, or back to its origin. */
    private static void scrollTo(LayoutEditor le, boolean farCorner) {
        JComponent targetPanel = le.getTargetPanel();
        if (farCorner) {
            targetPanel.scrollRectToVisible(new Rectangle(targetPanel.getWidth() - 1, targetPanel.getHeight() - 1, 1, 1));
        } else {
            targetPanel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }
    }

    /**
     * Whether any pixel near the line between two points of the layout, at the
     * zoom, differs from the background.
     */
    private static boolean isDrawn(Point2D from, Point2D to, double zoom, BufferedImage picture, Color background) {
        Rectangle2D line = new Rectangle2D.Double(from.getX(), from.getY(), 0, 0);
        line.add(to);
        int left = Math.max(0, (int) Math.floor((line.getMinX() - 4) * zoom));
        int top = Math.max(0, (int) Math.floor((line.getMinY() - 4) * zoom));
        int right = Math.min(picture.getWidth(), (int) Math.ceil((line.getMaxX() + 4) * zoom));
        int bottom = Math.min(picture.getHeight(), (int) Math.ceil((line.getMaxY() + 4) * zoom));
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                if ((picture.getRGB(x, y) & 0xffffff) != (background.getRGB() & 0xffffff)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Draw the whole panel, as Swing would, into a new picture filled first
     * with the background behind it.
     */
    private static BufferedImage draw(LayoutEditor le) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            JComponent targetPanel = le.getTargetPanel();
            int w = targetPanel.getWidth();
            int h = targetPanel.getHeight();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setClip(0, 0, w, h);
            g.setColor(backgroundBehind(targetPanel));
            g.fillRect(0, 0, w, h);
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
