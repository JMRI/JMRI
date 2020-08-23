package jmri.jmrix.maple;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MapleSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MapleSystemConnectionMemoTest extends SystemConnectionMemoTestBase<MapleSystemConnectionMemo> {

    @Test
    public void systemPrefixTest() {
        // default values would be changed to K2 as there is already a connection with prefix [K] active
        MapleSystemConnectionMemo m = new MapleSystemConnectionMemo("K9", SerialConnectionTypeList.MAPLE);
        Assert.assertEquals("Special System Prefix", "K9", m.getSystemPrefix());
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new MapleSystemConnectionMemo();
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
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();

    }

}
