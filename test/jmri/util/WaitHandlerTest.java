// WaitHandlerTest.java

package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import java.util.Calendar;

/**
 * Tests for the jmri.util.FileUtil class.
 * @author	Bob Jacobsen  Copyright 2003, 2009
 * @version	$Revision: 1.2 $
 */
public class WaitHandlerTest extends TestCase {
    static transient boolean flag1;
    static transient boolean flag2;

    public void testInline() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        new WaitHandler(this, 50);
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("wait time long enough", 50 <= endTime-startTime);
    }
    

    public void testSeparate() {
        long beginTime = Calendar.getInstance().getTimeInMillis();
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                flag1 = true;
                new WaitHandler(this, 50);
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long startTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);
        Assert.assertTrue("start delay short enough", 50 > startTime-beginTime);

        for (int i = 1; i < 100; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("run time long enough", 50 <= endTime-beginTime);
    }

    public void testInterrupt() {
        long beginTime = Calendar.getInstance().getTimeInMillis();
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                flag1 = true;
                new WaitHandler(this, 50);
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long startTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);
        Assert.assertTrue("start delay short enough", 50 > startTime-beginTime);

        // fire Interrupt
        Assert.assertTrue("notify early enough", 50 > Calendar.getInstance().getTimeInMillis() - startTime);
        t.interrupt();
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("ended early", 50 >= endTime-beginTime);
    }

    public void testSpuriousWake() {
        long beginTime = Calendar.getInstance().getTimeInMillis();
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                flag1 = true;
                new WaitHandler(this, 50);
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long startTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);
        Assert.assertTrue("start delay short enough", 50 > startTime-beginTime);

        // fire spurious wake
        synchronized (t) {
            Assert.assertTrue("notify early enough", 50 > Calendar.getInstance().getTimeInMillis() - startTime);
            t.notify();
        }
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("run time long enough", 50 <= endTime-beginTime);
    }

    public void testCheckMethod() {
        long beginTime = Calendar.getInstance().getTimeInMillis();
        flag1 = false;
        flag2 = false;
        Thread t = new Thread(){
            public void run() {
                flag1 = true;
                new WaitHandler(this, 100) {
                    public boolean wasSpurious() { return false; }
                };
                flag2 = true;
            }
        };
        t.start();
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 1 for start
            if (flag1) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long startTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("started", flag1);
        Assert.assertTrue("still running", !flag2);
        Assert.assertTrue("start delay short enough", 50 > startTime-beginTime);

        // fire intentional wake, which will pass test
        synchronized (t) {
            Assert.assertTrue("notify early enough", 50 > Calendar.getInstance().getTimeInMillis() - startTime);
            t.notify();
        }
        
        for (int i = 1; i < 100; i++) {
            // wait on flag 2 for end
            if (flag2) break;
            JUnitUtil.releaseThread(this, 1);
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        Assert.assertTrue("ended", flag2);

        Assert.assertTrue("run time shortened", 100 > endTime-beginTime);
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

	 static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WaitHandlerTest.class.getName());

}
