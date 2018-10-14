package jmri.jmrix.lenz.ztc640;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * ZTC640AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.ztc640.ZTC640Adapter class
 *
 * @author	Paul Bender
 */
public class ZTC640AdapterTest extends TestCase {

    public void testCtor() {
        ZTC640Adapter a = new ZTC640Adapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public ZTC640AdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ZTC640AdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ZTC640AdapterTest.class);
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
