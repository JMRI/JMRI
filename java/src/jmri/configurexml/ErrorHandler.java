package jmri.configurexml;

import org.apache.log4j.Logger;

/**
 * Default operation for reporting errors while loading.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision$
 */
    
public class ErrorHandler {

    /**
     * Handle an error.
     * <p>
     * Default implementation formats and puts in log.
     */

    public void handle(ErrorMemo e) {
        String m = e.description;
        if (e.systemName!=null) m += " System name \""+e.systemName+"\"";
        if (e.userName!=null && !e.userName.equals("")) m += " User name \""+e.userName+"\"";
        if (e.operation!=null) m += " while "+e.operation;
        if (e.adapter!=null) m += " in adaptor of type "+e.adapter.getClass().getName();
        if (e.exception!=null) m += " Exception: "+e.exception.toString();
        
        if (e.exception != null) log.log(e.level, m, e.exception);
        else log.log(e.level, m);
    }
    
    /**
     * Invoked when operation complete.
     *<p>
     * Default implementation doesn't do anything
     * here, everything already logged above.
     */
    public void done() {}
    
    // initialize logging
    static Logger log = Logger.getLogger(ErrorHandler.class.getName());
}

