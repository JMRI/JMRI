package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.net.serialovertcp.NetBidibServerPlainTcpPort;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBMessageReceiver class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBMessageReceiverTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        ServerMessageReceiver r = new ServerMessageReceiver(memo.getBiDiBTrafficController()) {
            @Override
            public void publishResponse(ByteArrayOutputStream output) throws ProtocolException {
            }

            @Override
            public void removeNodeListener(NodeListener nodeListener) {
            }
        };
        Assertions.assertNotNull(r, "r exists");
        TcpServerNetMessageHandler h = new TcpServerNetMessageHandler(r);
        Assertions.assertNotNull(h, "h exists");
        try {
            NetBidibServerPlainTcpPort p = new NetBidibServerPlainTcpPort(42, null, h);
            BiDiBMessageReceiver t = new BiDiBMessageReceiver(h, p);
            Assertions.assertNotNull(t, "t exists");
        }
        catch (Exception e) {}
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
