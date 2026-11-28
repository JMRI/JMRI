package jmri.implementation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.TurnoutManager;
import jmri.util.ThreadingUtil;

/**
 * Sends the turnout commands requested through
 * {@link AbstractTurnout#setCommandedStateAtInterval(int)}, such as those of a
 * Route, a Matrix Signal Mast or Dispatcher, and the commands to the output
 * sensors of a Route, spaced by the turnout output interval, when the
 * connection's {@link jmri.SystemConnectionMemo#isOutputIntervalKept()}
 * setting is on.
 * <p>
 * Commands are sent in the order requested, turnouts and sensors in one queue,
 * so commands from Routes set at the same time are spaced apart too. Each is
 * sent no earlier than its output interval slot from
 * {@link TurnoutManager#outputIntervalEnds()}, and no sooner than one output
 * interval after the command sent before it. The interval is kept when each
 * command is sent, not only when it is scheduled, so commands held up by a
 * busy layout thread (Swing thread), for example while panels load at
 * start-up, still reach the layout spaced apart instead of all at once.
 * <p>
 * Commands are sent on the layout thread (Swing thread), apart from a turnout
 * command requested through {@link #send(Runnable)} when nothing is waiting
 * and no wait is due, which is sent at once on the caller's thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class OutputIntervalSender implements InstanceManagerAutoDefault {

    private static final class Pending {
        private final Runnable command;
        private final long dueNanos;

        private Pending(Runnable command, long dueNanos) {
            this.command = command;
            this.dueNanos = dueNanos;
        }
    }

    // guarded by this
    private final Deque<Pending> queue = new ArrayDeque<>();
    private long lastSentNanos;
    private boolean sentAny = false;
    private boolean drainScheduled = false;

    /**
     * Create the sender. Normally obtained from the InstanceManager.
     */
    public OutputIntervalSender() {
    }

    /**
     * Send a turnout command at the output interval. When nothing is waiting
     * and no wait is due, it is sent at once on the caller's thread.
     *
     * @param command sends the command to the layout
     */
    void send(@Nonnull Runnable command) {
        send(command, true);
    }

    /**
     * Send a command at the output interval, always on the layout thread
     * (Swing thread).
     *
     * @param command sends the command to the layout
     * @since 5.19.2
     */
    void sendOnLayout(@Nonnull Runnable command) {
        send(command, false);
    }

    private void send(@Nonnull Runnable command, boolean nowOnCallerThread) {
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        boolean now;
        synchronized (this) {
            // the slot is taken under this lock, so the queue is in slot order
            LocalDateTime slot = tm.outputIntervalEnds();
            long delayMs = Math.max(0L, LocalDateTime.now().until(slot, ChronoUnit.MILLIS));
            long time = System.nanoTime();
            Pending p = new Pending(command, time + delayMs * 1_000_000L);
            now = nowOnCallerThread && queue.isEmpty() && dueNanos(p, tm) <= time;
            if (now) {
                markSent(time);
            } else {
                queue.addLast(p);
            }
        }
        if (now) {
            log.debug("output interval has passed, sending now");
            command.run();
        } else {
            ThreadingUtil.runOnLayoutEventually(this::drain);
        }
    }

    /**
     * Send every command that is due, in order, and schedule the next.
     * Runs on the layout thread (Swing thread).
     */
    private void drain() {
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        while (true) {
            Pending p;
            synchronized (this) {
                p = queue.peekFirst();
                if (p == null) {
                    return;
                }
                long time = System.nanoTime();
                long due = dueNanos(p, tm);
                if (due > time) {
                    if (!drainScheduled) {
                        drainScheduled = true;
                        int waitMs = (int) Math.max(1L, (due - time + 999_999L) / 1_000_000L);
                        ThreadingUtil.runOnLayoutDelayed(() -> {
                            synchronized (this) {
                                drainScheduled = false;
                            }
                            drain();
                        }, waitMs);
                    }
                    return;
                }
                queue.removeFirst();
                markSent(time);
            }
            log.debug("sending command at output interval");
            p.command.run();
        }
    }

    // called holding the lock
    private long dueNanos(Pending p, TurnoutManager tm) {
        if (!sentAny) {
            return p.dueNanos;
        }
        long spaced = lastSentNanos + tm.getOutputInterval() * 1_000_000L;
        return Math.max(p.dueNanos, spaced);
    }

    // called holding the lock
    private void markSent(long time) {
        lastSentNanos = time;
        sentAny = true;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OutputIntervalSender.class);

}
