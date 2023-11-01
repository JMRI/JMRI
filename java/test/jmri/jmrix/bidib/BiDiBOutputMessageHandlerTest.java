package jmri.jmrix.bidib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBOutputMessageHandler class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020-2023
 */
public class BiDiBOutputMessageHandlerTest {
    
    BiDiBSystemConnectionMemo memo;
    
    private class TestNamedBean implements BiDiBNamedBeanInterface {
        
        BiDiBAddress addr = new BiDiBAddress("BT42", 'T', memo);
        
        public TestNamedBean() {
        }
        
        @Override
        public BiDiBAddress getAddr() {
            return addr;
        }

        @Override
        public void nodeNew() {}

        @Override
        public void nodeLost() {}
    }

    @Test
    public void testCtor() {
        // infrastructure objects
        BiDiBOutputMessageHandler t = new BiDiBOutputMessageHandler(new TestNamedBean(), "T", memo.getBiDiBTrafficController());
        Assert.assertNotNull(t);
        
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
