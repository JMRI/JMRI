package jmri.configurexml.swing;

import javax.swing.JOptionPane;

/**
 * Swing dialog for reporting errors while loading.
 * Shows each one, could save until end if needed.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision: 1.1 $
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
        if (e.systemName!=null) m += "\n System name \""+e.systemName+"\"";
        if (e.userName!=null && !e.userName.equals("")) m += "\n User name \""+e.userName+"\"";
        if (e.operation!=null) m += "\n while "+e.operation;
        if (e.adapter!=null) m += "\n in adaptor of type "+e.adapter.getClass().getName();
        if (e.exception!=null) m += "\n Exception: "+e.exception.toString();
        
        if (e.level == org.apache.log4j.Level.ERROR) {
            JOptionPane.showMessageDialog(null,
                m, "Error during loading",
                JOptionPane.ERROR_MESSAGE);

        } else if (e.level == org.apache.log4j.Level.WARN) {
            JOptionPane.showMessageDialog(null,
                m, "Warning during loading",
                JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                m, "Message during loading",
                JOptionPane.PLAIN_MESSAGE);
        }
    
    }
    
    /**
     * Do nothing at end, already displayed
     */
    public void done() {}
}

