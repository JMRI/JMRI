package jmri.jmrix.sprog.sprogslotmon;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.sprog.SprogConstants;
import jmri.jmrix.sprog.SprogSlot;
import jmri.jmrix.sprog.SprogSlotListener;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of slot manager contents.
 *
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author  Andrew Crosland (C) 2006 ported to SPROG
 */
public class SprogSlotMonDataModel extends javax.swing.table.AbstractTableModel implements SprogSlotListener {

    static public final int SLOTCOLUMN = 0;
    static public final int ADDRCOLUMN = 1;
    static public final int SPDCOLUMN = 2;
    static public final int STATCOLUMN = 3;  // status: free, common, etc
    static public final int DIRCOLUMN = 4;

    static public final int NUMCOLUMN = 5;

    private SprogSystemConnectionMemo _memo = null;

    SprogSlotMonDataModel(int row, int column,SprogSystemConnectionMemo memo) {
        _memo = memo;
        // connect to SprogSlotManager for updates
        _memo.getCommandStation().addSlotListener(this);
    }

    /**
     * Check the requested number of slots against limits and return a valid slot
     * count.
     * 
     * @return validated number of slots
     */
    public static int getSlotCount() {
        int numSlots = SprogConstants.MAX_SLOTS;
        if (numSlots < SprogConstants.MIN_SLOTS) {
            numSlots = SprogConstants.MIN_SLOTS;
        } else if (numSlots > SprogConstants.SLOTS_LIMIT) {
            numSlots = SprogConstants.SLOTS_LIMIT;
        }
        return numSlots;
    }
    
    /**
     * Return the number of rows to be displayed. This can vary depending on
     * whether only active rows are displayed.
     * <p>
     * This should probably use a local cache instead of counting/searching each
     * time.
     */
    @Override
    public int getRowCount() {
        int nMax = getSlotCount();
        if (_allSlots) {
            // will show the entire set, so don't bother counting
            return nMax;
        }
        int n = 0;
        int nMin = 0;
        for (int i = nMin; i < nMax; i++) {
            SprogSlot s = _memo.getCommandStation().slot(i);
            if (s.isFree() != true) {
                n++;
            }
        }
        return n;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SLOTCOLUMN:
                return Bundle.getMessage("SlotCol");
//        case ESTOPCOLUMN: return "";     // no heading, as button is clear
            case ADDRCOLUMN:
                return Bundle.getMessage("AddressCol");
            case SPDCOLUMN:
                return Bundle.getMessage("SpeedCol");
            case STATCOLUMN:
                return Bundle.getMessage("StatusCol");
//        case CONSCOLUMN: return "Consisted";
            case DIRCOLUMN:
                return Bundle.getMessage("DirectionCol");
//        case DISPCOLUMN: return "";     // no heading, as button is clear
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SLOTCOLUMN:
                return Integer.class;
            case ADDRCOLUMN:
            case SPDCOLUMN:
            case STATCOLUMN:
//        case CONSCOLUMN:
            case DIRCOLUMN:
                return String.class;
//        case ESTOPCOLUMN:
//        case DISPCOLUMN:
//            return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
//        case ESTOPCOLUMN:
//        case DISPCOLUMN:
//            return true;
            default:
                return false;
        }
    }

    static final Boolean True = Boolean.valueOf("True");
    static final Boolean False = Boolean.valueOf("False");

    @SuppressWarnings("null")
    @Override
    public Object getValueAt(int row, int col) {
        SprogSlot s = _memo.getCommandStation().slot(slotNum(row));
        if (s == null) {
            log.error("slot pointer was null for slot row: " + row + " col: " + col);
        }

        switch (col) {
            case SLOTCOLUMN:  // slot number
                return Integer.valueOf(slotNum(row));
//        case ESTOPCOLUMN:  //
//            return "E Stop";          // will be name of button in default GUI
            case ADDRCOLUMN:  //
                    switch (s.slotStatus()) {
                        case SprogConstants.SLOT_IN_USE:
                            return Integer.toString(s.getAddr()) + "("+ (s.getIsLong() ? Bundle.getMessage("LongAddressChar") : Bundle.getMessage("ShortAddressChar")) + ")";
                        case SprogConstants.SLOT_FREE:
                            return "-";
                        default:
                            return Bundle.getMessage("StateError");
                    }
            case SPDCOLUMN:  //
                switch (s.slotStatus()) {
                    case SprogConstants.SLOT_IN_USE:
                        if (s.isF0to4Packet()) {
                            return "F0to4Pkt";
                        } else if (s.isF5to8Packet()) {
                            return "F5to8Pkt";
                        } else if (s.isF9to12Packet()) {
                            return "F9to12Pkt";
                        } else if (s.isOpsPkt()) {
                            return "OpsPkt";
                        } else if (s.isSpeedPacket()) {
                            String t;
                            if (s.speed() == 1) {
                                t = "(estop) 1";
                            } else {
                                t = "          " + s.speed();
                            }
                            return t.substring(t.length() - 9, t.length()); // 9 comes from (estop)
                        } else {
                          return Bundle.getMessage("StateError");
                        }
                    case SprogConstants.SLOT_FREE:
                        return "-";
                    default:
                        return Bundle.getMessage("StateError");
                }
            case STATCOLUMN:  //
                switch (s.slotStatus()) {
                    case SprogConstants.SLOT_IN_USE:
                        return Bundle.getMessage("StateInUse");
                    case SprogConstants.SLOT_FREE:
                        return Bundle.getMessage("StateFree");
                    default:
                        return Bundle.getMessage("StateError");
                }
//        case CONSCOLUMN:  //
//            return "<n/a>";
//        case DISPCOLUMN:  //
//            return Bundle.getMessage("ButtonRelease"); // will be name of button in default GUI
            case DIRCOLUMN:  //
                    switch (s.slotStatus()) {
                        case SprogConstants.SLOT_IN_USE:
                            if (s.isSpeedPacket()) {
                                return (s.isForward() ? Bundle.getMessage("DirColForward") : Bundle.getMessage("DirColReverse"));
                            } else {
                                return "-";                               
                            }
                        case SprogConstants.SLOT_FREE:
                            return "-";
                        default:
                            return Bundle.getMessage("StateError");
                    }

            default:
                log.error("internal state inconsistent with table request for row {}, col {}", row, col);
                return null;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES", 
                        justification="better to keep cases in column order rather than to combine")
    public int getPreferredWidth(int col) {
        switch (col) {
            case SLOTCOLUMN:
                return new JTextField(3).getPreferredSize().width;
//        case ESTOPCOLUMN:
//            return new JButton("E Stop").getPreferredSize().width;
            case ADDRCOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SPDCOLUMN:
                return new JTextField(6).getPreferredSize().width;
            case STATCOLUMN:
                return new JTextField(6).getPreferredSize().width;
//        case CONSCOLUMN:
//            return new JTextField(4).getPreferredSize().width;
            case DIRCOLUMN:
                return new JTextField(3).getPreferredSize().width;
//        case DISPCOLUMN:
//            return new JButton(Bundle.getMessage("ButtonRelease")).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // check for in use
        SprogSlot s = _memo.getCommandStation().slot(slotNum(row));
        if (s == null) {
            log.error("slot pointer was null for slot row: " + row + " col: " + col);
            return;
        }
//        if (col == ESTOPCOLUMN) {
//            log.debug("Start eStop in slot "+row);
//            _memo.getSlotManager().estopSlot(row);
//        }
//        else if (col == DISPCOLUMN) {
//            log.debug("Start freeing slot {}", row);
//            fireTableRowsUpdated(row,row);
//        }
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     */
    public void configureTable(JTable slotTable) {
        // allow reordering of the columns
        slotTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < slotTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            slotTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        slotTable.sizeColumnsToFit(-1);

//        // install a button renderer & editor in the "DISP" column for freeing a slot
//        setColumnToHoldButton(slotTable, SprogSlotMonDataModel.DISPCOLUMN);
//
//        // install a button renderer & editor in the "ESTOP" column for stopping a loco
//        setColumnToHoldEStopButton(slotTable, SprogSlotMonDataModel.ESTOPCOLUMN);
    }

//    void setColumnToHoldButton(JTable slotTable, int column) {
//        TableColumnModel tcm = slotTable.getColumnModel();
//        // install the button renderers & editors in this column
//        ButtonRenderer buttonRenderer = new ButtonRenderer();
//        tcm.getColumn(column).setCellRenderer(buttonRenderer);
//        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
//        tcm.getColumn(column).setCellEditor(buttonEditor);
//        // ensure the table rows, columns have enough room for buttons
//        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
//        slotTable.getColumnModel().getColumn(column)
//			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
//    }
//
//    void setColumnToHoldEStopButton(JTable slotTable, int column) {
//        TableColumnModel tcm = slotTable.getColumnModel();
//        // install the button renderers & editors in this column
//        ButtonRenderer buttonRenderer = new ButtonRenderer();
//        tcm.getColumn(column).setCellRenderer(buttonRenderer);
//        TableCellEditor buttonEditor = new ButtonEditor(new JButton()){
//            public void mousePressed(MouseEvent e) {
//                stopCellEditing();
//            }
//        };
//        tcm.getColumn(column).setCellEditor(buttonEditor);
//        // ensure the table rows, columns have enough room for buttons
//        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
//        slotTable.getColumnModel().getColumn(column)
//			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
//    }
    // methods to communicate with SprogSlotManager
    @Override
    public synchronized void notifyChangedSlot(SprogSlot s) {
        // update model from this slot

        int slotNum = -1;
        if (_allSlots) {          // this will be row until we show only active slots
            slotNum = s.getSlotNumber();  // and we are displaying the System slots
        }
        log.debug("Received notification of changed slot: " + slotNum);
        // notify the JTable object that a row has changed; do that in the Swing thread!
        Runnable r = new Notify(slotNum, this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
    }

    static class Notify implements Runnable {

        private int _row;
        javax.swing.table.AbstractTableModel _model;

        public Notify(int row, javax.swing.table.AbstractTableModel model) {
            _row = row;
            _model = model;
        }

        @Override
        public void run() {
            if (-1 == _row) {  // notify about entire table
                _model.fireTableDataChanged();  // just that row
            } else {
                // notify that _row has changed
                _model.fireTableRowsUpdated(_row, _row);  // just that row
            }
        }
    }

    // methods for control of "all slots" vs "only active slots"
    private boolean _allSlots = true;

    public void showAllSlots(boolean val) {
        _allSlots = val;
    }

    /**
     * Return slot number for a specific row.
     * <p>
     * This should probably use a local cache instead of counting/searching each
     * time.
     *
     * @param row Row number in the displayed table
     */
    protected int slotNum(int row) {
        // ??? Can't this just return row ???
        int slotNum;
        int n = -1;   // need to find a used slot to have the 0th one!
        int nMin = 0;
        int nMax = getSlotCount();
        for (slotNum = nMin; slotNum < nMax; slotNum++) {
            SprogSlot s = _memo.getCommandStation().slot(slotNum);
            if (_allSlots || s.slotStatus() != SprogConstants.SLOT_FREE) {
                n++;
            }
            if (n == row) {
                break;
            }
        }
        return slotNum;
    }

    public void dispose() {
        _memo.getCommandStation().removeSlotListener(this);
        // table.removeAllElements();
        // table = null;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogSlotMonDataModel.class);

}
