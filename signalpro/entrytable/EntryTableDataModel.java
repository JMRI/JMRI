// AutomatTableDataModel.java

package signalpro.entrytable;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.jmrix.loconet.Se8AlmImplementation;

import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import com.sun.java.util.collections.List;

/**
 * Table data model for display layout config.
 *<P>
 * This class works in terms of the visible numbers (1 based),
 * while the Se8AlmImplementation class works in terms of the
 * underlying (0 based) numbers.
 *
 *
 * @author		Bob Jacobsen   Copyright (C) 2004
 * @version		$Revision: 1.1.1.1 $
 */
public class EntryTableDataModel extends javax.swing.table.AbstractTableModel
            implements PropertyChangeListener  {

    static final int NUMBER_COL         = 0;		// SE number
    
    static final int SENSOR_COL         = 1;		// connected occupancy det
    static final int SENSOR_STAT_COL    = 2;		// state of connected det

    static final int TURNOUT_COL        = 3;		// connected turnout
    static final int TURNOUT_STAT_COL   = 4;		// state of connected turnout

    static final int CONNECT_A_SE_COL   = 5;		// display name
    static final int CONNECT_A_LG_COL   = 6;		// display name

    static final int CONNECT_B_SE_COL   = 7;		// display name
    static final int CONNECT_B_LG_COL   = 8;		// display name

    static final int CONNECT_C_SE_COL   = 9;		// display name
    static final int CONNECT_C_LG_COL   = 10;		// display name

    static final int RD_COL             = 11;		// Read button
    static final int WR_COL             = 12;		// write button

    static final int NUMCOLUMN = 13;

    static final int NUM_SE    = 16;
    
    // only need one ResourceBundle
    static final ResourceBundle rb = ResourceBundle.getBundle("SignalPro.entrytable.EntryTableBundle");

    // only need one ALM image
    static Se8AlmImplementation alm;
    
    class Holder {
        Holder(int se) { this.se = se; }
        int se;
        
        Integer get_A_con() { return new Integer(alm.getACon(se)+1); }
        void set_A_con(Integer v) { alm.setACon(se, v.intValue()-1); }
        Integer get_B_con() { return new Integer(alm.getBCon(se)+1); }
        void set_B_con(Integer v) { alm.setBCon(se, v.intValue()-1); }
        Integer get_C_con() { return new Integer(alm.getCCon(se)+1); }
        void set_C_con(Integer v) { alm.setCCon(se, v.intValue()-1); }

        Integer get_A_leg() { return new Integer(alm.getALeg(se)+1); }
        void set_A_leg(Integer v) { alm.setALeg(se, v.intValue()-1); }
        Integer get_B_leg() { return new Integer(alm.getBLeg(se)+1); }
        void set_B_leg(Integer v) { alm.setBLeg(se, v.intValue()-1); }
        Integer get_C_leg() { return new Integer(alm.getCLeg(se)+1); }
        void set_C_leg(Integer v) { alm.setCLeg(se, v.intValue()-1); }

        Integer get_TO() { return new Integer(alm.getTO(se)+1); }
        void set_TO(Integer v) { alm.setTO(se, v.intValue()-1); }
        Integer get_DS() { return new Integer(alm.getDS(se)+1); }
        void set_DS(Integer v) { alm.setDS(se, v.intValue()-1); }
        
    }
    
    Holder[] tdata = new Holder[NUM_SE];
    
    public EntryTableDataModel() {
        super();
        
        // create temporary data location
        for (int i = 0; i<NUM_SE; i++)
            tdata[i] = new Holder(i+1); // SEs are numbered from 1
            
        // connect to Alm image
        alm = new Se8AlmImplementation(4, true){  // true means image of real one
            public void noteChanged(int block) {
                super.noteChanged(block);  // do the regular stuff first
                // some data has changed, copy it over for now
                int row = block/(Se8AlmImplementation.ENTRYSIZE/4); // rows numbered from zero
                fireTableRowsUpdated(row, row);
            }
        };
        
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check if from a TO
        for (int i = 0; i<NUM_SE; i++) {
            if (e.getSource().equals(turnouts[i])) {
                // we accept either commanded or known state, so don't check
                // new value; just mark as changed
                fireTableCellUpdated( i, TURNOUT_STAT_COL);
                
            }
        }
        
        // check if from a DS
        for (int i = 0; i<NUM_SE; i++) {
            if (e.getSource().equals(sensors[i])) {
                // don't check
                // new value; just mark as changed
                fireTableCellUpdated( i, SENSOR_STAT_COL);
                
            }
        }
        
    }


    public int getColumnCount( ){ return NUMCOLUMN;}
    public int getRowCount() {
        return NUM_SE;
    }

    public String getColumnName(int col) {
        switch (col) {
        case NUMBER_COL:        return "SE";

        case SENSOR_COL:        return "DS";

        case SENSOR_STAT_COL:
                                return "DS Occ";

        case TURNOUT_STAT_COL:
                                return "TO Thrown";

        case TURNOUT_COL:       return "TO";

        case CONNECT_A_SE_COL:  return "A goes to";
        case CONNECT_A_LG_COL:  return "leg";

        case CONNECT_B_SE_COL:  return "B goes to";
        case CONNECT_B_LG_COL:  return "leg";

        case CONNECT_C_SE_COL:  return "C goes to";
        case CONNECT_C_LG_COL:  return "leg";

        case RD_COL:            return " ";
        case WR_COL:            return " ";

        default: return "unknown";
        }
    }

	/**
	 * Note that this returns String even for
	 * columns that contain buttons
	 */
    public Class getColumnClass(int col) {
        switch (col) {
        case SENSOR_STAT_COL:
        case TURNOUT_STAT_COL:
            return Boolean.class;
            
        case CONNECT_A_LG_COL:
        case CONNECT_B_LG_COL:
        case CONNECT_C_LG_COL:
            return JComboBox.class;

        case NUMBER_COL: 
        case SENSOR_COL:
        case TURNOUT_COL:
        case CONNECT_A_SE_COL:
        case CONNECT_B_SE_COL:
        case CONNECT_C_SE_COL:
        	return Integer.class;
        	
        default:
            return String.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case SENSOR_COL:
        case SENSOR_STAT_COL:
        case TURNOUT_COL:
        case TURNOUT_STAT_COL:
        case CONNECT_A_SE_COL:
        case CONNECT_B_SE_COL:
        case CONNECT_C_SE_COL:
        case CONNECT_A_LG_COL:
        case CONNECT_B_LG_COL:
        case CONNECT_C_LG_COL:
        case RD_COL:
        case WR_COL:
            return true;
        default:
            return false;
        }
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
        case NUMBER_COL:        return new Integer(tdata[row].se);

        case SENSOR_COL:        return tdata[row].get_DS();

        case SENSOR_STAT_COL: 
            if (sensors[row]==null) return new Boolean(false);
            else return new Boolean(sensors[row].getKnownState()==jmri.Sensor.ACTIVE);

        case TURNOUT_STAT_COL:
            if (turnouts[row]==null) return new Boolean(false);
            else return new Boolean(turnouts[row].getCommandedState()==jmri.Turnout.THROWN);
         
        case TURNOUT_COL:        return tdata[row].get_TO();
        case CONNECT_A_SE_COL:         return tdata[row].get_A_con();
        case CONNECT_A_LG_COL:     return legValue(tdata[row].get_A_leg().intValue());

        case CONNECT_B_SE_COL:         return tdata[row].get_B_con();
        case CONNECT_B_LG_COL:         return legValue(tdata[row].get_B_leg().intValue());

        case CONNECT_C_SE_COL:         return tdata[row].get_C_con();
        case CONNECT_C_LG_COL:         return legValue(tdata[row].get_C_leg().intValue());
        
        case RD_COL:                return "Read";
        case WR_COL:                return "Write";
        default:
            log.error("internal state inconsistent with table get requst for "+row+" "+col);
            return null;
        }
    };

    JComboBox legValue(int value) {
        int index = value & (16384-1);
        JComboBox j = new JComboBox(new String[]{"none", "A","B","C"});
        j.setSelectedIndex(index);
        return j;
    }
    
    public int getPreferredWidth(int col) {
        switch (col) {
        default:
            return new JTextField(5).getPreferredSize().width;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case TURNOUT_COL: 
            tdata[row].set_TO((Integer) value);
            resetTO(row);
            return;

        case TURNOUT_STAT_COL:        
            if (turnouts[row] == null) return;
            if (value.equals(new Boolean(true)))
                turnouts[row].setCommandedState(jmri.Turnout.THROWN);
            else
                turnouts[row].setCommandedState(jmri.Turnout.CLOSED);            
            return;

        case SENSOR_COL:        
            tdata[row].set_DS((Integer) value);
            resetDS(row);
            return;
        
        case CONNECT_A_SE_COL:         tdata[row].set_A_con((Integer) value);return;
        case CONNECT_A_LG_COL:         tdata[row].set_A_leg(new Integer(((JComboBox) value).getSelectedIndex()));return;
        
        case CONNECT_B_SE_COL:         tdata[row].set_B_con((Integer) value);return;
        case CONNECT_B_LG_COL:         tdata[row].set_B_leg(new Integer(((JComboBox) value).getSelectedIndex()));return;

        case CONNECT_C_SE_COL:         tdata[row].set_C_con((Integer) value);return;
        case CONNECT_C_LG_COL:         tdata[row].set_C_leg(new Integer(((JComboBox) value).getSelectedIndex()));return;
        
        case RD_COL:
            // start read process
            alm.triggerRead(tdata[row].se);
            return;
        
        case WR_COL:
            // start write process
            alm.triggerWrite(tdata[row].se);
            return;
            
        default:
            log.error("internal state inconsistent with table put requst for "+row+" "+col);
        }
    }

    jmri.Turnout[] turnouts = new jmri.Turnout[NUM_SE];
    jmri.Sensor[] sensors = new jmri.Sensor[NUM_SE];
    
    /**
     * TO number (possibly) changed, connect to
     * new Turnout object
     */
    void resetTO(int i) {
        if (turnouts[i]!=null)
            turnouts[i].removePropertyChangeListener(this);
        turnouts[i] = jmri.InstanceManager.turnoutManagerInstance().provideTurnout(""+tdata[i].get_TO());
        turnouts[i].addPropertyChangeListener(this);
    }
    
    /**
     * DS number (possibly) changed, connect to
     * new Sensor object
     */
    void resetDS(int i) {
        if (sensors[i]!=null)
            sensors[i].removePropertyChangeListener(this);
        sensors[i] = jmri.InstanceManager.sensorManagerInstance().provideSensor(""+tdata[i].get_DS());
        sensors[i].addPropertyChangeListener(this);
    }
    
    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param table
     */
    public void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // add renderer, editor for JComboBox
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // resize columns as requested
        for (int i=0; i<table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);

        // have the value column hold a button
        setColumnToHoldButton(table, RD_COL, new JButton("Read"));
        setColumnToHoldButton(table, WR_COL, new JButton("Write"));
    }

    /**
     * Service method to setup a column so that it will hold a
     * button for it's values
     * @param table
     * @param column
     * @param sample Typical button, used for size
     */
    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        TableColumnModel tcm = table.getColumnModel();

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);

        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
			.setPreferredWidth(sample.getPreferredSize().width);
    }

    synchronized public void dispose() {
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EntryTableDataModel.class.getName());

}
