package jmri.configurexml.swing;

/**
 * Swing dialog for reporting errors while loading.
 * Shows each one, could save until end if needed.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision$
 */
    
public class DialogErrorHandler extends jmri.configurexml.ErrorHandler {
    
    /**
     * Handle error by formatting and putting up a dialog box
     */
    public void handle(jmri.configurexml.ErrorMemo e) {
        // first, send to log
        super.handle(e);

        // then do dialog
        String m = e.description;
        if (e.systemName!=null) m += " System name \""+e.systemName+"\"";
        if (e.userName!=null && !e.userName.equals("")) m += "<br> User name \""+e.userName+"\"";
        if (e.operation!=null) m += " while "+e.operation;
        if (e.adapter!=null) m += " in adaptor of type "+e.adapter.getClass().getName();
        if (e.exception!=null) m += " Exception: "+e.exception.toString();

        if (e.level == org.apache.log4j.Level.ERROR) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage("Error during loading",m,e.description, "",true, false, e.level);
                            
        } else if (e.level == org.apache.log4j.Level.WARN) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage("Warning during loading",m,e.description, "", true, false, e.level);
        } else {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage("Message during loading",m,e.description, "",true, false, e.level);
        }
        
    }
    
    /**
     * Do nothing at end, already displayed
     */
    public void done() {}
}

