package jmri.jmrix.powerline;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SerialSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SerialSystemConnectionMemo();
        scm.setTrafficController(new SerialTrafficController() {
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
        scm.getTrafficController().setAdapterMemo(scm); // indirect way to link the two and prevent an NPE
        scm.setSerialAddress(new SerialAddress(scm));
        scm.setTurnoutManager(new SerialTurnoutManager(scm.getTrafficController()));
        scm.setLightManager(new SerialLightManager(scm.getTrafficController()) {
            @Override
            protected jmri.Light createNewSpecificLight(String systemName, String userName) {
                return null;
            }
        });
        scm.setSensorManager(new SerialSensorManager(scm.getTrafficController()) {
            @Override
            public void reply(SerialReply r) {
            }
        });
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
