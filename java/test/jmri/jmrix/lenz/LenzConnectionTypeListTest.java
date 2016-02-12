package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LenzConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.LenzConnectionTypeList class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class LenzConnectionTypeListTest extends TestCase {

    public void testCtor() {

        LenzConnectionTypeList c = new LenzConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public LenzConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LenzConnectionTypeListTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LenzConnectionTypeListTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LenzConnectionTypeListTest.class.getName());

}
