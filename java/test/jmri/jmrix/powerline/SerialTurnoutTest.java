// SerialTurnoutTest.java

package jmri.jmrix.powerline;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnoutTest;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import junit.framework.*;

/**
 * Tests for the jmri.jmrix.powerline.SerialTurnout class
 * @author			Bob Jacobsen Copyright 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version			$Revision$
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

	private SerialSystemConnectionMemo memo = null;
	private SerialTrafficControlScaffold tc = null;

	public void setUp() {
		// prepare an interface
		memo = new SpecificSystemConnectionMemo();
		tc = new SerialTrafficControlScaffold();
		memo.setTrafficController(tc);
		t = new SerialTurnout("PTA4", tc, "tA4");
	}

	public int numListeners() { return tc.numListeners(); }

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
		String[] testCaseName = {SerialTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialTurnoutTest.class);
		return suite;
	}

	 static Logger log = Logger.getLogger(SerialTurnoutTest.class.getName());

}
