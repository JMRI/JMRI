// CarsSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JTable;


/**
 * Frame for user to place a group of cars on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2011
 * @version $Revision: 1.12 $
 */

public class CarsSetFrame extends CarSetFrame implements java.beans.PropertyChangeListener {
	
	CarsTableModel _carsTableModel;
	JTable _carsTable;
		
	public CarsSetFrame() {
		super();
	}

	public void initComponents(CarsTableModel carsTableModel, JTable carsTable){
		_carsTableModel = carsTableModel;
		_carsTable = carsTable;
		
		// modify Save button text to "Change"
		saveButton.setText(rb.getString("Change"));
		// disable edit load button if no cars selected
		editLoadButton.setEnabled(false);
		
		int rows[] = _carsTable.getSelectedRows();
		super.initComponents();
		Car car;
		if (rows.length >0)
			car = _carsTableModel.getCarAtIndex(rows[0]);
		else
			return;		
		super.loadCar(car);
	}
	
	protected boolean save(){
		int rows[] = _carsTable.getSelectedRows();	
		for (int i=0; i<rows.length; i++){
			Car car = _carsTableModel.getCarAtIndex(rows[i]);
			if (!super.change(car))
				return false;
		}
		return true;
	}


	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarsSetFrame.class.getName());
}
