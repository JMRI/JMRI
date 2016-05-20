package jmri.jmrix.lenz.swing.mon;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetMonFrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.swing.mon.XNetMonFrame class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class XNetMonFrameTest extends TestCase {

    public void testCtor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo=new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

        XNetMonFrame f = new XNetMonFrame(memo);
        Assert.assertNotNull(f);
    }

	// from here down is testing infrastructure

	public XNetMonFrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetMonFrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetMonFrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(XNetMonFrameTest.class.getName());

}
