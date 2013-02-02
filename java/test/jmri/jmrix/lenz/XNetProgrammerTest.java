/**
 * XNetProgrammerTest.java
 *
 * Description:	    JUnit tests for the XNetProgrammer class
 * @author			Bob Jacobsen
 * @version         $Revision$
 */

package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import jmri.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XNetProgrammerTest extends TestCase {

	public void testWriteCvSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t);

		// and do the write
		p.writeCV(10, 20, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
                Assert.assertEquals("write message contents", "23 16 0A 14 2B", t.outbound.elementAt(0).toString());
                // send reply
                XNetReply mr1 = new XNetReply();
                mr1.setElement(0,0x61);
                mr1.setElement(1,0x02);
                mr1.setElement(2,0x63);
                t.sendTestMessage(mr1);

	        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
                Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

	}

	public void testWriteRegisterSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t);

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

		// and do the write
		p.writeCV(29, 12, l);
		// check "prog mode" message sent
		Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0,0x61);
        mr1.setElement(1,0x02);
        mr1.setElement(2,0x63);
        t.sendTestMessage(mr1);

	Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

	}

	public void testReadCvSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t);

		// and do the read
		p.readCV(10, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 0A 3D", t.outbound.elementAt(0).toString());

                // send reply
                XNetReply mr1 = new XNetReply();
                mr1.setElement(0,0x61);
                mr1.setElement(1,0x02);
                mr1.setElement(2,0x63);
                t.sendTestMessage(mr1);

	        Assert.assertEquals("enquire message sent", 2, t.outbound.size());
                Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

	}

	public void testReadRegisterSequence() throws JmriException {
		// infrastructure objects
		XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
		XNetListenerScaffold l = new XNetListenerScaffold();

		XNetProgrammer p = new XNetProgrammer(t);

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0,0x61);
        mr1.setElement(1,0x02);
        mr1.setElement(2,0x63);
        t.sendTestMessage(mr1);

	Assert.assertEquals("enquire message sent", 2, t.outbound.size());
        Assert.assertEquals("enquire message contents", "21 10 31", t.outbound.elementAt(1).toString());
    }

	// internal class to simulate a XNetListener
	class XNetListenerScaffold implements jmri.ProgListener {
		public XNetListenerScaffold() {
			rcvdInvoked = 0;
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
		String[] testCaseName = {"-noloading", XNetProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetProgrammerTest.class);
		return suite;
	}

    // The minimal setup is for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	static Logger log = Logger.getLogger(XNetProgrammerTest.class.getName());

}
