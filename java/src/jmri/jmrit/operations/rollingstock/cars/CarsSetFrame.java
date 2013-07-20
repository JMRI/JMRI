// CarsSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.MessageFormat;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import jmri.util.com.sun.TableSorter;

/**
 * Frame for user to place a group of cars on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * @version $Revision$
 */

public class CarsSetFrame extends CarSetFrame implements java.beans.PropertyChangeListener {

	CarsTableModel _carsTableModel;
	JTable _carsTable;
	TableSorter _sorter;

	public CarsSetFrame() {
		super();
	}

	// Ignore checkbox states
	private static boolean ignoreStatusCheckBoxSelected = false;
	private static boolean ignoreLocationCheckBoxSelected = false;
	private static boolean ignoreRWECheckBoxSelected = false;
	private static boolean ignoreLoadCheckBoxSelected = false;
	private static boolean ignoreKernelCheckBoxSelected = false;
	private static boolean ignoreDestinationCheckBoxSelected = false;
	private static boolean ignoreFinalDestinationCheckBoxSelected = false;
	private static boolean ignoreTrainCheckBoxSelected = false;

	public void initComponents(JTable carsTable) {
		_carsTable = carsTable;
		_sorter = (TableSorter) carsTable.getModel();
		_carsTableModel = (CarsTableModel) _sorter.getTableModel();

		super.initComponents();

		setTitle(Bundle.getMessage("TitleSetCars"));
		// modify Save button text to "Change"
		saveButton.setText(Bundle.getMessage("Change"));
		// disable edit load button if no cars selected
		editLoadButton.setEnabled(false);
		// show ignore checkboxes
		ignoreStatusCheckBox.setVisible(true);
		ignoreLocationCheckBox.setVisible(true);
		ignoreRWECheckBox.setVisible(true);
		ignoreLoadCheckBox.setVisible(true);
		ignoreKernelCheckBox.setVisible(true);
		ignoreDestinationCheckBox.setVisible(true);
		ignoreFinalDestinationCheckBox.setVisible(true);
		ignoreTrainCheckBox.setVisible(true);
		ignoreAllButton.setVisible(true);

		// set the last state
		ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
		ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
		ignoreRWECheckBox.setSelected(ignoreRWECheckBoxSelected);
		ignoreLoadCheckBox.setSelected(ignoreLoadCheckBoxSelected);
		ignoreKernelCheckBox.setSelected(ignoreKernelCheckBoxSelected);
		ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
		ignoreFinalDestinationCheckBox.setSelected(ignoreFinalDestinationCheckBoxSelected);
		ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);

		int rows[] = _carsTable.getSelectedRows();
		if (rows.length > 0) {
			Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(rows[0]));
			super.loadCar(car);
		}
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		super.buttonActionPerformed(ae);
		if (ae.getSource() == ignoreAllButton) {
			ignoreAll(toggle);
		}
	}
	
	boolean toggle = true;
	protected void ignoreAll(boolean b) {
		ignoreStatusCheckBox.setSelected(!locationUnknownCheckBox.isSelected() & b);
		ignoreLocationCheckBox.setSelected(b);
		ignoreRWECheckBox.setSelected(b);
		ignoreLoadCheckBox.setSelected(b);
		ignoreKernelCheckBox.setSelected(b);
		ignoreDestinationCheckBox.setSelected(b);
		ignoreFinalDestinationCheckBox.setSelected(b);
		ignoreTrainCheckBox.setSelected(b);
		enableComponents(!locationUnknownCheckBox.isSelected());
		toggle = !b;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected boolean save() {
		// save ignore states
		ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
		ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
		ignoreRWECheckBoxSelected = ignoreRWECheckBox.isSelected();
		ignoreLoadCheckBoxSelected = ignoreLoadCheckBox.isSelected();
		ignoreKernelCheckBoxSelected = ignoreKernelCheckBox.isSelected();
		ignoreDestinationCheckBoxSelected = ignoreKernelCheckBox.isSelected();
		ignoreFinalDestinationCheckBoxSelected = ignoreFinalDestinationCheckBox.isSelected();
		ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

		int rows[] = _carsTable.getSelectedRows();
		if (rows.length == 0)
			JOptionPane.showMessageDialog(this, Bundle.getMessage("selectCars"), Bundle
					.getMessage("carNoneSelected"), JOptionPane.WARNING_MESSAGE);
		
		askKernelChange = true;

		for (int i = 0; i < rows.length; i++) {
			Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(rows[i]));
			if (_car == null) {
				super.loadCar(car);
				continue;
			}
			if (i == 0 && car != _car) {
				log.debug("Default car isn't the first one selected");
				if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
						.getMessage("doYouWantToChange"), new Object[] { car.toString() }), Bundle
						.getMessage("changeDefaultCar"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					super.loadCar(car); // new default car
					break; // done, don't modify any of the cars selected
				}
			}
			if (!super.change(car))
				return false;
			else if (car.getKernel() != null && !ignoreKernelCheckBox.isSelected())
				askKernelChange = false;
		}
		// if car's load changes, we need to update track combo boxes if auto was selected
		updateComboBoxesLoadChange();
		return true;
	}

	static Logger log = LoggerFactory.getLogger(CarsSetFrame.class.getName());
}
