package jmri.jmrix.bidib.tcpserver;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the TcpServer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        TcpServer t = new TcpServer(memo);
        Assertions.assertNotNull(t,"exists");
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
