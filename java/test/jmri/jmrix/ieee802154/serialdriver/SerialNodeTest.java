package jmri.jmrix.ieee802154.serialdriver;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SerialNodeTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.serialdriver.SerialNode class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class SerialNodeTest extends TestCase {

    public void testCtor() {
        SerialNode m = new SerialNode();
        Assert.assertNotNull("exists",m);
    }

	// from here down is testing infrastructure

	public SerialNodeTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialNodeTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialNodeTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(SerialNodeTest.class.getName());

}
