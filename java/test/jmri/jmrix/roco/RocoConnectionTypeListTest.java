package jmri.jmrix.roco;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.roco.RocoConnectionTypeList class
 *
 * @author	Paul Bender
 */
public class RocoConnectionTypeListTest extends TestCase {

    public void testCtor() {

        RocoConnectionTypeList c = new RocoConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public RocoConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RocoConnectionTypeListTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RocoConnectionTypeListTest.class);
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
