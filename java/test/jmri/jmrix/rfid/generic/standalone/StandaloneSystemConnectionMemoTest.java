package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StandaloneSystemConnectionMemoTest.java
 * <p>
 * Test for the StandaloneSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StandaloneSystemConnectionMemoTest extends SystemConnectionMemoTestBase<StandaloneSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
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
    @After
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
