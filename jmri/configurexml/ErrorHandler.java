package jmri.configurexml;

/**
 * Default operation for reporting errors while loading.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision: 1.1 $
 */
    
public class ErrorHandler {

    /**
     * Handle an error.
     * <p>
     * Default implementation formats and puts in log.
     */

    public void handle(ErrorMemo e) {
        String m = e.description;
        if (e.systemName!=null) m += ";\n System name \""+e.systemName+"\"";
        if (e.userName!=null && !e.userName.equals("")) m += ";\n User name \""+e.userName+"\"";
        if (e.operation!=null) m += ";\n while "+e.operation;
        if (e.adapter!=null) m += ";\n in adaptor of type "+e.adapter.getClass().getName();
        if (e.exception!=null) m += ";\n Exception: "+e.exception.toString();
        
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ErrorHandler.class.getName());
}

