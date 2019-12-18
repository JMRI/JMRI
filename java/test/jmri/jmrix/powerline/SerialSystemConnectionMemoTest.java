package jmri.jmrix.powerline;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialSystemConnectionMemo memo = new SerialSystemConnectionMemo();
        memo.setTrafficController(new SerialTrafficController() {
            @Override
            public void sendSerialMessage(SerialMessage m, SerialListener reply) {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        });
        memo.getTrafficController().setAdapterMemo(memo);
        memo.setSerialAddress(new SerialAddress(memo));
        memo.setTurnoutManager(new SerialTurnoutManager(memo.getTrafficController()));
        memo.setLightManager(new SerialLightManager(memo.getTrafficController()) {
            @Override
            protected jmri.Light createNewSpecificLight(String systemName, String userName) {
                return null;
            }
        });
        memo.setSensorManager(new SerialSensorManager(memo.getTrafficController()) {
            @Override
            public void reply(SerialReply r) {
            }
        });
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        // put in place because AbstractMRTrafficController implementing
        // subclass was not terminated properly
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
