package jmri.jmrix.bidib;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBProgrammerManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBProgrammerManagerTest {
    
    @Test
    public void testCtor() {
        // infrastructure objects
        BiDiBSystemConnectionMemo memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        
        BiDiBProgrammerManager t = new BiDiBProgrammerManager(memo);
        Assert.assertNotNull(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
