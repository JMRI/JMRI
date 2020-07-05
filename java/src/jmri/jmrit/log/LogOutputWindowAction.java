package jmri.jmrit.log;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Open a window to receive Log4J output.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @see jmri.util.JLogoutputFrame
 * @see jmri.util.JTextPaneAppender
 */
@API(status = MAINTAINED)
public class LogOutputWindowAction extends AbstractAction {

    public LogOutputWindowAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame f = jmri.util.JLogoutputFrame.getInstance().getMainFrame();
        f.setTitle(Bundle.getMessage("LogOutputTitle"));
        f.setVisible(true);
    }

}
