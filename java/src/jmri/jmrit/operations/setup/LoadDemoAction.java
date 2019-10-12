package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.ExceptionContext;
import jmri.util.swing.ExceptionDisplayFrame;

/**
 * Swing action to load the operation demo files.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @author Gregory Madsen Copyright(C) 2012
 */
public class LoadDemoAction extends AbstractAction {

//    private final static Logger log = LoggerFactory.getLogger(LoadDemoAction.class);

    public LoadDemoAction(String s) {
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

        int results = JOptionPane.showConfirmDialog(null, Bundle.getMessage("AreYouSureDemoFiles"),
                Bundle.getMessage("LoadDemo"), JOptionPane.OK_CANCEL_OPTION);
        if (results != JOptionPane.OK_OPTION) {
            return;
        }

        AutoBackup backup = new AutoBackup();

        try {
            backup.autoBackup();

            backup.loadDemoFiles();

            // now deregister shut down task
            // If Trains window was opened, then task is active
            // otherwise it is normal to not have the task running
            InstanceManager.getDefault(OperationsManager.class).setShutDownTask(null);

            JOptionPane.showMessageDialog(null, Bundle.getMessage("YouMustRestartAfterLoadDemo"),
                    Bundle.getMessage("LoadDemoSuccessful"), JOptionPane.INFORMATION_MESSAGE);

            InstanceManager.getDefault(ShutDownManager.class).restart();

        } catch (IOException ex) {
            ExceptionContext context = new ExceptionContext(ex, Bundle.getMessage("LoadingDemoFiles"),
                    Bundle.getMessage("LoadingDemoMakeSure"));
            new ExceptionDisplayFrame(context, null).setVisible(true);
        }
    }
}


