package jmri.jmrix.bidib.tcpserver;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TcpServerAction class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerActionTest {
    
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
        TcpServerAction t = new TcpServerAction(memo, "Enabletext", "Disabletext");
        Assert.assertNotNull("exists",t);
    }
    
}
