package jmri.jmrix.lenz.li100f;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.lenz.li100f.LI100fAdapter class
 *
 * @author	Paul Bender
 */
public class LI100fAdapterTest extends TestCase {

    public void testCtor() {
        LI100fAdapter a = new LI100fAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LI100fAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LI100fAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LI100fAdapterTest.class);
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
