// WaitHandler.java

package jmri.util;

import java.util.Calendar;

/**
 * Common utility class for handling the 
 * "spurious wakeup from wait()" 
 * problem described in the 
 * {@link java.lang.Object#wait(long)} JavaDocs
 *
 * Generally, when waiting for a notify() operation,
 * you need to provide a test that a valid notify
 * had happened due to e.g. a state change, etc.
<code><pre>
new WaitHandler(this, 120) {
    protected boolean wasSpurious() {
        return !(state == expectedNextState); 
    }
};
</pre></code>
 * 
 * Interrupting the thread leaves the wait early with
 * the interrupted flag set.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @version $Revision: 1.4 $
 */

public class WaitHandler {
    
    /**
     * Wait for a specified interval,
     * robustly handling "spurious wake"
     * @param self waiting Object
     * @param interval in milliseconds
     */
    public WaitHandler(Object self, long interval) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime+interval;
        
        // loop until interrupted, or non-spurious wake
        while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())){
            long wait = endTime - currentTime;
            try {
                synchronized(self) {
                    if (wait > 0)
                        self.wait(wait);
                    else
                        self.wait();
                        
                    if (!wasSpurious()) break;
                }
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); // retain if needed later
                break;  // and leave the wait now
            }    
        }
    }

    /**
     * Wait forever, robustly handling "spurious wake"
     * @param self waiting Object
     */
    public WaitHandler(Object self) {
        this(self, -1);
    }
    
    /**
     * Method to determine if a wake was
     * spurious or not.  By default, all
     * wakes are spurious and the full time will
     * elapse.  Override to provide a test (returning false)
     * for a non-spurious wake.
     */
    protected boolean wasSpurious() { return true; }

}

