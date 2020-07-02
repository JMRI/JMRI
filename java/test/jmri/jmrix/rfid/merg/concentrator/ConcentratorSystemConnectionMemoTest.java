package jmri.jmrix.rfid.merg.concentrator;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * ConcentratorSystemConnectionMemoTest.java
 * <p>
 * Test for the ConcentratorSystemConnectionMemo class
 *
 * @author Paul Bender Copyright(C) 2016
 */
public class ConcentratorSystemConnectionMemoTest extends SystemConnectionMemoTestBase<ConcentratorSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new ConcentratorSystemConnectionMemo();
        ConcentratorTrafficController tc = new ConcentratorTrafficController(scm, "A-H") {
            @Override
            public void sendInitString() {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm.setRfidTrafficController(tc);
        scm.configureManagers(null, null);
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();

    }

}
