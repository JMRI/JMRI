package jmri.jmrix.lenz.hornbyelite;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HornbyEliteCommandStationTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.hornbyelite.HornbyEliteCommandStation class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class HornbyEliteCommandStationTest extends TestCase {

    public void testCtor() {

        HornbyEliteCommandStation c = new HornbyEliteCommandStation();
        Assert.assertNotNull(c);
    }

	// from here down is testing infrastructure

	public HornbyEliteCommandStationTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", HornbyEliteCommandStationTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(HornbyEliteCommandStationTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(HornbyEliteCommandStationTest.class.getName());

}
