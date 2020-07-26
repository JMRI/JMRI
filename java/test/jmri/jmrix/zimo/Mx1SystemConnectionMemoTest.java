package jmri.jmrix.zimo;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the Mx1SystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Mx1SystemConnectionMemoTest extends SystemConnectionMemoTestBase<Mx1SystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        Mx1TrafficController tc = new Mx1TrafficController() {
            @Override
            public boolean status() {
                return true;
            }

            @Override
            public void sendMx1Message(Mx1Message m, Mx1Listener reply) {
            }
        };
        scm = new Mx1SystemConnectionMemo(tc);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
