// BackupFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for backing up operation files
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.7 $
 */

public class BackupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	javax.swing.JLabel textBackup = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton backupButton = new javax.swing.JButton();


	// radio buttons
	

    // check boxes
	
	// text field
	javax.swing.JTextField backupTextField = new javax.swing.JTextField(20);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes
	
	Backup backup = new Backup() ;

	public BackupFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOperationsBackup"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state
		textBackup.setText(rb.getString("BackupFiles"));
		backupButton.setText(rb.getString("Backup"));
		backupTextField.setText(backup.getDirectoryName());
 
		// Layout the panel by rows
		// rows 1 - 3
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		addItem (panel, textBackup, 0, 1);
		addItem (panel, backupTextField, 0, 2);
		addItem (panel, backupButton, 0, 3);
		
		getContentPane().add(panel);

		// setup buttons
		addButtonAction(backupButton);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_BackupRestore", true);

		// set frame size and location for display
		pack();
		if (getHeight()<150)
			setSize(300, getHeight()+50);
		setVisible(true);
	}
		
	// buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == backupButton){
			log.debug("backup button activated");
			// check to see if directory already exists
			if (backup.checkDirectoryExists(backupTextField.getText())){
				if(JOptionPane.showConfirmDialog(this, "Directory "
						+ backupTextField.getText() + " already exists, overwrite it?",
						"Overwrite backup directory?", JOptionPane.OK_CANCEL_OPTION)!= JOptionPane.OK_OPTION) {
					return;
				}
			}
			boolean success = backup.backupFiles(backupTextField.getText());
			if (success){
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Could not backup operation files",
						"Backup failed!" ,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("BackupFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(BackupFrame.class.getName());
}
