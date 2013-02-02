// ResetAction.java

package jmri.jmrit.operations.setup;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.UnexpectedExceptionContext;
import jmri.jmrit.operations.trains.TrainsTableFrame;

import apps.Apps;

/**
 * Swing action to load the operation demo files.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision$
 */
public class ResetAction extends AbstractAction {

	public ResetAction(String s) {
		super(s);
	}

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
		if (results != JOptionPane.OK_OPTION)
			return;

		AutoBackup backup = new AutoBackup();

		try {
			backup.autoBackup();

			// now delete the operations files
			backup.deleteOperationsFiles();

			// now deregister shut down task
			// If Trains window was opened, then task is active
			// otherwise it is normal to not have the task running
			try {
				if (TrainsTableFrame.trainDirtyTask != null) {
					jmri.InstanceManager.shutDownManagerInstance().deregister(
							TrainsTableFrame.trainDirtyTask);
				}
			} catch (Exception ex) {
				log.debug("Unable to deregister Train Dirty Task");
			}

			JOptionPane.showMessageDialog(null, Bundle.getMessage("YouMustRestartAfterReset"),
					Bundle.getMessage("ResetSuccessful"), JOptionPane.INFORMATION_MESSAGE);

			Apps.handleRestart();

		} catch (Exception ex) {
			UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
					"Deleting Operations files"); // NOI18N
			new ExceptionDisplayFrame(context);
		}
	}

	static Logger log = Logger.getLogger(ResetAction.class
			.getName());
}

/* @(#)ResetAction.java */
