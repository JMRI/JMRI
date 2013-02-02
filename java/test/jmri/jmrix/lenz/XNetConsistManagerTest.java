package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetConsistManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetConsistManager class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class XNetConsistManagerTest extends TestCase {

    public void testCtor() {
       // infrastructure objects
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetConsistManager c = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(c);
    }

	// from here down is testing infrastructure

	public XNetConsistManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetConsistManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetConsistManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(XNetConsistManagerTest.class.getName());

}
