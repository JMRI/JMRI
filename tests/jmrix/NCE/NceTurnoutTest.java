/** 
 * NceTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.NceTurnout class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.nce.*;

public class NceTurnoutTest extends TestCase {

	public void testNceTurnoutCreate() {
		// prepare an interface
		NceTrafficControlScaffold tcis = new NceTrafficControlScaffold();
		
		NceTurnout t = new NceTurnout(21);
		
		// set closed 
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(tcis.outbound.elementAt(0).toString().equals("b0 14 30 0 "));  // CLOSED message
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);
		
		// set thrown 
		try {
			t.setCommandedState(jmri.Turnout.THROWN);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(tcis.outbound.elementAt(1).toString().equals("b0 14 10 0 "));  // THROWN message
		assert(t.getCommandedState() == jmri.Turnout.THROWN);

		// notify the Ln that somebody else changed it...
		NceMessage m = new NceMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x30);
		m.setElement(3, 0x00);
		tcis.sendTestMessage(m,t);
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);

		m = new NceMessage(4);
		m.setOpCode(0xb0);
		m.setElement(1, 0x14);     // set THROWN
		m.setElement(2, 0x10);
		m.setElement(3, 0x00);
		tcis.sendTestMessage(m,t);
		assert(t.getCommandedState() == jmri.Turnout.THROWN);
	}

	// test for incoming status message
	public void testNceTurnoutStatusMsg() {
		// prepare an interface
		NceTrafficControlScaffold tcis = new NceTrafficControlScaffold();
		
		NceTurnout t = new NceTurnout(21);
		
		// set closed 
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}	
		assert(tcis.outbound.elementAt(0).toString().equals("b0 14 30 0 "));  // CLOSED message
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);

		// notify the Ln that somebody else changed it...
		NceMessage m = new NceMessage(4);
		m.setOpCode(0xb1);
		m.setElement(1, 0x14);     // set CLOSED
		m.setElement(2, 0x20);
		m.setElement(3, 0x7b);
		tcis.sendTestMessage(m,t);
		assert(t.getCommandedState() == jmri.Turnout.CLOSED);

	}


	// from here down is testing infrastructure
	
	public NceTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(NceTurnoutTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutTest.class.getName());

}
