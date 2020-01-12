package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClientRxHandlerTest {
    
    @Test
    public void testCTor() throws InterruptedException {
        ClientRxHandler t = new ClientRxHandler("127.0.0.1",new java.net.Socket());
        Assert.assertNotNull("exists",t);
        
        // clean up started thread
        t.interrupt();
        t.join();
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class, memo);
        memo.setDCCppTrafficController(new DCCppOverTcpPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()));
    }
    
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
    
    // private final static Logger log = LoggerFactory.getLogger(ClientRxHandlerTest.class);

}
