package jmri.jmrix.lenz.swing.stackmon;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Lenz Command Station Stack information.
 *
 * @author Paul Bender Copyright (c) 2008
 */
public class StackMonDataModel extends javax.swing.table.AbstractTableModel {

    static private final int ADDRCOLUMN = 0;     // Locomotive address
    static private final int TYPECOLUMN = 1;     // Type of Database Entry
    static private final int DELCOLUMN = 3;      // Remove Button

    static private final int NUMCOLUMN = 4;

    // the stack frame containing this object
    StackMonFrame _stackFrame;

    // internal data structures used to store stack info
    java.util.ArrayList<Integer> _addressList;       // Store the addresses
    java.util.Hashtable<Integer, String> _typeList;  // Store the entry type

    protected XNetTrafficController tc = null;

    /**
     * Constructor for a new instance
     */
    StackMonDataModel(int row, int column, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        tc = memo.getXNetTrafficController();
    }

    void initTable(JTable stackTable, StackMonFrame stackFrame) {
        // Install the button handlers for the Delete Buttons
        TableColumnModel tcm = stackTable.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(DELCOLUMN).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(DELCOLUMN).setCellEditor(buttonEditor);
        _stackFrame = stackFrame;
    }

    @Override
    public int getRowCount() {
        try {
            return (_addressList.size());
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
                return Bundle.getMessage("AddressCol");
            case TYPECOLUMN:
                return Bundle.getMessage("EntryTypeCol");
            default:
                return ""; // no column title
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ADDRCOLUMN:
                return (Integer.class);
            case DELCOLUMN:
                return (javax.swing.JButton.class);
            default:
                return (String.class);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        log.debug("isCellEditable called for row: row: {} column: {}", row, col);
        if (col == DELCOLUMN) {
            return (true);
        } else {
            return (false);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        log.debug("getValueAt called for row: {} column: {}", row, col);
        // some error checking
        if (row >= _addressList.size()) {
            log.debug("row is greater thant address list size");
            return ("Error");
        }
        switch (col) {
            case ADDRCOLUMN:
                return (_addressList.get(row));
            case TYPECOLUMN:
                return (_typeList.get(_addressList.get(row)));
            case DELCOLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return ("");
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        log.debug("setValueAt called for row: {} column: {}", row, col);
        switch (col) {
            case DELCOLUMN:
                log.debug("Delete Called for row " + row);
                fireTableRowsDeleted(row, row);
                // delete address from table
                XNetMessage msg = XNetMessage.getDeleteAddressOnStackMsg((_addressList.get(row)).intValue());
                tc.sendXNetMessage(msg, _stackFrame);
                _typeList.remove(_addressList.get(row));
                _addressList.remove(row);
                fireTableDataChanged();
                break;
            default:
                log.error("Unknown Operation");
        }
    }

    /**
     * Update the internal data structures for a specified address.
     */
    public void updateData(Integer address, String type) {
        if (_addressList == null) {
            // initilize the address list
            _addressList = new java.util.ArrayList<Integer>();
            _typeList = new java.util.Hashtable<Integer, String>();
        }
        if (!_addressList.contains(address)) {
            _addressList.add(address);
            _typeList.put(address, type);
        } else {
            _typeList.put(address, type);
        }
        fireTableDataChanged();
    }

    /**
     * Update the internal data structures for a specified address.
     */
    public void clearData() {
        _addressList = new java.util.ArrayList<Integer>();
        _typeList = new java.util.Hashtable<Integer, String>();
        fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(StackMonDataModel.class);

}
