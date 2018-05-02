package jmri.jmrix.lenz.li100;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * LI100AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.li100.LI100Adapter class
 *
 * @author	Paul Bender
 */
public class LI100AdapterTest extends TestCase {

    public void testCtor() {
        LI100Adapter a = new LI100Adapter();
        Assert.assertNotNull("exists", a);
    }

    // from here down is testing infrastructure
    public LI100AdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LI100AdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LI100AdapterTest.class);
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
