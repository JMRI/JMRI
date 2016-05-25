// LogOutputWindowAction.java
package jmri.jmrit.log;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Open a window to recieve Log4J output
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version	$Revision$
 * @see jmri.util.JLogoutputFrame
 * @see jmri.util.JTextPaneAppender
 */
public class LogOutputWindowAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 3272363386014713812L;

    public LogOutputWindowAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        JFrame f = jmri.util.JLogoutputFrame.getInstance().getMainFrame();
        f.setVisible(true);
    }
}
