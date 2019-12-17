package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Title: XNetTrafficRouterTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 */
public class XNetTrafficRouterTest {

    @Test
    public void testConnectAndSend() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation());

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation()) {
            @Override
            protected void connectionWarn() {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };
        Assert.assertNotNull("exists", router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // send a message
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);
        router.sendXNetMessage(m, null);

        // check receipt
        Assert.assertEquals("one message sent", 1, upstream.outbound.size());
        Assert.assertTrue(upstream.outbound.elementAt(0) == m);
    }

    static int count = 0;

    static void resetCount() {
        count = 0;
    }

    static void incrementCount() {
        count++;
    }

    @Test
    public void testReceiveAndForward() {
        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation()) {
            @Override
            protected void connectionWarn() {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };
        Assert.assertNotNull("exists", router);

        resetCount();
        // register a listener
        XNetListener l = new XNetListener() {
            @Override
            public void message(XNetReply m) {
                incrementCount();
            }

            @Override
            public void message(XNetMessage m) {
            }

            @Override
            public void notifyTimeout(XNetMessage m) {
            }
        };
        router.addXNetListener(~0, l);
        // send a message
        XNetReply m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);
        router.forwardReply(l, m);

        // check receipt
        Assert.assertEquals("one message sent", 1, count);
    }

    @Test
    public void testConnectAndDisconnect() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation()) {
            @Override
            protected void connectionWarn() {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation());
        Assert.assertNotNull("exists", router);

        // connect
        router.connect(upstream);
        Assert.assertTrue("connected", router.status());

        // disconnect
        router.disconnectPort(upstream);
        Assert.assertTrue("not connected", !router.status());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
