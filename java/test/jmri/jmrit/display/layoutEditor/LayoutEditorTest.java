package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.*;
import jmri.util.junit.rules.*;
import org.junit.*;
import org.junit.rules.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 * Test simple functioning of LayoutEditor.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author George Warner Copyright (C) 2019
 */
public class LayoutEditorTest extends AbstractEditorTestBase<LayoutEditor> {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(3); // allow 3 retries

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new LayoutEditor("Layout Editor Test Layout");
            jmri.InstanceManager.setDefault(LayoutBlockManager.class, new LayoutBlockManager());
        }
    }

    @After
    @Override
    public void tearDown() {
        if (e != null) {
            JUnitUtil.dispose(e);
            e = null;
        }
        JUnitUtil.tearDown();
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor("Layout Editor Test Layout");
        Assert.assertNotNull("exists", e);
        JUnitUtil.dispose(e);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        Assert.assertNotNull("exists", e);
        JUnitUtil.dispose(e);
    }

    @Test
    public void testSavePanel() {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        //delete this file so we won't get the "<xxx> exists... do you want to replace?" dialog
        new File("temp/Layout Editor Test Layout.xml").delete();

        // test the file -> delete panel menu item
        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("StorePanelTitle"),
                Bundle.getMessage("ButtonSave"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("MenuItemStore"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        //clean up after ourselves...
        new File("temp/Layout Editor Test Layout.xml").delete();
    }

    @Test
    public void testDeletePanel() {

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        // test the file -> delete panel menu item
        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("DeleteVerifyTitle"),
                Bundle.getMessage("ButtonYesDelete"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("DeletePanel"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

    }

    @Test
    public void testGetFinder() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    @Override
    public void testSetSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();

        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    public void testGetSetZoom() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((m) -> {
            m.setSaveAllowed(false); // prevent attempts to save while zooming in rest of test
        });
        Assert.assertEquals("Zoom Get", 1.0, e.getZoom(), 0.0);
        // note: Layout Editor won't allow zooms below 0.25
        Assert.assertEquals("Zoom Set", 0.25, e.setZoom(0.1), 0.0);
        // note: Layout Editor won't allow zooms above 8.0.
        Assert.assertEquals("Zoom Set", 8.0, e.setZoom(10.0), 0.0);
        Assert.assertEquals("Zoom Set", 3.33, e.setZoom(3.33), 0.0);
        Assert.assertEquals("Zoom Get", 3.33, e.getZoom(), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testIsAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("isAnimating", e.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", e.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("layout width", 0, e.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("layout height", 0, e.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to screen width - 20
        int w = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20);
        Assert.assertEquals("window width", w, e.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to screen height - 120
        int h = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120);
        Assert.assertEquals("window height", h, e.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("upper left X", 0, e.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, e.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnGUI(() -> {
            // set the panel dimensions to known values
            e.setLayoutDimensions(100, 100, 100, 100, 100, 100);
            Assert.assertEquals("layout width after set", 100, e.getLayoutWidth());
            Assert.assertEquals("layout height after set", 100, e.getLayoutHeight());
            Assert.assertEquals("window width after set", 100, e.getWindowWidth());
            Assert.assertEquals("window height after set", 100, e.getWindowHeight());
            Assert.assertEquals("upper left X after set", 100, e.getUpperLeftX());
            Assert.assertEquals("upper left Y after set", 100, e.getUpperLeftX());
        });
    }

    @Test
    public void testSetGrideSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("grid size after set", 100, e.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("grid size", 10, e.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, e.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, e.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 2.
        Assert.assertEquals("side track width", 2, e.getSidelineTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setSidelineTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, e.getSidelineTrackWidth());
    }

    @Test
    public void testGetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, e.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, e.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, e.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, e.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Track Color", ColorUtil.ColorDarkGray, e.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Track Color after Set", ColorUtil.ColorPink, e.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Occupied Track Color", "red", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultOccupiedTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Occupied Track Color after Set", ColorUtil.ColorPink, e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Alternative Track Color", ColorUtil.ColorWhite, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultAlternativeTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Alternative Track Color after Set", ColorUtil.ColorPink, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Text Color", ColorUtil.ColorBlack, e.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDefaultTextColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Text Color after Set", ColorUtil.ColorPink, e.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Turnout Circle Color", ColorUtil.ColorBlack, e.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleThrownColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Turnout Circle Thrown Color", ColorUtil.ColorBlack, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testSetTurnoutCircleThrownColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleThrownColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testIsTurnoutFillControlCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("isTurnoutFillControlCircles", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testSetTurnoutFillControlCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTurnoutFillControlCircles(true);
        Assert.assertTrue("isTurnoutFillControlCircles after set true", e.isTurnoutFillControlCircles());
        // set back to default (false) and confirm new value
        e.setTurnoutFillControlCircles(false);
        Assert.assertFalse("isTurnoutFillControlCircles after set false", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, e.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, e.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.dispose(); // remove existing instance
        e = new LayoutEditor(); // create new instance to test the default name
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", e.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // the test layout editor setUp created is named this
        Assert.assertEquals("getLayoutName", "Layout Editor Test Layout", e.getLayoutName());
        // set to a known (different) value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", e.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testSetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testGetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getDrawGrid", e.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", e.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getSnapOnAdd", e.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", e.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getSnapOnMove", e.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", e.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getAntialiasingOn", e.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", e.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getTurnoutCircles", e.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", e.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", e.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", e.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("getTooltipsInEdit", e.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", e.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", e.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", e.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // set all dimensions to known value
        e.setTurnoutBX(2.0);
        e.setTurnoutCX(2.0);
        e.setTurnoutWid(2.0);
        e.setXOverLong(2.0);
        e.setXOverHWid(2.0);
        e.setXOverShort(2.0);

        // reset - uses reflection to get a private method.
        java.lang.reflect.Method resetTurnoutSize = null;
        try {
            resetTurnoutSize = e.getClass().getDeclaredMethod("resetTurnoutSize");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method resetTurnoutSize in LayoutEditor class.");
        }
        // override the default permissions.
        Assert.assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        try {
            resetTurnoutSize.invoke(e);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method resetTurnoutSize in LayoutEditor class.");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("resetTurnoutSize execution failed reason: " + cause.getMessage());
        }

        // then check for the default values.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutWid", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverHWid", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverShort", 30.0, e.getXOverLong(), 0.0);
        // and reset also sets the dirty bit.
        Assert.assertTrue("isDirty after resetTurnoutSize", e.isDirty());
    }

    @Test
    public void testGetDirectTurnoutControl() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControlOff() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setDirectTurnoutControl(false);
        Assert.assertFalse("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testIsEditableDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to true
        Assert.assertTrue("isEditable default true", e.isEditable());
    }

    @Test
    public void testSetAllEditableFalse() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setAllEditable(false);
        Assert.assertFalse("isEditable after setAllEditable(false)", e.isEditable());
    }

    @Test
    public void testSetAllEditableTrue() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", e.isEditable());
    }

    @Test
    public void testGetHighlightSelectedBlockDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // default to false
        Assert.assertFalse("le.getHighlightSelectedBlock default false", e.getHighlightSelectedBlock());
    }

    @Test
    public void testSetHighlightSelectedBlockTrue() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setHighlightSelectedBlock(true);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("le.getHighlightSelectedBlock after setHighlightSelectedBlock(true)", e.getHighlightSelectedBlock());
    }

    @Test
    @Ignore("unreliable on CI servers")
    public void testSetHighlightSelectedBlockFalse() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setHighlightSelectedBlock(false);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("le.getHighlightSelectedBlock after setHighlightSelectedBlock(false)", e.getHighlightSelectedBlock());
    }

    @Test
    public void checkOptionsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assert.assertNotNull("Options Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 17, jmo.getItemCount());
    }

    @Test
    public void checkToolsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuTools"));
        Assert.assertNotNull("Tools Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 16, jmo.getItemCount());
    }

    @Test
    public void checkZoomMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuZoom"));
        Assert.assertNotNull("Zoom Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 16, jmo.getItemCount());
    }

    @Test
    public void checkMarkerMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuMarker"));
        Assert.assertNotNull("Marker Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 3, jmo.getItemCount());
    }

    @Test
    public void checkDispatcherMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuDispatcher"));
        Assert.assertNotNull("Dispatcher Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 2, jmo.getItemCount());
    }

    @Test
    public void testToolBarPositionLeft() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Left
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideLeft"), "/");

        new EventTool().waitNoEvent(500);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testToolBarPositionBottom() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Bottom
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideBottom"), "/");

        new EventTool().waitNoEvent(500);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testToolBarPositionRight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Right
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideRight"), "/");

        new EventTool().waitNoEvent(500);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testToolBarPositionFloat() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Float
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideFloat"), "/");

        // bring this window back to the front...
        jfo.activate();

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testGetLEAuxTools() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        LayoutEditorAuxTools t = e.getLEAuxTools();
        Assert.assertNotNull("tools exist", t);
        JUnitUtil.dispose(e);
    }

    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorTest.class.getName());
}
