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
    public void testOneBufferedInputStreamPerSocket() throws IOException {
        try (ServerSocket gateway = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            Probe t = new Probe();
            t.connect(InetAddress.getLoopbackAddress().getHostAddress(), gateway.getLocalPort());
            try (Socket far = gateway.accept()) {
                far.getOutputStream().write(":SB020N9101020304;:SB020N9101020305;".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                far.getOutputStream().flush();

                // every caller gets the same stream, so what one buffers the next still reads
                java.io.DataInputStream in = t.getInputStream();
                Assert.assertSame("one stream for the socket", in, t.getInputStream());
                byte[] first = new byte[18];
                in.readFully(first);
                Assert.assertEquals(":SB020N9101020304;", new String(first, java.nio.charset.StandardCharsets.US_ASCII));
                byte[] second = new byte[18];
                t.getInputStream().readFully(second);
                Assert.assertEquals(":SB020N9101020305;", new String(second, java.nio.charset.StandardCharsets.US_ASCII));

                // a reconnection opens a new socket, with a stream of its own
                Socket old = t.socket();
                t.connect();
                try (Socket far2 = gateway.accept()) {
                    Assert.assertNotSame("new stream for the new socket", in, t.getInputStream());
                    far2.getOutputStream().write(":SB020N9101020306;".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                    far2.getOutputStream().flush();
                    byte[] third = new byte[18];
                    t.getInputStream().readFully(third);
                    Assert.assertEquals(":SB020N9101020306;", new String(third, java.nio.charset.StandardCharsets.US_ASCII));
                }
                old.close();
            }
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
