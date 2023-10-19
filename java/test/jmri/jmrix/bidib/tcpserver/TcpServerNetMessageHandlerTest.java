package jmri.jmrix.bidib.tcpserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;

/**
 * Tests for the TcpServerNetMessageHandler class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerNetMessageHandlerTest {
    
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
        TcpServerNetMessageHandler t = new TcpServerNetMessageHandler(r);
        Assert.assertNotNull("exists", t);
    }
    
}
