// EnginesTableModel.java

package jmri.jmrit.operations.engines;

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
 * Table Model for edit of engines used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */
public class EnginesTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");
   
    EngineManager manager = EngineManager.instance();		// There is only one manager
 
    // Defines the columns
    private static final int NUMCOLUMN   = 0;
    private static final int ROADCOLUMN   = 1;
    private static final int MODELCOLUMN = 2;
    private static final int LENGTHCOLUMN = 3;
    private static final int CONSISTCOLUMN  = 4;
    private static final int LOCATIONCOLUMN  = 5;
    private static final int DESTINATIONCOLUMN = 6;
    private static final int TRAINCOLUMN = 7;
    private static final int MOVESCOLUMN = 8;
    private static final int SETCOLUMN = 9;
    private static final int EDITCOLUMN = 10;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public EnginesTableModel() {
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
    public final int SORTBYCONSIST = 8;
    
    private int _sort = SORTBYNUMBER;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
    /**
     * Search for engine by road number
     * @param roadNumber
     * @return -1 if not found, table row number if found
     */
    public int findEngineByRoadNumber (String roadNumber){
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				Engine c = manager.getEngineById((String) sysList.get(i));
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
    	removePropertyChangeEngines();
     	sysList = getSelectedEngineList();
 		// and add listeners back in
		for (int i = 0; i < sysList.size(); i++)
			manager.getEngineById((String) sysList.get(i))
					.addPropertyChangeListener(this);
	}
    
    public List getSelectedEngineList(){
    	List list;
		if (_sort == SORTBYROAD)
			list = manager.getEnginesByRoadNameList();
		else if (_sort == SORTBYTYPE)
			list = manager.getEnginesByTypeList();
		else if (_sort == SORTBYLOCATION)
			list = manager.getEnginesByLocationList();
		else if (_sort == SORTBYDESTINATION)
			list = manager.getEnginesByDestinationList();
		else if (_sort == SORTBYTRAIN)
			list = manager.getEnginesByTrainList();
		else if (_sort == SORTBYMOVES)
			list = manager.getEnginesByMovesList();
		else if (_sort == SORTBYCONSIST)
			list = manager.getEnginesByConsistList();
		else
			list = manager.getEnginesByNumberList();
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
		table.getColumnModel().getColumn(MODELCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(CONSISTCOLUMN).setPreferredWidth(75);
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
        case MODELCOLUMN: return rb.getString("Model");
        case LENGTHCOLUMN: return rb.getString("Len");
        case CONSISTCOLUMN: return rb.getString("Consist");
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
        case LENGTHCOLUMN: return String.class;
        case MODELCOLUMN: return String.class;
        case CONSISTCOLUMN: return String.class;
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
    	String engineId = (String)sysList.get(row);
    	Engine engine = manager.getEngineById(engineId);
        switch (col) {
        case NUMCOLUMN: return engine.getNumber();
        case ROADCOLUMN: return engine.getRoad();
        case LENGTHCOLUMN: return engine.getLength();
        case MODELCOLUMN: return engine.getModel();
        
        case CONSISTCOLUMN: {
        	if (engine.getConsist() != null && engine.getConsist().isLeadEngine(engine))
        		return engine.getConsistName()+"*";
        	else
        		return engine.getConsistName();
        }
        case LOCATIONCOLUMN: {
        	String s ="";
        	if (!engine.getLocationName().equals(""))
        		s = engine.getLocationName() + " (" + engine.getSecondaryLocationName() + ")";
        	return s;
        }
        case DESTINATIONCOLUMN: {
        	String s ="";
        	if (!engine.getDestinationName().equals(""))
        		s = engine.getDestinationName() + " (" + engine.getSecondaryDestinationName() + ")";
        	return s;
        }
        case TRAINCOLUMN: return engine.getTrain();
        case MOVESCOLUMN: return Integer.toString(engine.getMoves());
        case SETCOLUMN: return "Set";
        case EDITCOLUMN: return "Edit";
 
        default: return "unknown "+col;
        }
    }

    EnginesEditFrame eef = null;
    EnginesSetFrame esf = null;
    
    public void setValueAt(Object value, int row, int col) {
		String engineId = (String)sysList.get(row);
    	Engine engine = manager.getEngineById(engineId);
        switch (col) {
        case SETCOLUMN:
        	log.debug("Set engine location");
           	if (esf == null){
        		esf = new EnginesSetFrame();
        		esf.initComponents();
        	}
	    	esf.loadEngine(engine);
	    	esf.setTitle("Set engine location");
	    	esf.setVisible(true);
	    	esf.setExtendedState(java.awt.Frame.NORMAL);
        	break;
        case EDITCOLUMN:
        	log.debug("Edit engine");
        	if (eef == null){
        		eef = new EnginesEditFrame();
        		eef.initComponents();
        	}
	    	eef.loadEngine(engine);
	    	eef.setTitle("Edit Engine");
	    	eef.setVisible(true);
	    	eef.setExtendedState(java.awt.Frame.NORMAL);
        	break;
        default:
            break;
        }
    }
    
    private boolean buttonEnabled(int row){
		Engine engine = manager.getEngineById((String)sysList.get(row));
		if (engine.getRouteLocationId() == null)
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
        if (log.isDebugEnabled()) log.debug("dispose EngineTableModel");
        manager.removePropertyChangeListener(this);
        removePropertyChangeEngines();
        if (esf != null)
        	esf.dispose();
        if (eef != null)
        	eef.dispose();
     }
    
    private void removePropertyChangeEngines() {
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Engine c = manager.getEngineById((String) sysList.get(i));
				if (c != null)
					c.removePropertyChangeListener(this);
			}
		}
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(EngineManager.LISTLENGTH) || e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH)) {
    		updateList();
    		fireTableDataChanged();
    	}
    	else {
    		// must be a engine change
    		String engineId = ((Engine) e.getSource()).getId();
    		int row = sysList.indexOf(engineId);
    		if(Control.showProperty && log.isDebugEnabled()) log.debug("Update engine table row: "+row);
    		fireTableRowsUpdated(row, row);
    	}
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EnginesTableModel.class.getName());
}

