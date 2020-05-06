package jmri.jmrit.roster.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import javax.annotation.CheckForNull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.XTableColumnModel;
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
    private int NUMCOL = PROTOCOL + 1;
    private String rosterGroup = null;
    boolean editable = false;

    public RosterTableModel() {
        this(false);
    }

    public RosterTableModel(boolean editable) {
        this.editable = editable;
        Roster.getDefault().addPropertyChangeListener(this);
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
    public void addColumn(Object c) {
        NUMCOL++;
        super.addColumn(c);
    }

    @Override
    public int getColumnCount() {
        return NUMCOL;
    }

    //The table columnModel is added to here to assist with getting the details of user generated roster attributes.
    public void setColumnModel(XTableColumnModel tcm) {
        _tcm = tcm;
    }
    XTableColumnModel _tcm = null;

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
                break;
        }
        if (_tcm != null) {
            TableColumn tc = _tcm.getColumnByModelIndex(col);
            if (tc != null) {
                return (String) tc.getHeaderValue();
            }
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
                return String.class;
        }
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
        String value = re.getAttribute(getColumnName(col).replaceAll("\\s", "")); // NOI18N
        if (value != null) {
            return value;
        }
        return "";
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
        String valueToSet = (String) value;
        switch (col) {
            case IDCOL:
                if (re.getId().equals(valueToSet)) {
                    return;
                }
                re.setId(valueToSet);
                break;
            case ROADNAMECOL:
                if (re.getRoadName().equals(valueToSet)) {
                    return;
                }
                re.setRoadName(valueToSet);
                break;
            case ROADNUMBERCOL:
                if (re.getRoadNumber().equals(valueToSet)) {
                    return;
                }
                re.setRoadNumber(valueToSet);
                break;
            case MFGCOL:
                if (re.getMfg().equals(valueToSet)) {
                    return;
                }
                re.setMfg(valueToSet);
                break;
            case MODELCOL:
                if (re.getModel().equals(valueToSet)) {
                    return;
                }
                re.setModel(valueToSet);
                break;
            case OWNERCOL:
                if (re.getOwner().equals(valueToSet)) {
                    return;
                }
                re.setOwner(valueToSet);
                break;
            default:
                String attributeName = (getColumnName(col)).replaceAll("\\s", "");
                if (re.getAttribute(attributeName) != null && re.getAttribute(attributeName).equals(valueToSet)) {
                    return;
                }
                if ((valueToSet == null) || valueToSet.isEmpty()) {
                    re.deleteAttribute(attributeName);
                } else {
                    re.putAttribute(attributeName, valueToSet);
                }
                break;
        }
        // need to mark as updated
        re.changeDateUpdated();
        re.updateFile();
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

    // drop listeners
    public void dispose() {
    }
    private final static Logger log = LoggerFactory.getLogger(RosterTableModel.class);
}
