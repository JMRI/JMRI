package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetThrottleTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetThrottle class
 * @author			Paul Bender
 * @version         $Revision: 2.1 $
 */
public class XNetThrottleTest extends TestCase {

    public void testCtor() {
        XNetThrottle t = new XNetThrottle();
        Assert.assertTrue(t != null);
    }

	// from here down is testing infrastructure

	public XNetThrottleTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetThrottleTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetThrottleTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetThrottleTest.class.getName());

}
