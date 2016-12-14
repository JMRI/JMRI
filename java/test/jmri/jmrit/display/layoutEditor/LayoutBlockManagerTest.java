package jmri.jmrit.display.layoutEditor;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of LayoutBlockManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutBlockManagerTest extends TestCase {

    public void testCtor() {
        LayoutBlockManager  b = new LayoutBlockManager();
        Assert.assertNotNull("exists", b );
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



    public LayoutBlockManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutBlockManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutBlockManagerTest.class);
        return suite;
    }

}
