/** 
 * EasyDccTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.EasyDccTurnout class
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.easydcc;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.AbstractTurnoutTest;

public class EasyDccTurnoutTest extends AbstractTurnoutTest {

	private EasyDccTrafficControlScaffold tcis = null;
	
	public void setUp() {
		// prepare an interface
		tcis = new EasyDccTrafficControlScaffold();
		
		t = new EasyDccTurnout(4);
	}
	
	public int numListeners() { return tcis.numListeners(); }
	
	public void checkThrownMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "S C02 81 fe 7f", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
	}
	
	public void checkClosedMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		Assert.assertEquals("content", "S C02 81 ff 7e", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
	}

	// from here down is testing infrastructure
	
	public EasyDccTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EasyDccTurnoutTest.class);
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccTurnoutTest.class.getName());

}
