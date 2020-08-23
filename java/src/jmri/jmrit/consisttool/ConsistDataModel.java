package jmri.jmrit.consisttool;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of consist information.
 *
 * @author Paul Bender Copyright (c) 2004-2005
 */
public class ConsistDataModel extends AbstractTableModel {

    private static final int ADDRCOLUMN = 0;    // Locomotive address
    private static final int ROSTERCOLUMN = 1;  // Roster Entry, this exists
    private static final int DIRECTIONCOLUMN = 2;  // Relative Direction
    private static final int DELCOLUMN = 3;     // Remove Button
    private static final int NUMCOLUMN = 4;
    // a place holder for a consist and Consist Manager objects.
    private Consist _consist = null;
    private ConsistManager consistMan = null;

    // Construct a new instance
    ConsistDataModel() {
        consistMan = InstanceManager.getDefault(jmri.ConsistManager.class);
    }

    void initTable(JTable ConsistTable) {
        // Install the button handlers for the Delete Buttons
        TableColumnModel tcm = ConsistTable.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(DELCOLUMN).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(DELCOLUMN).setCellEditor(buttonEditor);
    }

    public void setConsist(Consist consist) {
        log.debug("Setting Consist");
        _consist = consist;
        fireTableDataChanged();
    }

    public void setConsist(DccLocoAddress Address) {
        log.debug("Setting Consist using address: {}",Address);
        _consist = consistMan.getConsist(Address);
        fireTableDataChanged();
    }

    public Consist getConsist() {
        return _consist;
    }

    @Override
    public int getRowCount() {
        try {
            return (_consist.getConsistList().size());
        } catch (NullPointerException e) {
            return (0);
        }
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ADDRCOLUMN:
                return Bundle.getMessage("AddressColumnLabel");
            case ROSTERCOLUMN:
                return Bundle.getMessage("RosterColumnLabel");
            case DIRECTIONCOLUMN:
                return Bundle.getMessage("DirectionColumnLabel");
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ROSTERCOLUMN:
                return (String.class);
            case DELCOLUMN:
                return (JButton.class);
            case DIRECTIONCOLUMN:
                return (Boolean.class);
            default:
                return (String.class);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        log.debug("isCellEditable called for row: {} column: {}",row,col);
        if (col == DELCOLUMN) {
            return (true);
        } else if (row != 0 && col == DIRECTIONCOLUMN) {
            return (true);
        } else {
            return (false);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        log.debug("getValueAt called for row: {} column: {}",row,col);
        if (_consist == null) {
            log.debug("Consist not defined");
            return (null);
        }
        // some error checking
        if (row >= _consist.getConsistList().size()) {
            log.debug("row is greater than consist list size");
            return null;
        }
        switch (col) {
            case ADDRCOLUMN:
                return (_consist.getConsistList().get(row).toString());
            case ROSTERCOLUMN:
                return _consist.getRosterId(_consist.getConsistList().get(row));
            case DIRECTIONCOLUMN:
                return (Boolean.valueOf(_consist.getLocoDirection(_consist.getConsistList().get(row))));
            case DELCOLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return ("");
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        log.debug("setValueAt called for row: {} column: {}",row,col);
        if (_consist == null) {
            return;
        }
        switch (col) {
            case DIRECTIONCOLUMN:
                _consist.add(_consist.getConsistList().get(row), ((Boolean) value).booleanValue());
                fireTableDataChanged();
                break;
            case DELCOLUMN:
                log.debug("Delete Called for row {}",row);
                fireTableRowsDeleted(row, row);
                _consist.remove(_consist.getConsistList().get(row));
                fireTableDataChanged();
                break;
            default:
                log.error("Unknown Consist Operation");
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ConsistDataModel.class);
}
