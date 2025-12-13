package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Calendar;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the jmri.util.WaitHandler class.
 *
 * These tests are inherently time based, and may fail on a very busy computer.
 * Some have been commented out so they don't run during normal production
 * testing.
 *
 * @author Bob Jacobsen Copyright 2003, 2009, 2010
 */
public class WaitHandlerTest {

    private volatile boolean flag1;
    private volatile boolean flag2;

    static final int THREAD_DELAY = 500;   // time to delay thread under test

    private volatile long startTime;
    private volatile long endTime;

    @Test
    public void testInlineWait() {
        startTime = Calendar.getInstance().getTimeInMillis();

        // delay the test thread itself
        WaitHandler t = new WaitHandler(this, 50);
        assertNotNull(t);

        // check how long it took
        endTime = Calendar.getInstance().getTimeInMillis();
        assertTrue( 50 <= endTime - startTime, "wait time long enough");
    }

    @Test
    public void testSeparateThreadWaiting() {
        flag1 = false;
        flag2 = false;
        startTime = -1;

        Thread t = new Thread() {
            @Override
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                WaitHandler t = new WaitHandler(this, THREAD_DELAY);
                Assertions.assertNotNull(t);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Seperate Thread Waiting Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");

        assertTrue( flag1, "started");

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        assertTrue( flag2, "ended");
        assertTrue( THREAD_DELAY <= endTime - startTime, "run time long enough");
    }

    @Test
    public void testInterrupt() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                WaitHandler t = new WaitHandler(this, THREAD_DELAY);
                assertNotNull(t);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Interupt Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");

        assertTrue( flag1, "started");

        // fire Interrupt while still running
        assertFalse( flag2, "still running");
        t.interrupt();
        assertTrue( THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime, "notify early enough");

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        assertTrue( flag2, "ended");
        assertTrue( THREAD_DELAY >= endTime - startTime, "ended early");
    }

    @Test
    @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL", justification = "There should only ever be one thread waiting for this method.")
    public void testSpuriousWake() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;

                WaitHandler wh = new WaitHandler(this, THREAD_DELAY) {
                    @Override
                    public boolean wasSpurious() {
                        return true;
                    }
                };
                assertNotNull(wh);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Spurious Wake Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");

        assertTrue( flag1, "started");

        assertFalse( flag2, "started");

        // fire spurious wake
        synchronized (t) {
            assertTrue( THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime, "notify early enough");
            t.notify();
        }

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        assertTrue( flag2, "ended");

        assertTrue( THREAD_DELAY <= endTime - startTime, "run time long enough");
    }

    @Test
    @SuppressFBWarnings(value = "NO_NOTIFY_NOT_NOTIFYALL", justification = "There should only ever be one thread waiting for this method.")
    @Disabled("disabled in JUnit 3 testing paradigm")
    public void testCheckMethod() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                new WaitHandler(this, THREAD_DELAY) {
                    @Override
                    public boolean wasSpurious() {
                        return false;
                    }
                };
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Crosscheck Test Method Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");
        assertTrue( flag1, "started");
        assertFalse( flag2, "still running");

        // fire intentional wake, which will pass test
        synchronized (t) {
            t.notify();
            assertTrue( THREAD_DELAY >= Calendar.getInstance().getTimeInMillis() - startTime, "notify early enough");
        }

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");
        assertTrue( flag2, "ended");

        if (THREAD_DELAY <= endTime - startTime) {
            fail("run time: " + THREAD_DELAY + "  not shortened: " + ( endTime - startTime));
        }
        assertTrue( THREAD_DELAY > endTime - startTime, "run time shortened");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WaitHandlerTest.class);

}
