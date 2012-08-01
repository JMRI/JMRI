// ResetAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.UnexpectedExceptionContext;

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
		int results = JOptionPane.showConfirmDialog(null,
				rb.getString("AreYouSureDeleteAll"),
				rb.getString("ResetOperations"), JOptionPane.OK_CANCEL_OPTION);
		if (results != JOptionPane.OK_OPTION)
			return;

		AutoBackup backup = new AutoBackup();

		try {
			backup.autoBackup();
		} catch (Exception ex) {
			log.debug("Autobackup before Operations Reset", ex);
			// I really don't like to eat this exception, but it can be thrown
			// when there are no files to backup, such as after a previous
			// reset.
			// This should be fixed with a smarter auto backup that does nothing
			// if there are no files.
		}

		try {
			// now delete the operations files
			backup.deleteOperationsFiles();

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