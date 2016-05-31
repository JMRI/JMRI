package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.lenz.hornbyelite.EliteXNetTurnout} class.
 *
 * @author	Bob Jacobsen
 */
public class EliteXNetTurnoutTest extends jmri.implementation.AbstractTurnoutTest {

    public int numListeners() {
        return lnis.numListeners();
    }

    XNetInterfaceScaffold lnis;

    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "52 05 8A DD",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("CLOSED state", jmri.Turnout.CLOSED, t.getCommandedState());
    }

    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "52 05 8B DC",
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

    // Test that property change events are properly sent from the parent
    // to the propertyChange listener (this handles events for one sensor 
    // and twosensor feedback).
    public void testEliteXNetTurnoutPropertyChange() {
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        t = new EliteXNetTurnout("XT", 21, lnis);

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
        /*System.out.println(t.getKnownState());
         System.out.println(jmri.Turnout.THROWN);*/
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
    public EliteXNetTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteXNetTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EliteXNetTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        lnis = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        t = new EliteXNetTurnout("XT", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutTest.class.getName());

}
