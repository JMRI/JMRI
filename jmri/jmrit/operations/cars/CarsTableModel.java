// CarsTableModel.java

package jmri.jmrit.operations.cars;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import jmri.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of cars used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.2 $
 */
public class CarsTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle");
   
    CarManager manager = CarManager.instance();		// There is only one manager
 
    // Defines the columns
    private static final int NUMCOLUMN   = 0;
    private static final int ROADCOLUMN   = 1;
    private static final int TYPECOLUMN = 2;
    private static final int LENGTHCOLUMN = 3;
    private static final int COLORCOLUMN = 4;
    private static final int KERNELCOLUMN  = 5;
    private static final int LOCATIONCOLUMN  = 6;
    private static final int DESTINATIONCOLUMN = 7;
    private static final int TRAINCOLUMN = 8;
    private static final int MOVESCOLUMN = 9;
    private static final int SETCOLUMN = 10;
    private static final int EDITCOLUMN = 11;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public CarsTableModel() {
        super();
        manager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNUMBER = 1;
    public final int SORTBYROAD = 2;
    public final int SORTBYTYPE = 3;
    public final int SORTBYLOCATION = 4;
    public final int SORTBYDESTINATION = 5;
    public final int SORTBYTRAIN = 6;
    public final int SORTBYMOVES = 7;
    public final int SORTBYKERNEL = 8;
    
    private int _sort = SORTBYNUMBER;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
    /**
     * Search for car by road number
     * @param roadNumber
     * @return -1 if not found, table row number if found
     */
    public int findCarByRoadNumber (String roadNumber){
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				Car c = manager.getCarById((String) sysList.get(i));
				if (c != null){
					if (c.getNumber().equals(roadNumber)){
//						log.debug("found road number match "+roadNumber);
						return i;
					}
				}
			}
		}
		return -1;
    }
    
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeCars();
     	sysList = getSelectedCarList();
 		// and add listeners back in
		for (int i = 0; i < sysList.size(); i++)
			manager.getCarById((String) sysList.get(i))
					.addPropertyChangeListener(this);
	}
    
    public List getSelectedCarList(){
    	List list;
		if (_sort == SORTBYROAD)
			list = manager.getCarsByRoadNameList();
		else if (_sort == SORTBYTYPE)
			list = manager.getCarsByTypeList();
		else if (_sort == SORTBYLOCATION)
			list = manager.getCarsByLocationList();
		else if (_sort == SORTBYDESTINATION)
			list = manager.getCarsByDestinationList();
		else if (_sort == SORTBYTRAIN)
			list = manager.getCarsByTrainList();
		else if (_sort == SORTBYMOVES)
			list = manager.getCarsByMovesList();
		else if (_sort == SORTBYKERNEL)
			list = manager.getCarsByKernelList();
		else
			list = manager.getCarsByNumberList();
		return list;
    }

	List sysList = null;
    
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		tcm.getColumn(SETCOLUMN).setCellRenderer(buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(SETCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(NUMCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(ROADCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(COLORCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(TYPECOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(KERNELCOLUMN).setPreferredWidth(75);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(35);
		table.getColumnModel().getColumn(LOCATIONCOLUMN).setPreferredWidth(190);
		table.getColumnModel().getColumn(DESTINATIONCOLUMN).setPreferredWidth(190);
		table.getColumnModel().getColumn(TRAINCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(MOVESCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(SETCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case NUMCOLUMN: return rb.getString("Number");
        case ROADCOLUMN: return rb.getString("Road");
        case COLORCOLUMN: return rb.getString("Color");
        case TYPECOLUMN: return rb.getString("Type");
        case LENGTHCOLUMN: return rb.getString("Len");
        case KERNELCOLUMN: return rb.getString("Kernel");
        case LOCATIONCOLUMN: return rb.getString("Location");
        case DESTINATIONCOLUMN: return rb.getString("Destination");
        case TRAINCOLUMN: return rb.getString("Train");
        case MOVESCOLUMN: return rb.getString("Moves");
        case SETCOLUMN: return rb.getString("Location");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case NUMCOLUMN: return String.class;
        case ROADCOLUMN: return String.class;
        case COLORCOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case TYPECOLUMN: return String.class;
        case KERNELCOLUMN: return String.class;
        case LOCATIONCOLUMN: return String.class;
        case DESTINATIONCOLUMN: return String.class;
        case TRAINCOLUMN: return String.class;
        case MOVESCOLUMN: return String.class;
        case SETCOLUMN: return JButton.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case SETCOLUMN: 
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public String getName(int row) {  // name is text number
        return "";
    }

    public String getValString(int row) {
        return "";
    }

    public Object getValueAt(int row, int col) {
    	String carId = (String)sysList.get(row);
    	Car c = manager.getCarById(carId);
        switch (col) {
        case NUMCOLUMN: return c.getNumber();
        case ROADCOLUMN: return c.getRoad();
        case COLORCOLUMN: return c.getColor();
        case LENGTHCOLUMN: return c.getLength();
        case TYPECOLUMN: {
        	if (c.isCaboose())
        		return c.getType()+" (C)";
        	else if (c.hasFred())
        		return c.getType()+" (F)";
        	else if (c.isHazardous())
        		return c.getType()+" (H)";
        	else
        		return c.getType();
        }
        case KERNELCOLUMN: {
        	if (c.getKernel() != null && c.getKernel().isLeadCar(c))
        		return c.getKernelName()+"*";
        	else
        		return c.getKernelName();
        }
        case LOCATIONCOLUMN: {
        	String s ="";
        	if (!c.getLocationName().equals(""))
        		s = c.getLocationName() + " (" + c.getSecondaryLocationName() + ")";
        	return s;
        }
        case DESTINATIONCOLUMN: {
        	String s ="";
        	if (!c.getDestinationName().equals(""))
        		s = c.getDestinationName() + " (" + c.getSecondaryDestinationName() + ")";
        	return s;
        }
        case TRAINCOLUMN: return c.getTrain();
        case MOVESCOLUMN: return Integer.toString(c.getMoves());
        case SETCOLUMN: return "Set";
        case EDITCOLUMN: return "Edit";
 
        default: return "unknown "+col;
        }
    }

    CarsEditFrame cef = null;
    CarsSetFrame csf = null;
    
    public void setValueAt(Object value, int row, int col) {
		String carId = (String)sysList.get(row);
    	Car car = manager.getCarById(carId);
        switch (col) {
        case SETCOLUMN:
        	log.debug("Set car location");
           	if (csf != null)
           		csf.dispose();
       		csf = new CarsSetFrame();
    		csf.initComponents();
	    	csf.loadCar(car);
	    	csf.setTitle("Set car location");
	    	csf.setVisible(true);
	    	csf.setExtendedState(csf.NORMAL);
        	break;
        case EDITCOLUMN:
        	log.debug("Edit car");
        	if (cef != null)
        		cef.dispose();
    		cef = new CarsEditFrame();
    		cef.initComponents();
	    	cef.loadCar(car);
	    	cef.setTitle("Edit Car");
	    	cef.setVisible(true);
	    	cef.setExtendedState(cef.NORMAL);
        	break;
        default:
            break;
        }
    }
    
    private boolean buttonEnabled(int row){
		Car car = manager.getCarById((String)sysList.get(row));
		if (car.getRouteLocationId() == null)
			return true;
		else
			return false;
    }

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
    }

   public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose CarTableModel");
        manager.removePropertyChangeListener(this);
        removePropertyChangeCars();
        if (csf != null)
        	csf.dispose();
        if (cef != null)
        	cef.dispose();
     }
    
    private void removePropertyChangeCars() {
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Car c = manager.getCarById((String) sysList.get(i));
				if (c != null)
					c.removePropertyChangeListener(this);
			}
		}
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(CarManager.LISTLENGTH) || e.getPropertyName().equals(CarManager.KERNELLISTLENGTH)) {
    		updateList();
    		fireTableDataChanged();
    	}
    	else {
    		// must be a car change
    		String carId = ((Car) e.getSource()).getId();
    		int row = sysList.indexOf(carId);
    		if(Control.showProperty && log.isDebugEnabled()) log.debug("Update car table row: "+row);
    		fireTableRowsUpdated(row, row);
    	}
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CarsTableModel.class.getName());
}

