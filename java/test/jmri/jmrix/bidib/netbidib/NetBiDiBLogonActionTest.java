package jmri.jmrix.bidib.netbidib;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitAppender;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NetBiDiBLogonAction class
 * 
 * @author  Eckart Meyer  Copyright (C) 2024
 */
public class NetBiDiBLogonActionTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor3() {
        NetBiDiBLogonAction t = new NetBiDiBLogonAction(memo, "Login-text", "Logoff-text");
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testCTor() {
        NetBiDiBLogonAction t = new NetBiDiBLogonAction();
        Assertions.assertNotNull(t, "exists");
        JUnitAppender.assertWarnMessage("no connection");
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
