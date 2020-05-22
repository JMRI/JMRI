package jmri.jmrix.tmcc;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the TMCCSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TmccSystemConnectionMemoTest extends SystemConnectionMemoTestBase<TmccSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new TmccSystemConnectionMemo();
        new SerialTrafficController(scm) {
            @Override
            public void sendSerialMessage(SerialMessage m, SerialListener reply) {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
    }

    @Override
    @After
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();

    }

}
