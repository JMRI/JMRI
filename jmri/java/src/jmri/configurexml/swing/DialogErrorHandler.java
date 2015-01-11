package jmri.configurexml.swing;

import java.awt.HeadlessException;

/**
 * Swing dialog for reporting errors while loading. Shows each one, could save
 * until end if needed.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 * @version $Revision$
 */
public class DialogErrorHandler extends jmri.configurexml.ErrorHandler {

    /**
     * Handle error by formatting and putting up a dialog box
     */
    public void handle(jmri.configurexml.ErrorMemo e) {
        // first, send to log
        super.handle(e);

        try {
            // then do dialog
            String m = "<html>" + e.description;
            if (e.systemName != null) {
                m += " System name \"" + e.systemName + "\"";
            }
            if (e.userName != null && !e.userName.equals("")) {
                m += "<br> User name \"" + e.userName + "\"";
            }
            if (e.operation != null) {
                m += "<br> while " + e.operation;
            }
            if (e.adapter != null) {
                m += "<br> in adaptor of type " + e.adapter.getClass().getName();
            }
            if (e.exception != null) {
                m += "<br> Exception: " + e.exception.toString();
            }
            m += "</html>";

            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage("Error during " + e.title, m, e.description, "", true, false);
        } catch (HeadlessException ex) {
            // silently do nothig - we can't display a dialog and have already
            // logged the error
        }
    }

    /**
     * Do nothing at end, already displayed
     */
    public void done() {
    }
}
