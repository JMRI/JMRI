// SchedulesTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.List;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.util.Hashtable;

/**
 * Table Model for edit of schedules used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2011
 * @version   $Revision$
 */
public class SchedulesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {
   
    ScheduleManager manager;						// There is only one manager
 
    // Defines the columns
    private static final int IDCOLUMN = 0;
    private static final int NAMECOLUMN = IDCOLUMN+1;
    private static final int SCH_STATUSCOLUMN = NAMECOLUMN+1;
    private static final int SPURCOLUMN = SCH_STATUSCOLUMN+1;
    private static final int STATUSCOLUMN = SPURCOLUMN+1;
    private static final int EDITCOLUMN = STATUSCOLUMN+1;
    private static final int DELETECOLUMN = EDITCOLUMN+1;
    
    private static final int HIGHESTCOLUMN = DELETECOLUMN+1;

    public SchedulesTableModel() {
        super();
        manager = ScheduleManager.instance();
        manager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	synchronized (this){
    		_sort = sort;
    	}
        updateList();
        fireTableDataChanged();
    }
     
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeSchedules();
    	
		if (_sort == SORTBYID)
			sysList = manager.getSchedulesByIdList();
		else
			sysList = manager.getSchedulesByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("schedule ids: " + (String) sysList.get(i));
			manager.getScheduleById(sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List<String> sysList = null;
    
	void initTable(SchedulesTableFrame frame, JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETECOLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        setPreferredWidths(frame, table);

		// set row height
		table.setRowHeight(new JComboBox().getPreferredSize().height);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
	
	private void setPreferredWidths(SchedulesTableFrame frame, JTable table){
		if (frame.loadTableDetails(table))
			return;	// done
        log.debug("Setting preferred widths");
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(SCH_STATUSCOLUMN).setPreferredWidth(80);
		table.getColumnModel().getColumn(SPURCOLUMN).setPreferredWidth(350);
		table.getColumnModel().getColumn(STATUSCOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETECOLUMN).setPreferredWidth(90);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return Bundle.getMessage("Id");
        case NAMECOLUMN: return Bundle.getMessage("Name");
        case SCH_STATUSCOLUMN: return Bundle.getMessage("Status");
        case SPURCOLUMN: return Bundle.getMessage("Spurs");
        case STATUSCOLUMN: return Bundle.getMessage("StatusSpur");
        case EDITCOLUMN: return Bundle.getMessage("Edit");
        case DELETECOLUMN: return Bundle.getMessage("Delete");
        default: return "unknown"; // NOI18N
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case SCH_STATUSCOLUMN: return String.class;
        case SPURCOLUMN: return JComboBox.class;
        case STATUSCOLUMN: return String.class;
        case EDITCOLUMN: return JButton.class;
        case DELETECOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case EDITCOLUMN: 
        case DELETECOLUMN:
        case SPURCOLUMN:
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col) {
       	// Funky code to put the sef frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusSef){
    		focusSef = false;
    		sef.requestFocus();
    	}
       	if (row >= sysList.size())
    		return "ERROR row "+row; // NOI18N
    	String id = sysList.get(row);
    	Schedule s = manager.getScheduleById(id);
       	if (s == null)
    		return "ERROR schedule unknown "+row; // NOI18N
        switch (col) {
        case IDCOLUMN: return s.getId();
        case NAMECOLUMN: return s.getName();
        case SCH_STATUSCOLUMN: return getScheduleStatus(row);
        case SPURCOLUMN: {
        	JComboBox box = manager.getSpursByScheduleComboBox(s);
        	String index = comboSelect.get(sysList.get(row));
        	if (index != null){
        		box.setSelectedIndex(Integer.parseInt(index));
        	}
        	return box;
        }
        case STATUSCOLUMN: return getSpurStatus(row);
        case EDITCOLUMN: return Bundle.getMessage("Edit");
        case DELETECOLUMN: return Bundle.getMessage("Delete");
        default: return "unknown "+col; // NOI18N
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editSchedule(row);
        	break;
        case DELETECOLUMN: deleteSchedule(row);
    		break;
        case SPURCOLUMN: selectJComboBox(value, row);
        	break;
        default:
            break;
        }
    }

    boolean focusSef = false;
    ScheduleEditFrame sef = null;
    private void editSchedule (int row){
    	log.debug("Edit schedule");
    	if (sef != null)
    		sef.dispose();
    	Schedule s = manager.getScheduleById(sysList.get(row));
    	LocationTrackPair ltp = getLocationTrackPair(row);
    	if (ltp == null){
    		log.debug("Need location track pair");
			JOptionPane.showMessageDialog(null,
					MessageFormat.format(Bundle.getMessage("AssignSchedule"),new Object[]{s.getName()}),
					MessageFormat.format(Bundle.getMessage("CanNotSchedule"),new Object[]{Bundle.getMessage("Edit")}),
					JOptionPane.ERROR_MESSAGE);
    		return;
    	}
       	sef = new ScheduleEditFrame();
    	sef.setTitle(MessageFormat.format(Bundle.getMessage("TitleScheduleEdit"), new Object[]{ltp.getTrack().getName()}));
    	sef.initComponents(s, ltp.getLocation(), ltp.getTrack());
    	focusSef = true;
    }
    
    private void deleteSchedule (int row){
    	log.debug("Delete schedule");
    	Schedule s = manager.getScheduleById(sysList.get(row));
    	if (JOptionPane.showConfirmDialog(null,
    			MessageFormat.format(Bundle.getMessage("DoYouWantToDeleteSchedule"),new Object[]{s.getName()}),
    			Bundle.getMessage("DeleteSchedule?"),
    			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
    		manager.deregister(s);
    		OperationsXml.save();
    	}
    }

    protected Hashtable<String, String> comboSelect = new Hashtable<String, String>();
    private void selectJComboBox (Object value, int row){
    	String id = sysList.get(row);
    	JComboBox box = (JComboBox)value;
    	comboSelect.put(id, Integer.toString(box.getSelectedIndex()));
    	fireTableRowsUpdated(row, row);
    }
    
    private LocationTrackPair getLocationTrackPair(int row){
       	Schedule s = manager.getScheduleById(sysList.get(row));
       	JComboBox box = manager.getSpursByScheduleComboBox(s);
    	String index = comboSelect.get(sysList.get(row));
    	LocationTrackPair ltp;
    	if (index != null){
    		ltp = (LocationTrackPair)box.getItemAt(Integer.parseInt(index));
    	} else {
    		ltp = (LocationTrackPair)box.getItemAt(0);
    	}
    	return ltp;
    }
    
    private String getScheduleStatus(int row){
    	Schedule sch = manager.getScheduleById(sysList.get(row));
       	JComboBox box = manager.getSpursByScheduleComboBox(sch); 
       	for (int i=0; i<box.getItemCount(); i++){
           	LocationTrackPair ltp = (LocationTrackPair)box.getItemAt(i);
           	String status = ltp.getTrack().checkScheduleValid();
           	if (!status.equals(""))
           		return Bundle.getMessage("Error");
       	}
       	return Bundle.getMessage("Okay");
    }
    
    private String getSpurStatus(int row){
     	LocationTrackPair ltp = getLocationTrackPair(row);
    	if (ltp == null)
    		return "";
    	String status = ltp.getTrack().checkScheduleValid();
    	if (!status.equals(""))
    		return status;
    	return Bundle.getMessage("Okay");
    }
    
    private void removePropertyChangeSchedules() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Schedule l = manager.getScheduleById(sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
    		}
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
       	if (sef != null)
    		sef.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeSchedules();
    }

    //check for change in number of schedules, or a change in a schedule
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	 if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)) {
             updateList();
             fireTableDataChanged();
    	 }
    	 else if (e.getSource().getClass().equals(Schedule.class)){
    		 String id = ((Schedule) e.getSource()).getId();
    		 int row = sysList.indexOf(id);
    		 if (Control.showProperty && log.isDebugEnabled()) log.debug("Update schedule table row: "+row + " id: " + id);
    		 if (row >= 0)
    			 fireTableRowsUpdated(row, row);
    	 }
    }
    

    static Logger log = LoggerFactory.getLogger(SchedulesTableModel.class.getName());
}

