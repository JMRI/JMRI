package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialListener;

/**
 * Tests for SpecificSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class SpecificSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SpecificSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        scm = new SpecificSystemConnectionMemo();
        scm.setTrafficController(new SpecificTrafficController(scm) {
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
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();

    }

}
