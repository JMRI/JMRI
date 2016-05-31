package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of LayoutTurntable
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutTurntableTest extends TestCase {

    public void testCtor() {
        LayoutTurntable  t = new LayoutTurntable("test",new Point2D.Double(0.0,0.0),new LayoutEditor());
        Assert.assertNotNull("exists", t );
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



    public LayoutTurntableTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutTurntableTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutTurntableTest.class);
        return suite;
    }

}
