/** 
 * NceTrafficControllerTest.java
 *
 * Description:	    JUnit tests for the NceTrafficController class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import jmri.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NcePortController;

public class NceTrafficControllerTest extends TestCase {

	public void testCreate() {
		NceTrafficController m = new NceTrafficController();
	}

	public void testSend() throws Exception {
		NceTrafficController c = new NceTrafficController();
		
		// connect to iostream via port controller
		NcePortControllerScaffold p = new NcePortControllerScaffold();
		c.connectPort(p);
		
		// send a message
		NceMessage m = new NceMessage(3);
		m.setOpCode('0');
		m.setElement(1, '1');
		m.setElement(2, '2');
		c.sendNceMessage(m);
		Assert.assertEquals("total length ", 4, tostream.available());
		Assert.assertEquals("Char 0", '0', tostream.readByte());
		Assert.assertEquals("Char 1", '1', tostream.readByte());
		Assert.assertEquals("Char 2", '2', tostream.readByte());
		Assert.assertEquals("EOM", 0x0d, tostream.readByte());
		Assert.assertEquals("remaining ", 0, tostream.available());
	}

	// internal class to simulate a NcePortController
	class NcePortControllerScaffold extends NcePortController {
		protected NcePortControllerScaffold() throws Exception {
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
	
	public NceTrafficControllerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceTrafficControllerTest.class);
		return suite;
	}
	
}
