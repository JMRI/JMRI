package jmri.jmrit.operations.setup.backup;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.ExceptionDisplayFrame;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.UnexpectedExceptionContext;

/**
 * Swing action to load the operation demo files.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Gregory Madsen Copyright (C) 2012
 */
public class ResetAction extends AbstractAction {

    public ResetAction() {
        super(Bundle.getMessage("ResetOperations"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // check to see if files are dirty
        if (OperationsXml.areFilesDirty()) {
            if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("OperationsFilesModified"),
                    Bundle.getMessage("SaveOperationFiles"), JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                OperationsXml.save();
            }
        }

        int results = JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("AreYouSureDeleteAll"),
                Bundle.getMessage("ResetOperations"), JmriJOptionPane.OK_CANCEL_OPTION);
        if (results != JmriJOptionPane.OK_OPTION) {
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

            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("YouMustRestartAfterReset"),
                    Bundle.getMessage("ResetSuccessful"), JmriJOptionPane.INFORMATION_MESSAGE);

            try {
                InstanceManager.getDefault(jmri.ShutDownManager.class).restart();
            } catch (Exception er) {
                log.error("Continuing after error in handleRestart", er);
            }


        } catch (IOException ex) {
            UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
                    "Deleting Operations files"); // NOI18N
            ExceptionDisplayFrame.displayExceptionDisplayFrame(null, context);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResetAction.class);
}


