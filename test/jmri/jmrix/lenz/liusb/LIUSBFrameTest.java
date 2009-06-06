package jmri.jmrix.lenz.liusb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.liusb.LIUSBFrame class
 * @author			Paul Bender
 * @version         $Revision: 1.1 $
 */
public class LIUSBFrameTest extends TestCase {

    public void testCtor() {
        LIUSBFrame f = new LIUSBFrame();
        Assert.assertTrue(f != null);
    }

	// from here down is testing infrastructure

	public LIUSBFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LIUSBFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LIUSBFrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBFrameTest.class.getName());

}
