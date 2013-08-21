// UncaughtExceptionHandler.java

package jmri.util.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to log exceptions that rise to the top of 
 * threads, including to the top of 
 * the AWT event processing loop.
 *
 * Using code must install this with
<pre>
  Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
</pre>
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision$
 */

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public void uncaughtException(Thread t, Throwable e) {
    
        // see http://docs.oracle.com/javase/7/docs/api/java/lang/ThreadDeath.html
        if (e instanceof java.lang.ThreadDeath) return;
        
        log.error("Unhandled Exception: "+e, e);
    }
    
    static Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class.getName());
}
