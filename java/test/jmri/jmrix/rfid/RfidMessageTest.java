package jmri.jmrix.rfid;

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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
