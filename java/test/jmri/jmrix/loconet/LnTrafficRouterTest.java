package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen Copyright (c) 2002
 */
public class LnTrafficRouterTest {

    @Test
    public void testConnectAndSend() {
        // scaffold for upstream
        LocoNetInterfaceScaffold upstream = new LocoNetInterfaceScaffold();

        // create object
        LnTrafficRouter router = new LnTrafficRouter(memo);
        memo.setLnTrafficController(router);

        Assert.assertEquals("router is tc", memo.getLnTrafficController(), router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // send a message
        LocoNetMessage m = new LocoNetMessage(3);
        router.sendLocoNetMessage(m);

        // check receipt
        Assert.assertEquals("one message sent", 1, upstream.outbound.size());
        Assert.assertTrue(upstream.outbound.elementAt(0) == m);
    }

    private int count = 0;

    @Test
    public void testReceiveAndForward() {
        // create object
        LnTrafficRouter router = new LnTrafficRouter(memo);
        memo.setLnTrafficController(router);
        Assert.assertEquals("router is tc", memo.getLnTrafficController(), router);

        count = 0;
        // register a listener
        LocoNetListener l = (LocoNetMessage m) -> {
            count++;
        };
        router.addLocoNetListener(~0, l);
        // send a message
        LocoNetMessage m = new LocoNetMessage(3);
        router.message(m);

        // check receipt
        Assert.assertEquals("one message sent", 1, count);
    }

    @Test
    public void testConnectAndDisconnect() {
        // scaffold for upstream
        LocoNetInterfaceScaffold upstream = new LocoNetInterfaceScaffold(memo);

        // create object
        LnTrafficRouter router = new LnTrafficRouter(memo);
        memo.setLnTrafficController(router);
        Assert.assertEquals("router is tc", memo.getLnTrafficController(), router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // disconnect
        router.disconnectPort(upstream);
        Assert.assertTrue("not connected", !router.status());
    }

    private LocoNetSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
