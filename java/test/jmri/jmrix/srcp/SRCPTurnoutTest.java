package jmri.jmrix.srcp;

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
public class SRCPTurnoutTest {

    private SRCPTurnout m = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(m);
    }

    @Test
    public void testGetNumber(){
        Assert.assertEquals("Number",1,m.getNumber());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(et, "TEST", 1);
        memo.setTurnoutManager(new SRCPTurnoutManager(memo, memo.getBus()));
        m = new SRCPTurnout(1, memo);
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
