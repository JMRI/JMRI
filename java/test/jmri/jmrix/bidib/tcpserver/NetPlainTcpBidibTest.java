package jmri.jmrix.bidib.tcpserver;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NetPlainTcpBidib class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class NetPlainTcpBidibTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        NetPlainTcpBidib t = new NetPlainTcpBidib(memo.getBiDiBTrafficController());
        Assertions.assertNotNull(t, "exists");
        t.stop();
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
