package jmri.jmrit.roster.swing.rostergroup;

import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Table data model for display of Rosters entries to a specific Roster Group.
 * <p>
 * Any desired ordering, etc, is handled outside this class.
 * <p>
 * The initial implementation doesn't automatically update when roster entries
 * change, it only allows the setting of a roster entry, to a roster group.
 * Based Upon RosterTableModel
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Kevin Dickerson Copyright (C) 2009
 * @since 2.7.5
 */
public class RosterGroupTableModel extends javax.swing.table.AbstractTableModel {

    static final int IDCOL = 0;
    static final int ROADNUMBERCOL = 1;
    static final int ROADNAMECOL = 2;
    static final int MFGCOL = 3;
    static final int OWNERCOL = 4;
    static final int ADDTOGROUPCOL = 5;

    String group = "RosterGroup:";

    static final int NUMCOL = ADDTOGROUPCOL + 1;

    @Override
    public int getRowCount() {
        return Roster.getDefault().numEntries();
    }

    @Override
    public int getColumnCount() {
        return NUMCOL;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case IDCOL:
                return Bundle.getMessage("FieldID");
            case ROADNUMBERCOL:
                return Bundle.getMessage("FieldRoadNumber");
            case ROADNAMECOL:
                return Bundle.getMessage("FieldRoadName");
            case MFGCOL:
                return Bundle.getMessage("FieldManufacturer");
            case ADDTOGROUPCOL:
                return Bundle.getMessage("Include");
            case OWNERCOL:
                return Bundle.getMessage("FieldOwner");
            default:
                return "<UNKNOWN>"; // flags unforeseen case, NOI18N
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case IDCOL:
                return new JTextField(10).getPreferredSize().width;
            case ROADNUMBERCOL:
                return 75;
            case ROADNAMECOL:
            case OWNERCOL:
                return new JTextField(20).getPreferredSize().width;
            case ADDTOGROUPCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                return 50;
            case MFGCOL:
                return new JTextField(5).getPreferredSize().width;
            default:
                //log.warn("Unexpected column in getPreferredWidth: "+col);
                return new JTextField(8).getPreferredSize().width;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == ADDTOGROUPCOL) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    /**
     * This implementation can't edit the values yet
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case ADDTOGROUPCOL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Provides the empty String if attribute doesn't exist.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        RosterEntry re = Roster.getDefault().getEntry(row);

        switch (col) {
            case IDCOL:
                return re.getId();
            case ROADNUMBERCOL:
                return re.getRoadNumber();
            case ROADNAMECOL:
                return re.getRoadName();
            case MFGCOL:
                return re.getMfg();
            case OWNERCOL:
                return re.getOwner();
            case ADDTOGROUPCOL:
                if (group == null) {
                    return false;
                } else {
                    if (re.getAttribute(group) != null) {
                        return true;
                    } else {
                        return false;
                    }
                }
            default:
                return "<UNKNOWN>";
        }
    }

    public void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);
        //setUpRosterIdCol(table.getColumnModel().getColumn(3));
        //configAddToRosterColumn(table); Remarked out until the code for the add to roster has been completed

    }

    synchronized public void dispose() {
        //This needs to be sorted later.
        //getManager().removePropertyChangeListener(this);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        RosterEntry re = Roster.getDefault().getEntry(row);
        if ((col == ADDTOGROUPCOL) && (!group.equals("RosterGroup:"))) {
            if (value.toString().equals("true")) {
                re.putAttribute(group, "yes");
            } else {
                re.deleteAttribute(group);
            }
            re.updateFile();
            Roster.getDefault().writeRoster();

        }
        //re.updateFile();
        //Roster.getDefault().writeRosterFile();
    }

    public void setGroup(String grp) {
        group = grp;
    }

    public void getGroupEnabled(RosterEntry re) {

    }

    // private final static Logger log = LoggerFactory.getLogger(RosterGroupTableModel.class);
}
