// XNetTurnoutTest.java

package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetTurnout} class.
 * @author	    Bob Jacobsen
 * @version         $Revision: 2.1 $
 */
public class XNetTurnoutTest extends jmri.AbstractTurnoutTest {

	public void setUp() {
        log4jfixtureInst.setUp();
		// prepare an interface
		lnis = new XNetInterfaceScaffold(new LenzCommandStation());

		t = new XNetTurnout(21);
	}

	public int numListeners() {
		return lnis.numListeners();
	}

	XNetInterfaceScaffold lnis;

	public void checkClosedMsgSent() {
		Assert.assertEquals("closed message","52 05 88 00",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
		Assert.assertEquals("CLOSED state",jmri.Turnout.CLOSED,t.getCommandedState());
	}

	public void checkThrownMsgSent() {
		Assert.assertEquals("thrown message","52 05 89 00",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
		Assert.assertEquals("THROWN state",jmri.Turnout.THROWN,t.getCommandedState());
	}

	public void checkIncoming() {
		// notify the object that somebody else changed it...
		XNetReply m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x04);     // set CLOSED
		m.setElement(3, 0x43);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

		m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x08);     // set THROWN
		m.setElement(3, 0x4F);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
	}

	// XNetTurnout test for incoming status message
	public void testXNetTurnoutStatusMsg() {
		// prepare an interface
		// set closed
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

		// notify that somebody else changed it...
		XNetReply m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x04);     // set CLOSED
		m.setElement(3, 0x43);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

	}


	// from here down is testing infrastructure

	public XNetTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {XNetTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetTurnoutTest.class);
		return suite;
	}

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void tearDown() { log4jfixtureInst.tearDown(); }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutTest.class.getName());

}
