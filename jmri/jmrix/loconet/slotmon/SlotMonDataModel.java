// SlotMonDataModel.java

package jmri.jmrix.loconet.slotmon;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotManager;
import jmri.jmrix.loconet.locoio.ButtonEditor;
import jmri.jmrix.loconet.locoio.ButtonRenderer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

/**
 * Table data model for display of slot manager contents
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version		$Revision: 1.10 $
 */
public class SlotMonDataModel extends javax.swing.table.AbstractTableModel implements SlotListener  {

    static public final int SLOTCOLUMN = 0;
    static public final int ESTOPCOLUMN = 1;
    static public final int ADDRCOLUMN = 2;
    static public final int SPDCOLUMN  = 3;
    static public final int TYPECOLUMN = 4;
    static public final int STATCOLUMN = 5;  // status: free, common, etc
    static public final int DISPCOLUMN = 6;  // originally "dispatch" button, now "free"
    static public final int CONSCOLUMN = 7;  // consist state
    static public final int DIRCOLUMN  = 8;
    static public final int F0COLUMN   = 9;
    static public final int F1COLUMN   = 10;
    static public final int F2COLUMN   = 11;
    static public final int F3COLUMN   = 12;
    static public final int F4COLUMN   = 13;
    static public final int F5COLUMN   = 14;
    static public final int F6COLUMN   = 15;
    static public final int F7COLUMN   = 16;
    static public final int F8COLUMN   = 17;
    static public final int THROTCOLUMN = 18;

    static public final int NUMCOLUMN = 19;
    SlotMonDataModel(int row, int column) {

        // connect to SlotManager for updates
        SlotManager.instance().addSlotListener(this);

        // start update process
        SlotManager.instance().update();
    }

    /**
     * Returns the number of rows to be displayed.  This
     * can vary depending on whether only active rows
     * are displayed, and whether the system slots should be
     * displayed.
     * <P>
     * This should probably use a local cache instead
     * of counting/searching each time.
     * @param row Row number in the displayed table
     */
    public int getRowCount() {
        if (_allSlots) {
            // will show the entire set, so don't bother counting
            if (_systemSlots)
                return 128;
            else
                return 119;  // skip 0, and 120 through 127
        }
        int n = 0;
        int nMin = 1;
        int nMax = 119;
        if (_systemSlots) {
            nMin = 0;
            nMax = 128;
        }
        for (int i=nMin; i<nMax; i++) {
            LocoNetSlot s = SlotManager.instance().slot(i);
            if (s.slotStatus() != LnConstants.LOCO_FREE ||
                i ==0 || i >= 120) n++;    // always show system slots if requested
        }
        return n;
    }


    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case SLOTCOLUMN: return "Slot";
        case ESTOPCOLUMN: return "";     // no heading, as button is clear
        case ADDRCOLUMN: return "Address";
        case SPDCOLUMN: return "Speed";
        case TYPECOLUMN: return "Status";
        case STATCOLUMN: return "Use";
        case CONSCOLUMN: return "Consisted";
        case DIRCOLUMN: return "Direction";
        case DISPCOLUMN: return "";     // no heading, as button is clear
        case F0COLUMN: return "F0";
        case F1COLUMN: return "F1";
        case F2COLUMN: return "F2";
        case F3COLUMN: return "F3";
        case F4COLUMN: return "F4";
        case F5COLUMN: return "F5";
        case F6COLUMN: return "F6";
        case F7COLUMN: return "F7";
        case F8COLUMN: return "F8";
        case THROTCOLUMN: return "Throttle ID";
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case SLOTCOLUMN:
        case ADDRCOLUMN:
        case SPDCOLUMN:
        case TYPECOLUMN:
        case STATCOLUMN:
        case CONSCOLUMN:
        case DIRCOLUMN:
        case F0COLUMN:
        case F1COLUMN:
        case F2COLUMN:
        case F3COLUMN:
        case F4COLUMN:
        case F5COLUMN:
        case F6COLUMN:
        case F7COLUMN:
        case F8COLUMN:
        case THROTCOLUMN:
            return String.class;
        case ESTOPCOLUMN:
        case DISPCOLUMN:
            return JButton.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case ESTOPCOLUMN:
        case DISPCOLUMN:
            return true;
        default:
            return false;
        }
    }

    public Object getValueAt(int row, int col) {
        LocoNetSlot s = SlotManager.instance().slot(slotNum(row));
        if (s == null) log.error("slot pointer was null for slot row: "+row+" col: "+col);

        switch (col) {
        case SLOTCOLUMN:  // slot number
            return new Integer(slotNum(row));
        case ESTOPCOLUMN:  //
            return "E Stop";          // will be name of button in default GUI
        case ADDRCOLUMN:  //
            return new Integer(s.locoAddr());
        case SPDCOLUMN:  //
            if (s.speed() == 1) return "1 (estop)";
            else return new Integer(s.speed());
        case TYPECOLUMN:  //
            switch (s.decoderType()) {
            case LnConstants.DEC_MODE_128A:	return "128 step advanced";
            case LnConstants.DEC_MODE_28A:	return "28 step advanced";
            case LnConstants.DEC_MODE_128:	return "128 step";
            case LnConstants.DEC_MODE_14:	return "14 step";
            case LnConstants.DEC_MODE_28TRI:	return "28 step trinary";
            case LnConstants.DEC_MODE_28:	return "28 step";
            default:				return "<unknown>";
            }
        case STATCOLUMN:  //
            switch (s.slotStatus()) {
            case LnConstants.LOCO_IN_USE: 	return "In Use";
            case LnConstants.LOCO_IDLE:		return "Idle";
            case LnConstants.LOCO_COMMON: 	return "Common";
            case LnConstants.LOCO_FREE: 	return "Free";
            default: 	                        return "<error>";
            }
        case CONSCOLUMN:  //
            switch (s.consistStatus()) {
            case LnConstants.CONSIST_MID:	return "mid";
            case LnConstants.CONSIST_TOP:	return "top";
            case LnConstants.CONSIST_SUB:	return "sub";
            case LnConstants.CONSIST_NO:	return "none";
            default: 	                        return "<error>";
            }
        case DISPCOLUMN:  //
            return "Free";          // will be name of button in default GUI
        case DIRCOLUMN:  //
            return (s.isForward() ? "F" : "R");
        case F0COLUMN:  //
            return (s.isF0() ? "On" : "Off");
        case F1COLUMN:  //
            return (s.isF1() ? "On" : "Off");
        case F2COLUMN:  //
            return (s.isF2() ? "On" : "Off");
        case F3COLUMN:  //
            return (s.isF3() ? "On" : "Off");
        case F4COLUMN:  //
            return (s.isF4() ? "On" : "Off");
        case F5COLUMN:  //
            return (s.isF5() ? "On" : "Off");
        case F6COLUMN:  //
            return (s.isF6() ? "On" : "Off");
        case F7COLUMN:  //
            return (s.isF7() ? "On" : "Off");
        case F8COLUMN:  //
            return (s.isF8() ? "On" : "Off");
        case THROTCOLUMN:
            return new Integer(s.id());

        default:
            log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    };

    public int getPreferredWidth(int col) {
        switch (col) {
        case SLOTCOLUMN:
            return new JLabel(" 123 ").getPreferredSize().width;
        case ESTOPCOLUMN:
            return new JButton(" E Stop ").getPreferredSize().width;
        case ADDRCOLUMN:
            return new JLabel(" 1234 ").getPreferredSize().width;
        case SPDCOLUMN:
            return new JLabel(" 100 ").getPreferredSize().width;
        case TYPECOLUMN:
            return new JLabel(" 128 step advanced ").getPreferredSize().width;
        case STATCOLUMN:
            return new JLabel(" Common ").getPreferredSize().width;
        case CONSCOLUMN:
            return new JLabel("<error>").getPreferredSize().width;
        case DIRCOLUMN:
            return new JLabel(" R ").getPreferredSize().width;
        case DISPCOLUMN:
            return new JButton(" Free ").getPreferredSize().width;
        case F0COLUMN:
        case F1COLUMN:
        case F2COLUMN:
        case F3COLUMN:
        case F4COLUMN:
        case F5COLUMN:
        case F6COLUMN:
        case F7COLUMN:
        case F8COLUMN:
            return new JLabel(" off ").getPreferredSize().width;
        case THROTCOLUMN:
            return new JLabel(" 78187 ").getPreferredSize().width;
        default:
            return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == ESTOPCOLUMN) {
            log.debug("Start estop in slot "+row);
            // check for in use
            LocoNetSlot s = SlotManager.instance().slot(slotNum(row));
            if (s == null) {
                log.error("slot pointer was null for slot row: "+row+" col: "+col);
                return;
            }
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SPD);
            msg.setElement(1, s.getSlot());
            msg.setElement(2, 1);       // 1 here is estop
            LnTrafficController.instance().sendLocoNetMessage(msg);
            fireTableRowsUpdated(row,row);
        }
        else if (col == DISPCOLUMN) {
            log.debug("Start freeing slot "+row);
            // check for in use
            LocoNetSlot s = SlotManager.instance().slot(slotNum(row));
            if (s == null) {
                log.error("slot pointer was null for slot row: "+row+" col: "+col);
                return;
            }
            if (s.slotStatus()!=LnConstants.LOCO_FREE) {
                // send status to free
                LnTrafficController.instance().sendLocoNetMessage(
                        s.writeStatus(LnConstants.LOCO_FREE
                    ));
                // LnTrafficController.instance().sendLocoNetMessage(s.dispatchSlot());
            } else {
                log.debug("Slot not in use");
            }
            fireTableRowsUpdated(row,row);
        }
    }

    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param s
     */
    public void configureTable(JTable slotTable) {
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, SlotMonDataModel.DISPCOLUMN);

        // install a button renderer & editor in the "ESTOP" column for stopping a loco
        setColumnToHoldButton(slotTable, SlotMonDataModel.ESTOPCOLUMN);
    }

    void setColumnToHoldButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
    }

    // methods to communicate with SlotManager
    public synchronized void notifyChangedSlot(LocoNetSlot s) {
        // update model from this slot

        int slotNum = -1;
        if (_allSlots) slotNum=s.getSlot();		// this will be row until we show only active slots

        // notify the JTable object that a row has changed; do that in the Swing thread!
        Runnable r = new Notify(slotNum, this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
    }

    class Notify implements Runnable {
        private int _row;
        javax.swing.table.AbstractTableModel _model;
        public Notify(int row, javax.swing.table.AbstractTableModel model) {
            _row = row; _model = model;
        }
        public void run() {
            if (-1 == _row) {  // notify about entire table
                _model.fireTableDataChanged();  // just that row
            }
            else {
                // notify that _row has changed
                _model.fireTableRowsUpdated(_row, _row);  // just that row
            }
        }
    }

    // methods for control of "all slots" vs "only active slots"
    private boolean _allSlots = true;
    public void showAllSlots(boolean val) { _allSlots = val; }

    // methods for control of display of system slots
    private boolean _systemSlots = true;
    public void showSystemSlots(boolean val) { _systemSlots = val; }

    /**
     * Returns slot number for a specific row.
     * <P>
     * This should probably use a local cache instead
     * of counting/searching each time.
     * @param row Row number in the displayed table
     */
    protected int slotNum(int row) {
        int slotNum;
        int n = -1;   // need to find a used slot to have the 0th one!
        int nMin = 1;
        int nMax = 120;
        if (_systemSlots) {
            nMin = 0;
            nMax = 128;
        }
        for (slotNum=nMin; slotNum<nMax; slotNum++) {
            LocoNetSlot s = SlotManager.instance().slot(slotNum);
            if (_allSlots || s.slotStatus() != LnConstants.LOCO_FREE
                || slotNum ==0 || slotNum >= 120) n++;    // always show system slots if requested
            if (n == row) break;
        }
        return slotNum;
    }

    /**
     * This is a convenience method that makes it easier for classes
     * using this model to set all in-use slots to emergency stop
     */
    public void estopAll() {
        for (int slotNum=0; slotNum<120; slotNum++) {
            LocoNetSlot s = SlotManager.instance().slot(slotNum);
            if (s.slotStatus() != LnConstants.LOCO_FREE &&
                s.speed() != 1) {
                // send message to estop this loco
                LocoNetMessage msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SPD);
                msg.setElement(1, s.getSlot());
                msg.setElement(2, 1);  // emergency stop
                LnTrafficController.instance().sendLocoNetMessage(msg);
            }
        }
    }

    public void dispose() {
        SlotManager.instance().removeSlotListener(this);
        // table.removeAllElements();
        // table = null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SlotMonDataModel.class.getName());

}
