package jmri.jmrix.oaktree;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the OakTreeSystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class OakTreeSystemConnectionMemoTest extends SystemConnectionMemoTestBase<OakTreeSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new OakTreeSystemConnectionMemo();
        scm.setTrafficController(new SerialTrafficController(scm) {
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
