// WaitHandlerTest.java

package jmri.util;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.util.Calendar;

/**
 * Tests for the jmri.util.FileUtil class.
 *
 * These tests are inherently time based, and may fail 
 * on a very busy computer. Some have been commented out
 * so they don't run during normal production testing.
 *
 * @author	Bob Jacobsen  Copyright 2003, 2009, 2010
 * @version	$Revision$
 */
public class WaitHandlerTest extends TestCase {
    static transient boolean flag1;
    static transient boolean flag2;

    static final int THREAD_DELAY = 200;   // time to delay thread under test
    static final int TEST_DELAY = THREAD_DELAY+250;  // time to wait for thread to complete
    
    transient long startTime;
    transient long endTime;

    public void testInlineWait() {
        long startTime = Calendar.getInstance().getTimeInMillis();

        // delay the test thread itself
        new WaitHandler(this, 50);
        
        // check how long it took
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("wait time long enough", 50 <= endTime-startTime);
    }
    
    public void testSeparateThreadWaiting() {
        flag1 = false;
        flag2 = false;
        startTime = -1;
        
        Thread t = new Thread(){
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                new WaitHandler(this, THREAD_DELAY);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("started", flag1);

        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("ended", flag2);
        Assert.assertTrue("run time long enough", THREAD_DELAY <= endTime-startTime);
    }

    public void testInterrupt() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                new WaitHandler(this, THREAD_DELAY);
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("started", flag1);

        // fire Interrupt while still running
        Assert.assertTrue("still running", !flag2);
        t.interrupt();
        Assert.assertTrue("notify early enough", THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime);
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("ended", flag2);
        Assert.assertTrue("ended early", THREAD_DELAY >= endTime-startTime);
    }

    public void testSpuriousWake() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                new WaitHandler(this, THREAD_DELAY) {
                    @Override
                    public boolean wasSpurious() { return true; }
                };
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("started", flag1);

        Assert.assertTrue("still running", !flag2);

        // fire spurious wake
        synchronized (t) {
            Assert.assertTrue("notify early enough", THREAD_DELAY > Calendar.getInstance().getTimeInMillis() - startTime);
            t.notify();
        }
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("run time long enough", THREAD_DELAY <= endTime-startTime);
    }

    public void xtestCheckMethod() {
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                startTime = Calendar.getInstance().getTimeInMillis();
                flag1 = true;
                new WaitHandler(this, THREAD_DELAY) {
                    public boolean wasSpurious() { return false; }
                };
                endTime = Calendar.getInstance().getTimeInMillis();
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);

        // fire intentional wake, which will pass test
        synchronized (t) {
            t.notify();
            Assert.assertTrue("notify early enough", THREAD_DELAY >= Calendar.getInstance().getTimeInMillis() - startTime);
        }
        
        for (int i = 1; i < TEST_DELAY; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }

        Assert.assertTrue("ended", flag2);

        if (THREAD_DELAY <= endTime-startTime) log.error("run time not shortened: "+(endTime-startTime));
        Assert.assertTrue("run time shortened", THREAD_DELAY > endTime-startTime);
    }


	// from here down is testing infrastructure

	public WaitHandlerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {WaitHandlerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(WaitHandlerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

	 static Logger log = Logger.getLogger(WaitHandlerTest.class.getName());

}
