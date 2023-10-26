package jmri.jmrix.bidib.tcpserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;
import org.junit.Assert;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;

/**
 * Tests for the TcpServer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerTest {
    
    BiDiBSystemConnectionMemo memo;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testCTor() {
        TcpServer t = new TcpServer(memo);
        Assert.assertNotNull("exists",t);
    }
    
}
