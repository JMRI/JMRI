package jmri.jmrix.lenz.liusbserver;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBServerAdapterTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter class
 * @author			Paul Bender
 * @version         $Revision: 17977 $
 */
public class LIUSBServerAdapterTest extends TestCase {

    public void testCtor() {
        LIUSBServerAdapter a = new LIUSBServerAdapter();
        Assert.assertNotNull(a);
    }

	// from here down is testing infrastructure

	public LIUSBServerAdapterTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LIUSBServerAdapterTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LIUSBServerAdapterTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(LIUSBServerAdapterTest.class.getName());

}
