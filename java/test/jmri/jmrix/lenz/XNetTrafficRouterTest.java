package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.TestCase;

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
public class XNetTrafficRouterTest extends TestCase {

    public XNetTrafficRouterTest(String s) {
        super(s);
    }

    public void testConnectAndSend() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation());

        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation()) {
            protected void connectionWarn() {
            }

            public void receiveLoop() {
            }

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

    public void testReceiveAndForward() {
        // create object
        XNetTrafficRouter router = new XNetTrafficRouter(new LenzCommandStation()) {
            protected void connectionWarn() {
            }

            public void receiveLoop() {
            }

            protected void portWarn(Exception e) {
            }
        };
        Assert.assertNotNull("exists", router);

        resetCount();
        // register a listener
        XNetListener l = new XNetListener() {
            public void message(XNetReply m) {
                incrementCount();
            }

            public void message(XNetMessage m) {
            }

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

    public void testConnectAndDisconnect() {
        // scaffold for upstream
        XNetInterfaceScaffold upstream = new XNetInterfaceScaffold(new LenzCommandStation()) {
            protected void connectionWarn() {
            }

            public void receiveLoop() {
            }

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

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetTrafficRouterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
