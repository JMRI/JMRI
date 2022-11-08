package jmri.jmrit.roster.swing;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Roster variable values.
 * <p>
 * Any desired ordering, etc, is handled outside this class.
 * <p>
 * The initial implementation doesn't automatically update when roster entries
 * change, doesn't allow updating of the entries, and only shows some of the
 * fields. But it's a start....
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010
 * @since 2.7.5
 */
public class RosterTableModel extends DefaultTableModel implements PropertyChangeListener {

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
    public static final int NUMCOL = PROTOCOL + 1;
    private String rosterGroup = null;
    boolean editable = false;
    
    public RosterTableModel() {
        this(false);
    }

    public RosterTableModel(boolean editable) {
        this.editable = editable;
        Roster.getDefault().addPropertyChangeListener(RosterTableModel.this);
        setRosterGroup(null); // add prop change listeners to roster entries
    }

    /**
     * Create a table model for a Roster group.
     *
     * @param group the roster group to show; if null, behaves the same as
     *              {@link #RosterTableModel()}
     */
    public RosterTableModel(@CheckForNull RosterGroup group) {
        this(false);
        if (group != null) {
            this.setRosterGroup(group.getName());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Roster.ADD)) {
            setRosterGroup(getRosterGroup()); // add prop change listener to new entry
            fireTableDataChanged();
        } else if (e.getPropertyName().equals(Roster.REMOVE)) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals(Roster.SAVED)) {
            //TODO This really needs to do something like find the index of the roster entry here
            if (e.getSource() instanceof RosterEntry) {
                int row = Roster.getDefault().getGroupIndex(rosterGroup, (RosterEntry) e.getSource());
                fireTableRowsUpdated(row, row);
            } else {
                fireTableDataChanged();
            }
        } else if (e.getPropertyName().equals(RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            setRosterGroup((e.getNewValue() != null) ? e.getNewValue().toString() : null);
        } else if (e.getPropertyName().startsWith("attribute") && e.getSource() instanceof RosterEntry) { // NOI18N
            int row = Roster.getDefault().getGroupIndex(rosterGroup, (RosterEntry) e.getSource());
            fireTableRowsUpdated(row, row);
        } else if (e.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED) && e.getNewValue().equals(rosterGroup)) {
            fireTableDataChanged();
        }
    }

    @Override
    public int getRowCount() {
        return Roster.getDefault().numGroupEntries(rosterGroup);
    }

    @Override
    public int getColumnCount() {
        return NUMCOL + getModelAttributeKeyColumnNames().length;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case IDCOL:
                return Bundle.getMessage("FieldID");
            case ADDRESSCOL:
                return Bundle.getMessage("FieldDCCAddress");
            case DECODERCOL:
                return Bundle.getMessage("FieldDecoderModel");
            case MODELCOL:
                return Bundle.getMessage("FieldModel");
            case ROADNAMECOL:
                return Bundle.getMessage("FieldRoadName");
            case ROADNUMBERCOL:
                return Bundle.getMessage("FieldRoadNumber");
            case MFGCOL:
                return Bundle.getMessage("FieldManufacturer");
            case ICONCOL:
                return Bundle.getMessage("FieldIcon");
            case OWNERCOL:
                return Bundle.getMessage("FieldOwner");
            case DATEUPDATECOL:
                return Bundle.getMessage("FieldDateUpdated");
            case PROTOCOL:
                return Bundle.getMessage("FieldProtocol");
            default:
                return getColumnNameAttribute(col);
        }
    }

    private String getColumnNameAttribute(int col) {
        if ( col < getColumnCount() ) {
            String attributeKey = getAttributeKey(col);
            try {
                return Bundle.getMessage(attributeKey);
            } catch (java.util.MissingResourceException ex){}

            String[] r = attributeKey.split("(?=\\p{Lu})"); // NOI18N
            StringBuilder sb = new StringBuilder();
            sb.append(r[0].trim());
            for (int j = 1; j < r.length; j++) {
                sb.append(" ");
                sb.append(r[j].trim());
            }
            return sb.toString();
        }
        return "<UNKNOWN>"; // NOI18N
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ADDRESSCOL:
                return Integer.class;
            case ICONCOL:
                return ImageIcon.class;
            case DATEUPDATECOL:
                return Date.class;
            default:
                return getColumnClassAttribute(col);
        }
    }

    private Class<?> getColumnClassAttribute(int col){
        if (RosterEntry.ATTRIBUTE_LAST_OPERATED.equals( getAttributeKey(col))) {
            return Date.class;
        }
        return String.class;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the table can be set to be non-editable when constructed, in
     * which case this always returns false.
     *
     * @return true if cell is editable in roster entry model and table allows
     *         editing
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == ADDRESSCOL) {
            return false;
        }
        if (col == PROTOCOL) {
            return false;
        }
        if (col == DECODERCOL) {
            return false;
        }
        if (col == ICONCOL) {
            return false;
        }
        if (col == DATEUPDATECOL) {
            return false;
        }
        if (editable) {
            RosterEntry re = Roster.getDefault().getGroupEntry(rosterGroup, row);
            if (re != null) {
                return (!re.isOpen());
            }
        }
        return editable;
    }

    RosterIconFactory iconFactory = null;

    ImageIcon getIcon(RosterEntry re) {
        // defer image handling to RosterIconFactory
        if (iconFactory == null) {
            iconFactory = new RosterIconFactory(Math.max(19, new JLabel(getColumnName(0)).getPreferredSize().height));
        }
        return iconFactory.getIcon(re);
    }

    /**
     * {@inheritDoc}
     *
     * Provides an empty string for a column if the model returns null for that
     * value.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.getDefault().getGroupEntry(rosterGroup, row);
        if (re == null) {
            log.debug("roster entry is null!");
            return null;
        }
        switch (col) {
            case IDCOL:
                return re.getId();
            case ADDRESSCOL:
                return re.getDccLocoAddress().getNumber();
            case DECODERCOL:
                return re.getDecoderModel();
            case MODELCOL:
                return re.getModel();
            case ROADNAMECOL:
                return re.getRoadName();
            case ROADNUMBERCOL:
                return re.getRoadNumber();
            case MFGCOL:
                return re.getMfg();
            case ICONCOL:
                return getIcon(re);
            case OWNERCOL:
                return re.getOwner();
            case DATEUPDATECOL:
                // will not display last update if not parsable as date
                return re.getDateModified();
            case PROTOCOL:
                return re.getProtocolAsString();
            default:
                break;
        }
        return getValueAtAttribute(re, col);
    }

    private Object getValueAtAttribute(RosterEntry re, int col){
        String attributeKey = getAttributeKey(col);
        String value = re.getAttribute(attributeKey); // NOI18N
        if (RosterEntry.ATTRIBUTE_LAST_OPERATED.equals( attributeKey)) {
            if (value == null){
                return null;
            }
            try {
                return new StdDateFormat().parse(value);
            } catch (ParseException ex){
                return null;
            }
        }
        return (value == null ? "" : value);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.getDefault().getGroupEntry(rosterGroup, row);
        if (re == null) {
            log.warn("roster entry is null!");
            return;
        }
        if (re.isOpen()) {
            log.warn("Entry is already open");
            return;
        }
        if (Objects.equals(value, getValueAt(row, col))) {
            return;
        }
        String valueToSet = (String) value;
        switch (col) {
            case IDCOL:
                re.setId(valueToSet);
                break;
            case ROADNAMECOL:
                re.setRoadName(valueToSet);
                break;
            case ROADNUMBERCOL:
                re.setRoadNumber(valueToSet);
                break;
            case MFGCOL:
                re.setMfg(valueToSet);
                break;
            case MODELCOL:
                re.setModel(valueToSet);
                break;
            case OWNERCOL:
                re.setOwner(valueToSet);
                break;
            default:
                setValueAtAttribute(valueToSet, re, col);
                break;
        }
        // need to mark as updated
        re.changeDateUpdated();
        re.updateFile();
    }

    private void setValueAtAttribute(String valueToSet, RosterEntry re, int col) {
        String attributeKey = getAttributeKey(col);
        if ((valueToSet == null) || valueToSet.isEmpty()) {
            re.deleteAttribute(attributeKey);
        } else {
            re.putAttribute(attributeKey, valueToSet);
        }
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new JLabel(getColumnName(column)).getPreferredSize().width + 15);  // leave room for sorter arrow
        for (int row = 0; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class)) {
                retval = Math.max(retval, new JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(Integer.class)) {
                retval = Math.max(retval, new JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(ImageIcon.class)) {
                retval = Math.max(retval, new JLabel((Icon) getValueAt(row, column)).getPreferredSize().width);
            }
        }
        return retval + 5;
    }

    public final void setRosterGroup(String rosterGroup) {
        Roster.getDefault().getEntriesInGroup(this.rosterGroup).forEach((re) -> {
            re.removePropertyChangeListener(this);
        });
        this.rosterGroup = rosterGroup;
        Roster.getDefault().getEntriesInGroup(rosterGroup).forEach((re) -> {
            re.addPropertyChangeListener(this);
        });
        fireTableDataChanged();
    }

    public final String getRosterGroup() {
        return this.rosterGroup;
    }

    // access via method to ensure not null
    private String[] attributeKeys = null; 

    private String[] getModelAttributeKeyColumnNames() {
        if ( attributeKeys == null ) {
            Set<String> result = new TreeSet<>();
            for (String s : Roster.getDefault().getAllAttributeKeys()) {
                if ( !s.contains("RosterGroup")
                    && !s.toLowerCase().startsWith("sys")
                    && !s.toUpperCase().startsWith("VSD")) { // NOI18N
                    result.add(s);
                }
            }
            attributeKeys = result.toArray(String[]::new);
            }
        return attributeKeys;
    }

    private String getAttributeKey(int col) {
        if ( col >= NUMCOL && col < getColumnCount() ) {
            return getModelAttributeKeyColumnNames()[col - NUMCOL ];
        }
        return "";
    }

    // drop listeners
    public void dispose() {
        Roster.getDefault().removePropertyChangeListener(this);
        Roster.getDefault().getEntriesInGroup(this.rosterGroup).forEach((re) -> {
            re.removePropertyChangeListener(this);
        });
    }

    private final static Logger log = LoggerFactory.getLogger(RosterTableModel.class);
}
