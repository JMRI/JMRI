package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.annotation.CheckForNull;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * In operating mode, the popup menu for the Block of a Layout Editor track.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEditorTrackPopupTest {

    private LayoutEditor le;

    @CheckForNull
    private static JPopupMenu showingPopup() {
        for (Window w : Window.getWindows()) {
            JPopupMenu p = showingPopup(w);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    @CheckForNull
    private static JPopupMenu showingPopup(Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof JPopupMenu && k.isShowing()) {
                return (JPopupMenu) k;
            }
            if (k instanceof Container) {
                JPopupMenu p = showingPopup((Container) k);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    @Test
    public void testPopupWhereTheButtonPressIsThePopupTrigger() {
        // as on Linux: the right button press is the popup trigger, with meta down; the release isn't
        popupOnTrack(InputEvent.BUTTON3_DOWN_MASK | InputEvent.META_DOWN_MASK, MouseEvent.BUTTON3);
    }

    @Test
    public void testPopupWhereAControlClickIsThePopupTrigger() {
        // as on macOS: a control-click is the popup trigger
        popupOnTrack(InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1);
    }

    private void popupOnTrack(int modifiers, int button) {
        PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p1, new PositionablePointView(p1, new Point2D.Double(100.0, 100.0), le));
        PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, le);
        le.addLayoutTrack(p2, new PositionablePointView(p2, new Point2D.Double(300.0, 100.0), le));
        TrackSegment ts = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, false, le);
        le.addLayoutTrack(ts, new TrackSegmentView(ts, le));
        LayoutBlock lb = java.util.Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB1", "LB4"));
        lb.initializeLayoutBlock();
        ts.setLayoutBlock(lb);
        jmri.Block block = java.util.Objects.requireNonNull(lb.getBlock());
        // the press is the popup trigger, the release isn't; made before the window is shown, while the window
        // system isn't sending mouse events
        Component panel = le.getTargetPanel();
        long t = System.currentTimeMillis();
        MouseEvent press = new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, t, modifiers,
                200, 101, 1, true, button);
        int up = modifiers & ~(InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK);
        MouseEvent release = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, t + 50, up,
                200, 101, 1, false, button);
        ThreadingUtil.runOnGUI(() -> {
            le.setAllEditable(false);
            le.setVisible(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            panel.dispatchEvent(press);
            panel.dispatchEvent(release);
        });
        JUnitUtil.waitFor(() -> showingPopup() != null, "the Block's popup menu is shown");
        JPopupMenu popup = ThreadingUtil.runOnGUIwithReturn(LayoutEditorTrackPopupTest::showingPopup);
        assertNotNull(popup);
        boolean found = false;
        for (Component c : popup.getComponents()) {
            if (c instanceof JMenuItem && jmri.jmrit.trainrecord.swing.BlockDialog.menuItemText(block)
                    .equals(((JMenuItem) c).getText())) {
                found = true;
            }
        }
        assertTrue(found, "it opens the Block dialog");
        for (Component c : popup.getComponents()) {
            if (c instanceof JMenuItem && jmri.jmrit.trainrecord.swing.BlockDialog.menuItemText(block)
                    .equals(((JMenuItem) c).getText())) {
                ThreadingUtil.runOnGUI(((JMenuItem) c)::doClick);
            }
        }
        JUnitUtil.waitFor(() -> !jmri.util.JmriJFrame.getFrameList(jmri.jmrit.trainrecord.swing.BlockDialog.class)
                .isEmpty(), "chosen, the Block dialog opens");
        assertTrue(jmri.util.JmriJFrame.getFrameList(jmri.jmrit.trainrecord.swing.BlockDialog.class).get(0)
                .getBlock() == block, "for the track's Block");
        ThreadingUtil.runOnGUI(() -> popup.setVisible(false));
        assertTrue(le.blockOfTrackAt(new Point2D.Double(200.0, 101.0)) == block, "on the track");
        assertTrue(le.blockOfTrackAt(new Point2D.Double(200.0, 200.0)) == null, "off the track, none");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Track Popup Test"));
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
