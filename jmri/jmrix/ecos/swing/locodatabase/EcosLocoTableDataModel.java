// EcosLocoTableDataModel.java

package jmri.jmrix.ecos.swing.locodatabase;

import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;

/**
 * Table data model for display of jmri.jmrix.ecos.EcosLocoAddressManager manager contents
 * This extends the BeanTableDataModel, but the majority of it is customised.
 * @author		Kevin Dickerson   Copyright (C) 2009
 * @version		$Revision: 1.11 $
 */
abstract public class EcosLocoTableDataModel extends jmri.jmrit.beantable.BeanTableDataModel {

    static public final int ADDTOROSTERCOL = 5;

    static public final int NUMCOLUMN = 6;  //Set to 5 as the code behind ADDTOROSTERCOL isn't ready

    public EcosLocoTableDataModel() {
        super();
        // Not worried about this just yet
		getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    //private EcosLocoAddressManager objEcosLocoManager;
    @Override
    protected synchronized void updateNameList() {
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
    @Override
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
             //                                   +VALUECOL+" for "+name);
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
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        refreshSelections();
        return true;
    }

    @Override
    public int getRowCount() {
        return ecosObjectIdList.size();
    }
    
    @Override
    public String getColumnName(int col) {
         switch (col) {
        case SYSNAMECOL: return "ECoS Object Id";
        case USERNAMECOL: return "ECoS Descritpion";
        case VALUECOL: return "ECoS Address";
        case COMMENTCOL: return "JMRI Roster Id";
        case DELETECOL: return "ECOS Protocol";
        case ADDTOROSTERCOL: return "Add to Roster";
        default: return "unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
        case SYSNAMECOL:
        case USERNAMECOL:
        case VALUECOL:
        case DELETECOL:
            return String.class;
        case ADDTOROSTERCOL:
            return JButton.class;
        case COMMENTCOL:
          return JComboBox.class;
        default:
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case COMMENTCOL:
            return true;
        case ADDTOROSTERCOL:
            if (getValueAt(row, COMMENTCOL)==null)
                return true;
            else
                return false;
        default:
            return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
    	// some error checking
    	if (row >= ecosObjectIdList.size()){
    		log.debug("row is greater than list size");
    		return null;
    	}
        jmri.jmrix.ecos.EcosLocoAddress b;
        switch (col) {
        case SYSNAMECOL:
            return ecosObjectIdList.get(row);
        case USERNAMECOL:  // return user name
            // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getEcosDescription() : null;
        case VALUECOL:  //
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getEcosLocoAddress() : null;
        case COMMENTCOL:
            b = getByEcosObject(ecosObjectIdList.get(row));
            if (b!=null){
                if (b.getRosterId()!=null){
                    return b.getRosterId();
                }
            }
            return (b!=null) ? b.getRosterId() : null;
        case DELETECOL:
            b = getByEcosObject(ecosObjectIdList.get(row));
            return (b!=null) ? b.getProtocol() : null;
        case ADDTOROSTERCOL:  //
            if (getValueAt(row, COMMENTCOL)==null)
                return "Add To Roster";
            else
                return " ";
            
        default:
            //log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    }
    
    JComboBox selections;
    
    public void setUpCOMMENTCOL(TableColumn Rosterid){
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
        Roster.instance().updateComboBoxGlobal(selections);
        selections.insertItemAt(" ",0);
        selections.setSelectedIndex(-1);
        fireTableRowsUpdated(0, getRowCount());
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
        case SYSNAMECOL:
            return new JTextField(5).getPreferredSize().width;
        case COMMENTCOL:
            return 75;
        case USERNAMECOL:
            return new JTextField(20).getPreferredSize().width;
        case ADDTOROSTERCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
            return new JTextField(22).getPreferredSize().width;
        case VALUECOL: 
            return new JTextField(5).getPreferredSize().width;
        default:
        	//log.warn("Unexpected column in getPreferredWidth: "+col);
            return new JTextField(8).getPreferredSize().width;
        }
    }

    abstract public String getValue(String systemName);

    abstract protected jmri.jmrix.ecos.EcosLocoAddressManager getManager();

    abstract jmri.jmrix.ecos.EcosLocoAddress getByEcosObject(String object);
    abstract jmri.jmrix.ecos.EcosLocoAddress getByDccAddress(int address);
    abstract void clickOn(jmri.jmrix.ecos.EcosLocoAddressManager t);

    @Override
    public void setValueAt(Object value, int row, int col) {
    
        if (col==COMMENTCOL) {
            List<RosterEntry> l;
            if (value==null)
                return;
            if (value.equals(" ")){
                l = Roster.instance().getEntriesWithAttributeKeyValue("EcosObject", ecosObjectIdList.get(row));
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
            Roster.writeRosterFile();

        }  else if (col==ADDTOROSTERCOL) {
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
    
    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param table
     */
    @Override
    public void configureTable(JTable table) {
        super.configureTable(table);

        setUpCOMMENTCOL(table.getColumnModel().getColumn(COMMENTCOL));
        configAddToRosterColumn(table);
    }
    
    @Override
    protected void configValueColumn(JTable table) {
        // We don't have a delete Button!
    }
   
    void configAddToRosterColumn(JTable table) {
        setColumnToHoldButton(table, ADDTOROSTERCOL, 
                new JButton("Add to Roster"));
    }
    
    @Override
    protected void configDeleteColumn(JTable table) {
        // We don't have a delete Button!
    }

    @Override
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
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoTableDataModel.class.getName());

}