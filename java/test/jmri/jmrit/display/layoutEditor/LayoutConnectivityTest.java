package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of LayoutConnectivity
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutConnectivityTest extends TestCase {

    public void testCtor() {
        LayoutBlock  b = new LayoutBlock("testb","testb");
        LayoutBlock  d = new LayoutBlock("testd","testd");
        LayoutConnectivity c = new LayoutConnectivity(b,d);
        Assert.assertNotNull("exists", c );
    }

    // from here down is testing infrastructure
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }



    public LayoutConnectivityTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutConnectivityTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutConnectivityTest.class);
        return suite;
    }

}
