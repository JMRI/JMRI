/** 
 * LnSensorTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.LnSensor class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.loconet;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

public class LnSensorTest extends TestCase {

	public void testLnSensorCreate() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		
		LnSensor t = new LnSensor(21);
		
		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x30);
		m.setElement(3, 0x00);
		lnis.sendTestMessage(m);
		// assert(t.getCommandedState() == jmri.Turnout.CLOSED);

		m = new LocoNetMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set THROWN
		m.setElement(2, 0x10);
		m.setElement(3, 0x00);
		lnis.sendTestMessage(m);
		// assert(t.getCommandedState() == jmri.Turnout.THROWN);
	}

	// LnSensor test for incoming status message
	public void testLnSensorStatusMsg() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		
		LnTurnout t = new LnTurnout(21);
		
		// set closed 
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(lnis.outbound.elementAt(0).toString().equals("b0 14 30 0 "));  // CLOSED loconet message
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);

		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb1);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x20);
		m.setElement(3, 0x7b);
		lnis.sendTestMessage(m);
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);

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
