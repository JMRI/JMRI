// TrainCopyFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Frame for copying a train for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TrainCopyFrame extends OperationsFrame {

	TrainManager trainManager = TrainManager.instance();

	// labels
	javax.swing.JLabel textCopyTrain = new javax.swing.JLabel(Bundle.getMessage("SelectTrain"));
	javax.swing.JLabel textTrainName = new javax.swing.JLabel(Bundle.getMessage("Name"));

	// text field
	javax.swing.JTextField trainNameTextField = new javax.swing.JTextField(20);

	// major buttons
	javax.swing.JButton copyButton = new javax.swing.JButton(Bundle.getMessage("Copy"));

	// combo boxes
	javax.swing.JComboBox trainBox = TrainManager.instance().getComboBox();

	public TrainCopyFrame() {
		// general GUI config

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		// Layout the panel by rows
		// row 1
		addItem(p1, textTrainName, 0, 1);
		addItemWidth(p1, trainNameTextField, 3, 1, 1);

		// row 2
		addItem(p1, textCopyTrain, 0, 2);
		addItemWidth(p1, trainBox, 3, 1, 2);

		// row 4
		addItem(p1, copyButton, 1, 4);

		getContentPane().add(p1);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N

		pack();
		if (getWidth() < 400)
			setSize(400, getHeight());
		if (getHeight() < 150)
			setSize(getWidth(), 150);
		
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
			f.setTitle(Bundle.getMessage("TitleTrainEdit"));
			f.setVisible(true);
		}
	}

	private void reportTrainExists() {
		JOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNameExists"),
				MessageFormat.format(Bundle.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }),
				JOptionPane.ERROR_MESSAGE);
	}

	private void reportTrainDoesNotExist() {
		JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectTrain"),
				MessageFormat.format(Bundle.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }),
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 
	 * @return true if name isn't too long
	 */
	private boolean checkName() {
		if (trainNameTextField.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("EnterTrainName"), MessageFormat.format(
					Bundle.getMessage("CanNotTrain"), new Object[] { Bundle.getMessage("copy") }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trainNameTextField.getText().length() > Control.max_len_string_train_name) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainNameLess"),
					new Object[] { Control.max_len_string_train_name + 1 }), MessageFormat.format(
					Bundle.getMessage("CanNot"), new Object[] { Bundle.getMessage("copy") }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void dispose() {
		super.dispose();
	}

	static Logger log = LoggerFactory.getLogger(TrainCopyFrame.class
			.getName());
}
