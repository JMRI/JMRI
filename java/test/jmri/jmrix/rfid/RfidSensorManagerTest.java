package jmri.jmrix.rfid;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        RfidSensorManager c = new RfidSensorManager(new RfidSystemConnectionMemo()){
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
