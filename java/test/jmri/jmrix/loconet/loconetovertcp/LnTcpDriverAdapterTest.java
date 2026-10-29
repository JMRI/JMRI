package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnTcpDriverAdapterTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LnTcpDriverAdapter t = new LnTcpDriverAdapter();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testMemoCTor() {
        LnTcpDriverAdapter tm = new LnTcpDriverAdapter(memo);
        Assert.assertNotNull("exists", tm);
    }

    @Test
    public void testReconnectDefaultsOn() {
        LnTcpDriverAdapter a = new LnTcpDriverAdapter();
        Assert.assertTrue("automatic reconnect should be enabled by default",
                a.getAllowConnectionRecovery());
        Assert.assertEquals("default reconnect attempts should be unlimited",
                -1, a.getReconnectMaxAttempts());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(LnTcpDriverAdapterTest.class);

}
