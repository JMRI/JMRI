// LoadDemoAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.ExceptionContext;
import jmri.jmrit.operations.ExceptionDisplayFrame;

import apps.Apps;

/**
 * Swing action to load the operation demo files.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @author Gregory Madsen Copyright(C) 2012
 * @version $Revision$
 */
public class LoadDemoAction extends AbstractAction {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(LoadDemoAction.class.getName());

	static ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

	public LoadDemoAction(String s) {
		super(s);
	}

	public void actionPerformed(ActionEvent e) {
		int results = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to load the demo operation files?",
				"Load Demo Files", JOptionPane.OK_CANCEL_OPTION);
		if (results != JOptionPane.OK_OPTION)
			return;

		AutoBackup backup = new AutoBackup();

		try {
			backup.autoBackup();
		} catch (Exception ex) {
			// Don't like eating this exception, but have to until the
			// autobackup is smarter and skips with no files to backup.
		}

		try {
			backup.loadDemoFiles();

			JOptionPane
					.showMessageDialog(
							null,
							"You must restart JMRI to complete the load demo operation",
							"Demo load successful!",
							JOptionPane.INFORMATION_MESSAGE);

			Apps.handleRestart();

		} catch (Exception ex) {
			ExceptionContext context = new ExceptionContext(ex,
					"Loading demo files",
					"Make sure that all of the demo files exist and can be read.");
			new ExceptionDisplayFrame(context);
		}
	}
}

/* @(#)LoadDemoAction.java */