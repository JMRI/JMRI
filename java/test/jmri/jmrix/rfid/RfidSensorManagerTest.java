package jmri.jmrix.rfid;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
            @Nonnull
            protected Sensor createNewSensor(@Nonnull String systemName, String userName){
                throw new IllegalArgumentException("Scaffold Class");
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

        };
        Assert.assertNotNull(c);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RfidSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
