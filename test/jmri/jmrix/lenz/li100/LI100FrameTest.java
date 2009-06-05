package jmri.jmrix.lenz.li100;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LI100FrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.li100.LI100Frame class
 * @author			Paul Bender
 * @version         $Revision: 1.1 $
 */
public class LI100FrameTest extends TestCase {

    public void testCtor() {
        LI100Frame f = new LI100Frame();
        Assert.assertTrue(f != null);
    }

	// from here down is testing infrastructure

	public LI100FrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LI100FrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LI100FrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LI100FrameTest.class.getName());

}
