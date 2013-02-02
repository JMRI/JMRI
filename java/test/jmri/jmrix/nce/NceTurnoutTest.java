/**
 * NceTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.NceTurnout class
 * @author			Bob Jacobsen
 * @version			$Revision$
 */

package jmri.jmrix.nce;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class NceTurnoutTest extends AbstractTurnoutTest {

	private NceTrafficControlScaffold tcis = null;

	public void setUp() {
		// prepare an interface
		tcis = new NceTrafficControlScaffold();

		t = new NceTurnout(tcis, "NT", 4);
	}

	public int numListeners() { return tcis.numListeners(); }

	public void checkThrownMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		// 2004 eprom output:
		// Assert.assertEquals("content", "93 02 81 FE 7F", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
		Assert.assertEquals("content", "AD 00 04 04 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
	}

	public void checkClosedMsgSent() {
		Assert.assertTrue("message sent", tcis.outbound.size()>0);
		// 2004 eprom output:
		//Assert.assertEquals("content", "93 02 81 FF 7E", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
		Assert.assertEquals("content", "AD 00 04 03 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
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

	 static Logger log = Logger.getLogger(NceTurnoutTest.class.getName());

}
