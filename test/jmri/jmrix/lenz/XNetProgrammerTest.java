/**
 * XNetProgrammerTest.java
 *
 * Description:	    JUnit tests for the XNetProgrammer class
 * @author			Bob Jacobsen
 * @version         $Revision: 1.2 $
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
        Assert.assertEquals("write message contents", "23 17 a 14 0 ", t.outbound.get(0).toString());
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
		p.writeCV(29, 12, l);
		// check "prog mode" message sent
		Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 5 c 0 ", t.outbound.get(0).toString());

        // send reply
        XNetMessage mr1 = new XNetMessage(3);
        mr1.setElement(0,0x61);
        mr1.setElement(1,0x02);
        mr1.setElement(2,0x63);
        t.sendTestMessage(mr1);

		Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31 ", t.outbound.get(0).toString());

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
        Assert.assertEquals("read message contents", "22 14 a 0 ", t.outbound.get(0).toString());
	}

	public void testReadRegisterSequence() throws JmriException {
		XNetProgrammer.self = null; // avoid spurious warning message
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer();

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

		// and do the read
		p.readCV(29, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "??", t.outbound.get(0).toString());
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
