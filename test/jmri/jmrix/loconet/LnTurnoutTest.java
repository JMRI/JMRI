//LnTurnoutTest.java

package jmri.jmrix.loconet;

import java.io.*;
import junit.framework.*;

import jmri.jmrix.loconet.*;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnout class
 * @author			Bob Jacobsen
 * @version         $Revision: 1.3 $
 */
public class LnTurnoutTest extends jmri.AbstractTurnoutTest {

	public void setUp() {
		// prepare an interface
		lnis = new LocoNetInterfaceScaffold();

		t = new LnTurnout(21);
	}

	public int numListeners() {
		return lnis.numListeners();
	}

	LocoNetInterfaceScaffold lnis;

	public void checkClosedMsgSent() {
		Assert.assertTrue(lnis.outbound.elementAt(lnis.outbound.size()-1).toString().equals("b0 14 30 0 "));  // CLOSED loconet message
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);
	}

	public void checkThrownMsgSent() {
		Assert.assertTrue(lnis.outbound.elementAt(lnis.outbound.size()-1).toString().equals("b0 14 10 0 "));  // THROWN loconet message
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
	}

	public void checkIncoming() {
		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x30);
		m.setElement(3, 0x00);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

		m = new LocoNetMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set THROWN
		m.setElement(2, 0x10);
		m.setElement(3, 0x00);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
	}

	// LnTurnout test for incoming status message
	public void testLnTurnoutStatusMsg() {
		// prepare an interface
		// set closed
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}
		Assert.assertTrue(lnis.outbound.elementAt(0).toString().equals("b0 14 30 0 "));  // CLOSED loconet message
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb1);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x20);
		m.setElement(3, 0x7b);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

	}

	// from here down is testing infrastructure

	public LnTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnTurnoutTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTurnoutTest.class.getName());

}
