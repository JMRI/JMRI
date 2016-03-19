package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * EliteAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.hornbyelite.EliteAdapter class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class EliteAdapterTest extends TestCase {

    public void testCtor() {
        EliteAdapter a = new EliteAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public EliteAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EliteAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
