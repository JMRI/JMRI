package jmri.jmrix.lenz.liusb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBConfigFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.liusb.LIUSBConfigFrame class
 * @author			Paul Bender
 * @version         $Revision: 1.1 $
 */
public class LIUSBConfigFrameTest extends TestCase {

    public void testCtor() {
        LIUSBConfigFrame f = new LIUSBConfigFrame();
        Assert.assertTrue(f != null);
    }

	// from here down is testing infrastructure

	public LIUSBConfigFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LIUSBConfigFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LIUSBConfigFrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBConfigFrameTest.class.getName());

}
