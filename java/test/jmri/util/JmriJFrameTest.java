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
     * Moving a window saves its location once it has stopped moving, not
     * for every move: each save writes the user interface preferences file.
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
        ThreadingUtil.runOnGUI(saved::clear);

        ThreadingUtil.runOnGUI(() -> {
            f.setLocation(110, 110);
            f.setLocation(120, 120);
            f.setLocation(130, 130);
        });
        JUnitUtil.waitFor(() -> ThreadingUtil.runOnGUIwithReturn(() -> !saved.isEmpty()), "location saved");
        // allow ten times the save delay for any further save
        JUnitUtil.waitFor(10 * JmriJFrame.WINDOW_DETAILS_SAVE_DELAY);
        List<Point> after = ThreadingUtil.runOnGUIwithReturn(() -> new ArrayList<>(saved));
        Assertions.assertEquals(List.of(new Point(130, 130)), after, "saved once, where the window stopped");
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
