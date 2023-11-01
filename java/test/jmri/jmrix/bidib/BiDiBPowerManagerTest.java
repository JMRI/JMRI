package jmri.jmrix.bidib;

import jmri.JmriException;
import jmri.PowerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import jmri.util.JUnitUtil;
import org.junit.Assert;

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
        Assert.assertNotNull(p);
    }
    
    // test setting power on, off
    @Test
    public void testSetPowerOn() throws JmriException {
        Assert.assertEquals("initial state", PowerManager.UNKNOWN, p.getPower());
        p.setPower(PowerManager.ON);
        p.setPower(PowerManager.OFF);
    }
    
    @Test
    public void testSetPowerIdle() throws JmriException {
        Assert.assertFalse("BiDiB does not implement IDLE", p.implementsIdle());
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        p = new BiDiBPowerManager(memo);
    }
    
    @After
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }

}
