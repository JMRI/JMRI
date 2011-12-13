package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LenzCommandStationTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.LenzCommandStation class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class LenzCommandStationTest extends TestCase {

    public void testCtor() {

        LenzCommandStation c = new LenzCommandStation();
        Assert.assertNotNull(c);
    }

	// from here down is testing infrastructure

	public LenzCommandStationTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LenzCommandStationTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LenzCommandStationTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LenzCommandStationTest.class.getName());

}
