package jmri.jmrix.rfid;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new RfidTrafficController(){
           @Override
           public void sendInitString(){
           }
        };
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
