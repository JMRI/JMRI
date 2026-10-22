package jmri.jmrix.dccpp.swing.messages;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.LongSupplier;

/** Timestamped DCC-EX messages, newest first, with expiry and a line limit; no Swing, thread-safe. */
public class MessageQueue {

    public static final int DEFAULT_EXPIRY_SECONDS = 30;
    public static final int DEFAULT_MAX_LINES = 10;

    private final LongSupplier clock;
    private final Deque<Entry> entries = new ArrayDeque<>();
    private int expirySeconds = DEFAULT_EXPIRY_SECONDS;
    private int maxLines = DEFAULT_MAX_LINES;

    public MessageQueue() {
        this(System::currentTimeMillis);
    }

    public MessageQueue(LongSupplier clock) {
        this.clock = clock;
    }

    public synchronized void add(String text) {
        entries.addFirst(new Entry(clock.getAsLong(), text));
        trim();
    }

    /** Unmodifiable copy of the messages, newest first. */
    public synchronized List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /** Remove messages older than the expiry time; true if any were removed. */
    public synchronized boolean expire() {
        if (expirySeconds <= 0) {
            return false;
        }
        long cutoff = clock.getAsLong() - expirySeconds * 1000L;
        boolean removed = false;
        while (!entries.isEmpty() && entries.peekLast().getTimeMillis() <= cutoff) {
            entries.removeLast();
            removed = true;
        }
        return removed;
    }

    public synchronized void clear() {
        entries.clear();
    }

    /** Seconds a message is kept; 0 keeps it until newer messages push it out. */
    public synchronized void setExpirySeconds(int seconds) {
        expirySeconds = Math.max(0, seconds);
    }

    public synchronized int getExpirySeconds() {
        return expirySeconds;
    }

    public synchronized void setMaxLines(int lines) {
        maxLines = Math.max(1, lines);
        trim();
    }

    public synchronized int getMaxLines() {
        return maxLines;
    }

    private void trim() {
        while (entries.size() > maxLines) {
            entries.removeLast();
        }
    }

    /** One received message. */
    public static final class Entry {

        private final long timeMillis;
        private final String text;

        Entry(long timeMillis, String text) {
            this.timeMillis = timeMillis;
            this.text = text;
        }

        public long getTimeMillis() {
            return timeMillis;
        }

        public String getText() {
            return text;
        }
    }

}
