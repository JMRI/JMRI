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
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor("Test Layout");
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testGetFinder() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    public void testSetSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
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
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Zoom Get", 1.0, e.getZoom(), 0.0);
        Assert.assertEquals("Zoom Set", 3.33, e.setZoom(3.33), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testIsAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("isAnimating", e.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", e.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("layout width", 0, e.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("layout height", 0, e.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("window width", 0, e.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("window height", 0, e.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("upper left X", 0, e.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, e.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("grid size after set", 100, e.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("grid size", 10, e.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, e.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, e.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 2.
        Assert.assertEquals("side track width", 2, e.getSideTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setSideTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, e.getSideTrackWidth());
    }

    @Test
    public void testGetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, e.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, e.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, e.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, e.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Track Color", "black", e.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setDefaultTrackColor("pink");
        Assert.assertEquals("Default Track Color after Set", "pink", e.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Occupied Track Color", "red", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setDefaultOccupiedTrackColor("pink");
        Assert.assertEquals("Default Occupied Track Color after Set", "pink", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Alternative Track Color", "white", e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setDefaultAlternativeTrackColor("pink");
        Assert.assertEquals("Default Alternative Track Color after Set", "pink", e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Text Color", "black", e.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setDefaultTextColor("pink");
        Assert.assertEquals("Default Text Color after Set", "pink", e.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Turnout Circle Color", "black", e.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setTurnoutCircleColor("pink");
        Assert.assertEquals("Turnout Circle after Set", "pink", e.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, e.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        e.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, e.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", e.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", e.getTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", e.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to a known value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", e.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
    }

    @Test
    public void testSetShowHelpBar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setShowHelpBar(false);
        Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
    }

    @Test
    public void testGetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getDrawGrid", e.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", e.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getSnapOnAdd", e.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", e.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getSnapOnMove", e.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", e.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getAntialiasingOn", e.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", e.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getTurnoutCircles", e.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", e.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", e.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", e.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("getTooltipsInEdit", e.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", e.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", e.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", e.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
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
        LayoutEditor e = new LayoutEditor();
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
