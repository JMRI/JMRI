package jmri.jmrix.rfid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.Sensor;

/**
 * RfidSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidSensorManagerTest {

    RfidTrafficController tc = null;

    @Test
    public void testCtor() {
        RfidSensorManager c = new RfidSensorManager("R"){
            @Override
            protected Sensor createNewSensor(String systemName, String userName){
               return null;
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

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
