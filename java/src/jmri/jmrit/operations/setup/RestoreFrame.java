// RestoreFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import apps.Apps;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainsTableFrame;


/**
 * Frame for restoring operation files
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */

public class RestoreFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels
	javax.swing.JLabel textRestore = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton restoreButton = new javax.swing.JButton();


	// radio buttons
	

    // check boxes
	
	// text field

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes
	javax.swing.JComboBox restoreComboBox = new javax.swing.JComboBox();

	public RestoreFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOperationsRestore"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state
		textRestore.setText(rb.getString("RestoreFiles"));
		restoreButton.setText(rb.getString("Restore"));
 
		// Layout the panel by rows
		// rows 1 - 3
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		addItem (panel, textRestore, 0, 1);
		addItem (panel, restoreComboBox, 0, 2);
		addItem (panel, restoreButton, 0, 3);
		
		getContentPane().add(panel);
		
		// load combo box
		String[] backupDirectoryNames = new Backup().getBackupList();
		if (backupDirectoryNames != null){
			for (int i=0; i<backupDirectoryNames.length; i++){
				restoreComboBox.addItem(backupDirectoryNames[i]);
			}
		}

		// setup buttons
		addButtonAction(restoreButton);

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
		if (ae.getSource() == restoreButton){
			log.debug("restore button activated");
			// first backup the users data in case they forgot
		   	Backup backup = new Backup();
	    	String backupName = backup.createBackupDirectoryName();
	    	// now backup files
	    	boolean success = backup.backupFiles(backupName);
	    	if(!success){
	    		log.error("Could not backup files");
	    		return;
	    	}
			String directoryName = (String)restoreComboBox.getSelectedItem();
			success = new Backup().restore(directoryName);
			if (success){
				JOptionPane.showMessageDialog(this, "You must restart JMRI to complete the restore operation",
						"Restore successful!" ,
						JOptionPane.INFORMATION_MESSAGE);
				dispose();
		    	// now clear dirty bit
				try {
					jmri.InstanceManager.shutDownManagerInstance().deregister(TrainsTableFrame.trainDirtyTask);
				} catch (IllegalArgumentException e){
					
				}
				Apps.handleRestart();
			} else {
				JOptionPane.showMessageDialog(this, "Could not restore operation files",
						"Restore failed!" ,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("RestoreFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());

	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RestoreFrame.class.getName());
}
