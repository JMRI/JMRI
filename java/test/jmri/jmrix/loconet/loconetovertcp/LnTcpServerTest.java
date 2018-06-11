package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LnTcpServer class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class LnTcpServerTest {

    private LocoNetInterfaceScaffold lnis;

    @Test
    public void getInstanceTest() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        lnis.setSystemConnectionMemo(memo);
        memo.setLnTrafficController(lnis);
        Assert.assertNotNull("Server getInstance", LnTcpServer.getDefault());
        LnTcpServer.getDefault().disable();  // turn the server off after enabled during creation.
        memo.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        lnis = new LocoNetInterfaceScaffold();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

}
