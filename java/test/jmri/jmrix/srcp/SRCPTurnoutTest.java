package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * SRCPTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTurnout class
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class SRCPTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    private SRCPTrafficControlScaffold stc = null;

    @Override
    public int numListeners() {
        return stc.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
       Assert.assertTrue("message sent", stc.outbound.size()>0);
       Assert.assertEquals("content", "SET 1 GA 1 0 1 -1\n", stc.outbound.elementAt(stc.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
       Assert.assertTrue("message sent", stc.outbound.size()>0);
       Assert.assertEquals("content", "SET 1 GA 1 0 0 -1\n", stc.outbound.elementAt(stc.outbound.size()-1).toString());  // THROWN message
    }

    @Test
    public void testGetNumber(){
        Assert.assertEquals("Number",1,((SRCPTurnout) t).getNumber());
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController
        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        stc = new SRCPTrafficControlScaffold();
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(stc, "TEST", 1);
        memo.setTurnoutManager(new SRCPTurnoutManager(memo, memo.getBus()));
        t = new SRCPTurnout(1, memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
