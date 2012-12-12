// CarsSetFrameAction.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JTable;

/**
 * Swing action to create and register a CarsSetFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class CarsSetFrameAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

    CarsTableModel _carsTableModel;
    JTable _carsTable;
    
    public CarsSetFrameAction(String s) {
    	super(s);
    }

    public CarsSetFrameAction(CarsTableModel carsTableModel, JTable carsTable){
    	this(rb.getString("TitleSetCars"));
    	_carsTableModel = carsTableModel;
    	_carsTable = carsTable;
    }

    public void actionPerformed(ActionEvent e) {
        // create a car table frame
        CarsSetFrame csf = new CarsSetFrame();
        csf.initComponents(_carsTableModel, _carsTable);
    }
}

/* @(#)CarsSetFrameAction.java */
