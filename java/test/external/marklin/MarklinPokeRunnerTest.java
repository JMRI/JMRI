package external.marklin;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for {@link MarklinPokeRunner} using a loopback ServerSocket.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class MarklinPokeRunnerTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /** Open a ServerSocket on a free port; return the port. */
    private static ServerSocket openServer() throws IOException {
        return new ServerSocket(0); // OS picks free port
    }

    /**
     * Echo server: accepts one client, echoes all bytes back until client disconnects.
     * Returns the port the server listens on.
     */
    private static int startEchoServer(ServerSocket server) {
        Thread t = new Thread(() -> {
            try (Socket client = server.accept()) {
                byte[] buf = new byte[4096];
                int n;
                OutputStream out = client.getOutputStream();
                while ((n = client.getInputStream().read(buf)) != -1) {
                    out.write(buf, 0, n);
                    out.flush();
                }
            } catch (IOException e) {
                // expected when client closes
            }
        });
        t.setDaemon(true);
        t.start();
        return server.getLocalPort();
    }

    @Test
    void run_sendsFramesAndPrintsThemToOut() throws Exception {
        try (ServerSocket server = openServer()) {
            int echoPort = startEchoServer(server);

            byte[] frame = new byte[13]; // all zeros
            MarklinPoke.Args args = new MarklinPoke.Args(
                "localhost", echoPort, List.of(frame), 0L, 1);

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            MarklinPokeRunner runner = new MarklinPokeRunner(args,
                new PrintStream(outBuf), System.err);
            runner.run();

            String out = outBuf.toString();
            assertTrue(out.contains(">"), "sent frame must appear with > prefix");
            assertTrue(out.contains("<"), "echoed frame must appear with < prefix");
        }
    }

    @Test
    void run_connect_returnsFalse_whenBridgeNotReachable() {
        MarklinPoke.Args args = new MarklinPoke.Args(
            "localhost", 19999, List.of(new byte[13]), 0L, 1);
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        MarklinPokeRunner runner = new MarklinPokeRunner(args,
            System.out, new PrintStream(errBuf));
        assertFalse(runner.connect(), "connect to unreachable port must return false");
        assertTrue(errBuf.toString().contains("localhost:19999"),
            "error message must name host:port");
        assertTrue(runner.socket == null || runner.socket.isClosed(),
            "socket must be closed/null after failed connect (no file descriptor leak)");
    }

    @Test
    void run_frameAccumulationHandlesPartialReads() throws Exception {
        try (ServerSocket server = openServer()) {
            int port = server.getLocalPort();

            // Server: sends 5 bytes, sleeps 50ms, sends remaining 8 bytes of a 13-byte frame
            CountDownLatch accepted = new CountDownLatch(1);
            AtomicReference<byte[]> received = new AtomicReference<>();
            Thread serverThread = new Thread(() -> {
                try (Socket client = server.accept()) {
                    accepted.countDown();
                    // Read and discard the incoming frame
                    byte[] inBuf = new byte[13];
                    int total = 0;
                    while (total < 13) {
                        int n = client.getInputStream().read(inBuf, total, 13 - total);
                        if (n < 0) break;
                        total += n;
                    }
                    received.set(inBuf);
                    // Send back in two pieces: 5 bytes, then 8 bytes
                    byte[] response = new byte[13];
                    response[0] = (byte) 0xAB;
                    response[12] = (byte) 0xCD;
                    client.getOutputStream().write(response, 0, 5);
                    client.getOutputStream().flush();
                    Thread.sleep(50);
                    client.getOutputStream().write(response, 5, 8);
                    client.getOutputStream().flush();
                    Thread.sleep(200); // give runner time to read
                } catch (Exception e) {
                    // expected on close
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            byte[] frame = new byte[13];
            MarklinPoke.Args args = new MarklinPoke.Args(
                "localhost", port, List.of(frame), 0L, 1);

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            MarklinPokeRunner runner = new MarklinPokeRunner(args,
                new PrintStream(outBuf), System.err);
            runner.run();

            String out = outBuf.toString();
            // Count the number of "< " lines  -  should be exactly 1 (one 13-byte frame reassembled)
            long receivedLines = out.lines()
                .filter(l -> l.startsWith("< "))
                .count();
            assertEquals(1, receivedLines,
                "partial TCP read must be reassembled into exactly one received frame");
        }
    }

    @Test
    void run_outputFormat_sentFrameHasCorrectPrefix() throws Exception {
        try (ServerSocket server = openServer()) {
            int echoPort = startEchoServer(server);

            byte[] frame = new byte[13];
            frame[0] = (byte) 0xAB;
            frame[12] = (byte) 0xCD;
            MarklinPoke.Args args = new MarklinPoke.Args(
                "localhost", echoPort, List.of(frame), 0L, 0);

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            MarklinPokeRunner runner = new MarklinPokeRunner(args,
                new PrintStream(outBuf), System.err);
            runner.run();

            String out = outBuf.toString();
            // Sent line should start with "> +0000ms" for first frame
            assertTrue(out.lines().anyMatch(l -> l.startsWith("> +0000ms")),
                "first sent frame must have +0000ms offset");
            // And contain AB at start and CD at end
            assertTrue(out.lines()
                    .filter(l -> l.startsWith("> "))
                    .anyMatch(l -> l.contains("AB") && l.contains("CD")),
                "sent frame must contain frame bytes in uppercase hex");
        }
    }
}
