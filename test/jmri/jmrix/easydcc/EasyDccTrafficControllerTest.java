/**
 * EasyDccTrafficControllerTest.java
 *
 * Description:	    JUnit tests for the EasyDccTrafficController class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.easydcc;

import jmri.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.easydcc.*;

public class EasyDccTrafficControllerTest extends TestCase {

	public void testCreate() {
		EasyDccTrafficController m = new EasyDccTrafficController();
	}

	public void testSendAscii() throws Exception {
		EasyDccTrafficController c = new EasyDccTrafficController();

		// connect to iostream via port controller
		EasyDccPortControllerScaffold p = new EasyDccPortControllerScaffold();
		c.connectPort(p);

		// send a message
		EasyDccMessage m = new EasyDccMessage(3);
		m.setOpCode('0');
		m.setElement(1, '1');
		m.setElement(2, '2');
		c.sendEasyDccMessage(m, new EasyDccListenerScaffold());
		Assert.assertEquals("total length ", 4, tostream.available());
		Assert.assertEquals("Char 0", '0', tostream.readByte());
		Assert.assertEquals("Char 1", '1', tostream.readByte());
		Assert.assertEquals("Char 2", '2', tostream.readByte());
		Assert.assertEquals("EOM", 0x0d, tostream.readByte());
		Assert.assertEquals("remaining ", 0, tostream.available());
	}

	public void testMonitor() throws Exception {
		EasyDccTrafficController c = new EasyDccTrafficController();

		// connect to iostream via port controller
		EasyDccPortControllerScaffold p = new EasyDccPortControllerScaffold();
		c.connectPort(p);

		// start monitor
		rcvdMsg = null;
		EasyDccListenerScaffold s = new EasyDccListenerScaffold();
		c.addEasyDccListener(s);

		// send a message
		EasyDccMessage m = new EasyDccMessage(3);
		m.setOpCode('0');
		m.setElement(1, '1');
		m.setElement(2, '2');
		c.sendEasyDccMessage(m, new EasyDccListenerScaffold());
		synchronized (this) {wait(100);}
		
		// check it arrived at monitor
		Assert.assertTrue("message not null", rcvdMsg != null);
		Assert.assertEquals("total length ", 4, tostream.available());
		Assert.assertEquals("Char 0", '0', tostream.readByte());
		Assert.assertEquals("Char 1", '1', tostream.readByte());
		Assert.assertEquals("Char 2", '2', tostream.readByte());
		Assert.assertEquals("EOM", 0x0d, tostream.readByte());
		Assert.assertEquals("remaining ", 0, tostream.available());
	}

	public void testRcvReply() throws Exception {
		EasyDccTrafficController c = new EasyDccTrafficController();

		// connect to iostream via port controller
		EasyDccPortControllerScaffold p = new EasyDccPortControllerScaffold();
		c.connectPort(p);

		// object to receive reply
		EasyDccListener l = new EasyDccListenerScaffold();
		c.addEasyDccListener(l);

		// send a message
		EasyDccMessage m = new EasyDccMessage(3);
		m.setOpCode('0');
		m.setElement(1, '1');
		m.setElement(2, '2');
		c.sendEasyDccMessage(m, l);
		// that's already tested, so don't do here.

		// now send reply
		tistream.write('P');
		tistream.write(0x0d);

		// drive the mechanism
		c.handleOneIncomingReply();
		Assert.assertTrue("reply received ", waitForReply());
		Assert.assertEquals("first char of reply ", 'P', rcvdReply.getOpCode());
	}


	private boolean waitForReply() {
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( rcvdReply == null && i++ < 100  )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i
									+" reply="+rcvdReply);
		return i<100;
	}

	// internal class to simulate a EasyDccListener
	class EasyDccListenerScaffold implements EasyDccListener {
		public EasyDccListenerScaffold() {
			rcvdReply = null;
			rcvdMsg = null;
		}
		public void message(EasyDccMessage m) {rcvdMsg = m;}
		public void reply(EasyDccReply r) {rcvdReply = r;}
	}
	EasyDccReply rcvdReply;
	EasyDccMessage rcvdMsg;

	// internal class to simulate a EasyDccPortController
	class EasyDccPortControllerScaffold extends EasyDccPortController {
            public java.util.Vector getPortNames() { return null; }
	    public String openPort(String portName, String appName) { return null; }
	    public void configure() {}
	    public String[] validBaudRates() { return null; }

		protected EasyDccPortControllerScaffold() throws Exception {
			PipedInputStream tempPipe;
			tempPipe = new PipedInputStream();
			tostream = new DataInputStream(tempPipe);
			ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
			tempPipe = new PipedInputStream();
			istream = new DataInputStream(tempPipe);
			tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
		}

		// returns the InputStream from the port
		public DataInputStream getInputStream() { return istream; }

		// returns the outputStream to the port
		public DataOutputStream getOutputStream() { return ostream; }

		// check that this object is ready to operate
		public boolean status() { return true; }
	}
	static DataOutputStream ostream;  // Traffic controller writes to this
	static DataInputStream  tostream; // so we can read it from this

	static DataOutputStream tistream; // tests write to this
	static DataInputStream  istream;  // so the traffic controller can read from this

	// from here down is testing infrastructure

	public EasyDccTrafficControllerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EasyDccTrafficControllerTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccTrafficControllerTest.class.getName());


}
