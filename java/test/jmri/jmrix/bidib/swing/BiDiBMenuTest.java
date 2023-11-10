package jmri.jmrix.bidib.swing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Tests for the BiDiBMenu class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBMenuTest {
    
    BiDiBSystemConnectionMemo memo;
    
    @Test
    public void testCTor() {
        BiDiBMenu t = new BiDiBMenu(memo);
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
