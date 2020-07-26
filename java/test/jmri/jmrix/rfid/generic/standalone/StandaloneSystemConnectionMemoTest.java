package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * StandaloneSystemConnectionMemoTest.java
 * <p>
 * Test for the StandaloneSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 * @deprecated since 4.21.1 test of deprecated {@link StandaloneSystemConnectionMemo}.
 */
@Deprecated
public class StandaloneSystemConnectionMemoTest extends SystemConnectionMemoTestBase<StandaloneSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new StandaloneSystemConnectionMemo();
        StandaloneTrafficController tc = new StandaloneTrafficController(scm) {
            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm.setRfidTrafficController(tc);
        scm.configureManagers(
                new StandaloneSensorManager(scm),
                new StandaloneReporterManager(scm));
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
