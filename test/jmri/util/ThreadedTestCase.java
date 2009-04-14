package jmri.util;

import junit.framework.Assert;

/**
 * Intermediate implementation of TestCase that
 * adds the ability to release control of the current thread 
 * temporarily
 *
 * @author	Bob Jacobsen - Copyright 2009
 * @version	$Revision: 1.4 $
 */
 
public class ThreadedTestCase extends InitializingTestCase {

    public ThreadedTestCase(String s) { super(s); }
    
    static int DEFAULTDELAY = 200;
    
    /** 
     * Release the current thread, allowing other 
     * threads to process
     */
	public synchronized void releaseThread(int delay) {
		try {
		    int priority = Thread.currentThread().getPriority(); 
		    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		    Thread.yield();
		    Thread.sleep(delay);
		    Thread.currentThread().setPriority(priority);
			super.wait(delay);
		}
		catch (InterruptedException e) {
		    Assert.fail("failed due to InterruptedException");
		}
	}

    public synchronized void waitSwingClear() {
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable(){ 
                public void run() {}
            });
        } catch (Exception e) {
		    Assert.fail("failed due to exception during deferral: "+e);
        }
    }
    
	public synchronized void releaseThread() {
	    releaseThread(DEFAULTDELAY);
    }
    
}
