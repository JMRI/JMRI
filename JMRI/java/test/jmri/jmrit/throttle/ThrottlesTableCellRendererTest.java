package jmri.jmrit.throttle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of ThrottlesTableCellRenderer
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottlesTableCellRendererTest extends TestCase {

    public void testCtor() {
        ThrottlesTableCellRenderer panel = new ThrottlesTableCellRenderer();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public ThrottlesTableCellRendererTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ThrottlesTableCellRendererTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThrottlesTableCellRendererTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
