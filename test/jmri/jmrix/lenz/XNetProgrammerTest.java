/**
 * XNetProgrammerTest.java
 *
 * Description:	    JUnit tests for the XNetProgrammer class
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */

package jmri.jmrix.lenz;

import jmri.*;

import java.util.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XNetProgrammerTest extends TestCase {

	public void testWriteCvSequence() throws JmriException {
		XNetProgrammer.self = null; // avoid spurious warning message
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer();

		// and do the write
		p.writeCV(10, 20, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
	}

	public void testWriteRegisterSequence() throws JmriException {
		XNetProgrammer.self = null; // avoid spurious warning message
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer();

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

		// and do the write
		p.writeCV(3, 12, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
	}

	public void testReadCvSequence() throws JmriException {
		XNetProgrammer.self = null; // avoid spurious warning message
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer();

		// and do the read
		p.readCV(10, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
	}

	// internal class to simulate a XNetListener
	class XNetListenerScaffold implements jmri.ProgListener {
		public XNetListenerScaffold() {
			rcvdInvoked = 0;;
			rcvdValue = 0;
			rcvdStatus = 0;
		}
		public void programmingOpReply(int value, int status) {
			rcvdValue = value;
			rcvdStatus = status;
			rcvdInvoked++;
		}
	}
	int rcvdValue;
	int rcvdStatus;
	int rcvdInvoked;

	// from here down is testing infrastructure

	public XNetProgrammerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {XNetProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetProgrammerTest.class);
		return suite;
	}

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetProgrammerTest.class.getName());

}
