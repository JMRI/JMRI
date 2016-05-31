package jmri.jmrit.display.layoutEditor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;
import java.awt.geom.Point2D;

/**
 * Test simple functioning of TrackSegment 
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TrackSegmentTest extends TestCase {

    public void testCtor() {
        LayoutEditor le = new LayoutEditor();
        PositionablePoint p1 = new PositionablePoint("a",PositionablePoint.ANCHOR,new Point2D.Double(0.0,0.0),le);
        PositionablePoint p2 = new PositionablePoint("b",PositionablePoint.ANCHOR,new Point2D.Double(1.0,1.0),le);
        TrackSegment s = new TrackSegment("test",p1,LayoutEditor.POS_POINT,p2,LayoutEditor.POS_POINT,false,true,le);
        Assert.assertNotNull("exists", s );
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



    public TrackSegmentTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrackSegmentTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrackSegmentTest.class);
        return suite;
    }

}
