// SerialTurnoutTest.java

package jmri.jmrix.grapevine;

import jmri.*;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.1 $
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

	private SerialTrafficControlScaffold tcis = null;
        private SerialNode n = new SerialNode();

	public void setUp() {
		// prepare an interface
		tcis = new SerialTrafficControlScaffold();
        tcis.registerSerialNode(new SerialNode(1, SerialNode.NODE2002V6));
        
		t = new SerialTurnout("GT1004","t4");
	}

	public int numListeners() { return tcis.numListeners(); }

	public void checkThrownMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "81 26 81 07", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
	}

	public void checkClosedMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "81 20 81 0D", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
	}

	// from here down is testing infrastructure

	public SerialTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialTurnoutTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnoutTest.class.getName());

}
