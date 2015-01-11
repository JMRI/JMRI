package jmri.jmrix.lenz.swing.lzv100;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * LZV100FrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.swing.lzv100.LZV100Frame class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class LZV100FrameTest extends TestCase {

    public void testCtor() {
       // infrastructure objects
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        LZV100Frame f = new LZV100Frame(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(f);
    }

	// from here down is testing infrastructure

	public LZV100FrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LZV100FrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LZV100FrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(LZV100FrameTest.class.getName());

}
