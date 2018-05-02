package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidReply class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidReplyTest {

    RfidTrafficController tc = null;

    @Test
    public void testCtor() {
        RfidReply c = new RfidReply(tc){
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
        tc = new RfidTrafficController(){
           @Override
           public void sendInitString(){
           }
        };
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }

}
