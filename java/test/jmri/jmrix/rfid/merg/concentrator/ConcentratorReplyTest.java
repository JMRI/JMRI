package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ConcentratorReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.merg.concentrator.ConcentratorReply class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorReplyTest {

    ConcentratorTrafficController tc = null;

    @Test
    @Ignore("setup not complete, created object is null")
    public void testCtor() {
        ConcentratorReply c = new ConcentratorReply(tc){
            @Override
            public String toMonitorString(){
               return "";
            }
        };
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
        tc.getAdapterMemo().setProtocol(new jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol());
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }

}
