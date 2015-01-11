package jmri.jmrix.roco.z21;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * z21AdapterTest.java
 *
 * Description:	    tests for the jmri.jmrix.roco.z21.z21Adapter class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class z21AdapterTest extends TestCase {

    public void testCtor() {
        z21Adapter a = new z21Adapter();
        Assert.assertNotNull(a);
    }

	// from here down is testing infrastructure

	public z21AdapterTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", z21AdapterTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(z21AdapterTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(z21AdapterTest.class.getName());

}
