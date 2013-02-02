// LogOutputWindowAction.java

package jmri.jmrit.log;

import org.apache.log4j.Logger;
import java.awt.event.*;
import javax.swing.*;

/**
 * Open a window to recieve Log4J output
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 * @see         jmri.util.JLogoutputFrame
 * @see         jmri.util.JTextPaneAppender
 */
public class LogOutputWindowAction extends AbstractAction {

    public LogOutputWindowAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        JFrame f = jmri.util.JLogoutputFrame.getInstance().getMainFrame();
        f.setVisible(true);
    }

    // initialize logging
    static Logger log = Logger.getLogger(LogOutputWindowAction.class.getName());
}
