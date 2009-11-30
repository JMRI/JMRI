// EcosLocoTableDataModel.java

package jmri.jmrix.ecos.swing.locodatabase;

//import jmri.jmrix.ecos.EcosLocoAddressManager;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table data model for display of jmri.jmrix.ecos.EcosLocoAddressManager manager contents
 * @author		Kevin Dickerson   Copyright (C) 2009
 * @version		$Revision: 1.6 $
 */
abstract public class EcosLocoTableDataModel extends javax.swing.table.AbstractTableModel
            implements PropertyChangeListener  {

    static public final int ECOSOBJECTCOL  = 0;
    static public final int ECOSDESCRIPTCOL = 1;
    static public final int DCCADDRESSCOL = 2;
    static public final int PROTOCOL = 4;
    static public final int ROSTERIDCOL = 3;
    static public final int PROTOCOLCOL = 4;
    static public final int ADDTOROSTERCOL = 5;


    static public final int NUMCOLUMN = 6;  //Set to 5 as the code behind ADDTOROSTERCOL isn't ready

    public EcosLocoTableDataModel() {
        super();
        // Not worried about this just yet
		getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    //private EcosLocoAddressManager objEcosLocoManager;
    
    synchronized void updateNameList() {
        // first, remove listeners from the individual objects
       //objEcosLocoManager = (EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);
        if (ecosObjectIdList != null) {
            for (int i = 0; i< ecosObjectIdList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                //interlock.Interlock b = getBySystemName((String)ecosObjectIdList.get(i));
                //Look at this later.
                if (b!=null) b.removePropertyChangeListener(this);
            }
        }
        // Need to get a way to return a getSystemNameList.
        ecosObjectIdList = getManager().getEcosObjectList();
        // and add them back in
        // look at later
        for (int i = 0; i< ecosObjectIdList.size(); i++)
            getByEcosObject(ecosObjectIdList.get(i)).addPropertyChangeListener(this);
            //getBySystemName((String)ecosObjectIdList.get(i)).addPropertyChangeListener(this);
    }

    List<String> ecosObjectIdList = null;
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new jmri.jmrix.ecos.EcosLocoAddressManager is available in the manager
            updateNameList();
            //log.debug("Table changed length to "+ecosObjectIdList.size());
            fireTableDataChanged();
        } else if (matchPropertyName(e)) {
            // a value changed.  Find it, to avoid complete redraw
            String object = ((jmri.jmrix.ecos.EcosLocoAddress)e.getSource()).getEcosObject();
            //if (log.isDebugEnabled()) log.debug("Update cell "+ecosObjectIdList.indexOf(name)+","
             //                                   +DCCADDRESSCOL+" for "+name);
            // since we can add columns, the entire row is marked as updated
            int row = ecosObjectIdList.indexOf(object);
            fireTableRowsUpdated(row, row);
        }
    }

	/**
	 * Is this property event announcing a change this table should display?
	 * <P>
	 * Note that events will come both from the jmri.jmrix.ecos.EcosLocoAddressManagers and also from the manager
	 */
    boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
//		return (e.getPropertyName().indexOf("Start Signal")>=0 || e.getPropertyName().indexOf("Appearance")>=0
//		        || e.getPropertyName().indexOf("Finish Signal")>=0);
    	//return (e.getPropertyName().indexOf("Start Signal")>=0 || e.getPropertyName().indexOf("Finish Signal")>=0);
        refreshSelections();
        return true;
    }

    public int getRowCount() {
        return ecosObjectIdList.size();
    }

    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
         switch (col) {
        case ECOSOBJECTCOL: return "ECoS Object Id";
        case ECOSDESCRIPTCOL: return "ECoS Descritpion";
        case DCCADDRESSCOL: return "ECoS Address";
        case ROSTERIDCOL: return "JMRI Roster Id";
        case PROTOCOLCOL: return "ECOS Protocol";
        case ADDTOROSTERCOL: return "Add to Roster";
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case ECOSOBJECTCOL:
            return String.class;
        case ECOSDESCRIPTCOL:
            return String.class;
        case ROSTERIDCOL:
          return JComboBox.class;
        case DCCADDRESSCOL:
            return String.class;
        case PROTOCOLCOL:
            return String.class;
        case ADDTOROSTERCOL:
            return JButton.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case ECOSDESCRIPTCOL:
            return false;
        case DCCADDRESSCOL:
            return false;
        case ROSTERIDCOL:
            return true;
        case ADDTOROSTERCOL:
            if (getValueAt(row, ROSTERIDCOL)==null)
                return true;
            else
                return false;
        default:
            return false;
        }
    }
    
    public Object getValueAt(int row, int col) {
    	// some error checking
    	if (row >= ecosObjectIdList.size()){
    		log.debug("row is greater than list size");
    		return null;
    	}
        jmri.jmrix.ecos.EcosLocoAddress b;
        switch (col) {
        case ECOSOBJECTCOL:
            return ecosObjectIdList.get(row);
        case ECOSDESCRIPTCOL:  // return user name
            // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getEcosDescription() : null;
        case DCCADDRESSCOL:  //
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getEcosLocoAddress() : null;
        /*case PROTOCOL:
            b = getByEcosObject((String)ecosObjectIdList.get(row));
            return (b!=null) ? b.getProtocol() : null;*/
        case ROSTERIDCOL:
            b = getByEcosObject(ecosObjectIdList.get(row));
            if (b!=null){
                if (b.getRosterId()!=null){
                    return b.getRosterId();
                }
            }
            return (b!=null) ? b.getRosterId() : null;
        case PROTOCOLCOL:
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getProtocol() : null;
        case ADDTOROSTERCOL:  //
            if (getValueAt(row, ROSTERIDCOL)==null)
                return "Add To Roster";
            else
                return " ";
            
        default:
            //log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    }
    
    JComboBox selections;
    
    public void setUpRosterIdCol(TableColumn Rosterid){
        selections = Roster.instance().fullRosterComboBoxGlobal();
        selections.insertItemAt(" ",0);
        selections.setSelectedIndex(-1);
        Rosterid.setCellEditor(new DefaultCellEditor(selections));
        
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        Rosterid.setCellRenderer(renderer);
    }

    public void refreshSelections(){
        System.out.println("Refresh of combo list " + ecosObjectIdList.size());
        Roster.instance().updateComboBoxGlobal(selections);
        selections.insertItemAt(" ",0);
        selections.setSelectedIndex(-1);
        fireTableRowsUpdated(0, getRowCount());
    }
    public int getPreferredWidth(int col) {
        switch (col) {
        case ECOSOBJECTCOL:
            return new JTextField(5).getPreferredSize().width;
        case ROSTERIDCOL:
            return 75;
        case ECOSDESCRIPTCOL:
            return new JTextField(20).getPreferredSize().width;
        case ADDTOROSTERCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            return new JTextField(22).getPreferredSize().width;
        case DCCADDRESSCOL: 
            return new JTextField(5).getPreferredSize().width;
        default:
        	//log.warn("Unexpected column in getPreferredWidth: "+col);
            return new JTextField(8).getPreferredSize().width;
        }
    }

    abstract public String getValue(String systemName);

    abstract jmri.jmrix.ecos.EcosLocoAddressManager getManager();

    abstract jmri.jmrix.ecos.EcosLocoAddress getByEcosObject(String object);
    abstract jmri.jmrix.ecos.EcosLocoAddress getByDccAddress(int address);
    abstract void clickOn(jmri.jmrix.ecos.EcosLocoAddressManager t);

    public void setValueAt(Object value, int row, int col) {
    
        if (col==ROSTERIDCOL) {
            List<RosterEntry> l;
            if (value==null)
                return;
            if (value.equals(" ")){
                System.out.println(ecosObjectIdList.get(row));
                l = Roster.instance().getEntriesWithAttributeKeyValue("EcosObject", ecosObjectIdList.get(row));
                System.out.println(l.size());
                if(l.size()!=0){
                    l.get(0).deleteAttribute("EcosObject");
                    getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                    l.get(0).updateFile();
                }
            } else{
                l = Roster.instance().matchingList(null, null, null, null, null, null, (String) value);
                for (int i = 0; i < l.size(); i++) {
                    if ((l.get(i).getAttribute("EcosObject")==null)||(l.get(i).getAttribute("EcosObject").equals(""))){
                        l.get(i).putAttribute("EcosObject", ecosObjectIdList.get(row));
                        getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                    } else{
                        value=null;
                        //return;
                    }
                    l.get(i).updateFile();
                }
            }
            fireTableRowsUpdated(row, row);
            Roster.instance().writeRosterFile();

        }  else if (col==ADDTOROSTERCOL) {
            // button fired, delete Bean
            addToRoster(row, col);
        }
    }
    
    void addToRoster(int row, int col){
        if (getByEcosObject(ecosObjectIdList.get(row)).getRosterId()==null){
            EcosLocoToRoster addLoco = new EcosLocoToRoster();
            addLoco.ecosLocoToRoster(ecosObjectIdList.get(row));
            updateNameList();
            fireTableRowsUpdated(row, row);
        }
    }

	boolean noWarnDelete = false;

    
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
        setUpRosterIdCol(table.getColumnModel().getColumn(3));
        configAddToRosterColumn(table);// Remarked out until the code for the add to roster has been completed
        
    }

   void configAddToRosterColumn(JTable table) {
        setColumnToHoldButton(table, ADDTOROSTERCOL, 
                new JButton("Add to Roster"));
    }
    
    /**
     * Service method to setup a column so that it will hold a
     * button for it's values
     * @param table
     * @param column
     * @param sample Typical button, used for size
     */
    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        //TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
		table.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
		table.setDefaultEditor(JButton.class,buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
			.setPreferredWidth((sample.getPreferredSize().width)+4);
    }

    synchronized public void dispose() {
    //This needs to be sorted later.
        getManager().removePropertyChangeListener(this);
        if (ecosObjectIdList != null) {
            for (int i = 0; i< ecosObjectIdList.size(); i++) {
                jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                if (b!=null) b.removePropertyChangeListener(this);
            }
        }
        selections=null;
    }
    
    /**
     * Method to self print or print preview the table.
     * Printed in equally sized columns across the page with headings and
     * vertical lines between each column. Data is word wrapped within a column.
     * Can handle data as strings, comboboxes or booleans
     */
/*    public void printTable(HardcopyWriter w) {
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
    }*/

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoTableDataModel.class.getName());

}