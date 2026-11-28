package jmri.jmris;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.JmriConnection class 
 *
 * @author Paul Bender
 */
public class JmriConnectionTest {

    @Test
    public void testCtorwithStreamParameter() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) {
                    }
                });
        JmriConnection a = new JmriConnection(output);
        assertThat(a).isNotNull();
    }


    /**
     * A message sent from the layout thread does not wait for a client that
     * is not reading; it is written when the client reads again.
     */
    @Test
    public void testLayoutThreadDoesNotWaitForClient() {
        CountDownLatch reading = new CountDownLatch(1);
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        OutputStream client = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                try {
                    // a client that is not reading holds the write
                    if (!reading.await(10, TimeUnit.SECONDS)) {
                        throw new IOException("client never read");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(ex);
                }
                synchronized (received) {
                    received.write(b);
                }
            }
        };
        JmriConnection connection = new JmriConnection(new DataOutputStream(client));
        IOException[] thrown = new IOException[1];
        boolean[] clientReadBeforeReturn = new boolean[1];
        ThreadingUtil.runOnLayout(() -> {
            try {
                connection.sendMessage("hello");
            } catch (IOException ex) {
                thrown[0] = ex;
            }
            clientReadBeforeReturn[0] = reading.getCount() == 0;
        });
        assertThat(thrown[0]).isNull();
        assertThat(clientReadBeforeReturn[0]).as("returned before the client read").isFalse();

        reading.countDown();
        JUnitUtil.waitFor(() -> {
            synchronized (received) {
                return received.toString().equals("hello");
            }
        }, "message written once the client reads");
    }

    /**
     * A message to a WebSocket client is sent with the session's remote
     * endpoint.
     *
     * @throws IOException if the message can't be sent
     */
    @Test
    public void testWebSocketSend() throws IOException {
        org.eclipse.jetty.websocket.api.Session session = org.mockito.Mockito.mock(
                org.eclipse.jetty.websocket.api.Session.class);
        org.eclipse.jetty.websocket.api.RemoteEndpoint remote = org.mockito.Mockito.mock(
                org.eclipse.jetty.websocket.api.RemoteEndpoint.class);
        org.mockito.Mockito.when(session.isOpen()).thenReturn(true);
        org.mockito.Mockito.when(session.getRemote()).thenReturn(remote);
        org.mockito.Mockito.when(session.getRemoteAddress()).thenReturn(
                new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 12080));
        JmriConnection connection = new JmriConnection(session);
        connection.sendMessage("hello");
        org.mockito.Mockito.verify(remote).sendString("hello");
    }

    /**
     * A client whose connection fails while a message from the layout thread
     * is being sent is closed.
     */
    @Test
    public void testClosedWhenQueuedSendFails() {
        boolean[] closed = new boolean[1];
        OutputStream broken = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("broken");
            }

            @Override
            public void close() {
                closed[0] = true;
            }
        };
        JmriConnection connection = new JmriConnection(new DataOutputStream(broken));
        ThreadingUtil.runOnLayout(() -> {
            try {
                connection.sendMessage("hello");
            } catch (IOException ex) {
                // not expected here: the failure comes later, on the sender's thread
            }
        });
        JUnitUtil.waitFor(() -> closed[0], "connection closed");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

}
