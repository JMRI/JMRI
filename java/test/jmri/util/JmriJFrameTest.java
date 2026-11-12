package jmri.util;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.managers.TestUserPreferencesManager;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriJFrameTest extends JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new JmriJFrame("JmriJFrame ConstructorTest");
        }
    }


    /**
     * Dragging a window saves its location where it started moving and where
     * it stopped, not at every step: each save writes the user interface
     * preferences file.
     */
    @Test
    @DisabledIfHeadless
    public void testLocationSavedOnceWindowStopsMoving() {
        List<Point> saved = new ArrayList<>(); // written and read on the GUI thread
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager() {
            @Override
            public void setWindowLocation(String strClass, Point location) {
                saved.add(location);
            }
        });
        JmriJFrame f = new JmriJFrame("JmriJFrame move test", false, true);
        ThreadingUtil.runOnGUI(() -> {
            f.setVisible(true);
            f.setLocation(100, 100);
        });
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !saved.isEmpty()), "first location saved");
        // let the window finish opening and settle
        JUnitUtil.waitFor(JmriJFrame.WINDOW_OPENING_TIME + 2 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        ThreadingUtil.runOnGUI(saved::clear);

        // each step in its own event, as when dragged
        ThreadingUtil.runOnGUI(() -> f.setLocation(110, 110));
        ThreadingUtil.runOnGUI(() -> f.setLocation(120, 120));
        ThreadingUtil.runOnGUI(() -> f.setLocation(130, 130));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(
            () -> saved.contains(new Point(130, 130))), "location saved where the window stopped");
        // allow ten times the save delay for any further save
        JUnitUtil.waitFor(10 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        List<Point> after = ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved));
        Assertions.assertTrue(after.size() <= 2, () -> "saved at most twice: " + after);
        Assertions.assertFalse(after.contains(new Point(120, 120)), () -> "not saved on the way: " + after);
        Assertions.assertEquals(new Point(130, 130), after.get(after.size() - 1), "last saved where the window stopped");
        JUnitUtil.dispose(f);
    }

    /**
     * A window that is opened and placed, and placed again while it is still
     * opening, saves each place at once, as it did before saves waited for a
     * window to stop moving, so nothing is written later while another window
     * is being opened.
     */
    @Test
    @DisabledIfHeadless
    public void testLocationSavedAtOnceWhenPlaced() {
        List<Point> saved = new ArrayList<>(); // written and read on the GUI thread
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager() {
            @Override
            public void setWindowLocation(String strClass, Point location) {
                saved.add(location);
            }
        });
        JmriJFrame f = new JmriJFrame("JmriJFrame place test", false, true);
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(f::isShowing), "window shown");
        JUnitUtil.waitFor(100); // let the window system finish showing it

        ThreadingUtil.runOnGUI(() -> f.setLocation(200, 200));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> saved.contains(new Point(200, 200))),
            "location saved at once");
        Assertions.assertFalse(ThreadingUtil.runOnGUIwithReturn(f::isWindowDetailsSavePending),
            "nothing left to save later while the window is opening");

        // placed again while opening, in its own event: saved at once too
        ThreadingUtil.runOnGUI(() -> f.setLocation(210, 210));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> saved.contains(new Point(210, 210))),
            "second place saved");
        Assertions.assertFalse(ThreadingUtil.runOnGUIwithReturn(f::isWindowDetailsSavePending),
            "still nothing left to save later");

        // once opened, the window stays put, so nothing more is written
        JUnitUtil.waitFor(JmriJFrame.WINDOW_OPENING_TIME);
        ThreadingUtil.runOnGUI(saved::clear);
        JUnitUtil.waitFor(4 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        Assertions.assertEquals(List.of(), ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved)),
            "not saved again when the window has not moved");
        JUnitUtil.dispose(f);
    }


    /**
     * A window hidden before it has stopped moving is not saved where it was
     * hidden; it is saved when it is closed.
     */
    @Test
    @DisabledIfHeadless
    public void testHiddenWindowNotSavedAfterMove() {
        List<Point> saved = new ArrayList<>(); // written and read on the GUI thread
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager() {
            @Override
            public void setWindowLocation(String strClass, Point location) {
                saved.add(location);
            }
        });
        JmriJFrame f = new JmriJFrame("JmriJFrame hide test", false, true);
        ThreadingUtil.runOnGUI(() -> {
            f.setVisible(true);
            f.setLocation(100, 100);
        });
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !saved.isEmpty()), "first location saved");
        // let the window finish opening and settle
        JUnitUtil.waitFor(JmriJFrame.WINDOW_OPENING_TIME + 2 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        // start moving, then move again and hide before the window stops
        ThreadingUtil.runOnGUI(() -> f.setLocation(120, 120));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> saved.contains(new Point(120, 120))),
            "location saved when the window started moving");
        ThreadingUtil.runOnGUI(() -> {
            saved.clear();
            f.setLocation(140, 140);
            f.setVisible(false);
        });
        // allow ten times the save delay for a save
        JUnitUtil.waitFor(10 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        Assertions.assertEquals(List.of(), ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved)),
            "not saved while hidden");
        ThreadingUtil.runOnGUI(f::dispose);
        Assertions.assertEquals(List.of(new Point(140, 140)),
            ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved)), "saved when closed");
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(JmriJFrameTest.class);

}
