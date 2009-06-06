package jmri.jmrix.lenz.ztc640;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * ZTC640FrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.ztc640.ZTC640Frame class
 * @author			Paul Bender
 * @version         $Revision: 1.1 $
 */
public class ZTC640FrameTest extends TestCase {

    public void testCtor() {
        ZTC640Frame f = new ZTC640Frame();
        Assert.assertTrue(f != null);
    }

	// from here down is testing infrastructure

	public ZTC640FrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", ZTC640FrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ZTC640FrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZTC640FrameTest.class.getName());

}
