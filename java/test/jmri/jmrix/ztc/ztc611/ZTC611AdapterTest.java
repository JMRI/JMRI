package jmri.jmrix.ztc.ztc611;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * ZTC611AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.ztc611.ZTC611Adapter class
 *
 * @author	Paul Bender
 */
public class ZTC611AdapterTest extends TestCase {

    public void testCtor() {
        ZTC611Adapter a = new ZTC611Adapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public ZTC611AdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ZTC611AdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ZTC611AdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
