package jmri.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ClientMessageSender}.
 * <p>
 * A "client" here is a writer that records each message, and that can be
 * held at the start of a write until the test releases it, as a client that
 * has stopped reading holds a write.
 */
public class ClientMessageSenderTest {

    private final List<String> written = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger failures = new AtomicInteger();
    private CountDownLatch release; // held writes wait for this
    private CountDownLatch writing; // counted down as a held write starts
    private volatile boolean hold;

    private ClientMessageSender sender(int maxQueuedChars) {
        return new ClientMessageSender("Test", message -> {
            if (hold) {
                writing.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new IOException("not released");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(ex);
                }
            }
            written.add(message);
        }, failures::incrementAndGet, maxQueuedChars);
    }

    private void holdWrites() {
        release = new CountDownLatch(1);
        writing = new CountDownLatch(1);
        hold = true;
    }

    private void releaseWrites() {
        hold = false;
        release.countDown();
    }

    private void awaitHeldWrite() throws InterruptedException {
        assertTrue(writing.await(10, TimeUnit.SECONDS), "a write started");
    }

    private interface IoAction {

        void run() throws IOException;
    }

    /**
     * Run on the layout thread.
     *
     * @return the IOException thrown, or null
     */
    private static IOException onLayout(IoAction action) {
        IOException[] thrown = new IOException[1];
        ThreadingUtil.runOnLayout(() -> {
            try {
                action.run();
            } catch (IOException ex) {
                thrown[0] = ex;
            }
        });
        return thrown[0];
    }

    private static void waitForSenderThreadToEnd() {
        JUnitUtil.waitFor(() -> Thread.getAllStackTraces().keySet().stream()
                .noneMatch(t -> t.getName().equals("Test sender")), "sender thread ended");
    }

    @Test
    public void testOtherThreadWritesAtOnce() throws IOException {
        ClientMessageSender s = sender(1000);
        s.send("a");
        s.send("b");
        assertEquals(List.of("a", "b"), written);
    }

    @Test
    public void testLayoutThreadDoesNotWaitForClient() throws Exception {
        ClientMessageSender s = sender(1000);
        holdWrites();
        // the layout thread returns while the client holds the write
        onLayout(() -> s.send("a"));
        awaitHeldWrite();
        onLayout(() -> s.send("b"));
        assertTrue(written.isEmpty(), "nothing written yet");

        releaseWrites();
        JUnitUtil.waitFor(() -> written.size() == 2, "both written");
        assertEquals(List.of("a", "b"), written);
        waitForSenderThreadToEnd();
    }

    @Test
    public void testOtherThreadWritesAfterQueuedMessages() throws Exception {
        ClientMessageSender s = sender(1000);
        holdWrites();
        onLayout(() -> {
            s.send("a");
            s.send("b");
        });
        awaitHeldWrite();
        Thread other = new Thread(() -> {
            try {
                s.send("c");
            } catch (IOException ex) {
                fail(ex);
            }
        }, "other");
        other.start();
        releaseWrites();
        other.join(10000);
        assertFalse(other.isAlive(), "other thread finished");
        assertEquals(List.of("a", "b", "c"), written);
        waitForSenderThreadToEnd();
    }

    @Test
    public void testKeyReplacesQueuedMessageInItsPlace() throws Exception {
        ClientMessageSender s = sender(1000);
        holdWrites();
        onLayout(() -> s.send("first"));
        awaitHeldWrite(); // "first" is being written, the rest are queued
        Object sensor = new Object();
        onLayout(() -> {
            s.send("sensor active", sensor);
            s.send("other");
            s.send("sensor inactive", sensor);
        });
        releaseWrites();
        JUnitUtil.waitFor(() -> written.size() == 3, "three written");
        assertEquals(List.of("first", "sensor inactive", "other"), written);
        waitForSenderThreadToEnd();
    }

    @Test
    public void testFailsWhenQueuePassesLimit() throws Exception {
        ClientMessageSender s = sender(10);
        holdWrites();
        onLayout(() -> s.send("first"));
        awaitHeldWrite();
        onLayout(() -> s.send("0123456789")); // 10 queued: at the limit
        assertFalse(s.isFailed());
        assertNotNull(onLayout(() -> s.send("x")), "IOException when the limit is passed");
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("Client Test is not reading: 11 characters");
        assertTrue(s.isFailed());
        JUnitUtil.waitFor(() -> failures.get() == 1, "failure action run");

        assertThrows(IOException.class, () -> s.send("later"));
        releaseWrites();
        waitForSenderThreadToEnd();
        assertEquals(List.of("first"), written, "queued messages dropped");
        assertEquals(1, failures.get(), "failure action run once");
    }

    @Test
    public void testFailsWhenWriteFails() throws Exception {
        ClientMessageSender s = new ClientMessageSender("Test", message -> {
            throw new IOException("broken");
        }, failures::incrementAndGet, 1000);
        onLayout(() -> s.send("a"));
        JUnitUtil.waitFor(s::isFailed, "failed");
        JUnitUtil.waitFor(() -> failures.get() == 1, "failure action run");
        assertThrows(IOException.class, () -> s.send("b"));
        waitForSenderThreadToEnd();
        assertEquals(1, failures.get(), "failure action run once");
    }

    @Test
    public void testOtherThreadRefusedWhenWriteBeforeItFails() throws Exception {
        boolean[] fail = new boolean[1];
        holdWrites();
        ClientMessageSender s = new ClientMessageSender("Test", message -> {
            if (hold) {
                writing.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            if (fail[0]) {
                throw new IOException("broken");
            }
            written.add(message);
        }, failures::incrementAndGet, 1000);
        onLayout(() -> s.send("a"));
        awaitHeldWrite();
        IOException[] thrown = new IOException[1];
        Thread other = new Thread(() -> {
            try {
                s.send("b");
            } catch (IOException ex) {
                thrown[0] = ex;
            }
        }, "other");
        other.start();
        // "b" waits for the held write of "a"
        JUnitUtil.waitFor(() -> other.getState() == Thread.State.BLOCKED, "other thread waiting to write");
        fail[0] = true;
        releaseWrites(); // the write of "a" fails
        other.join(10000);
        assertFalse(other.isAlive(), "other thread finished");
        assertNotNull(thrown[0], "other thread refused");
        assertTrue(written.isEmpty(), "nothing written");
        waitForSenderThreadToEnd();
        JUnitUtil.waitFor(() -> failures.get() == 1, "failure action run once");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        if (release != null) {
            release.countDown();
        }
        JUnitUtil.tearDown();
    }

}
