/**
 * EasyDccConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.EasyDccConsist class
 *
 * @author	Paul Bender
 * @version
 */
package jmri.jmrix.easydcc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class EasyDccConsistTest extends TestCase {

    public void testCtor() {
        EasyDccConsist m = new EasyDccConsist(5);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public EasyDccConsistTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccConsistTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccConsistTest.class);
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
