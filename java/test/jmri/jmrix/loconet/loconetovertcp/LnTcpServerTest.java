package jmri.jmrix.loconet.loconetovertcp;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Tests for LnTcpServer class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerTest {

    LocoNetSystemConnectionMemo memo;
    
    @Test
    public void getInstanceTest() {
        Assert.assertNotNull("Server getInstance", LnTcpServer.getDefault());
        LnTcpServer.getDefault().disable();  // turn the server off after enabled during creation.
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = Mockito.mock(LocoNetSystemConnectionMemo.class);
        // ensure memo exists in order to later use InstanceManager.getDefault()
        InstanceManager.store(memo,LocoNetSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
