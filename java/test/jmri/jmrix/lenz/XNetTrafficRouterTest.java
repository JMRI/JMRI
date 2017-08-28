package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.TestCase;
import org.junit.Assert;

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

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetTrafficRouterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
