package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Calendar;

/**
 * Common utility class for handling the "spurious wakeup from wait()" problem
 * documented in {@link java.lang.Object#wait(long)}.
 *
 * Generally, when waiting for a notify() operation, you need to provide a test
 * that a valid notify had happened due to a state change or other .
 * <pre><code>
 * new WaitHandler(this, 120) {
 * protected boolean wasSpurious() {
 * return !(state == expectedNextState);
 * }
 * };
 * </code></pre>
 *
 * By default, interrupting the thread leaves the wait early with the
 * interrupted flag set. InterruptedException is not thrown. You can modify this
 * behavior via the handleInterruptedException routine.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class WaitHandler {

    /**
     * Wait for a specified interval, robustly handling "spurious wake"
     *
     * @param self     waiting Object
     * @param interval in milliseconds
     */
    public WaitHandler(Object self, long interval) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime + interval;

        // loop until interrupted, or non-spurious wake
        while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())) {
            long wait = endTime - currentTime;
            try {
                synchronized (self) {
                    self.wait(wait);

                    if (!wasSpurious()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                break;  // and leave the wait now
            }
        }
    }

    /**
     * Wait forever, robustly handling "spurious wake"
     *
     * @param self waiting Object
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "unguarded wait() used intentionally here as part of utility class")
    public WaitHandler(Object self) {
        // loop until interrupted, or non-spurious wake
        while (true) {
            try {
                synchronized (self) {
                    self.wait();

                    if (!wasSpurious()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                if (handleInterruptedException(e)) {
                    break;
                }
            }
        }
    }

    /**
     * Method to determine if a wake was spurious or not. By default, all wakes
     * are considered not spurious and the full time may not elapse. Override to
     * provide a test (returning true) when there's a way to tell that a wake
     * was spurious and the wait() should continue.
     *
     * @return false unless overridden by a subclass
     */
    protected boolean wasSpurious() {
        return false;
    }

    /**
     * Define interrupt processing.
     *
     * By default, just records and leaves the wait early.
     *
     * @param e the exception to handle
     * @return true if should break out of wait
     */
    boolean handleInterruptedException(InterruptedException e) {
        Thread.currentThread().interrupt(); // retain if needed later
        return true;  // and leave the wait now
    }
}
