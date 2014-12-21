// OperationsSetupFrame.java

package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrit.operations.OperationsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of operation parameters
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision$
 */

public class OperationsSetupFrame extends OperationsFrame implements PropertyChangeListener {

	public OperationsSetupFrame() {
            super(Bundle.getMessage("TitleOperationsSetup"), new OperationsSetupPanel());
	}

        @Override
	public void initComponents() {
            ((OperationsSetupPanel) this.getContentPane()).initComponents();
		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new OptionAction(Bundle.getMessage("TitleOptions")));
		toolMenu.add(new PrintOptionAction());
		toolMenu.add(new BuildReportOptionAction());
		toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
		toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
		toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
		toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
		toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));

		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N

		initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight500));
	}

	// Save, Delete, Add buttons
        @Override
	public void buttonActionPerformed(ActionEvent ae) {
            ((OperationsSetupPanel) this.getContentPane()).buttonActionPerformed(ae);
	}

        @Override
	public void checkBoxActionPerformed(ActionEvent ae) {
            ((OperationsSetupPanel) this.getContentPane()).checkBoxActionPerformed(ae);
	}

        @Override
	public void propertyChange(PropertyChangeEvent e) {
            ((OperationsSetupPanel) this.getContentPane()).propertyChange(e);
	}

	static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class.getName());
}
