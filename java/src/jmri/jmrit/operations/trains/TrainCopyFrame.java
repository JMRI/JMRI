// TrainCopyFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Frame for copying a train for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011, 2013
 * @version $Revision: 17977 $
 */
public class TrainCopyFrame extends OperationsFrame {

	TrainManager trainManager = TrainManager.instance();

	// labels
	javax.swing.JLabel textCopyTrain = new javax.swing.JLabel(Bundle.getMessage("SelectTrain"));

	// text field
	javax.swing.JTextField trainNameTextField = new javax.swing.JTextField(Control.max_len_string_train_name);

	// major buttons
	javax.swing.JButton copyButton = new javax.swing.JButton(Bundle.getMessage("Copy"));

	// combo boxes
	javax.swing.JComboBox trainBox = TrainManager.instance().getComboBox();

	public TrainCopyFrame() {
		// general GUI config

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels

		// Layout the panel by rows
		// row 1
		JPanel pName = new JPanel();
		pName.setLayout(new GridBagLayout());
		pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
		addItem(pName, trainNameTextField, 0, 0);

		// row 2
		JPanel pCopy = new JPanel();
		pCopy.setLayout(new GridBagLayout());
		pCopy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectTrain")));
		addItem(pCopy, trainBox, 0, 0);

		// row 4
		JPanel pButton = new JPanel();
		pButton.add(copyButton);

		getContentPane().add(pName);
		getContentPane().add(pCopy);
		getContentPane().add(pButton);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N

		pack();
		setMinimumSize(new Dimension(Control.mediumPanelWidth, Control.smallPanelHeight));

		setTitle(Bundle.getMessage("TitleTrainCopy"));

		// setup buttons
		addButtonAction(copyButton);
	}

	public void setTrainName(String trainName) {
		trainBox.setSelectedItem(trainManager.getTrainByName(trainName));
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == copyButton) {
			log.debug("copy train button activated");
			if (!checkName())
				return;

			Train newTrain = trainManager.getTrainByName(trainNameTextField.getText());
			if (newTrain != null) {
				reportTrainExists();
				return;
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")) {
				reportTrainDoesNotExist();
				return;
			}
			Train oldTrain = (Train) trainBox.getSelectedItem();
			if (oldTrain == null) {
				reportTrainDoesNotExist();
				return;
			}

			// now copy
			newTrain = trainManager.copyTrain(oldTrain, trainNameTextField.getText());

			TrainEditFrame f = new TrainEditFrame();
			f.initComponents(newTrain);
		}
	}

	private void reportTrainExists() {
		JOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNameExists"), MessageFormat.format(Bundle
				.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }), JOptionPane.ERROR_MESSAGE);
	}

	private void reportTrainDoesNotExist() {
		JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectTrain"), MessageFormat.format(Bundle
				.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 
	 * @return true if name isn't too long
	 */
	private boolean checkName() {
		if (trainNameTextField.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("EnterTrainName"), MessageFormat.format(Bundle
					.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trainNameTextField.getText().length() > Control.max_len_string_train_name) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainNameLess"),
					new Object[] { Control.max_len_string_train_name + 1 }), MessageFormat.format(Bundle
					.getMessage("CanNot"), new Object[] { Bundle.getMessage("copy") }), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void dispose() {
		super.dispose();
	}

	static Logger log = LoggerFactory.getLogger(TrainCopyFrame.class.getName());
}
