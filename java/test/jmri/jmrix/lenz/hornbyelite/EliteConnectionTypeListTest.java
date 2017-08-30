package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * EliteConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.EliteConnectionTypeList class
 *
 * @author	Paul Bender
 */
public class EliteConnectionTypeListTest extends TestCase {

    public void testCtor() {

        EliteConnectionTypeList c = new EliteConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public EliteConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteConnectionTypeListTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EliteConnectionTypeListTest.class);
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
