package jmri.jmris;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the jmri.jmris.AbstractTurnoutServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
abstract public class AbstractTurnoutServerTestBase {

    protected AbstractTurnoutServer ts = null;

    @Test
    public void testCtor() {
        assertNotNull(ts);
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
        assertNotNull((InstanceManager.getDefault(TurnoutManager.class)).getTurnout("IT1"));
    }

    // test sending an Thrown status message.
    @Test 
    public void testCheckSendThrownStatus() throws java.io.IOException{
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",Turnout.THROWN);
        checkTurnoutThrownSent();
    }

    // test sending an Closed status message.
    @Test
    public void testCheckSendClosedStatus() throws java.io.IOException {
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",Turnout.CLOSED);
        checkTurnoutClosedSent();
    }

    // test sending an UNKNOWN status message.
    @Test
    public void testCheckSendUnkownStatus() throws java.io.IOException {
        ts.initTurnout("IT1");
        ts.sendStatus("IT1",Turnout.UNKNOWN);
        checkTurnoutUnknownSent();
    }

    // test the property change sequence for an THROWN property change.
    @Test
    public void testPropertyChangeThrownStatus() throws Exception {
        assertDoesNotThrow ( () -> {
            ts.initTurnout("IT1");
            InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1").setState(Turnout.THROWN);
        },"Exception setting Status");
        checkTurnoutThrownSent();
    }

    // test the property change sequence for an CLOSED property change.
    @Test
    public void testPropertyChangeClosedStatus() throws Exception {
        assertDoesNotThrow ( () -> {
            ts.initTurnout("IT1");
            InstanceManager.getDefault(TurnoutManager.class)
                            .provideTurnout("IT1").setState(Turnout.CLOSED);
        },"Exception setting Status");
        checkTurnoutClosedSent();
    }

    /**
     * pre test setup.  Must setup TurnoutServer ts.
     */
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
