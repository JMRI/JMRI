package jmri.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Intermediate implementation of TestCase that
 * adds the ability to release control of the current thread 
 * temporarily
 *
 * @author	Bob Jacobsen - Copyright 2009
 * @version	$Revision: 1.1 $
 */
 
public class ThreadedTestCase extends TestCase {

    public ThreadedTestCase(String s) { super(s); }
    
    /** 
     * Release the current thread, allowing other 
     * threads to process
     */
	public synchronized void releaseThread() {
		try {
		    int priority = Thread.currentThread().getPriority(); 
		    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		    Thread.yield();
		    Thread.sleep(200);
		    Thread.currentThread().setPriority(priority);
			super.wait(100);
		}
		catch (InterruptedException e) {
		    Assert.fail("failed due to InterruptedException");
		}
	}

}
