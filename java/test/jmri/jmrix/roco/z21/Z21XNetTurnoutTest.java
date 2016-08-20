package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.roco.z21.Z21XNetTurnout} class.
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016	
 */
public class Z21XNetTurnoutTest extends jmri.implementation.AbstractTurnoutTest {

    public int numListeners() {
        return lnis.numListeners();
    }

    XNetInterfaceScaffold lnis;

    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "53 00 15 88 CE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("CLOSED state", jmri.Turnout.CLOSED, t.getCommandedState());
    }

    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "53 00 15 89 CF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("THROWN state", jmri.Turnout.THROWN, t.getCommandedState());
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

    // Test the Z21XNetTurnout message sequence.
    public void testz21XNetTurnoutMsgSequence() {
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // prepare an interface
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        Assert.assertEquals("on message sent", "53 00 15 88 CE",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x43);
        m.setElement(1, 0x00);
        m.setElement(2, 0x15);
        m.setElement(3, 0x00);     // set CLOSED
        m.setElement(4, 0x56);

        int n = lnis.outbound.size();

        ((jmri.jmrix.roco.z21.Z21XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for 
        // outbound size to change.
        Assert.assertEquals("off message sent", "53 00 15 80 C6",
                lnis.outbound.elementAt(n).toString());

        // the turnout will not set its state until it sees a reply message.
        m = new XNetReply();
        m.setElement(0, 0x43);
        m.setElement(1, 0x00);
        m.setElement(2, 0x15);
        m.setElement(3, 0x00);
        m.setElement(4, 0x56);

        ((jmri.jmrix.roco.z21.Z21XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to 
        // set it's state, but it will not cause another reply.
        Assert.assertTrue(t.getKnownState() == jmri.Turnout.CLOSED);
    }

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor 
    // and twosensor feedback).
    public void testz21XNetTurnoutPropertyChange() {
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        t = new Z21XNetTurnout("XT", 21, lnis);

        // set thrown
        try {
            t.setCommandedState(jmri.Turnout.THROWN);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);

        t.setFeedbackMode(jmri.Turnout.ONESENSOR);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        try {
            s.setState(jmri.Sensor.INACTIVE);
            t.provideFirstFeedbackSensor("IS1");
        } catch (Exception x1) {
            log.error("TO exception: " + x1);
        }
        try {
            s.setState(jmri.Sensor.ACTIVE);
        } catch (Exception x) {
            log.error("TO exception: " + x);
        }
        // check to see if the turnout state changes.
        Assert.assertTrue(t.getKnownState() == jmri.Turnout.THROWN);
    }

    @Override
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }

    // from here down is testing infrastructure
    public Z21XNetTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21XNetTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XNetTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        lnis = new XNetInterfaceScaffold(new RocoZ21CommandStation());

        t = new Z21XNetTurnout("XT", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetTurnoutTest.class.getName());

}
