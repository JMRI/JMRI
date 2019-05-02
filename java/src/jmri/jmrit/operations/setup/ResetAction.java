package jmri.jmrit.operations.setup;

import apps.Apps;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.ExceptionDisplayFrame;
import jmri.util.swing.UnexpectedExceptionContext;

/**
 * Swing action to load the operation demo files.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Gregory Madsen Copyright (C) 2012
 */
public class ResetAction extends AbstractAction {

    public ResetAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // check to see if files are dirty
        if (OperationsXml.areFilesDirty()) {
            if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("OperationsFilesModified"),
                    Bundle.getMessage("SaveOperationFiles"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                OperationsXml.save();
            }
        }

        int results = JOptionPane.showConfirmDialog(null, Bundle.getMessage("AreYouSureDeleteAll"),
                Bundle.getMessage("ResetOperations"), JOptionPane.OK_CANCEL_OPTION);
        if (results != JOptionPane.OK_OPTION) {
            return;
        }

        AutoBackup backup = new AutoBackup();

        try {
            backup.autoBackup();

            // now delete the operations files
            backup.deleteOperationsFiles();

            // now deregister shut down task
            // If Trains window was opened, then task is active
            // otherwise it is normal to not have the task running
            InstanceManager.getDefault(OperationsManager.class).setShutDownTask(null);

            JOptionPane.showMessageDialog(null, Bundle.getMessage("YouMustRestartAfterReset"),
                    Bundle.getMessage("ResetSuccessful"), JOptionPane.INFORMATION_MESSAGE);

            Apps.handleRestart();

        } catch (IOException ex) {
            UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
                    "Deleting Operations files"); // NOI18N
            new ExceptionDisplayFrame(context, null).setVisible(true);
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(ResetAction.class);
}


