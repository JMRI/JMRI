package jmri.util;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

/**
 * Sends messages to one client of a server without making the layout thread
 * (Swing thread) wait for the client.
 * <p>
 * A message sent from the layout thread is queued and written by a thread of
 * this sender's own, which ends when the queue is empty. A message sent from
 * any other thread is written by that thread, after the messages already
 * queued, so the client gets the messages in the order they were sent.
 * <p>
 * A message sent with a key replaces a queued message with the same key, in
 * its place in the queue. Use a key for messages that each give the whole
 * state of one thing, such as a bean, so that a client that is behind gets
 * only the latest.
 * <p>
 * A client that stops reading fills its connection's buffers, and a write to
 * it then waits. The sender fails when a write fails or when the queued
 * messages pass a limit: it drops the queue, runs its failure action once, on
 * a thread of its own, and from then on refuses every message with an
 * {@link IOException}, so the server's usual handling closes the connection.
 */
public class ClientMessageSender {

    /**
     * Writes one message to the client.
     */
    @FunctionalInterface
    public interface Writer {

        /**
         * Write a message to the client, waiting if the client is behind.
         *
         * @param message the message
         * @throws IOException if the message can't be written
         */
        void write(@Nonnull String message) throws IOException;
    }

    /**
     * The default limit on the characters queued for one client: 4 MiB, far
     * more than a client that is reading falls behind by.
     */
    public static final int DEFAULT_MAX_QUEUED_CHARS = 4 * 1024 * 1024;

    private final String name;
    private final Writer writer;
    private final Runnable onFailure;
    private final int maxQueuedChars;

    /**
     * Held while writing to the client, so writes are made one at a time and
     * in order.
     */
    private final Object writeLock = new Object();

    /**
     * A message waiting to be written.
     */
    private static final class Entry {

        final Object key;
        String message;

        Entry(Object key, String message) {
            this.key = key;
            this.message = message;
        }
    }

    @GuardedBy("this")
    private final Deque<Entry> queue = new ArrayDeque<>();
    @GuardedBy("this")
    private final Map<Object, Entry> queuedByKey = new HashMap<>();
    @GuardedBy("this")
    private long queuedChars = 0;
    @GuardedBy("this")
    private boolean writerThreadRunning = false;
    @GuardedBy("this")
    private IOException failure = null;

    /**
     * Create a sender with the default limit on queued characters.
     *
     * @param name      the client's name, for the writing thread and the log
     * @param writer    writes one message to the client
     * @param onFailure run once, on a thread of its own, when the sender fails;
     *                  usually closes the connection
     */
    public ClientMessageSender(@Nonnull String name, @Nonnull Writer writer, @Nonnull Runnable onFailure) {
        this(name, writer, onFailure, DEFAULT_MAX_QUEUED_CHARS);
    }

    /**
     * Create a sender.
     *
     * @param name           the client's name, for the writing thread and the
     *                       log
     * @param writer         writes one message to the client
     * @param onFailure      run once, on a thread of its own, when the sender
     *                       fails; usually closes the connection
     * @param maxQueuedChars the sender fails when more than this many
     *                       characters are queued
     */
    public ClientMessageSender(@Nonnull String name, @Nonnull Writer writer, @Nonnull Runnable onFailure,
            int maxQueuedChars) {
        this.name = name;
        this.writer = writer;
        this.onFailure = onFailure;
        this.maxQueuedChars = maxQueuedChars;
    }

    /**
     * Send a message to the client.
     *
     * @param message the message
     * @throws IOException if the sender has failed, or if the message makes it
     *                     fail
     */
    public void send(@Nonnull String message) throws IOException {
        send(message, null);
    }

    /**
     * Send a message to the client, replacing a queued message with the same
     * key.
     *
     * @param message the message
     * @param key     the thing the message gives the whole state of, or null
     *                if the message must not replace another
     * @throws IOException if the sender has failed, or if the message makes it
     *                     fail
     */
    public void send(@Nonnull String message, @CheckForNull Object key) throws IOException {
        if (ThreadingUtil.isLayoutThread()) {
            queue(message, key);
            return;
        }
        synchronized (this) {
            throwIfFailed(); // without waiting for a write that is held up
        }
        synchronized (writeLock) {
            writeQueued(); // the messages queued earlier go first
            synchronized (this) {
                throwIfFailed();
            }
            write(message);
        }
    }

    /**
     * Tell whether the sender has failed.
     *
     * @return true if the sender has failed and refuses messages
     */
    public synchronized boolean isFailed() {
        return failure != null;
    }

    private void queue(@Nonnull String message, @CheckForNull Object key) throws IOException {
        boolean startWriterThread;
        synchronized (this) {
            throwIfFailed();
            Entry entry = (key == null) ? null : queuedByKey.get(key);
            if (entry != null) {
                queuedChars += message.length() - entry.message.length();
                entry.message = message;
            } else {
                entry = new Entry(key, message);
                queue.add(entry);
                if (key != null) {
                    queuedByKey.put(key, entry);
                }
                queuedChars += message.length();
            }
            if (queuedChars > maxQueuedChars) {
                log.warn("Client {} is not reading: {} characters are waiting to be sent. Closing it.", name,
                        queuedChars);
                fail(new IOException("Client " + name + " is not reading"));
                throwIfFailed();
            }
            startWriterThread = !writerThreadRunning;
            writerThreadRunning = true;
        }
        if (startWriterThread) {
            Thread thread = ThreadingUtil.newThread(this::writeUntilEmpty, name + " sender");
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Write the queued messages until the queue is empty, then end. Runs on
     * the sender's own thread.
     */
    private void writeUntilEmpty() {
        while (true) {
            synchronized (writeLock) {
                try {
                    writeQueued();
                } catch (IOException ex) {
                    log.debug("Client {}: {}", name, ex.getMessage());
                }
            }
            synchronized (this) {
                if (queue.isEmpty()) {
                    writerThreadRunning = false;
                    return;
                }
            }
        }
    }

    /**
     * Write the queued messages. Call holding {@link #writeLock}.
     *
     * @throws IOException if a write fails
     */
    private void writeQueued() throws IOException {
        while (true) {
            Entry entry;
            synchronized (this) {
                entry = queue.poll();
                if (entry == null) {
                    return;
                }
                if (entry.key != null) {
                    queuedByKey.remove(entry.key);
                }
                queuedChars -= entry.message.length();
            }
            write(entry.message);
        }
    }

    /**
     * Write one message. Call holding {@link #writeLock}.
     */
    private void write(@Nonnull String message) throws IOException {
        try {
            writer.write(message);
        } catch (IOException ex) {
            synchronized (this) {
                fail(ex);
            }
            throw ex;
        }
    }

    @GuardedBy("this")
    private void fail(@Nonnull IOException ex) {
        if (failure != null) {
            return;
        }
        failure = ex;
        queue.clear();
        queuedByKey.clear();
        queuedChars = 0;
        Thread thread = ThreadingUtil.newThread(onFailure, name + " close");
        thread.setDaemon(true);
        thread.start();
    }

    @GuardedBy("this")
    private void throwIfFailed() throws IOException {
        if (failure != null) {
            throw new IOException("Client " + name + " has failed", failure);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClientMessageSender.class);

}
