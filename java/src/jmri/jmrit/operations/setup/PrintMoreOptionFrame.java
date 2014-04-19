// PrintMoreOptionFrame.java

package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame for user edit of additional manifest print options
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 21846 $
 */

public class PrintMoreOptionFrame extends OperationsFrame {

	// labels

	// major buttons
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// radio buttons

	// check boxes

	// text field
	JTextField tab1TextField = new JTextField(2);
	JTextField tab2TextField = new JTextField(2);
	JTextField tab3TextField = new JTextField(2);

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
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));

		JPanel pTab = new JPanel();
		pTab.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutTab1")));
		pTab.add(tab1TextField);
		p1.add(pTab);

		JPanel pTab2 = new JPanel();
		pTab2.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutTab2")));
		pTab2.add(tab2TextField);
		p1.add(pTab2);

		JPanel pTab3 = new JPanel();
		pTab3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutTab3")));
		pTab3.add(tab3TextField);
		p1.add(pTab3);

		tab1TextField.setText(Integer.toString(Setup.getTab1Length()));
		tab2TextField.setText(Integer.toString(Setup.getTab2Length()));
		tab3TextField.setText(Integer.toString(Setup.getTab3Length()));

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
		addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N

		initMinimumSize(new Dimension(Control.smallPanelWidth, Control.mediumPanelHeight));
	}

	// Save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {

			try {
				Setup.setTab1length(Integer.parseInt(tab1TextField.getText()));
				Setup.setTab2length(Integer.parseInt(tab2TextField.getText()));
				Setup.setTab3length(Integer.parseInt(tab3TextField.getText()));
			} catch (Exception e) {
				log.error("Tab wasn't a number");
			}

			OperationsSetupXml.instance().writeOperationsFile();

			// recreate all train manifests
			TrainManager.instance().setTrainsModified();

			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	static Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class.getName());
}
