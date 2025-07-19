package jmri.jmrix.bidib;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull(t);
        
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
