// SerialTurnoutTest.java

package jmri.jmrix.cmri.serial;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.AbstractTurnoutTest;

/**
 * SerialTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.cmri.serial.SerialTurnout class
 * @author			Bob Jacobsen
 * @version			$Revision: 1.1 $
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

	private SerialTrafficControlScaffold tcis = null;

	public void setUp() {
		// prepare an interface
		tcis = new SerialTrafficControlScaffold();

		t = new SerialTurnout(4);
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
