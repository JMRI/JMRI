/**
 * LnSensorTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.LnSensor class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.loconet;

import java.io.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

public class LnSensorTest extends TestCase {

	public void testLnSensorCreate() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LnSensor t = new LnSensor("LS042");

		// created in UNKNOWN state
		Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);
	}

	// LnSensor test for incoming status message
	public void testLnSensorStatusMsg() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LnSensor t = new LnSensor("LS043");

		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb2);         // OPC_INPUT_REP
		m.setElement(1, 0x15);     // all but lowest bit of address
		m.setElement(2, 0x60);     // Aux (low addr bit high), sensor high
		m.setElement(3, 0x38);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getKnownState() == jmri.Sensor.ACTIVE);

		m = new LocoNetMessage(4);
		m.setOpCode(0xb2);         // OPC_INPUT_REP
		m.setElement(1, 0x15);     // all but lowest bit of address
		m.setElement(2, 0x40);     // Aux (low addr bit high), sensor low
		m.setElement(3, 0x18);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getKnownState() == jmri.Sensor.INACTIVE);

	}


	// LnSensor test for outgoing status request
	public void testLnSensorStatusRequest() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		LnSensor t = new LnSensor("LS042");

		t.requestUpdateFromLayout();
		// doesn't send a message right now, pending figuring out what
		// to send.
	}

	// from here down is testing infrastructure

	public LnSensorTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnSensor.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnSensorTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorTest.class.getName());

}
