// EngineAttributeEditFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame for adding and editing the engine roster for operations.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	EngineManager engineManager = EngineManager.instance();

	// labels
	javax.swing.JLabel textAttribute = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton deleteButton = new javax.swing.JButton();
	javax.swing.JButton replaceButton = new javax.swing.JButton();

	// combo box
	javax.swing.JComboBox comboBox;

	// text box
	javax.swing.JTextField addTextBox = new javax.swing.JTextField(Control.max_len_string_attibute);

	// property change
	public static final String DISPOSE = "dispose"; // NOI18N

	public EngineAttributeEditFrame() {
	}

	String _comboboxName; // track which combo box is being edited
	boolean menuActive = false;

	public void initComponents(String comboboxName) {
		initComponents(comboboxName, "");
	}

	public void initComponents(String comboboxName, String select) {

		getContentPane().removeAll();

		setTitle(MessageFormat.format(Bundle.getMessage("TitleEngineEditAtrribute"), new Object[] { comboboxName }));

		// track which combo box is being edited
		_comboboxName = comboboxName;
		loadCombobox();
		comboBox.setSelectedItem(select);

		// general GUI config
		getContentPane().setLayout(new GridBagLayout());

		textAttribute.setText(comboboxName);

		addButton.setText(Bundle.getMessage("Add"));
		addButton.setVisible(true);
		deleteButton.setText(Bundle.getMessage("Delete"));
		deleteButton.setVisible(true);
		replaceButton.setText(Bundle.getMessage("Replace"));
		replaceButton.setVisible(true);

		// row 1
		addItem(textAttribute, 1, 1);
		// row 2
		addItem(addTextBox, 1, 2);
		addItem(addButton, 2, 2);

		// row 3
		addItem(comboBox, 1, 3);
		addItem(deleteButton, 2, 3);

		// row 4
		addItem(replaceButton, 2, 4);

		addButtonAction(addButton);
		addButtonAction(deleteButton);
		addButtonAction(replaceButton);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Locomotives", true); // NOI18N

		initMinimumSize(new Dimension(Control.mediumPanelWidth, Control.minPanelHeight));

	}

	// add, delete or replace button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("edit frame button activated");
		if (ae.getSource() == addButton) {
			String addItem = addTextBox.getText();
			if (addItem.equals(""))
				return;
			if (addItem.length() > Control.max_len_string_attibute) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("engineAttribute"),
						new Object[] { Control.max_len_string_attibute }), MessageFormat.format(Bundle
						.getMessage("canNotAdd"), new Object[] { _comboboxName }), JOptionPane.ERROR_MESSAGE);
				return;
			}
			addItemToCombobox(addItem);
		}
		if (ae.getSource() == deleteButton) {
			String deleteItem = (String) comboBox.getSelectedItem();
			deleteItemFromCombobox(deleteItem);
		}
		if (ae.getSource() == replaceButton) {
			String newItem = addTextBox.getText();
			if (newItem.equals(""))
				return;
			if (newItem.length() > Control.max_len_string_attibute) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("engineAttribute"),
						new Object[] { Control.max_len_string_attibute }), MessageFormat.format(Bundle
						.getMessage("canNotReplace"), new Object[] { _comboboxName }), JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldItem = (String) comboBox.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceMsg"), new Object[] {
					oldItem, newItem }), Bundle.getMessage("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			if (newItem.equals(oldItem))
				return;
			// need to make sure locations and trains are loaded
			TrainManager.instance();
			addItemToCombobox(newItem);
			replaceItem(oldItem, newItem);
			deleteItemFromCombobox(oldItem);
		}
	}

	private void deleteItemFromCombobox(String deleteItem) {
		if (_comboboxName == EngineEditFrame.ROAD) {
			// purge train and locations by using replace
			CarRoads.instance().replaceName(deleteItem, null);
		}
		if (_comboboxName == EngineEditFrame.MODEL) {
			EngineModels.instance().deleteName(deleteItem);
		}
		if (_comboboxName == EngineEditFrame.TYPE) {
			EngineTypes.instance().deleteName(deleteItem);
		}
		if (_comboboxName == EngineEditFrame.LENGTH) {
			EngineLengths.instance().deleteName(deleteItem);
		}
		if (_comboboxName == EngineEditFrame.OWNER) {
			CarOwners.instance().deleteName(deleteItem);
		}
		if (_comboboxName == EngineEditFrame.CONSIST) {
			engineManager.deleteConsist(deleteItem);
		}
	}

	private void addItemToCombobox(String addItem) {
		if (_comboboxName == EngineEditFrame.ROAD) {
			CarRoads.instance().addName(addItem);
		}
		if (_comboboxName == EngineEditFrame.MODEL) {
			EngineModels.instance().addName(addItem);
		}
		if (_comboboxName == EngineEditFrame.TYPE) {
			EngineTypes.instance().addName(addItem);
		}
		if (_comboboxName == EngineEditFrame.LENGTH) {
			// convert from inches to feet if needed
			if (addItem.endsWith("\"")) { // NOI18N
				addItem = addItem.substring(0, addItem.length() - 1);
				try {
					double inches = Double.parseDouble(addItem);
					int feet = (int) (inches * Setup.getScaleRatio() / 12);
					addItem = Integer.toString(feet);
				} catch (NumberFormatException e) {
					log.error("can not convert from inches to feet");
					JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertFeet"), Bundle
							.getMessage("ErrorEngineLength"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (addItem.endsWith("cm")) { // NOI18N
				addItem = addItem.substring(0, addItem.length() - 2);
				try {
					double cm = Double.parseDouble(addItem);
					int meter = (int) (cm * Setup.getScaleRatio() / 100);
					addItem = Integer.toString(meter);
				} catch (NumberFormatException e) {
					log.error("Can not convert from cm to meters");
					JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertMeter"), Bundle
							.getMessage("ErrorEngineLength"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			// confirm that length is a number and less than 10000 feet
			try {
				int length = Integer.parseInt(addItem);
				if (length < 0) {
					log.error("engine length has to be a positive number");
					return;
				}
				if (addItem.length() > Control.max_len_string_length_name) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("engineAttribute"),
							new Object[] { Control.max_len_string_length_name }), MessageFormat.format(Bundle
							.getMessage("canNotAdd"), new Object[] { _comboboxName }), JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException e) {
				log.error("length not an integer");
				return;
			}
			EngineLengths.instance().addName(addItem);
			comboBox.setSelectedItem(addItem);
		}
		if (_comboboxName == EngineEditFrame.CONSIST) {
			engineManager.newConsist(addItem);
		}
		if (_comboboxName == EngineEditFrame.OWNER) {
			CarOwners.instance().addName(addItem);
		}
	}

	private void replaceItem(String oldItem, String newItem) {
		List<RollingStock> engines = engineManager.getList();
		for (int i = 0; i < engines.size(); i++) {
			Engine engine = (Engine) engines.get(i);
			if (_comboboxName == EngineEditFrame.MODEL) {
				// we need to copy the old model attributes, so find an engine.
				if (engine.getModel().equals(oldItem)) {
					// Has this model been configured?
					if (EngineModels.instance().getModelLength(newItem) != null) {
						engine.setModel(newItem);
					} else {
						// get the old configuration for this model
						String length = engine.getLength();
						String hp = engine.getHp();
						String type = engine.getTypeName();
						// now update the new model
						engine.setModel(newItem);
						engine.setLength(length);
						engine.setHp(hp);
						engine.setTypeName(type);
					}
				}
			}
		}
		if (_comboboxName == EngineEditFrame.CONSIST) {
			engineManager.replaceConsistName(oldItem, newItem);
		}
		// now adjust locations and trains
		if (_comboboxName == EngineEditFrame.TYPE) {
			EngineTypes.instance().replaceName(oldItem, newItem);
		}
		if (_comboboxName == EngineEditFrame.ROAD) {
			CarRoads.instance().replaceName(oldItem, newItem);
		}
		if (_comboboxName == EngineEditFrame.OWNER) {
			CarOwners.instance().replaceName(oldItem, newItem);
		}
		if (_comboboxName == EngineEditFrame.LENGTH) {
			EngineLengths.instance().replaceName(oldItem, newItem);
		}
		if (_comboboxName == EngineEditFrame.MODEL) {
			EngineModels.instance().replaceName(oldItem, newItem);
		}
	}

	private void loadCombobox() {
		if (_comboboxName == EngineEditFrame.ROAD) {
			comboBox = CarRoads.instance().getComboBox();
			CarRoads.instance().addPropertyChangeListener(this);
		}
		if (_comboboxName == EngineEditFrame.MODEL) {
			comboBox = EngineModels.instance().getComboBox();
			EngineModels.instance().addPropertyChangeListener(this);
		}
		if (_comboboxName == EngineEditFrame.TYPE) {
			comboBox = EngineTypes.instance().getComboBox();
			EngineTypes.instance().addPropertyChangeListener(this);
		}
		if (_comboboxName == EngineEditFrame.LENGTH) {
			comboBox = EngineLengths.instance().getComboBox();
			EngineLengths.instance().addPropertyChangeListener(this);
		}
		if (_comboboxName == EngineEditFrame.OWNER) {
			comboBox = CarOwners.instance().getComboBox();
			CarOwners.instance().addPropertyChangeListener(this);
		}
		if (_comboboxName == EngineEditFrame.CONSIST) {
			comboBox = engineManager.getConsistComboBox();
			engineManager.addPropertyChangeListener(this);
		}
	}

	public void dispose() {
		CarRoads.instance().removePropertyChangeListener(this);
		EngineModels.instance().removePropertyChangeListener(this);
		EngineTypes.instance().removePropertyChangeListener(this);
		EngineLengths.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		engineManager.removePropertyChangeListener(this);
		firePcs(DISPOSE, _comboboxName, null);
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY))
			CarRoads.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY))
			EngineModels.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY))
			EngineTypes.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY))
			EngineLengths.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_LENGTH_CHANGED_PROPERTY))
			CarOwners.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY))
			engineManager.updateConsistComboBox(comboBox);
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	// note firePropertyChange occurs during frame creation
	private void firePcs(String p, Object old, Object n) {
		log.debug("EngineAttribute firePropertyChange " + p + " ");
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(EngineAttributeEditFrame.class.getName());
}
