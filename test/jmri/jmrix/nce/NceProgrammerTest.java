/**
 * NceProgrammerTest.java
 *
 * Description:	    JUnit tests for the NceProgrammer class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.nce;

import jmri.*;

import java.util.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceProgrammer;

public class NceProgrammerTest extends TestCase {

	public void testWriteCvSequence() throws JmriException {
		// infrastructure objects
		NceInterfaceScaffold t = new NceInterfaceScaffold();
		NceListenerScaffold l = new NceListenerScaffold();

		NceProgrammer p = new NceProgrammer();

		// and do the write
		p.writeCV(10, 20, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
		Assert.assertEquals("mode message contents", "M",
			((NceMessage)(t.outbound.elementAt(0))).toString());
		// reply from programmer arrives
		NceReply r = new NceReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check write message sent
		Assert.assertEquals("write message sent", 2, t.outbound.size());
		Assert.assertEquals("write message contents", "P010 020",
			((NceMessage)(t.outbound.elementAt(1))).toString());
		// reply from programmer arrives
		r = new NceReply();
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check "leave prog mode" message sent
		Assert.assertEquals("normal mode message sent", 3, t.outbound.size());
		Assert.assertEquals("normal mode message contents", "X",
			((NceMessage)(t.outbound.elementAt(2))).toString());
		// reply from programmer arrives
		r = new NceReply();
		t.sendTestReply(r);
		Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
		Assert.assertEquals(" got data value back", 20, rcvdValue);
	}

	public void testWriteRegisterSequence() throws JmriException {
		// infrastructure objects
		NceInterfaceScaffold t = new NceInterfaceScaffold();
		NceListenerScaffold l = new NceListenerScaffold();

		NceProgrammer p = new NceProgrammer();

        // set register mode
        p.setMode(Programmer.REGISTERMODE);

		// and do the write
		p.writeCV(3, 12, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
		Assert.assertEquals("mode message contents", "M",
			((NceMessage)(t.outbound.elementAt(0))).toString());
		// reply from programmer arrives
		NceReply r = new NceReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check write message sent
		Assert.assertEquals("write message sent", 2, t.outbound.size());
		Assert.assertEquals("write message contents", "S3 012",
			((NceMessage)(t.outbound.elementAt(1))).toString());
		// reply from programmer arrives
		r = new NceReply();
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check "leave prog mode" message sent
		Assert.assertEquals("normal mode message sent", 3, t.outbound.size());
		Assert.assertEquals("normal mode message contents", "X",
			((NceMessage)(t.outbound.elementAt(2))).toString());
		// reply from programmer arrives
		r = new NceReply();
		t.sendTestReply(r);
		Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
		Assert.assertEquals(" got data value back", 12, rcvdValue);
	}

	public void testReadCvSequence() throws JmriException {
		log.error("expect next message: ERROR - Creating too many NceProgrammer objects");
		// infrastructure objects
		NceInterfaceScaffold t = new NceInterfaceScaffold();
		NceListenerScaffold l = new NceListenerScaffold();

		NceProgrammer p = new NceProgrammer();

		// and do the read
		p.readCV(10, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
		Assert.assertEquals("mode message contents", "M",
			((NceMessage)(t.outbound.elementAt(0))).toString());
		// reply from programmer arrives
		NceReply r = new NceReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);


		// check "read command" message sent
		Assert.assertEquals("read message sent", 2, t.outbound.size());
		Assert.assertEquals("read message contents", "R010",
			((NceMessage)(t.outbound.elementAt(1))).toString());
		// reply from programmer arrives
		r = new NceReply();
		r.setElement(0, '0');
		r.setElement(1, '2');
		r.setElement(2, '0');
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check "leave prog mode" message sent
		Assert.assertEquals("normal mode message sent", 3, t.outbound.size());
		Assert.assertEquals("normal mode message contents", "X",
			((NceMessage)(t.outbound.elementAt(2))).toString());
		// reply from programmer arrives
		r = new NceReply();
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
		Assert.assertEquals(" value read", 20, rcvdValue);
	}

	// internal class to simulate a NceListener
	class NceListenerScaffold implements jmri.ProgListener {
		public NceListenerScaffold() {
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

	// service internal class to handle transmit/receive for tests
	class NceInterfaceScaffold extends NceTrafficController {
		public NceInterfaceScaffold() {
		}

		// override some NceInterfaceController methods for test purposes

		public boolean status() { return true;
		}

		/**
	 	* record messages sent, provide access for making sure they are OK
	 	*/
		public Vector outbound = new Vector();  // public OK here, so long as this is a test class
		public void sendNceMessage(NceMessage m, jmri.jmrix.nce.NceListener l) {
			if (this.log.isDebugEnabled()) this.log.debug("sendNceMessage ["+m+"]");
			// save a copy
			outbound.addElement(m);
			lastSender = l;
		}

		// test control member functions

		/**
		 * forward a message to the listeners, e.g. test receipt
		 */
		protected void sendTestMessage (NceMessage m) {
			// forward a test message to Listeners
			if (this.log.isDebugEnabled()) this.log.debug("sendTestMessage    ["+m+"]");
			notifyMessage(m, null);
			return;
		}
		protected void sendTestReply (NceReply m) {
			// forward a test message to Listeners
			if (this.log.isDebugEnabled()) this.log.debug("sendTestReply    ["+m+"]");
			notifyReply(m);
			return;
		}

		/*
		* Check number of listeners, used for testing dispose()
		*/
		public int numListeners() {
			return cmdListeners.size();
		}

	}

	// from here down is testing infrastructure

	public NceProgrammerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceProgrammerTest.class);
		return suite;
	}

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceProgrammerTest.class.getName());

}
