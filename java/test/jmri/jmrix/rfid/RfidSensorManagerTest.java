package jmri.jmrix.rfid;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * Tests for the jmri.jmrix.rfid.RfidSensorManager class
 *
 * @author Paul Bender Copyright (C) 2012, 2016
 */
public class RfidSensorManagerTest {

    private RfidSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        RfidSensorManager c = new RfidSensorManager(memo){
            @Override
            protected Sensor createNewSensor(@Nonnull String systemName, String userName){
                return null;
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

        };
        Assert.assertNotNull(c);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RfidSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
