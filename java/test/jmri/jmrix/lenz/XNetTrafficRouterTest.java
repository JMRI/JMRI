package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen Copyright 2002
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
        assertNotNull( router, "exists");

        // connect
        router.connect(upstream);
        assertTrue( router.status(), "connected");

        // send a message
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);
        router.sendXNetMessage(m, null);

        // check receipt
        assertEquals( 1, upstream.outbound.size(), "one message sent");
        assertSame(upstream.outbound.elementAt(0), m);
    }

    private static int count = 0;

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
        assertNotNull( router, "exists");

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
        assertEquals( 1, count, "one message sent");
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
        assertNotNull( router, "exists");

        // connect
        router.connect(upstream);
        assertTrue( router.status(), "connected");

        // disconnect
        router.disconnectPort(upstream);
        assertFalse(router.status());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
