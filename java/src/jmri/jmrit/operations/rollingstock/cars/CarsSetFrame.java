// CarsSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.text.MessageFormat;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import jmri.util.com.sun.TableSorter;

/**
 * Frame for user to place a group of cars on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2011
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
	private static boolean ignoreDestinationCheckBoxSelected = false;
	private static boolean ignoreFinalDestinationCheckBoxSelected = false;
	private static boolean ignoreTrainCheckBoxSelected = false;

	public void initComponents(JTable carsTable) {
		_carsTable = carsTable;
		_sorter = (TableSorter) carsTable.getModel();
		_carsTableModel = (CarsTableModel) _sorter.getTableModel();

		super.initComponents();

		setTitle(Bundle.getString("TitleSetCars"));
		// modify Save button text to "Change"
		saveButton.setText(Bundle.getString("Change"));
		// disable edit load button if no cars selected
		editLoadButton.setEnabled(false);
		// show ignore checkboxes
		ignoreStatusCheckBox.setVisible(true);
		ignoreLocationCheckBox.setVisible(true);
		ignoreRWECheckBox.setVisible(true);
		ignoreLoadCheckBox.setVisible(true);
		ignoreDestinationCheckBox.setVisible(true);
		ignoreFinalDestinationCheckBox.setVisible(true);
		ignoreTrainCheckBox.setVisible(true);

		// set the last state
		ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
		ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
		ignoreRWECheckBox.setSelected(ignoreRWECheckBoxSelected);
		ignoreLoadCheckBox.setSelected(ignoreLoadCheckBoxSelected);
		ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
		ignoreFinalDestinationCheckBox.setSelected(ignoreFinalDestinationCheckBoxSelected);
		ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);

		int rows[] = _carsTable.getSelectedRows();
		if (rows.length > 0) {
			Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(rows[0]));
			super.loadCar(car);
		}
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected boolean save() {
		// save ignore states
		ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
		ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
		ignoreRWECheckBoxSelected = ignoreRWECheckBox.isSelected();
		ignoreLoadCheckBoxSelected = ignoreLoadCheckBox.isSelected();
		ignoreDestinationCheckBoxSelected = ignoreDestinationCheckBox.isSelected();
		ignoreFinalDestinationCheckBoxSelected = ignoreFinalDestinationCheckBox.isSelected();
		ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

		int rows[] = _carsTable.getSelectedRows();
		if (rows.length == 0)
			JOptionPane.showMessageDialog(this, Bundle.getString("selectCars"),
					Bundle.getString("carNoneSelected"), JOptionPane.WARNING_MESSAGE);

		for (int i = 0; i < rows.length; i++) {
			Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(rows[i]));
			if (_car == null) {
				super.loadCar(car);
				continue;
			}
			if (i == 0 && car != _car) {
				log.debug("Default car isn't the first one selected");
				if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
						Bundle.getString("doYouWantToChange"), new Object[] { car.toString() }),
						Bundle.getString("changeDefaultCar"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					super.loadCar(car); // new default car
					break; // done, don't modify any of the cars selected
				}
			}
			if (!super.change(car))
				return false;
		}
		return true;
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarsSetFrame.class
			.getName());
}
