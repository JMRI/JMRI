package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of LayoutBlockConnectivityTools
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutBlockConnectivityToolsTest extends TestCase {

    public void testCtor() {
        LayoutBlockConnectivityTools  t = new LayoutBlockConnectivityTools();
        Assert.assertNotNull("exists", t );
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

    public LayoutBlockConnectivityToolsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LayoutBlockConnectivityToolsTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutBlockConnectivityToolsTest.class);
        return suite;
    }

}
