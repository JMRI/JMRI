package jmri.jmrix.bidib.swing;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBComponentFactory class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBComponentFactoryTest {
    
    BiDiBSystemConnectionMemo memo;
    
    @Test
    public void testCTor() {
        BiDiBComponentFactory t = new BiDiBComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
