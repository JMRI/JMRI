package jmri.jmrix.bidib;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBThrottleManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBThrottleManagerTest  extends jmri.managers.AbstractThrottleManagerTestBase {
    
    BiDiBSystemConnectionMemo memo;
            
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        tm = new BiDiBThrottleManager(memo);
    }
    
    @AfterEach
    public void tearDown() {
        tm = null;
        JUnitUtil.tearDown();
    }

}
