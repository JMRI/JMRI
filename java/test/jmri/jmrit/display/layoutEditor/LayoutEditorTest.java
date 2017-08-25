package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutEditor
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutEditorTest {

    private LayoutEditor le = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        le = new LayoutEditor("Test Layout");
        Assert.assertNotNull("exists", le);
    }

    @Test
    public void testStringCtor() {
        testCtor(); // create layout editor
        Assert.assertNotNull("exists", le);
    }

    @Test
    public void testGetFinder() {
        testCtor(); // create layout editor
        LayoutEditorFindItems f = le.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    public void testSetSize() {
        testCtor(); // create layout editor
        le.setSize(100, 100);
        java.awt.Dimension d = le.getSize();

        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    public void testGetSetZoom() {
        testCtor(); // create layout editor
        Assert.assertEquals("Zoom Get", 1.0, le.getZoom(), 0.0);
        // note: Layout Editor won't allow zooms below 0.25
        Assert.assertEquals("Zoom Set", 0.25, le.setZoom(0.1), 0.0);
        // note: Layout Editor won't allow zooms above 6.0.
        Assert.assertEquals("Zoom Set", 6.0, le.setZoom(10.0), 0.0);
        Assert.assertEquals("Zoom Set", 3.33, le.setZoom(3.33), 0.0);
        Assert.assertEquals("Zoom Get", 3.33, le.getZoom(), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        testCtor(); // create layout editor
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", le.getOpenDispatcherOnLoad());
        testCtor(); // create layout editor
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        testCtor(); // create layout editor
        // defaults to false, so set to true.
        le.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", le.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        testCtor(); // create layout editor
        // defaults to false.
        Assert.assertFalse("isDirty", le.isDirty());
    }

    @Test
    public void testSetDirty() {
        testCtor(); // create layout editor
        // defaults to false, setDirty() sets it to true.
        le.setDirty();
        Assert.assertTrue("isDirty after set", le.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        testCtor(); // create layout editor
        // defaults to false, so set it to true.
        le.setDirty(true);
        Assert.assertTrue("isDirty after set", le.isDirty());
    }

    @Test
    public void testResetDirty() {
        testCtor(); // create layout editor
        // defaults to false, so set it to true.
        le.setDirty(true);
        // then call resetDirty, which sets it back to false.
        le.resetDirty();
        Assert.assertFalse("isDirty after reset", le.isDirty());
    }

    @Test
    public void testIsAnimating() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("isAnimating", le.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        le.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", le.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("layout width", 0, le.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("layout height", 0, le.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("window width", 0, le.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("window height", 0, le.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("upper left X", 0, le.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, le.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        testCtor(); // create layout editor
        // set the panel dimensions to known values
        le.setLayoutDimensions(100, 100, 100, 100, 100, 100);
        Assert.assertEquals("layout width after set", 100, le.getLayoutWidth());
        Assert.assertEquals("layout height after set", 100, le.getLayoutHeight());
        Assert.assertEquals("window width after set", 100, le.getWindowWidth());
        Assert.assertEquals("window height after set", 100, le.getWindowHeight());
        Assert.assertEquals("upper left X after set", 100, le.getUpperLeftX());
        Assert.assertEquals("upper left Y after set", 100, le.getUpperLeftX());
    }

    @Test
    public void testSetGrideSize() {
        testCtor(); // create layout editor
        Assert.assertEquals("grid size after set", 100, le.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("grid size", 10, le.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        testCtor(); // create layout editor
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, le.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        testCtor(); // create layout editor
        // set to known value
        le.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, le.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        testCtor(); // create layout editor
        // defaults to 2.
        Assert.assertEquals("side track width", 2, le.getSideTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        testCtor(); // create layout editor
        // set to known value
        le.setSideTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, le.getSideTrackWidth());
    }

    @Test
    public void testGetXScale() {
        testCtor(); // create layout editor
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, le.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        testCtor(); // create layout editor
        // set to known value
        le.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, le.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        testCtor(); // create layout editor
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, le.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        testCtor(); // create layout editor
        // set to known value
        le.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, le.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Track Color", "black", le.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        testCtor(); // create layout editor
        le.setDefaultTrackColor("pink");
        Assert.assertEquals("Default Track Color after Set", "pink", le.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Occupied Track Color", "red", le.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        testCtor(); // create layout editor
        le.setDefaultOccupiedTrackColor("pink");
        Assert.assertEquals("Default Occupied Track Color after Set", "pink", le.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Alternative Track Color", "white", le.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        testCtor(); // create layout editor
        le.setDefaultAlternativeTrackColor("pink");
        Assert.assertEquals("Default Alternative Track Color after Set", "pink", le.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Text Color", "black", le.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        testCtor(); // create layout editor
        le.setDefaultTextColor("pink");
        Assert.assertEquals("Default Text Color after Set", "pink", le.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Turnout Circle Color", "black", le.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        testCtor(); // create layout editor
        le.setTurnoutCircleColor("pink");
        Assert.assertEquals("Turnout Circle after Set", "pink", le.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        testCtor(); // create layout editor
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, le.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        testCtor(); // create layout editor
        le.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, le.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", le.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        le.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", le.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        le = new LayoutEditor(); // we do this here to test the default name
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", le.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        testCtor(); // create layout editor
        // the test layout editor testCtor created is named this
        Assert.assertEquals("getLayoutName", "Test Layout", le.getLayoutName());
        // set to a known (different) value
        le.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", le.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getShowHelpBar", le.getShowHelpBar());
    }

    @Test
    public void testSetShowHelpBar() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        le.setShowHelpBar(false);
        Assert.assertFalse("getShowHelpBar after set", le.getShowHelpBar());
    }

    @Test
    public void testGetDrawGrid() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getDrawGrid", le.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", le.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getSnapOnAdd", le.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", le.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getSnapOnMove", le.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", le.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getAntialiasingOn", le.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", le.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getTurnoutCircles", le.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", le.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", le.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", le.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getTooltipsInEdit", le.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        le.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", le.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", le.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", le.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        testCtor(); // create layout editor
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, le.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        testCtor(); // create layout editor
        // set to known value
        le.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, le.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        testCtor(); // create layout editor
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, le.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        testCtor(); // create layout editor
        // set to known value
        le.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, le.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, le.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        testCtor(); // create layout editor
        // set to known value
        le.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, le.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        testCtor(); // create layout editor
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, le.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        testCtor(); // create layout editor
        // set to known value
        le.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, le.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, le.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        testCtor(); // create layout editor
        // set to known value
        le.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, le.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, le.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        testCtor(); // create layout editor
        // set to known value
        le.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, le.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        testCtor(); // create layout editor
        // set all dimensions to known value
        le.setTurnoutBX(2.0);
        le.setTurnoutCX(2.0);
        le.setTurnoutWid(2.0);
        le.setXOverLong(2.0);
        le.setXOverHWid(2.0);
        le.setXOverShort(2.0);
        // reset - uses reflection to get a private method.
        java.lang.reflect.Method resetTurnoutSize = null;
        try {
            resetTurnoutSize = le.getClass().getDeclaredMethod("resetTurnoutSize");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method resetTurnoutSize in LayoutEditor class.");
        }
        // override the default permissions.
        Assert.assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        try {
            resetTurnoutSize.invoke(le);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method resetTurnoutSize in LayoutEditor class.");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("resetTurnoutSize execution failed reason: " + cause.getMessage());
        }

        // then check for the default values.
        Assert.assertEquals("getTurnoutBX", 20.0, le.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutCX", 20.0, le.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutWid", 20.0, le.getTurnoutBX(), 0.0);
        Assert.assertEquals("getXOverLong", 30.0, le.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverHWid", 30.0, le.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverShort", 30.0, le.getXOverLong(), 0.0);
        // and reset also sets the dirty bit.
        Assert.assertTrue("isDirty after resetTurnoutSize", le.isDirty());
    }

    @Test
    public void testGetDirectTurnoutControl() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", le.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        le.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", le.getDirectTurnoutControl());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetWindows(false);
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
