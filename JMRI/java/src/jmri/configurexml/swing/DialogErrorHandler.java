package jmri.configurexml.swing;

import java.awt.HeadlessException;

/**
 * Swing dialog for reporting errors while loading. Shows each one, could save
 * until end if needed.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class DialogErrorHandler extends jmri.configurexml.ErrorHandler {

    /**
     * Handle error by formatting and putting up a dialog box
     *
     * @param e the error memo
     */
    @Override
    public void handle(jmri.configurexml.ErrorMemo e) {
        // first, send to log
        super.handle(e);

        try {
            // then do dialog
            StringBuilder m = new StringBuilder("<html>").append(e.description);
            if (e.systemName != null) {
                m.append(" System name \"").append(e.systemName).append("\"");
            }
            if (e.userName != null && !e.userName.isEmpty()) {
                m.append("<br> User name \"").append(e.userName).append("\"");
            }
            if (e.operation != null) {
                m.append("<br> while ").append(e.operation);
            }
            if (e.adapter != null) {
                m.append("<br> in adaptor of type ").append(e.adapter.getClass().getName());
            }
            if (e.exception != null) {
                m.append("<br> Exception: ").append(e.exception.toString());
            }
            m.append("<br> See http://jmri.org/help/en/package/jmri/configurexml/ErrorHandler.shtml for more information.</html>");

            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage("Error during " + e.title, m.toString(), e.description, "", true, false);
        } catch (HeadlessException ex) {
            // silently do nothig - we can't display a dialog and have already
            // logged the error
        }
    }

    /**
     * Do nothing at end, already displayed
     */
    @Override
    public void done() {
    }
}
