// PacketDataModel.java

package jmri.jmrix.pricom.pockettester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.Vector;

/**
 * Table data model for display of DCC packet contents
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version		$Revision$
 */
public class PacketDataModel extends javax.swing.table.AbstractTableModel  {

    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
    static public final int ADDRESSCOLUMN = 0;
    static public final int TYPECOLUMN = 1;
    static public final int DETAILCOLUMN = 2;
    static public final int MONITORBUTTONCOLUMN = 3;

    static public final int NUMCOLUMN = 4;

    /**
     * Returns the number of rows to be displayed.  This
     * can vary depending on what has been seen
     */
    public int getRowCount() {
        return keys.size();
    }


    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case ADDRESSCOLUMN: return rb.getString("ColumnAddress");
        case TYPECOLUMN: return rb.getString("ColumnType");   
        case DETAILCOLUMN: return rb.getString("ColumnDetails");   
        case MONITORBUTTONCOLUMN: return "";   // no heading, as button is clear
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case ADDRESSCOLUMN:
        case TYPECOLUMN:
        case DETAILCOLUMN:
            return String.class;
        case MONITORBUTTONCOLUMN:
            return JButton.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case MONITORBUTTONCOLUMN:
            return true;
        default:
            return false;
        }
    }

    static final Boolean True = Boolean.valueOf(true);
    static final Boolean False = Boolean.valueOf(false);

    public Object getValueAt(int row, int col) {

        switch (col) {
        case ADDRESSCOLUMN:  // slot number
            return addresses.elementAt(row);
        case TYPECOLUMN:  //
            return types.elementAt(row);
        case DETAILCOLUMN:  //
            return details.elementAt(row);
        case MONITORBUTTONCOLUMN:  //
            return rb.getString("ButtonTrace");
        default:
            log.error("internal state inconsistent with table request for "+row+" "+col);
            return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
        case ADDRESSCOLUMN:
            return new JTextField(8).getPreferredSize().width;
        case TYPECOLUMN:
            return new JTextField(12).getPreferredSize().width;
        case DETAILCOLUMN:
            return new JTextField(12).getPreferredSize().width;
        case MONITORBUTTONCOLUMN:
            return new JButton("Details").getPreferredSize().width;
        default:
            return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case MONITORBUTTONCOLUMN: 
		    MonitorFrame f = new MonitorFrame();
		    try {
			    f.initComponents();
                f.setFilter((String)getValueAt(row, ADDRESSCOLUMN));
                source.addListener(f);
			} catch (Exception ex) {
			    log.error("starting MonitorFrame caught exception: "+ex.toString());
			}
		    f.setVisible(true);
            
            return;
        default:
            return;
        }
    }

    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param slotTable
     */
    public void configureTable(JTable slotTable) {
        // allow reordering of the columns
        slotTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i=0; i<slotTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            slotTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        slotTable.sizeColumnsToFit(-1);

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, PacketDataModel.MONITORBUTTONCOLUMN);

    }

    void setColumnToHoldButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
    }



    public void dispose() {
    }

    public void asciiFormattedMessage(String m) {
        String key = getKey(m);
        if (key == null) return;  // ignore this input
        
        String address = getPrefix(m);
        String type = getType(m);
        String detail = getDetails(m);
        
        // see if exists
        int index = keys.indexOf(key);
        
        if (index == -1) {
            // not present, add
            keys.addElement(key);
            addresses.addElement(address);
            types.addElement(type);
            details.addElement(detail);
            
            index = keys.indexOf(key);
            fireTableRowsInserted(index, index);
        } else {
            // index has been set, update  
            keys.setElementAt(key, index);
            addresses.setElementAt(address, index);
            types.setElementAt(type, index);
            details.setElementAt(detail, index);
            fireTableRowsUpdated(index, index);
        }
    }

    DataSource source;
    public void setSource(DataSource d) {
        source = d;
    }
    
    public void reset() {
        int count = keys.size();
        keys = new Vector<String>();
        addresses = new Vector<String>();
        types = new Vector<String>();
        details = new Vector<String>();
        fireTableRowsDeleted(0, count-1);
    }
    
    Vector<String> keys = new Vector<String>();
    Vector<String> addresses = new Vector<String>();
    Vector<String> types = new Vector<String>();
    Vector<String> details = new Vector<String>();
    
    /**
     * Find the display key from the current input line.
     * A later input line that maps to the same key will overwrite the
     * earlier line.
     *<P>
     * The current implementation is address+type, so that separate
     * lines will be used for each type sent to the same address.
     *
     * @return null if not to be displayed, e.g. no address
     */
    String getKey(String s) {
        if (!s.startsWith("ADR=")) 
            return null;
        else 
            return s.substring(0,22);
    }
    
    /**
     * Find the address (1st column) from the current input line
     */
    String getPrefix(String s) {
        return s.substring(0,8);
    }
    
    /**
     * Find the message type (2nd column) from the current input line.
     * Should not be called if getPrefix has returned null.
     * @return null if not to be displayed, e.g. too short
     */
    String getType(String s) {
        return s.substring(9, 22);
    }
    
    /**
     * Find the message arguments (3rd column) from the current input line.
     * Should not be called if getPrefix has returned null.
     * @return null if not to be displayed, e.g. too short
     */
    String getDetails(String s) {
        return s.substring(23, s.length()-1);
    }
    
    static Logger log = LoggerFactory.getLogger(PacketDataModel.class.getName());

}
