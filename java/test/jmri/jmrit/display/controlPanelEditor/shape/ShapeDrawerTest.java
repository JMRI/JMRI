package jmri.jmrit.display.controlPanelEditor.shape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class ShapeDrawerTest {

    @Test
    public void testCTor() {

        ControlPanelEditor frame = new ControlPanelEditor();
        ShapeDrawer t = new ShapeDrawer(frame);
        assertNotNull( t, "exists");
        frame.dispose();
    }

    @Test
    public void testCreateCircleFromShapeDrawerMenu() {
        ControlPanelEditor frame = new ControlPanelEditor("CPE ShapeDrawerTest");
        ShapeDrawer t = new ShapeDrawer(frame);
        assertNotNull( t, "exists");

        ThreadingUtil.runOnGUI( () -> {
            frame.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator("CPE ShapeDrawerTest");
        assertNotNull(jfo);

        JMenuBarOperator jmbo = new JMenuBarOperator(jfo);
        assertNotNull(jmbo);

        JMenuOperator jmo = new JMenuOperator(jmbo, 7);
        assertEquals(6, jmo.getMenuComponentCount(), "all shapes in menu");

        var addCircleButton = jmo.getItem(3);
        assertEquals( Bundle.getMessage("drawSth", Bundle.getMessage("Circle")), addCircleButton.getText());

        JMenuItemOperator addCircleMenuItem = new JMenuItemOperator(addCircleButton);
        addCircleMenuItem.doClick();
        addCircleMenuItem.getQueueTool().waitEmpty();

        JUnitUtil.waitFor(500);
        // Msg to user : Drag a selection rectangle to hold the circle

        // CPE now expects a rectangle to be drawn within the Frame to place the circle
        // we first select a point at top left, then drag it to bottom right.
        // Once this rectangle is drawn, the DrawCircle Frame is visible.
        MouseRobotDriver mouseDriver = new MouseRobotDriver(new Timeout("Mouse", 1000L)); // 1 000 ms.

        // Start and end points, relative to the frame
        Point start = new Point(130, 130); // pad out to avoid menu bar
        Point end = new Point(160, 160);

        // move mouse to start point
        mouseDriver.moveMouse(jfo, start.x, start.y);

        // Simulate left mouse press at start point
        mouseDriver.pressMouse(jfo, start.x, start.y,
            java.awt.event.InputEvent.BUTTON1_DOWN_MASK, 0);

        // Simulate drag (mouse moved while pressed)
        mouseDriver.moveMouse(jfo, end.x, end.y);

        // Simulate mouse release at end point
        mouseDriver.releaseMouse(jfo, end.x, end.y,
            java.awt.event.InputEvent.BUTTON1_DOWN_MASK, 0);

        JFrameOperator jfoCircleEditor = new JFrameOperator("Edit Circle Shape");
        JButtonOperator jbo = new JButtonOperator(jfoCircleEditor, Bundle.getMessage("ButtonDone"));
        jbo.doClick();
        jbo.getQueueTool().waitEmpty();

        JUnitUtil.dispose(jfo.getWindow());

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShapeDrawerTest.class);

}
