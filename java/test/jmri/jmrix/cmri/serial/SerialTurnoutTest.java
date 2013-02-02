// SerialTurnoutTest.java

package jmri.jmrix.cmri.serial;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnoutTest;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.cmri.serial.SerialTurnout class
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

	private SerialTrafficControlScaffold tcis = null;
    private SerialNode n = new SerialNode();

	public void setUp() {
		// prepare an interface
		tcis = new SerialTrafficControlScaffold();
        n = new SerialNode(0,SerialNode.SMINI);
		t = new SerialTurnout("CT4","t4");
		Assert.assertNotNull("exists", n );
	}

	public int numListeners() { return tcis.numListeners(); }

	public void checkThrownMsgSent() {
                
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
	}

	public void checkClosedMsgSent() {
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
	}

	// from here down is testing infrastructure

	public SerialTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialTurnoutTest.class);
		return suite;
	}

	 static Logger log = Logger.getLogger(SerialTurnoutTest.class.getName());

}
