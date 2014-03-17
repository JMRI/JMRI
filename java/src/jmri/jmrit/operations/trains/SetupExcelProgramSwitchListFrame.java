// SetupExcelProgramSwitchListFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 22249 $
 */

public class SetupExcelProgramSwitchListFrame extends OperationsFrame {

	// text windows
	JTextField fileName = new JTextField(30);

	// major buttons
	JButton testButton = new JButton(Bundle.getMessage("Test"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	public void initComponents() {

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Layout the panel by rows

		// row 1
		JPanel pDirectoryName = new JPanel();
		pDirectoryName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Directory")));
		pDirectoryName.add(new JLabel(OperationsManager.getInstance().getFile(TrainCustomSwitchList.getDirectoryName()).getPath()));

		JPanel pFileName = new JPanel();
		pFileName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FileName")));
		pFileName.add(fileName);

		fileName.setText(TrainCustomSwitchList.getFileName());

		// row 4 buttons
		JPanel pB = new JPanel();
		pB.setLayout(new GridBagLayout());
		addItem(pB, testButton, 1, 0);
		addItem(pB, saveButton, 3, 0);

		getContentPane().add(pDirectoryName);
		getContentPane().add(pFileName);
		getContentPane().add(pB);

		// setup buttons
		addButtonAction(testButton);
		addButtonAction(saveButton);

		addHelpMenu("package.jmri.jmrit.operations.Operations_SetupExcelProgram", true); // NOI18N
		setTitle(Bundle.getMessage("MenuItemSetupExcelProgram"));

		setMinimumSize(new Dimension(Control.smallPanelWidth, Control.smallPanelHeight));
		pack();
		setVisible(true);
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
