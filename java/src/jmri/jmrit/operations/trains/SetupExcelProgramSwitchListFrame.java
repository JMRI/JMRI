// SetupExcelProgramSwitchListFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of the name and directory of an Excel program used for switch lists.
 * 
 * @author Dan Boudreau Copyright (C) 2013, 2014
 * @version $Revision: 22249 $
 */

public class SetupExcelProgramSwitchListFrame extends OperationsFrame {

	// text windows
	JTextField fileName = new JTextField(30);
	
	// checkboxes
	protected static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	JCheckBox generateCvsSwitchListCheckBox = new JCheckBox(rb.getString("GenerateCsvSwitchList")); 

	// major buttons
	JButton testButton = new JButton(Bundle.getMessage("Test"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	public void initComponents() {

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Layout the panel by rows
		// row 1
		
		JPanel pOptions = new JPanel();
		pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
		pOptions.add(generateCvsSwitchListCheckBox);
		
		generateCvsSwitchListCheckBox.setSelected(Setup.isGenerateCsvSwitchListEnabled());

		// row 3
		JPanel pDirectoryName = new JPanel();
		pDirectoryName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Directory")));
		pDirectoryName.add(new JLabel(OperationsManager.getInstance().getFile(TrainCustomSwitchList.getDirectoryName()).getPath()));

		JPanel pFileName = new JPanel();
		pFileName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FileName")));
		pFileName.add(fileName);

		fileName.setText(TrainCustomSwitchList.getFileName());

		// row 4 buttons
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, testButton, 1, 0);
		addItem(pButtons, saveButton, 3, 0);

		getContentPane().add(pOptions);
		getContentPane().add(pDirectoryName);
		getContentPane().add(pFileName);
		getContentPane().add(pButtons);

		// setup buttons
		addButtonAction(testButton);
		addButtonAction(saveButton);

		addHelpMenu("package.jmri.jmrit.operations.Operations_SetupExcelProgram", true); // NOI18N
		setTitle(Bundle.getMessage("MenuItemSetupExcelProgram"));

		initMinimumSize(new Dimension(Control.mediumPanelWidth, Control.panelHeight300));
	}

	// Save and Test
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

		TrainCustomSwitchList.setFileName(fileName.getText());

		if (ae.getSource() == testButton) {
			if (TrainCustomSwitchList.manifestCreatorFileExists()) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("DirectoryNameFileName"),
						new Object[] { TrainCustomSwitchList.getDirectoryName(), TrainCustomSwitchList.getFileName() }),
						Bundle.getMessage("ManifestCreatorFound"), JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Bundle.getMessage("LoadDirectoryNameFileName"), new Object[] {
								TrainCustomSwitchList.getDirectoryName(), TrainCustomSwitchList.getFileName() }), Bundle
						.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
			}
		}
		if (ae.getSource() == saveButton) {
			log.debug("Save button activated");
			Setup.setGenerateCsvSwitchListEnabled(generateCvsSwitchListCheckBox.isSelected());
			TrainManagerXml.instance().writeOperationsFile();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void dispose() {
		super.dispose();
	}

	static Logger log = LoggerFactory.getLogger(SetupExcelProgramSwitchListFrame.class.getName());
}
