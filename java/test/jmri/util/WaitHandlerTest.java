package jmri.util;

import java.util.Calendar;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.WaitHandler class.
 *
 * These tests are inherently time based, and may fail on a very busy computer.
 * Some have been commented out so they don't run during normal production
 * testing.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009, 2010
 */
public class WaitHandlerTest {

    static transient boolean flag1;
    static transient boolean flag2;

    static final int THREAD_DELAY = 200;   // time to delay thread under test
    static final int TEST_DELAY = THREAD_DELAY + 250;  // time to wait for thread to complete

    transient long startTime;
    transient long endTime;

    @Test
    public void testInlineWait() {
        long startTime = Calendar.getInstance().getTimeInMillis();

        // delay the test thread itself
        new WaitHandler(this, 50);

        // check how long it took
        long endTime = Calendar.getInstance().getTimeInMillis();
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
                new WaitHandler(this, THREAD_DELAY);
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
                new WaitHandler(this, THREAD_DELAY);
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
    public void testSpuriousWake() {
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
                        return true;
                    }
                };
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
    @Ignore("disabled in JUnit 3 testing paradigm")
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
            log.error("run time not shortened: " + (endTime - startTime));
        }
        Assert.assertTrue("run time shortened", THREAD_DELAY > endTime - startTime);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(WaitHandlerTest.class);

}
