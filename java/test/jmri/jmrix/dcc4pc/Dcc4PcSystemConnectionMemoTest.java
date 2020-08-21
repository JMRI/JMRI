package jmri.jmrix.dcc4pc;

import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the Dcc4PcSystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Dcc4PcSystemConnectionMemoTest extends SystemConnectionMemoTestBase<Dcc4PcSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        // Dcc4Pc systems report being able to provide an addresed programmer, 
        // but they really just forward it to another connection. 
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController() {
            @Override
            public void sendDcc4PcMessage(Dcc4PcMessage m, Dcc4PcListener reply) {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm = new Dcc4PcSystemConnectionMemo(tc);
        InstanceManager.setDefault(Dcc4PcSystemConnectionMemo.class, scm);
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.getDcc4PcTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
