package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.InstanceManager;
import jmri.jmrit.display.EditHistory;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for undo and redo in Layout Editor: each step puts back the same
 * objects, with their connections, Blocks and Block use counts.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorEditHistoryTest {

    private LayoutEditor le;
    private EditHistory history;
    private PositionablePoint p1;
    private PositionablePoint p2;
    private TrackSegment ts;
    private LayoutBlock lb1;

    @Test
    public void testMoveIsUndoneAndRedone() {
        LayoutTrackView v = le.getLayoutTrackView(p1);
        step("Move", () -> v.setCoordsCenter(new Point2D.Double(150.0, 120.0)));
        assertEquals("Move", history.getUndoName());

        assertTrue(history.undo());
        assertEquals(new Point2D.Double(100.0, 100.0), v.getCoordsCenter());
        assertSame(v, le.getLayoutTrackView(p1), "the same view");
        assertTrue(history.redo());
        assertEquals(new Point2D.Double(150.0, 120.0), v.getCoordsCenter());
    }

    @Test
    public void testDeletedTrackComesBackConnectedWithItsBlock() {
        assertEquals(1, lb1.getUseCount());
        step("Delete", () -> le.removeTrackSegment(ts));
        assertFalse(le.getLayoutTracks().contains(ts));
        assertNull(p1.getConnect1(), "the anchor lost its connection");
        assertEquals(0, lb1.getUseCount(), "the Block is no longer used");

        assertTrue(history.undo());
        assertTrue(le.getLayoutTracks().contains(ts), "the track is back");
        TrackSegmentView view = le.getTrackSegmentView(ts);
        assertNotNull(view);
        assertSame(ts, view.getTrackSegment(), "the same object");
        assertSame(ts, p1.getConnect1(), "connected to the anchor again");
        assertSame(ts, p2.getConnect1());
        assertSame(p1, ts.getConnect1());
        assertSame(lb1, ts.getLayoutBlock(), "with its Block");
        assertEquals(1, lb1.getUseCount(), "which counts its use again");

        assertTrue(history.redo());
        assertFalse(le.getLayoutTracks().contains(ts));
        assertEquals(0, lb1.getUseCount());
    }

    @Test
    public void testBlockChangeKeepsUseCounts() {
        LayoutBlock lb2 = Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB2", "LB2"));
        // as the Track Segment dialog does it
        step("Edit", () -> le.getTrackSegmentView(ts).setLayoutBlock(le.provideLayoutBlock("LB2")));
        assertSame(lb2, ts.getLayoutBlock());
        assertEquals(0, lb1.getUseCount());
        assertEquals(1, lb2.getUseCount());

        assertTrue(history.undo());
        assertSame(lb1, ts.getLayoutBlock());
        assertEquals(1, lb1.getUseCount());
        assertEquals(0, lb2.getUseCount());
        assertTrue(history.redo());
        assertSame(lb2, ts.getLayoutBlock());
        assertEquals(0, lb1.getUseCount());
        assertEquals(1, lb2.getUseCount());
    }

    @Test
    public void testTurnoutPropertiesAndGeometry() {
        LayoutRHTurnout to = new LayoutRHTurnout("TO1", le);
        LayoutRHTurnoutView tov = new LayoutRHTurnoutView(to, new Point2D.Double(200.0, 200.0), 0.0, 1.0, 1.0, le);
        le.addLayoutTrack(to, tov);
        Point2D a = tov.getCoordsA();
        step("Edit", () -> {
            to.setContinuingSense(jmri.Turnout.THROWN);
            to.setDisabled(true);
            tov.setCoordsA(new Point2D.Double(a.getX() - 30.0, a.getY()));
        });
        assertTrue(history.undo());
        assertEquals(jmri.Turnout.CLOSED, to.getContinuingSense());
        assertFalse(to.isDisabled());
        assertEquals(a.getX(), tov.getCoordsA().getX(), 1.0E-6);
        assertEquals(a.getY(), tov.getCoordsA().getY(), 1.0E-6);
    }

    @Test
    public void testDeletedIconComesBackAsTheSameObject() {
        PositionableLabel label = new PositionableLabel("Label", le);
        label.setLocation(20, 30);
        ThreadingUtil.runOnGUI(() -> {
            try {
                le.putItem(label);
            } catch (jmri.jmrit.display.Positionable.DuplicateIdException e) {
                fail("the label is added", e);
            }
        });
        step("Delete", label::remove);
        assertFalse(le.getContents().contains(label));

        assertTrue(history.undo());
        assertTrue(le.getContents().contains(label), "the same icon back");
        assertTrue(label.isActive());
        assertEquals(20, label.getX());
        assertTrue(history.redo());
        assertFalse(le.getContents().contains(label));
        assertTrue(history.undo());
        assertTrue(le.getContents().contains(label));
    }

    @Test
    public void testOnlyInEditMode() {
        step("Move", () -> le.getLayoutTrackView(p1).setCoordsCenter(new Point2D.Double(150.0, 120.0)));
        ThreadingUtil.runOnGUI(() -> le.setAllEditable(false));
        assertFalse(history.undo(), "no undo outside edit mode");
        assertEquals(new Point2D.Double(150.0, 120.0), le.getLayoutTrackView(p1).getCoordsCenter());
    }

    @Test
    public void testChangedSinceIsDropped() {
        LayoutTrackView v = le.getLayoutTrackView(p1);
        step("Move", () -> v.setCoordsCenter(new Point2D.Double(150.0, 120.0)));
        v.setCoordsCenter(new Point2D.Double(10.0, 10.0));  // as by a script
        assertFalse(history.undo());
        JUnitAppender.assertWarnMessageStartingWith("Panel changed since \"Move\" was done");
        assertEquals(new Point2D.Double(10.0, 10.0), v.getCoordsCenter(), "left as it is");
        assertFalse(history.canUndo());
    }

    @Test
    public void testDragIsOneStep() {
        Component panel = le.getTargetPanel();
        int meta = InputEvent.META_DOWN_MASK;
        long t = System.currentTimeMillis();
        ThreadingUtil.runOnGUI(() -> {
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, t, meta | InputEvent.BUTTON1_DOWN_MASK,
                    100, 100, 1, false, MouseEvent.BUTTON1));
            for (int x = 110; x <= 160; x += 10) {
                panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_DRAGGED, t + x,
                        meta | InputEvent.BUTTON1_DOWN_MASK, x, 100, 0, false, MouseEvent.BUTTON1));
            }
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, t + 200, meta,
                    160, 100, 1, false, MouseEvent.BUTTON1));
        });
        Point2D moved = le.getLayoutTrackView(p1).getCoordsCenter();
        assertTrue(moved.getX() > 100.0, "the anchor was dragged");
        assertEquals(Bundle.getMessage("StepMove"), history.getUndoName());
        assertTrue(history.undo());
        assertEquals(new Point2D.Double(100.0, 100.0), le.getLayoutTrackView(p1).getCoordsCenter());
        assertFalse(history.canUndo(), "the whole drag was one step");
    }

    @Test
    public void testEditMenu() {
        JMenuBar bar = le.getJMenuBar();
        JMenu edit = null;
        for (int i = 0; i < bar.getMenuCount(); i++) {
            if (Bundle.getMessage("MenuEdit").equals(bar.getMenu(i).getText())) {
                edit = bar.getMenu(i);
            }
        }
        JMenu editMenu = Objects.requireNonNull(edit, "Layout Editor has an Edit menu");
        JMenuItem undo = editMenu.getItem(0);
        assertFalse(undo.isEnabled());
        step("Move", () -> le.getLayoutTrackView(p1).setCoordsCenter(new Point2D.Double(150.0, 120.0)));
        assertEquals(Bundle.getMessage("MenuItemUndoStep", "Move"), undo.getText());
        assertTrue(undo.isEnabled());
        ThreadingUtil.runOnGUI(undo::doClick);
        assertEquals(new Point2D.Double(100.0, 100.0), le.getLayoutTrackView(p1).getCoordsCenter());
        assertTrue(editMenu.getItem(1).isEnabled(), "Redo");
    }

    @Test
    public void testPopupCommandIsAStep() {
        Component panel = le.getTargetPanel();
        ThreadingUtil.runOnGUI(() -> le.setVisible(true));
        long t = System.currentTimeMillis();
        int trigger = InputEvent.BUTTON3_DOWN_MASK | InputEvent.META_DOWN_MASK;
        ThreadingUtil.runOnGUI(() -> {
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, t, trigger,
                    200, 101, 1, true, MouseEvent.BUTTON3));
            panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, t + 50, InputEvent.META_DOWN_MASK,
                    200, 101, 1, false, MouseEvent.BUTTON3));
        });
        JUnitUtil.waitFor(() -> showingPopup() != null, "the track's pop-up menu is shown");
        JPopupMenu popup = Objects.requireNonNull(ThreadingUtil.runOnGUIwithReturn(
                LayoutEditorEditHistoryTest::showingPopup));
        JMenuItem delete = null;
        for (Component c : popup.getComponents()) {
            if (c instanceof JMenuItem && Bundle.getMessage("ButtonDelete").equals(((JMenuItem) c).getText())) {
                delete = (JMenuItem) c;
            }
        }
        JMenuItem d = Objects.requireNonNull(delete, "the pop-up menu has Delete");
        ThreadingUtil.runOnGUI(() -> {
            popup.setVisible(false);
            d.doClick();
        });
        assertFalse(le.getLayoutTracks().contains(ts), "deleted from the pop-up menu");
        assertTrue(history.undo(), "the command is a step");
        assertTrue(le.getLayoutTracks().contains(ts));
        assertSame(ts, p1.getConnect1());
        ThreadingUtil.runOnGUI(() -> le.setVisible(false));
    }

    private static JPopupMenu showingPopup() {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            JPopupMenu p = showingPopup(w);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    private static JPopupMenu showingPopup(java.awt.Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof JPopupMenu && k.isShowing()) {
                return (JPopupMenu) k;
            }
            if (k instanceof java.awt.Container) {
                JPopupMenu p = showingPopup((java.awt.Container) k);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    private void step(String name, Runnable r) {
        ThreadingUtil.runOnGUI(() -> {
            history.begin(name);
            r.run();
            history.end();
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Undo Test"));
        ThreadingUtil.runOnGUI(() -> le.setAllEditable(true));
        history = le.getEditHistory();

        p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p1, new PositionablePointView(p1, new Point2D.Double(100.0, 100.0), le));
        p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p2, new PositionablePointView(p2, new Point2D.Double(300.0, 100.0), le));
        ts = new TrackSegment("T1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, le);
        le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
        p1.setTrackConnection(ts);
        p2.setTrackConnection(ts);
        lb1 = Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "LB1"));
        ts.setLayoutBlock(le.provideLayoutBlock("LB1"));
    }

    @AfterEach
    public void tearDown() {
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
