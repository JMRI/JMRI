// ResetAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

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
	static ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

	public ResetAction(String s) {
		super(s);
	}

	public void actionPerformed(ActionEvent e) {
		// check to see if files are dirty
		if (OperationsXml.areFilesDirty()) {
			if (JOptionPane.showConfirmDialog(null,
					rb.getString("OperationsFilesModified"),
					rb.getString("SaveOperationFiles"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				OperationsXml.save();
			}
		}

		int results = JOptionPane.showConfirmDialog(null,
				rb.getString("AreYouSureDeleteAll"),
				rb.getString("ResetOperations"), JOptionPane.OK_CANCEL_OPTION);
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

			JOptionPane.showMessageDialog(null,
					rb.getString("YouMustRestartAfterReset"),
					rb.getString("ResetSuccessful"),
					JOptionPane.INFORMATION_MESSAGE);

			Apps.handleRestart();

		} catch (Exception ex) {
			UnexpectedExceptionContext context = new UnexpectedExceptionContext(
					ex, "Deleting Operations files");
			new ExceptionDisplayFrame(context);
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(ResetAction.class.getName());
}

/* @(#)ResetAction.java */