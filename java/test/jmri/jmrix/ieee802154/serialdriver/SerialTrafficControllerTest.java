package jmri.jmrix.ieee802154.serialdriver;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SerialTrafficControllerTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.serialdriver.SerialTrafficController class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class SerialTrafficControllerTest extends TestCase {

    public void testCtor() {
        SerialTrafficController m = new SerialTrafficController();
        Assert.assertNotNull("exists",m);
    }

	// from here down is testing infrastructure

	public SerialTrafficControllerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(SerialTrafficControllerTest.class.getName());

}
