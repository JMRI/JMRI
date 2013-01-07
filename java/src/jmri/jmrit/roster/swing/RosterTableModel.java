// RosterTableModel.java

package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.XTableColumnModel;

/**
 * Table data model for display of Roster variable values.
 *<P>
 * Any desired ordering, etc, is handled outside this class.
 *<P>
 * The initial implementation doesn't automatically update when
 * roster entries change, doesn't allow updating of the entries,
 * and only shows some of the fields.  But it's a start....
 *
 * @author              Bob Jacobsen   Copyright (C) 2009, 2010
 * @version             $Revision$
 * @since 2.7.5
 */
public class RosterTableModel extends DefaultTableModel implements PropertyChangeListener {

    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
    
    public static final int IDCOL = 0;
    static final int ADDRESSCOL = 1;
    static final int ICONCOL = 2;
    static final int DECODERCOL = 3;
    static final int ROADNAMECOL = 4;
    static final int ROADNUMBERCOL = 5;
    static final int MFGCOL = 6;
    static final int MODELCOL = 7;
    static final int OWNERCOL = 8;
    static final int DATEUPDATECOL = 9;
    public static final int PROTOCOL = 10;

    int NUMCOL = PROTOCOL+1;
    
    private String rosterGroup = null;
    
    boolean editable = false;
    
    public RosterTableModel() {
        this(false);
    }
    
    public RosterTableModel(boolean editable) {
        this.editable = editable;
        Roster.instance().addPropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("add")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("remove")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("saved")) {
            //TODO This really needs to do something like find the index of the roster entry here
            if(e.getSource() instanceof RosterEntry){
                int row = Roster.instance().getGroupIndex(rosterGroup, (RosterEntry)e.getSource());
                fireTableRowsUpdated(row, row);
            } else {
                fireTableDataChanged();
            }
        } else if (e.getPropertyName().equals("selectedRosterGroup")) {
            setRosterGroup((e.getNewValue() != null) ? e.getNewValue().toString() : null);
        } else if (e.getPropertyName().startsWith("attribute") && e.getSource() instanceof RosterEntry){
            int row = Roster.instance().getGroupIndex(rosterGroup, (RosterEntry)e.getSource());
            fireTableRowsUpdated(row, row);
        }
    }
    
    public int getRowCount() {
        return Roster.instance().numGroupEntries(rosterGroup);
    }
    
    public void addColumn(Object c){
        NUMCOL++;
        super.addColumn(c);
    }

    public int getColumnCount(){
        return NUMCOL;
    }
    
    //The table columnModel is added to here to assist with getting the details of user generated roster attributes.
    public void setColumnModel(XTableColumnModel tcm){
        _tcm = tcm;
    }
    
    XTableColumnModel _tcm = null;
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case IDCOL:         return rb.getString("FieldID");
            case ADDRESSCOL:    return rb.getString("FieldDCCAddress");
            case DECODERCOL:    return rb.getString("FieldDecoderModel");
            case MODELCOL:      return rb.getString("FieldModel");
            case ROADNAMECOL:   return rb.getString("FieldRoadName");
            case ROADNUMBERCOL: return rb.getString("FieldRoadNumber");
            case MFGCOL:        return rb.getString("FieldManufacturer");
            case ICONCOL:       return rb.getString("FieldIcon");
            case OWNERCOL:      return rb.getString("FieldOwner");
            case DATEUPDATECOL: return rb.getString("FieldDateUpdated");
            case PROTOCOL:      return rb.getString("FieldProtocol");
            default : break;
        }
        if(_tcm!=null){
            TableColumn tc = _tcm.getColumnByModelIndex(col);
            if(tc!=null){
                return (String)tc.getHeaderValue();
            }
        }
        return "<UNKNOWN>";
    }
    
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == ADDRESSCOL) return Integer.class;
        if (col == ICONCOL) return ImageIcon.class;
        return String.class;
    }
    
    /**
     * Editable state must be set in ctor.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == ADDRESSCOL) return false;
        if (col == PROTOCOL) return false;
        if (col == DECODERCOL) return false;
        if (col == ICONCOL) return false;
        if (col == DATEUPDATECOL) return false;
        if(editable){
            RosterEntry re = Roster.instance().getGroupEntry(rosterGroup, row);
            if (re != null){
                return (!re.isOpen());
            }
        }
        return editable;
    }
    
    jmri.jmrit.roster.RosterIconFactory iconFactory = null;
    
    ImageIcon getIcon(RosterEntry re) {
        // defer image handling to RosterIconFactory
        if (iconFactory == null)
            iconFactory = new jmri.jmrit.roster.RosterIconFactory(Math.max(19, new javax.swing.JLabel(getColumnName(0)).getPreferredSize().height));
        return iconFactory.getIcon(re);
    }
    
    /**
     * Provides the empty String if attribute doesn't exist.
     */
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.instance().getGroupEntry(rosterGroup, row);
        if (re == null){
        	log.debug("roster entry is null!");
        	return null;
        }    
        switch (col) {
            case IDCOL:         return re.getId();
            case ADDRESSCOL:    return Integer.valueOf(re.getDccLocoAddress().getNumber());
            case DECODERCOL:    return re.getDecoderModel();
            case MODELCOL:      return re.getModel();
            case ROADNAMECOL:   return re.getRoadName();
            case ROADNUMBERCOL: return re.getRoadNumber();
            case MFGCOL:        return re.getMfg();
            case ICONCOL:       return getIcon(re);
            case OWNERCOL:      return re.getOwner();
            case DATEUPDATECOL: return re.getDateUpdated();
            case PROTOCOL:      return re.getProtocolAsString();
            default:            break;
        }
        String value = re.getAttribute(getColumnName(col).replaceAll("\\s", ""));
        if(value!=null)
            return value;
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.instance().getGroupEntry(rosterGroup, row);
        if (re == null){
        	log.warn("roster entry is null!");
        	return;
        }
        if (re.isOpen()){
            log.warn("Entry is already open");
            return;
        }
        String valueToSet = (String) value;
        switch (col) {
            case IDCOL:         if(re.getId().equals(valueToSet)) return;
                                re.setId(valueToSet); break;
            case ROADNAMECOL:   if(re.getRoadName().equals(valueToSet)) return;
                                re.setRoadName(valueToSet); break;
            case ROADNUMBERCOL: if(re.getRoadNumber().equals(valueToSet)) return;
                                re.setRoadNumber(valueToSet); break;
            case MFGCOL:        if(re.getMfg().equals(valueToSet)) return;
                                re.setMfg(valueToSet); break;
            case MODELCOL:      if(re.getModel().equals(valueToSet)) return;
                                re.setModel(valueToSet); break;
            case OWNERCOL:      if(re.getOwner().equals(valueToSet)) return;
                                re.setOwner(valueToSet); break;
            default:            String attributeName = (getColumnName(col)).replaceAll("\\s", "");
                                if(re.getAttribute(attributeName)!=null && re.getAttribute(attributeName).equals(valueToSet)) return;
                                if((valueToSet==null) || valueToSet.equals(""))
                                    re.deleteAttribute(attributeName);
                                else
                                    re.putAttribute(attributeName, valueToSet);
                                break;
        }
        // need to mark as updated
        re.changeDateUpdated();
        re.updateFile();
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width+15);  // leave room for sorter arrow
        for (int row = 0 ; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class)){
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(Integer.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            else if (getColumnClass(column).equals(ImageIcon.class))
                retval = Math.max(retval, new javax.swing.JLabel((ImageIcon)getValueAt(row, column)).getPreferredSize().width);
        }    
        return retval+5;
    }
    
    public final void setRosterGroup(String rosterGroup) {
        for(RosterEntry re:Roster.instance().getEntriesInGroup(rosterGroup)){
            re.removePropertyChangeListener(this);
        }
        this.rosterGroup = rosterGroup;
        for(RosterEntry re:Roster.instance().getEntriesInGroup(rosterGroup)){
            re.addPropertyChangeListener(this);
        }
        fireTableDataChanged();
    }

    public final String getRosterGroup() {
        return this.rosterGroup;
    }
    
    // drop listeners
    public void dispose() {
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterTableModel.class.getName());
}
