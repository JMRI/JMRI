package jmri.jmris;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.AbstractTurnoutServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
abstract public class AbstractTurnoutServerTestBase {

    protected AbstractTurnoutServer ts = null;

    @Test public void testCtor() {
        Assert.assertNotNull(ts);
    }

    // test sending an error message.
    @Test 
    public void testSendErrorStatus() throws java.io.IOException {
        ts.sendErrorStatus("IT1");
        checkErrorStatusSent();
    }

    // test intializing a Turnout status message.
    @Test 
    public void checkInitTurnout() {
        ts.initTurnout("IT1");
        Assert.assertNotNull((jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)).getTurnout("IT1"));
    }

    // test sending an Thrown status message.
    @Test 
    public void CheckSendThrownStatus() throws java.io.IOException{
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",jmri.Turnout.THROWN);
        checkTurnoutThrownSent();
    }

    // test sending an Closed status message.
    @Test
    public void CheckSendClosedStatus() throws java.io.IOException {
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",jmri.Turnout.CLOSED);
        checkTurnoutClosedSent();
    }

    // test sending an UNKNOWN status message.
    @Test
    public void CheckSendUnkownStatus() throws java.io.IOException {
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",jmri.Turnout.UNKNOWN);
        checkTurnoutUnknownSent();
    }

    // test the property change sequence for an THROWN property change.
    @Test
    public void testPropertyChangeThrownStatus() {
        try {
            ts.initTurnout("IT1");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.THROWN);
            checkTurnoutThrownSent();
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    // test the property change sequence for an CLOSED property change.
    @Test
    public void testPropertyChangeClosedStatus() {
        try {
            ts.initTurnout("IT1");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.CLOSED);
            checkTurnoutClosedSent();
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    /**
     * pre test setup.  Must setup TurnoutServer ts.
     */
    @Before 
    abstract public void setUp(); 

    /**
     * check that an error status message was sent by the server
     */
    abstract public void checkErrorStatusSent();

    /**
     * check that an thrown status message was sent by the server
     */
    abstract public void checkTurnoutThrownSent();

    /**
     * check that an closed status message was sent by the server
     */
    abstract public void checkTurnoutClosedSent();

    /**
     * check that an unknown status message was sent by the server
     */
    abstract public void checkTurnoutUnknownSent();
}
