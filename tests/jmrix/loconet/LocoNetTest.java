/** 
 * LocoNetTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.loconet;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.jmrix.loconet.*;

public class LocoNetTest extends TestCase {

	// a simple test skeleton
	public void testDemo() {
		assert(true);
	}

	// Start LnTurnout tests
	public void testLnTurnoutCreate() {
		// prepare an interface
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		
		LnTurnout t = new LnTurnout(21);
		assert(true);
		
		// set closed 
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(lnis.outbound.elementAt(0).toString().equals("b0 14 30 0 "));  // output loconet message

		// set thrown 
		try {
			t.setCommandedState(jmri.Turnout.THROWN);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(lnis.outbound.elementAt(1).toString().equals("b0 14 10 0 "));  // output loconet message

		// notify the Ln that somebody else changed it...
		LocoNetMessage m = new LocoNetMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);
		m.setElement(2, 0x30);
		m.setElement(3, 0x00);
		lnis.sendTestMessage(m);
		// following print says 1, 2 - UNKNOWN and THROWN
		System.out.println("pass3:"+lnis.outbound+" // "+t.getKnownState()+" // "+t.getCommandedState());
	}


	// from here down is testing infrastructure
	
	public LocoNetTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoNetTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoNetTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetTest.class.getName());

}
