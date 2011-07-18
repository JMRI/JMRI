// UncaughtExceptionHandler.java

package jmri.util.exceptionhandler;

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
        log.error("Unhandled Exception: "+e, e);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UncaughtExceptionHandler.class.getName());
}