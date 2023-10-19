package jmri.jmrix.bidib.tcpserver;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.bidib package
 *
 * @author  Eckart Meyer  Copyright (C) 2023
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.bidib.tcpserver.BiDiBMessageReceiverTest.class,
    jmri.jmrix.bidib.tcpserver.NetPlainTcpBidibTest.class,
    jmri.jmrix.bidib.tcpserver.ServerMessageReceiverTest.class,
    jmri.jmrix.bidib.tcpserver.TcpServerActionTest.class,
    jmri.jmrix.bidib.tcpserver.TcpServerNetMessageHandlerTest.class,
    jmri.jmrix.bidib.tcpserver.TcpServerStartupActionFactoryTest.class,
    jmri.jmrix.bidib.tcpserver.TcpServerTest.class
})

public class PackageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
