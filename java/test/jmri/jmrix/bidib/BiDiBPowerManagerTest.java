package jmri.jmrix.bidib;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBPowerManager class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBPowerManagerTest {

    BiDiBSystemConnectionMemo memo;
    PowerManager p = null;  // holds objects under test

    @Test
    public void testCtor() {
        Assertions.assertNotNull(p);
    }

    // test setting power on, off
    @Test
    public void testSetPowerOn() throws JmriException {
        Assertions.assertEquals(PowerManager.UNKNOWN, p.getPower(),"initial state");
        p.setPower(PowerManager.ON);
        p.setPower(PowerManager.OFF);
    }

    @Test
    public void testSetPowerIdle() throws JmriException {
        Assertions.assertFalse(p.implementsIdle(), "BiDiB does not implement IDLE");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        p = new BiDiBPowerManager(memo);
    }

    @AfterEach
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }

}
