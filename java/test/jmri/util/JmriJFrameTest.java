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
     * Show the window at 100, 100 and wait until the window system has
     * reported where it is and the window has saved it. The window system
     * reports moves later than they are made; a report arriving after a
     * later move would set the window back to where it was.
     */
    private static void showAndLetSettle(JmriJFrame f, List<Point> saved) {
        ThreadingUtil.runOnGUI(() -> {
            f.setVisible(true);
            f.setLocation(100, 100);
        });
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !saved.isEmpty()), "first location saved");
        JUnitUtil.waitFor(4 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        ThreadingUtil.runOnGUI(saved::clear);
    }

    /**
     * Move the window one step, as a drag does, and give the window system
     * time to report it before the next step.
     *
     * @return when the window system had reported the move, from
     *         {@link System#nanoTime()}
     */
    private static long moveAndLetWindowSystemReport(JmriJFrame f, int x, int y) {
        Point p = new Point(x, y);
        ThreadingUtil.runOnGUI(() -> f.setLocation(p));
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> p.equals(f.getLocation())), "moved");
        long movedAt = System.nanoTime();
        JUnitUtil.waitFor(100); // well within the save delay
        return movedAt;
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
        showAndLetSettle(f, saved);

        // The steps must follow each other within the save delay, as in a drag; a step
        // that comes later is a new move, and is rightly saved at once. On a busy machine
        // the window system can take longer than that to report a step, so try again,
        // from where the window is, until the steps come close enough together.
        long slowest = Long.MAX_VALUE;
        int x = 100;
        for (int attempt = 0; attempt < MOVE_ATTEMPTS; attempt++) {
            if (attempt > 0) {
                JUnitUtil.waitFor(4 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY); // let it settle
                ThreadingUtil.runOnGUI(saved::clear);
            }
            // each step in its own event, as when dragged
            long first = moveAndLetWindowSystemReport(f, x + 10, x + 10);
            long second = moveAndLetWindowSystemReport(f, x + 20, x + 20);
            long third = moveAndLetWindowSystemReport(f, x + 30, x + 30);
            Point stopped = new Point(x + 30, x + 30);
            Point onTheWay = new Point(x + 20, x + 20);
            x += 30;
            slowest = Math.max(second - first, third - second) / 1_000_000L;
            JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(
                () -> saved.contains(stopped)), "location saved where the window stopped");
            if (slowest >= JmriJFrame.WINDOW_DETAILS_SAVE_DELAY * 4 / 5) {
                continue; // too slow to be one drag: try again
            }
            // allow ten times the save delay for any further save
            JUnitUtil.waitFor(10 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
            List<Point> after = ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved));
            Assertions.assertTrue(after.size() <= 2, () -> "saved at most twice: " + after);
            Assertions.assertFalse(after.contains(onTheWay), () -> "not saved on the way: " + after);
            Assertions.assertEquals(stopped, after.get(after.size() - 1), "last saved where the window stopped");
            JUnitUtil.dispose(f);
            return;
        }
        JUnitUtil.dispose(f);
        long ms = slowest;
        Assumptions.abort(() -> "the window system took " + ms + " ms to report a move, in each of "
            + MOVE_ATTEMPTS + " tries; a drag needs steps within " + JmriJFrame.WINDOW_DETAILS_SAVE_DELAY + " ms");
    }

    /** How many times testLocationSavedOnceWindowStopsMoving tries a drag. */
    private static final int MOVE_ATTEMPTS = 5;

    /**
     * A window that is opened and placed saves its location at once, as it did
     * before saves waited for a window to stop moving, so nothing is written
     * later while another window is being opened.
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
        showAndLetSettle(f, saved);
        ThreadingUtil.runOnGUI(() -> f.setLocation(200, 200));
        // well within the save delay
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> saved.contains(new Point(200, 200))),
            "location saved at once");
        ThreadingUtil.runOnGUI(saved::clear);
        // the window stays put, so nothing more is written
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
        showAndLetSettle(f, saved);
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
