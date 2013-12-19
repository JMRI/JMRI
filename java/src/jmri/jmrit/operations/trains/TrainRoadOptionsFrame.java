// TrainRoadOptionsFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
//import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user edit of a train's road options
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 23502 $
 */

public class TrainRoadOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	Train _train = null;

	JPanel pRoadControls = new JPanel();
	JPanel panelRoads = new JPanel();
	JScrollPane paneRoads = new JScrollPane(panelRoads);

	// labels
	JLabel trainName = new JLabel();
	JLabel trainDescription = new JLabel();

	// major buttons
	JButton addRoadButton = new JButton(Bundle.getMessage("AddRoad"));
	JButton deleteRoadButton = new JButton(Bundle.getMessage("DeleteRoad"));
	JButton deleteAllRoadsButton = new JButton(Bundle.getMessage("DeleteAll"));
	JButton saveTrainButton = new JButton(Bundle.getMessage("SaveTrain"));

	// radio buttons
	JRadioButton roadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
	JRadioButton roadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
	JRadioButton roadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

	ButtonGroup roadGroup = new ButtonGroup();

	// check boxes

	// text field

	// combo boxes
	JComboBox comboBoxRoads = CarRoads.instance().getComboBox();

	public static final String DISPOSE = "dispose"; // NOI18N

	public TrainRoadOptionsFrame() {
		super(Bundle.getMessage("MenuItemRoadOptions"));
	}

	public void initComponents(TrainEditFrame parent) {

		parent.setChildFrame(this);
		_train = parent._train;

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Layout the panel by rows
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		p1.setMaximumSize(new Dimension(2000, 250));

		// Layout the panel by rows
		// row 1a
		JPanel pName = new JPanel();
		pName.setLayout(new GridBagLayout());
		pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
		addItem(pName, trainName, 0, 0);

		// row 1b
		JPanel pDesc = new JPanel();
		pDesc.setLayout(new GridBagLayout());
		pDesc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
		addItem(pDesc, trainDescription, 0, 0);

		p1.add(pName);
		p1.add(pDesc);

		// row 3
		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
		JScrollPane pane3 = new JScrollPane(p3);
		pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadsTrain")));
		pane3.setMaximumSize(new Dimension(2000, 400));

		JPanel pRoadRadioButtons = new JPanel();
		pRoadRadioButtons.setLayout(new FlowLayout());

		pRoadRadioButtons.add(roadNameAll);
		pRoadRadioButtons.add(roadNameInclude);
		pRoadRadioButtons.add(roadNameExclude);

		pRoadControls.setLayout(new FlowLayout());

		pRoadControls.add(comboBoxRoads);
		pRoadControls.add(addRoadButton);
		pRoadControls.add(deleteRoadButton);
		pRoadControls.add(deleteAllRoadsButton);

		pRoadControls.setVisible(false);

		p3.add(pRoadRadioButtons);
		p3.add(pRoadControls);

		// row 4
		panelRoads.setLayout(new GridBagLayout());
		paneRoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Roads")));

		ButtonGroup roadGroup = new ButtonGroup();
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);

		// row 12
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());
		panelButtons.setBorder(BorderFactory.createTitledBorder(""));
		panelButtons.setMaximumSize(new Dimension(2000, 200));

		// row 13
		addItem(panelButtons, saveTrainButton, 0, 0);

		getContentPane().add(p1);
		getContentPane().add(pane3);
		getContentPane().add(paneRoads);
		getContentPane().add(panelButtons);

		// setup buttons
		addButtonAction(saveTrainButton);

		addButtonAction(deleteRoadButton);
		addButtonAction(deleteAllRoadsButton);
		addButtonAction(addRoadButton);

		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);

		if (_train != null) {
			trainName.setText(_train.getName());
			trainDescription.setText(_train.getDescription());
			updateButtons(true);
			// listen for train changes
			_train.addPropertyChangeListener(this);
		} else {
			updateButtons(false);
		}
		addHelpMenu("package.jmri.jmrit.operations.Operations_TrainRoadOptions", true); // NOI18N
		updateRoadComboBoxes();
		updateRoadNames();

		// get notified if car roads, roads, and owners gets modified
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);

		initMinimumSize(new Dimension(Control.panelWidth, Control.mediumPanelHeight));
	}

	// Save
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train != null) {
			if (ae.getSource() == saveTrainButton) {
				log.debug("train save button activated");
				saveTrain();
			}
			if (ae.getSource() == addRoadButton) {
				String roadName = (String) comboBoxRoads.getSelectedItem();
				if (_train.addRoadName(roadName))
					updateRoadNames();
				selectNextItemComboBox(comboBoxRoads);
			}
			if (ae.getSource() == deleteRoadButton) {
				String roadName = (String) comboBoxRoads.getSelectedItem();
				if (_train.deleteRoadName(roadName))
					updateRoadNames();
				selectNextItemComboBox(comboBoxRoads);
			}
			if (ae.getSource() == deleteAllRoadsButton) {
				deleteAllRoads();
			}
		}
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (_train != null) {
			if (ae.getSource() == roadNameAll) {
				_train.setRoadOption(Train.ALL_LOADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameInclude) {
				_train.setRoadOption(Train.INCLUDE_LOADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameExclude) {
				_train.setRoadOption(Train.EXCLUDE_LOADS);
				updateRoadNames();
			}
		}
	}

	protected void updateButtons(boolean enabled) {
		saveTrainButton.setEnabled(enabled);

		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
	}

	private void updateRoadNames() {
		log.debug("Update road names");
		panelRoads.removeAll();
		if (_train != null) {
			// set radio button
			roadNameAll.setSelected(_train.getRoadOption().equals(Train.ALL_LOADS));
			roadNameInclude.setSelected(_train.getRoadOption().equals(Train.INCLUDE_ROADS));
			roadNameExclude.setSelected(_train.getRoadOption().equals(Train.EXCLUDE_ROADS));

			pRoadControls.setVisible(!roadNameAll.isSelected());

			if (!roadNameAll.isSelected()) {
				int x = 0;
				int y = 0; // vertical position in panel

				int numberOfRoads = 6;
				String[] carRoads = _train.getRoadNames();
				for (int i = 0; i < carRoads.length; i++) {
					JLabel road = new JLabel();
					road.setText(carRoads[i]);
					addItemTop(panelRoads, road, x++, y);
					// limit the number of roads per line
					if (x > numberOfRoads) {
						y++;
						x = 0;
					}
				}
				validate();
			}
		} else {
			roadNameAll.setSelected(true);
		}
		panelRoads.repaint();
		panelRoads.validate();
	}

	private void deleteAllRoads() {
		if (_train != null) {
			String[] trainRoads = _train.getRoadNames();
			for (int i = 0; i < trainRoads.length; i++) {
				_train.deleteRoadName(trainRoads[i]);
			}
		}
		updateRoadNames();
	}

	private void saveTrain() {
		// save the last state of the "Use car type and road" checkbox
		OperationsXml.save();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}

	private void updateRoadComboBoxes() {
		CarRoads.instance().updateComboBox(comboBoxRoads);
	}

	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);
		CarRoads.instance().removePropertyChangeListener(this);
		if (_train != null) {
			_train.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)) {
			updateRoadComboBoxes();
			updateRoadNames();
		}
	}

	static Logger log = LoggerFactory.getLogger(TrainRoadOptionsFrame.class.getName());
}
