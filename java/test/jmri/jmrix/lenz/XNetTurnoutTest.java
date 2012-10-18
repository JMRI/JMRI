// XNetTurnoutTest.java

package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetTurnout} class.
 * @author	    Bob Jacobsen
 * @version         $Revision$
 */
public class XNetTurnoutTest extends jmri.implementation.AbstractTurnoutTest {

	public int numListeners() {
		return lnis.numListeners();
	}

	XNetInterfaceScaffold lnis;

	public void checkClosedMsgSent() {
		Assert.assertEquals("closed message","52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
		Assert.assertEquals("CLOSED state",jmri.Turnout.CLOSED,t.getCommandedState());
	}

	public void checkThrownMsgSent() {
		Assert.assertEquals("thrown message","52 05 89 DE",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
		Assert.assertEquals("THROWN state",jmri.Turnout.THROWN,t.getCommandedState());
	}

	public void checkIncoming() {
                t.setFeedbackMode(jmri.Turnout.MONITORING);
		// notify the object that somebody else changed it...
		XNetReply m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x04);     // set CLOSED
		m.setElement(3, 0x43);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getKnownState() == jmri.Turnout.CLOSED);

		m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x08);     // set THROWN
		m.setElement(3, 0x4F);
		lnis.sendTestMessage(m);
		Assert.assertTrue(t.getKnownState() == jmri.Turnout.THROWN);
	}

	// Test the XNetTurnout message sequence.
	public void testXNetTurnoutMsgSequence() {
                t.setFeedbackMode(jmri.Turnout.DIRECT);
		// prepare an interface
		// set closed
		try {
			t.setCommandedState(jmri.Turnout.CLOSED);
		} catch (Exception e) { log.error("TO exception: "+e);
		}
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

		Assert.assertEquals("on message sent","52 05 88 DF",
                    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());

		
 
		// notify that the command station received the reply
		XNetReply m = new XNetReply();
		m.setElement(0, 0x42);
		m.setElement(1, 0x05);
		m.setElement(2, 0x04);     // set CLOSED
		m.setElement(3, 0x43);

		int n = lnis.outbound.size();

                ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

		while(n==lnis.outbound.size()) {} // busy loop.  Wait for 
                                                // outbound size to change.
                Assert.assertEquals("off message sent","52 05 80 D7",
                    lnis.outbound.elementAt(n).toString());

                // the turnout will not set its state until it sees an OK message.
		m = new XNetReply();
                m.setElement(0,0x01);
                m.setElement(1,0x04);
                m.setElement(2,0x05);

		n = lnis.outbound.size();

                ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

		while(n==lnis.outbound.size()) {} // busy loop.  Wait for 
                                                // outbound size to change.

                Assert.assertEquals("off message sent","52 05 80 D7",
                    lnis.outbound.elementAt(n).toString());

		m = new XNetReply();
                m.setElement(0,0x01);
                m.setElement(1,0x04);
                m.setElement(2,0x05);
		
                ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

		// no wait here.  The last reply should cause the turnout to 
	        // set it's state, but it will not cause another reply.

		Assert.assertTrue(t.getKnownState() == jmri.Turnout.CLOSED);
	}

        // Test that property change events are properly sent from the parent
        // to the propertyChange listener (this handles events for one sensor 
        // and twosensor feedback).
	public void testXNetTurnoutPropertyChange() {
		// prepare an interface
		// set thrown
		try {
			t.setCommandedState(jmri.Turnout.THROWN);
		} catch (Exception e) { log.error("TO exception: "+e);
		}
		Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);

                t.setFeedbackMode(jmri.Turnout.ONESENSOR);
                jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
                try {
                t.provideFirstFeedbackSensor("IS1");
                } catch (Exception x1) { log.error("TO exception: " +x1);
                }
                try {
                    s.setState(jmri.Sensor.ACTIVE);
                } catch (Exception x) {log.error("TO exception: " +x);
                } 
                // check to see if the turnout state changes.
		Assert.assertTrue(t.getKnownState() == jmri.Turnout.THROWN);
        }

        @Override
        public void testDispose(){
                t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

                //is deferred to after first use
                t.dispose();
                Assert.assertEquals("controller listeners remaining", 1, numListeners());
        }



	// from here down is testing infrastructure

	public XNetTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetTurnoutTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
		// prepare an interface
		lnis = new XNetInterfaceScaffold(new LenzCommandStation());

		t = new XNetTurnout("XT",21,lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetTurnoutTest.class.getName());

}
