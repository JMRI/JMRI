// PrintMoreOptionFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of manifest and switch list print options
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 21846 $
 */

public class PrintMoreOptionFrame extends OperationsFrame {

	// labels
	JLabel textBuildReport = new JLabel(Bundle.getMessage("BuildReport"));
	JLabel logoURL = new JLabel("");

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// radio buttons

	// check boxes

	// text field
	JTextField tabTextField = new JTextField(10);

	// text area

	// combo boxes

	public PrintMoreOptionFrame() {
		super(Bundle.getMessage("TitlePrintMoreOptions"));
	}

	public void initComponents() {

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// row 1 font type and size
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

		JPanel pTab = new JPanel();
		pTab.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutTab")));
		pTab.add(tabTextField);

		tabTextField.setText(Integer.toString(Setup.getTabLength()));

		p1.add(pTab);

		// add tool tips
		saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

		// row 11
		JPanel pControl = new JPanel();
		pControl.setBorder(BorderFactory.createTitledBorder(""));
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 0, 0);

		getContentPane().add(p1);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(saveButton);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_PrintOptions", true); // NOI18N

		pack();
		setVisible(true);
	}

	// Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {

			Setup.setTablength(Integer.parseInt(tabTextField.getText()));

			OperationsSetupXml.instance().writeOperationsFile();
			// Check font if user selected tab output
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static Logger log = LoggerFactory
			.getLogger(OperationsSetupFrame.class.getName());
}
