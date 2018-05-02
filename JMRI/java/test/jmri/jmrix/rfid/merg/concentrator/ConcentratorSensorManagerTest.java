package jmri.jmrix.rfid.merg.concentrator;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorSensorManagerTest.java
 *
 * Description:	tests for the ConcentratorSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    ConcentratorTrafficController tc = null;

    @Override
    public String getSystemName(int i) {
        return "RS" + i;
    }


    @Test
    public void testCtor() {
        Assert.assertNotNull(l);
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
        l = new ConcentratorSensorManager(tc,"R"){
            @Override
            public void message(jmri.jmrix.rfid.RfidMessage m){}

            @Override
            public void reply(jmri.jmrix.rfid.RfidReply m){}

        };
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }

}
