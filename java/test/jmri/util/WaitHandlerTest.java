package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Calendar;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    private transient boolean flag1;
    private transient boolean flag2;

    static final int THREAD_DELAY = 500;   // time to delay thread under test

    private transient long startTime;
    private transient long endTime;

    @Test
    public void testInlineWait() {
        startTime = Calendar.getInstance().getTimeInMillis();

        // delay the test thread itself
        WaitHandler t = new WaitHandler(this, 50);
        Assertions.assertNotNull(t);

        // check how long it took
        endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("wait time long enough", 50 <= endTime - startTime);
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

        Assert.assertTrue("started", flag1);

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        Assert.assertTrue("ended", flag2);
        Assert.assertTrue("run time long enough", THREAD_DELAY <= endTime - startTime);
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
                Assertions.assertNotNull(t);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Interupt Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");

        Assert.assertTrue("started", flag1);

        // fire Interrupt while still running
        Assert.assertTrue("still running", !flag2);
        t.interrupt();
        Assert.assertTrue("notify early enough", THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime);

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        Assert.assertTrue("ended", flag2);
        Assert.assertTrue("ended early", THREAD_DELAY >= endTime - startTime);
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
                Assertions.assertNotNull(wh);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.setName("Spurious Wake Test Thread");
        t.start();

        JUnitUtil.waitFor(()->{return flag1;},"flag1 not set");

        Assert.assertTrue("started", flag1);

        Assert.assertTrue("still running", !flag2);

        // fire spurious wake
        synchronized (t) {
            Assert.assertTrue("notify early enough", THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime);
            t.notify();
        }

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");

        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("run time long enough", THREAD_DELAY <= endTime - startTime);
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
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);

        // fire intentional wake, which will pass test
        synchronized (t) {
            t.notify();
            Assert.assertTrue("notify early enough", THREAD_DELAY >= Calendar.getInstance().getTimeInMillis() - startTime);
        }

        JUnitUtil.waitFor(()->{return flag2;},"flag2 not set");
        Assert.assertTrue("ended", flag2);

        if (THREAD_DELAY <= endTime - startTime) {
            Assert.fail("run time: " + THREAD_DELAY + "  not shortened: " + ( endTime - startTime));
        }
        Assert.assertTrue("run time shortened", THREAD_DELAY > endTime - startTime);
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WaitHandlerTest.class);

}
