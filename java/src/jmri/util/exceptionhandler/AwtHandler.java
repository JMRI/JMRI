// AwtHandler.java

package jmri.util.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to log exceptions that rise to the top of 
 * the AWT event processing loop.
 *
 * Using code must install this with
<pre>
  System.setProperty("sun.awt.exception.handler", jmri.util.AwtHandler.class.getName());
</pre>
 * @author Bob Jacobsen  Copyright 2010
 * @version $Revision$
 */

public class AwtHandler {

    public void handle(Throwable t) {
        log.error("Unhandled AWT Exception: "+t, t);
    }
    
    static Logger log = LoggerFactory.getLogger(AwtHandler.class.getName());
}
