package jmri.jmrit.display.layoutEditor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of LayoutEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorTest extends TestCase {

    public void testCtor() {
        LayoutEditor  e = new LayoutEditor();
        Assert.assertNotNull("exists", e );
    }

    public void testStringCtor() {
        LayoutEditor  e = new LayoutEditor("Test Layout");
        Assert.assertNotNull("exists", e );
    }

    public void testGetFinder() {
        LayoutEditor e = new LayoutEditor();
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f );
    }

    public void testSetSize() {
        LayoutEditor e = new LayoutEditor();
        e.setSize(100,100);
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth());
        Assert.assertEquals("Height Set", 100.0, d.getHeight());
    }

    public void testGetOpenDispatcherOnLoad(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad",e.getOpenDispatcherOnLoad());
    }

    public void testSetOpenDispatcherOnLoad(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set",e.getOpenDispatcherOnLoad());
    }

    public void testIsDirty(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false.
        Assert.assertFalse("isDirty",e.isDirty());
    }

    public void testSetDirty(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set",e.isDirty());
    }

    public void testSetDirtyWithParameter(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set",e.isDirty());
    }

    public void testResetDirty(){
        LayoutEditor e = new LayoutEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset",e.isDirty());
    }

    public void testIsAnimating(){
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("isAnimating",e.isAnimating());
    }

    public void testSetTurnoutAnimating(){
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set",e.isAnimating());
    }

    public void testGetLayoutWidth(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("layout width",0,e.getLayoutWidth());
    }


    public void testGetLayoutHeight(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("layout height",0,e.getLayoutHeight());
    }

    public void testGetWindowWidth(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("window width",0,e.getWindowWidth());
    }


    public void testGetWindowHeight(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("window height",0,e.getWindowHeight());
    }

    public void testGetUpperLeftX(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("upper left X",0,e.getUpperLeftX());
    }

    public void testGetUpperLeftY(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 0
        Assert.assertEquals("upper left Y",0,e.getUpperLeftY());
    }

    public void testSetLayoutDimensions(){
        LayoutEditor e = new LayoutEditor();
        // set the panel dimensions to known values
        e.setLayoutDimensions(100,100,100,100,100,100);
        Assert.assertEquals("layout width after set",100,e.getLayoutWidth());
        Assert.assertEquals("layout height after set",100,e.getLayoutHeight());
        Assert.assertEquals("window width after set",100,e.getWindowWidth());
        Assert.assertEquals("window height after set",100,e.getWindowHeight());
        Assert.assertEquals("upper left X after set",100,e.getUpperLeftX());
        Assert.assertEquals("upper left Y after set",100,e.getUpperLeftX());
    }

    public void testSetGrideSize(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("grid size after set",100,e.setGridSize(100));
    }

    public void testGetGrideSize(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("grid size",10,e.getGridSize());
    }

    public void testGetMainlineTrackWidth(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 4.
        Assert.assertEquals("mainline track width",4,e.getMainlineTrackWidth());
    }

    public void testSetMainlineTrackWidth(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set",10,e.getMainlineTrackWidth());
    }

    public void testGetSidelineTrackWidth(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 2.
        Assert.assertEquals("side track width",2,e.getSideTrackWidth());
    }

    public void testSetSideTrackWidth(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setSideTrackWidth(10);
        Assert.assertEquals("Side track width after set",10,e.getSideTrackWidth());
    }

    public void testGetXScale(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 1.
        Assert.assertEquals("XScale",1.0,e.getXScale());
    }

    public void testSetXScale(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXScale(2.0);
        Assert.assertEquals("XScale after set ",2.0,e.getXScale());
    }

    public void testGetYScale(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 1.
        Assert.assertEquals("YScale",1.0,e.getYScale());
    }

    public void testSetYScale(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setYScale(2.0);
        Assert.assertEquals("YScale after set ",2.0,e.getYScale());
    }

    public void testGetDefaultTrackColor(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Track Color","black",e.getDefaultTrackColor());
    }

    public void testSetDefaultTrackColor(){
        LayoutEditor e = new LayoutEditor();
        e.setDefaultTrackColor("pink");
        Assert.assertEquals("Default Track Color after Set","pink",e.getDefaultTrackColor());
    }

    public void testGetDefaultOccupiedTrackColor(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Occupied Track Color","red",e.getDefaultOccupiedTrackColor());
    }

    public void testSetDefaultOccupiedTrackColor(){
        LayoutEditor e = new LayoutEditor();
        e.setDefaultOccupiedTrackColor("pink");
        Assert.assertEquals("Default Occupied Track Color after Set","pink",e.getDefaultOccupiedTrackColor());
    }

    public void testGetDefaultAlternativeTrackColor(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Alternative Track Color","white",e.getDefaultAlternativeTrackColor());
    }

    public void testSetDefaultAlternativeTrackColor(){
        LayoutEditor e = new LayoutEditor();
        e.setDefaultAlternativeTrackColor("pink");
        Assert.assertEquals("Default Alternative Track Color after Set","pink",e.getDefaultAlternativeTrackColor());
    }

    public void testGetDefaultTextColor(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Default Text Color","black",e.getDefaultTextColor());
    }

    public void testSetDefaultTextColor(){
        LayoutEditor e = new LayoutEditor();
        e.setDefaultTextColor("pink");
        Assert.assertEquals("Default Text Color after Set","pink",e.getDefaultTextColor());
    }

    public void testGetTurnoutCircleColor(){
        LayoutEditor e = new LayoutEditor();
        Assert.assertEquals("Turnout Circle Color","black",e.getTurnoutCircleColor());
    }

    public void testSetTurnoutCircleColor(){
        LayoutEditor e = new LayoutEditor();
        e.setTurnoutCircleColor("pink");
        Assert.assertEquals("Turnout Circle after Set","pink",e.getTurnoutCircleColor());
    }

    public void testGetTurnoutCircleSize(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 2.
        Assert.assertEquals("turnout circle size",2,e.getTurnoutCircleSize());
    }

    public void testSetTurnoutCircleSize(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutCircleSize(10);
        Assert.assertEquals("turnout circle size after set",10,e.getTurnoutCircleSize());
    }

    public void testGetTurnoutDrawUnselectedLeg(){
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg",e.getTurnoutDrawUnselectedLeg());
    }

    public void testSetTurnoutDrawUnselectedLeg(){
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set",e.getTurnoutDrawUnselectedLeg());
    }

    public void testGetLayoutName(){
        LayoutEditor e = new LayoutEditor();
        // default is "My Layout"
        Assert.assertEquals("getLayoutName","My Layout",e.getLayoutName());
    }

    public void testSetLayoutName(){
        LayoutEditor e = new LayoutEditor();
        // set to a known value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set","foo",e.getLayoutName());
    }

    public void testGetShowHelpBar(){
        LayoutEditor e = new LayoutEditor();
        // default to true
        Assert.assertTrue("getShowHelpBar",e.getShowHelpBar());
    }

    public void testSetShowHelpBar(){
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setShowHelpBar(false);
        Assert.assertFalse("getShowHelpBar after set",e.getShowHelpBar());
    }

    public void testGetDrawGrid(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getDrawGrid",e.getDrawGrid());
    }

    public void testSetDrawGrid(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set",e.getDrawGrid());
    }

    public void testGetSnapOnAdd(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getSnapOnAdd",e.getSnapOnAdd());
    }

    public void testSetSnapOnAdd(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set",e.getSnapOnAdd());
    }

    public void testGetSnapOnMove(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getSnapOnMove",e.getSnapOnMove());
    }

    public void testSetSnapOnMove(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set",e.getSnapOnMove());
    }

    public void testGetAntialiasingOn(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getAntialiasingOn",e.getAntialiasingOn());
    }

    public void testSetAntialiasingOn(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set",e.getAntialiasingOn());
    }

    public void testGetTurnoutCircles(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getTurnoutCircles",e.getTurnoutCircles());
    }

    public void testSetTurnoutCircles(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set",e.getTurnoutCircles());
    }

    public void testGetTooltipsNotEdit(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getTooltipsNotEdit",e.getTooltipsNotEdit());
    }

    public void testSetTooltipsNotEdit(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set",e.getTooltipsNotEdit());
    }
 
    public void testGetTooltipsInEdit(){
        LayoutEditor e = new LayoutEditor();
        // default to true 
        Assert.assertTrue("getTooltipsInEdit",e.getTooltipsInEdit());
    }

    public void testSetTooltipsInEdit(){
        LayoutEditor e = new LayoutEditor();
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set",e.getTooltipsInEdit());
    }

    public void testGetAutoBlockAssignment(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getAutoBlockAssignment",e.getAutoBlockAssignment());
    }

    public void testSetAutoBlockAssignment(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set",e.getAutoBlockAssignment());
    }

    public void testGetTurnoutBX(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 20. 
        Assert.assertEquals("getTurnoutBX",20.0,e.getTurnoutBX());
    }

    public void testSetTurnoutBX(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ",2.0,e.getTurnoutBX());
    }

    public void testGetTurnoutCX(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX",20.0,e.getTurnoutCX());
    }

    public void testSetTurnoutCX(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ",2.0,e.getTurnoutCX());
    }

    public void testGetTurnoutWid(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid",10.0,e.getTurnoutWid());
    }

    public void testSetTurnoutWid(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ",2.0,e.getTurnoutWid());
    }

    public void testGetXOverLong(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 30.
        Assert.assertEquals("getXOverLong",30.0,e.getXOverLong());
    }

    public void testSetXOverLong(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ",2.0,e.getXOverLong());
    }

    public void testGetXOverHWid(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getXOverHWid",10.0,e.getXOverHWid());
    }

    public void testSetXOverHWid(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ",2.0,e.getXOverHWid());
    }
 
   public void testGetXOverShort(){
        LayoutEditor e = new LayoutEditor();
        // defaults to 10.
        Assert.assertEquals("getXOverShort",10.0,e.getXOverShort());
    }

    public void testSetXOverShort(){
        LayoutEditor e = new LayoutEditor();
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ",2.0,e.getXOverShort());
    }

    public void testResetTurnoutSizes(){
        LayoutEditor e = new LayoutEditor();
        // set all dimensions to known value
        e.setTurnoutBX(2.0);
        e.setTurnoutCX(2.0);
        e.setTurnoutWid(2.0);
        e.setXOverLong(2.0);
        e.setXOverHWid(2.0);
        e.setXOverShort(2.0);
        // reset - uses reflection to get a private method.
        java.lang.reflect.Method resetTurnoutSize=null;
        try {
            resetTurnoutSize = e.getClass().getDeclaredMethod("resetTurnoutSize");
        } catch(java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method resetTurnoutSize in LayoutEditor class.");
        }
        // override the default permissions.
        Assert.assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        try {
           resetTurnoutSize.invoke(e);
        } catch ( java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method resetTurnoutSize in LayoutEditor class.");
        } catch ( java.lang.reflect.InvocationTargetException ite) {
           Throwable cause = ite.getCause();
           Assert.fail("resetTurnoutSize execution failed reason: " + cause.getMessage());
        }

        // then check for the default values.
        Assert.assertEquals("getTurnoutBX",20.0,e.getTurnoutBX());
        Assert.assertEquals("getTurnoutCX",20.0,e.getTurnoutBX());
        Assert.assertEquals("getTurnoutWid",20.0,e.getTurnoutBX());
        Assert.assertEquals("getXOverLong",30.0,e.getXOverLong());
        Assert.assertEquals("getXOverHWid",30.0,e.getXOverLong());
        Assert.assertEquals("getXOverShort",30.0,e.getXOverLong());
        // and reset also sets the dirty bit.
        Assert.assertTrue("isDirty after resetTurnoutSize",e.isDirty());
    }

    public void testGetDirectTurnoutControl(){
        LayoutEditor e = new LayoutEditor();
        // default to false 
        Assert.assertFalse("getDirectTurnoutControl",e.getDirectTurnoutControl());
    }

    public void testSetDirectTurnoutControl(){
        LayoutEditor e = new LayoutEditor();
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set",e.getDirectTurnoutControl());
    }

    // from here down is testing infrastructure


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        // reset the instance manager. 
        JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }




    public LayoutEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutEditorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutEditorTest.class);
        return suite;
    }

}
