package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;
import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;

import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;

import jmri.util.JUnitUtil;
import org.bidib.jbidibc.net.serialovertcp.NetBidibServerPlainTcpPort;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the BiDiBMessageReceiver class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBMessageReceiverTest {
    
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
        ServerMessageReceiver r = new ServerMessageReceiver(memo.getBiDiBTrafficController()) {
            @Override
            public void publishResponse(ByteArrayOutputStream output) throws ProtocolException {
            }

            @Override
            public void removeNodeListener(NodeListener nodeListener) {
            }
        };
        Assert.assertNotNull("exists", r);
        TcpServerNetMessageHandler h = new TcpServerNetMessageHandler(r);
        Assert.assertNotNull("exists", h);
        try {
            NetBidibServerPlainTcpPort p = new NetBidibServerPlainTcpPort(42, null, h);
            BiDiBMessageReceiver t = new BiDiBMessageReceiver(h, p);
            Assert.assertNotNull("exists",t);
        }
        catch (Exception e) {}
    }

    
}
