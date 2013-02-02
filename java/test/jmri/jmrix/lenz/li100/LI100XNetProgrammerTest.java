package jmri.jmrix.lenz.li100;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.LenzCommandStation;

/**
 * LI100XNetProgrammerTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.li100.LI100XNetProgrammer class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class LI100XNetProgrammerTest extends TestCase {

    public void testCtor() {
// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());

        LI100XNetProgrammer p = new LI100XNetProgrammer(t);
        Assert.assertNotNull(p);
    }

	// from here down is testing infrastructure

	public LI100XNetProgrammerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LI100XNetProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LI100XNetProgrammerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
    }
    protected void tearDown() throws Exception { 
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }

    static Logger log = Logger.getLogger(LI100XNetProgrammerTest.class.getName());

}
