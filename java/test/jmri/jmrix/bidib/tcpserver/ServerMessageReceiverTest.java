package jmri.jmrix.bidib.tcpserver;

import java.io.ByteArrayOutputStream;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.exception.ProtocolException;

import org.junit.jupiter.api.*;

/**
 * Tests for the ServerMessageReceiver class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class ServerMessageReceiverTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        ServerMessageReceiver t = new ServerMessageReceiver(memo.getBiDiBTrafficController()) {
            @Override
            public void publishResponse(ByteArrayOutputStream output) throws ProtocolException {
            }

            @Override
            public void removeNodeListener(NodeListener nodeListener) {
            }
        };
        Assertions.assertNotNull(t, "exists");
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
