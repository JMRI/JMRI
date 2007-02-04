// BeanTableDataModel.java

package jmri.jmrit.beantable;

import jmri.Manager;
import jmri.NamedBean;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import javax.swing.JComboBox;
import java.awt.Font;

import com.sun.java.util.collections.List;

/**
 * Table data model for display of NamedBean manager contents
 * @author		Bob Jacobsen   Copyright (C) 2003
 * @author      Dennis Miller   Copyright (C) 2006
 * @version		$Revision: 1.16 $
 */
abstract public class BeanTableDataModel extends javax.swing.table.AbstractTableModel
            implements PropertyChangeListener  {

    static public final int SYSNAMECOL  = 0;
    static public final int USERNAMECOL = 1;
    static public final int VALUECOL = 2;


    static public final int NUMCOLUMN = 3;

    public BeanTableDataModel() {
        super();
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    synchronized void updateNameList() {
        // first, remove listeners from the individual objects
        if (sysNameList != null) {
            for (int i = 0; i< sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName((String)sysNameList.get(i));
                if (b!=null)
                    b.removePropertyChangeListener(this);
            }
        }
        sysNameList = getManager().getSystemNameList();
        // and add them back in
        for (int i = 0; i< sysNameList.size(); i++)
            getBySystemName((String)sysNameList.get(i)).addPropertyChangeListener(this);
    }

    List sysNameList = null;

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            updateNameList();
            log.debug("Table changed length to "+sysNameList.size());
            fireTableDataChanged();
        } else if (matchPropertyName(e)) {
            // a value changed.  Find it, to avoid complete redraw
            String name = ((NamedBean)e.getSource()).getSystemName();
            if (log.isDebugEnabled()) log.debug("Update cell "+sysNameList.indexOf(name)+","
                                                +VALUECOL+" for "+name);
            // since we can add columns, the entire row is marked as updated
            int row = sysNameList.indexOf(name);
            fireTableRowsUpdated(row, row);
        }
    }

	/**
	 * Is this property event announcing a change this table should display?
	 * <P>
	 * Note that events will come both from the NamedBeans and also from the manager
	 */
	boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
		return (e.getPropertyName().indexOf("State")>=0 || e.getPropertyName().indexOf("Appearance")>=0);
	}

    public int getRowCount() {
        return sysNameList.size();
    }


    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case SYSNAMECOL: return "System Name";
        case USERNAMECOL: return "User Name";
        case VALUECOL: return "State";

        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case SYSNAMECOL:
        case USERNAMECOL:
            return String.class;
        case VALUECOL:
            return JButton.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case USERNAMECOL:
        case VALUECOL:
            return true;
        default:
            return false;
        }
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
        case SYSNAMECOL:  // slot number
            return sysNameList.get(row);
        case USERNAMECOL:  //
            return getBySystemName((String)sysNameList.get(row)).getUserName();
        case VALUECOL:  //
            return getValue((String)sysNameList.get(row));
        default:
            log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    };

    public int getPreferredWidth(int col) {
        switch (col) {
        case SYSNAMECOL:
            return new JTextField(5).getPreferredSize().width;
        case USERNAMECOL:
            return new JTextField(15).getPreferredSize().width;
        case VALUECOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            return new JTextField(22).getPreferredSize().width;
        default:
        	log.warn("Unexpected column in getPreferredWidth: "+col);
            return new JTextField(8).getPreferredSize().width;
        }
    }

    abstract public String getValue(String systemName);

    abstract Manager getManager();

    abstract NamedBean getBySystemName(String name);
    abstract void clickOn(NamedBean t);

    public void setValueAt(Object value, int row, int col) {
        if (col==USERNAMECOL) {
            getBySystemName((String)sysNameList.get(row))
                        .setUserName((String)value);
            fireTableRowsUpdated(row,row);
        } else if (col==VALUECOL) {
            // button fired, swap state
            NamedBean t = getBySystemName((String)sysNameList.get(row));
            clickOn(t);
        }
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

        // resize columns as requested
        for (int i=0; i<table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);

        // have the value column hold a button
        setColumnToHoldButton(table, VALUECOL, configureButton());
    }

    public JButton configureButton() {
        return new JButton(AbstractTableAction.rbean.getString("BeanStateInconsistent"));
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
		table.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
		table.setDefaultEditor(JButton.class,buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
			.setPreferredWidth(sample.getPreferredSize().width);
    }

    synchronized public void dispose() {
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i< sysNameList.size(); i++)
                getBySystemName((String)sysNameList.get(i)).removePropertyChangeListener(this);
        }
    }
    
    /**
     * Method to self print or print preview the table.
     * Printed in equally sized columns across the page with headings and
     * vertical lines between each column. Data is word wrapped within a column.
     * Can handle data as strings, comboboxes or booleans
     */
    public void printTable(HardcopyWriter w) {
        // determine the column size - evenly sized, with space between for lines
        int columnSize = (w.getCharactersPerLine()- this.getColumnCount() - 1)/this.getColumnCount();
        
        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
              (columnSize+1)*this.getColumnCount());
        
        // print the column header labels
        String[] columnStrings = new String[this.getColumnCount()];
        // Put each column header in the array
        for (int i = 0; i < this.getColumnCount(); i++){
            columnStrings[i] = this.getColumnName(i);
        }
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnSize);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize+1)*this.getColumnCount());
  
        // now print each row of data
        // create a base string the width of the column
        String spaces = "";
        for (int i = 0; i < columnSize; i++) {
            spaces = spaces + " ";
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = 0; j < this.getColumnCount(); j++) {
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[j] = spaces;
                } else if (this.getValueAt(i, j)instanceof JComboBox){
                        columnStrings[j] = (String)((JComboBox) this.getValueAt(i, j)).getSelectedItem();
                    } else if (this.getValueAt(i, j)instanceof Boolean){
                            columnStrings[j] = ( this.getValueAt(i, j)).toString();
                        }else columnStrings[j] = (String) this.getValueAt(i, j);
            }
        printColumns(w, columnStrings, columnSize);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize+1)*this.getColumnCount());
        }            
        w.close();
    }
    
    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize) {
        String columnString = "";
        String lineString = "";
        // create a base string the width of the column
        String spaces = "";
        for (int i = 0; i < columnSize; i++) {
            spaces = spaces + " ";
        }
        // loop through each column
        boolean complete = false;
        while (!complete){
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                // if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
                // use the intial part of the text,pad it with spaces and place the remainder back in the array
                // for further processing on next line
                // if column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnSize) {
                    boolean noWord = true;
                    for (int k = columnSize; k >= 1 ; k--) {
                        if (columnStrings[i].substring(k-1,k).equals(" ") 
                            || columnStrings[i].substring(k-1,k).equals("-")
                            || columnStrings[i].substring(k-1,k).equals("_")) {
                            columnString = columnStrings[i].substring(0,k) 
                                + spaces.substring(columnStrings[i].substring(0,k).length());
                            columnStrings[i] = columnStrings[i].substring(k);
                            noWord = false;
                            complete = false;
                            break;
                        }
                    }
                    if (noWord) {
                        columnString = columnStrings[i].substring(0,columnSize);
                        columnStrings[i] = columnStrings[i].substring(columnSize);
                        complete = false;
                    }
                    
                } else {
                    columnString = columnStrings[i] + spaces.substring(columnStrings[i].length());
                    columnStrings[i] = "";
                }
                lineString = lineString + columnString + " ";
            }
            try {
                w.write(lineString);
                //write vertical dividing lines
                for (int i = 0; i < w.getCharactersPerLine(); i = i+columnSize+1) {
                    w.write(w.getCurrentLineNumber(), i, w.getCurrentLineNumber() + 1, i);
                }
                lineString = "\n";
                w.write(lineString);
                lineString = "";
            } catch (IOException e) { log.warn("error during printing: "+e);}
        }
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BeanTableDataModel.class.getName());

}