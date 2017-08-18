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

    private LayoutEditor e = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e = new LayoutEditor("Test Layout");
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testStringCtor() {
        testCtor(); // create layout editor
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testGetFinder() {
        testCtor(); // create layout editor
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    public void testSetSize() {
        testCtor(); // create layout editor
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    public void testGetSetZoom() {
        testCtor(); // create layout editor
        Assert.assertEquals("Zoom Get", 1.0, e.getZoom(), 0.0);
        Assert.assertEquals("Zoom Set", 3.33, e.setZoom(3.33), 0.0);
        Assert.assertEquals("Zoom Get", 3.33, e.getZoom(), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        testCtor(); // create layout editor
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", e.getOpenDispatcherOnLoad());
        testCtor(); // create layout editor
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        testCtor(); // create layout editor
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        testCtor(); // create layout editor
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        testCtor(); // create layout editor
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        testCtor(); // create layout editor
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        testCtor(); // create layout editor
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testIsAnimating() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("isAnimating", e.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", e.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("layout width", 0, e.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("layout height", 0, e.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("window width", 0, e.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("window height", 0, e.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("upper left X", 0, e.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        testCtor(); // create layout editor
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, e.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        testCtor(); // create layout editor
        // set the panel dimensions to known values
        e.setLayoutDimensions(100, 100, 100, 100, 100, 100);
        Assert.assertEquals("layout width after set", 100, e.getLayoutWidth());
        Assert.assertEquals("layout height after set", 100, e.getLayoutHeight());
        Assert.assertEquals("window width after set", 100, e.getWindowWidth());
        Assert.assertEquals("window height after set", 100, e.getWindowHeight());
        Assert.assertEquals("upper left X after set", 100, e.getUpperLeftX());
        Assert.assertEquals("upper left Y after set", 100, e.getUpperLeftX());
    }

    @Test
    public void testSetGrideSize() {
        testCtor(); // create layout editor
        Assert.assertEquals("grid size after set", 100, e.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("grid size", 10, e.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        testCtor(); // create layout editor
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, e.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        testCtor(); // create layout editor
        // set to known value
        e.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, e.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        testCtor(); // create layout editor
        // defaults to 2.
        Assert.assertEquals("side track width", 2, e.getSideTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        testCtor(); // create layout editor
        // set to known value
        e.setSideTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, e.getSideTrackWidth());
    }

    @Test
    public void testGetXScale() {
        testCtor(); // create layout editor
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, e.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        testCtor(); // create layout editor
        // set to known value
        e.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, e.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        testCtor(); // create layout editor
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, e.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        testCtor(); // create layout editor
        // set to known value
        e.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, e.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Track Color", "black", e.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        testCtor(); // create layout editor
        e.setDefaultTrackColor("pink");
        Assert.assertEquals("Default Track Color after Set", "pink", e.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Occupied Track Color", "red", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        testCtor(); // create layout editor
        e.setDefaultOccupiedTrackColor("pink");
        Assert.assertEquals("Default Occupied Track Color after Set", "pink", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Alternative Track Color", "white", e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        testCtor(); // create layout editor
        e.setDefaultAlternativeTrackColor("pink");
        Assert.assertEquals("Default Alternative Track Color after Set", "pink", e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Default Text Color", "black", e.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        testCtor(); // create layout editor
        e.setDefaultTextColor("pink");
        Assert.assertEquals("Default Text Color after Set", "pink", e.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        testCtor(); // create layout editor
        Assert.assertEquals("Turnout Circle Color", "black", e.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        testCtor(); // create layout editor
        e.setTurnoutCircleColor("pink");
        Assert.assertEquals("Turnout Circle after Set", "pink", e.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        testCtor(); // create layout editor
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, e.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        testCtor(); // create layout editor
        e.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, e.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", e.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", e.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e = new LayoutEditor(); // we do this here to test the default name
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", e.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        testCtor(); // create layout editor
        // the test layout editor testCtor created is named this
        Assert.assertEquals("getLayoutName", "Test Layout", e.getLayoutName());
        // set to a known (different) value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", e.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
    }

    @Test
    public void testSetShowHelpBar() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        e.setShowHelpBar(false);
        Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
    }

    @Test
    public void testGetDrawGrid() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getDrawGrid", e.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", e.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getSnapOnAdd", e.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", e.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getSnapOnMove", e.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", e.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getAntialiasingOn", e.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", e.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getTurnoutCircles", e.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", e.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", e.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", e.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        testCtor(); // create layout editor
        // default to true
        Assert.assertTrue("getTooltipsInEdit", e.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        testCtor(); // create layout editor
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", e.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", e.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", e.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        testCtor(); // create layout editor
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        testCtor(); // create layout editor
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        testCtor(); // create layout editor
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        testCtor(); // create layout editor
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        testCtor(); // create layout editor
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        testCtor(); // create layout editor
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        testCtor(); // create layout editor
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        testCtor(); // create layout editor
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        testCtor(); // create layout editor
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        testCtor(); // create layout editor
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        testCtor(); // create layout editor
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
        testCtor(); // create layout editor
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        testCtor(); // create layout editor
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
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
        if (e != null) {
            e.dispose();
            e = null;
        }
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
