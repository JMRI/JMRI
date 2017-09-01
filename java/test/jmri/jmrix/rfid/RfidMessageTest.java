package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidMessage class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidMessageTest {

    @Test
    public void testCtor() {
        RfidMessage c = new RfidMessage(20){
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
