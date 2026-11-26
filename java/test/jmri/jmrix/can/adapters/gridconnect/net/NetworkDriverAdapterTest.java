package jmri.jmrix.can.adapters.gridconnect.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NetworkDriverAdapterTest {

    @Test
    public void testCTor() {
        NetworkDriverAdapter t = new NetworkDriverAdapter();
        Assert.assertNotNull("exists",t);
    }

    /** Gives the test the adapter's socket. */
    private static class Probe extends NetworkDriverAdapter {
        Socket socket() {
            return socketConn;
        }

        void close() {
            closeConnection();
        }
    }

    @Test
    public void testFramesNotHeldBackBySocket() throws IOException {
        try (ServerSocket gateway = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            Probe t = new Probe();
            t.connect(InetAddress.getLoopbackAddress().getHostAddress(), gateway.getLocalPort());
            Assert.assertTrue("connected", t.status());
            Assert.assertTrue("TCP no delay", t.socket().getTcpNoDelay());

            // a reconnection opens a new socket, set the same way
            Socket first = t.socket();
            t.connect();
            Assert.assertNotSame("new socket", first, t.socket());
            Assert.assertTrue("TCP no delay after reconnecting", t.socket().getTcpNoDelay());
            first.close();
            t.close();
        }
    }

    @Test
    public void testFramesGroupedWhenChosen() throws IOException {
        Probe d = new Probe();
        Assert.assertEquals("each frame at once by default", NetworkDriverAdapter.SEND_AT_ONCE,
            d.getOptionState(NetworkDriverAdapter.FRAME_SENDING_OPTION));
        Assert.assertFalse("not grouped by default", d.sendGrouped());

        try (ServerSocket gateway = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            Probe t = new Probe();
            t.setOptionState(NetworkDriverAdapter.FRAME_SENDING_OPTION, NetworkDriverAdapter.SEND_GROUPED);
            t.connect(InetAddress.getLoopbackAddress().getHostAddress(), gateway.getLocalPort());
            Assert.assertTrue("connected", t.status());
            Assert.assertFalse("TCP no delay off: frames grouped", t.socket().getTcpNoDelay());

            // a reconnection opens a new socket, set the same way
            Socket first = t.socket();
            t.connect();
            Assert.assertNotSame("new socket", first, t.socket());
            Assert.assertFalse("TCP no delay still off after reconnecting", t.socket().getTcpNoDelay());
            first.close();
            t.close();
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(NetworkDriverAdapterTest.class);

}
