package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * EliteFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.hornbyelite.EliteFrame class
 * @author			Paul Bender
 * @version         $Revision: 1.1 $
 */
public class EliteFrameTest extends TestCase {

    public void testCtor() {
        EliteFrame f = new EliteFrame();
        Assert.assertNotNull(f);
    }

	// from here down is testing infrastructure

	public EliteFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", EliteFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EliteFrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteFrameTest.class.getName());

}
