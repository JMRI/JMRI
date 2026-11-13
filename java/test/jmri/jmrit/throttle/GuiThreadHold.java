package jmri.jmrit.throttle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jmri.util.ThreadingUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Holds the GUI thread (Swing thread) for a test, so that a change made to a
 * Swing component while it is held can only have been made off it.
 */
class GuiThreadHold implements AutoCloseable {

    private final CountDownLatch release = new CountDownLatch(1);

    GuiThreadHold() throws InterruptedException {
        CountDownLatch held = new CountDownLatch(1);
        ThreadingUtil.runOnGUIEventually(() -> {
            held.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(held.await(10, TimeUnit.SECONDS), "GUI thread held");
    }

    /**
     * Let the GUI thread go on.
     */
    @Override
    public void close() {
        release.countDown();
    }
}
