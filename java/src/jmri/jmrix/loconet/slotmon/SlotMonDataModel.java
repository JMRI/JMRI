package jmri.jmrix.loconet.slotmon;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.Throttle;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotListener;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of slot manager contents.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Jeffrey Machacek 2013
 */
public class SlotMonDataModel extends javax.swing.table.AbstractTableModel implements SlotListener {

    static public final int SLOTCOLUMN = 0;
    static public final int ESTOPCOLUMN = 1;
    static public final int ADDRCOLUMN = 2;
    static public final int SPDCOLUMN = 3;
    static public final int TYPECOLUMN = 4;
    static public final int STATCOLUMN = 5;  // status: free, common, etc
    static public final int DISPCOLUMN = 6;  // originally "dispatch" button, now "free"
    static public final int CONSCOLUMN = 7;  // consist state
    static public final int THROTCOLUMN = 8;
    static public final int DIRCOLUMN = 9;
    static public final int F0COLUMN = 10;
    static public final int F1COLUMN = 11;
    static public final int F2COLUMN = 12;
    static public final int F3COLUMN = 13;
    static public final int F4COLUMN = 14;
    static public final int F5COLUMN = 15;
    static public final int F6COLUMN = 16;
    static public final int F7COLUMN = 17;
    static public final int F8COLUMN = 18;

    static public final int NUMCOLUMN = 19;

    private final transient LocoNetSystemConnectionMemo memo;

    SlotMonDataModel(int row, int column, LocoNetSystemConnectionMemo memo) {
        this.memo = memo;

        // connect to SlotManager for updates
        memo.getSlotManager().addSlotListener(this);

        // start update process
        memo.getSlotManager().update();
    }

    /**
     * Return the number of rows to be displayed. This can vary depending on
     * whether only active rows are displayed, and whether the system slots
     * should be displayed.
     * <p>
     * This should probably use a local cache instead of counting/searching each
     * time.
     *
     * @return the number of rows
     */
    @Override
    public int getRowCount() {
        return 128;
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
            case ESTOPCOLUMN:
                return "";     // no heading, as button is clear
            case ADDRCOLUMN:
                return Bundle.getMessage("AddressCol");
            case SPDCOLUMN:
                return Bundle.getMessage("SpeedCol");
            case TYPECOLUMN:
                return Bundle.getMessage("StatusCol");
            case STATCOLUMN:
                return Bundle.getMessage("UseCol");
            case CONSCOLUMN:
                return Bundle.getMessage("ConsistedCol");
            case DIRCOLUMN:
                return Bundle.getMessage("DirectionCol");
            case DISPCOLUMN:
                return "";     // no heading, as button is clear
            case F0COLUMN:
                return Throttle.F0;
            case F1COLUMN:
                return Throttle.F1;
            case F2COLUMN:
                return Throttle.F2;
            case F3COLUMN:
                return Throttle.F3;
            case F4COLUMN:
                return Throttle.F4;
            case F5COLUMN:
                return Throttle.F5;
            case F6COLUMN:
                return Throttle.F6;
            case F7COLUMN:
                return Throttle.F7;
            case F8COLUMN:
                return Throttle.F8;
            case THROTCOLUMN:
                return Bundle.getMessage("ThrottleIDCol");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SLOTCOLUMN:
            case ADDRCOLUMN:
                return Integer.class;
            case SPDCOLUMN:
            case TYPECOLUMN:
            case STATCOLUMN:
            case CONSCOLUMN:
            case DIRCOLUMN:
            case THROTCOLUMN:
                return String.class;
            case ESTOPCOLUMN:
            case DISPCOLUMN:
                return JButton.class;
            case F0COLUMN:
            case F1COLUMN:
            case F2COLUMN:
            case F3COLUMN:
            case F4COLUMN:
            case F5COLUMN:
            case F6COLUMN:
            case F7COLUMN:
            case F8COLUMN:
                return Boolean.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case ESTOPCOLUMN:
            case DISPCOLUMN:
            case F0COLUMN:
            case F1COLUMN:
            case F2COLUMN:
            case F3COLUMN:
            case F4COLUMN:
            case F5COLUMN:
            case F6COLUMN:
            case F7COLUMN:
            case F8COLUMN:
                // only loco slots (1-120 incl) to be marked writeable only, system slot are read only
                return ((row > 0) && (row < 121));
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        LocoNetSlot s = memo.getSlotManager().slot(row);
        String t;
        if (s == null) {
            log.error("slot pointer was null for slot row: {} col: {}", row, col);
            return null;
        }

        switch (col) {
            case SLOTCOLUMN:  // slot number
                return row;
            case ESTOPCOLUMN:
                return Bundle.getMessage("ButtonEstop"); // will be name of button in default GUI
            case ADDRCOLUMN:
                return s.locoAddr();
            case SPDCOLUMN:
                switch (s.consistStatus()) {
                    case LnConstants.CONSIST_TOP:
                    case LnConstants.CONSIST_NO:
                        if (s.speed() == 1) {
                            t = "(estop) 1";
                        } else {
                            t = "          " + s.speed();
                        }
                        return t.substring(t.length() - 9, t.length()); // 9 comes from length of the "(estop)" prefix
                    case LnConstants.CONSIST_MID:
                    case LnConstants.CONSIST_SUB:
                        return Bundle.getMessage("SlotSpeedConsist");
                    default:
                        return Bundle.getMessage("StateError");
                }
            case TYPECOLUMN:
                switch (s.decoderType()) {
                    case LnConstants.DEC_MODE_128A:
                        return "128 step adv";
                    case LnConstants.DEC_MODE_28A:
                        return " 28 step adv";
                    case LnConstants.DEC_MODE_128:
                        return "128 step";
                    case LnConstants.DEC_MODE_14:
                        return " 14 step";
                    case LnConstants.DEC_MODE_28TRI:
                        return " 28 step trinary";
                    case LnConstants.DEC_MODE_28:
                        return " 28 step";
                    default:
                        return Bundle.getMessage("StateUnknown");
                }
            case STATCOLUMN:
                switch (s.slotStatus()) {
                    case LnConstants.LOCO_IN_USE:
                        return Bundle.getMessage("StateInUse");
                    case LnConstants.LOCO_IDLE:
                        return Bundle.getMessage("StateIdle");
                    case LnConstants.LOCO_COMMON:
                        return Bundle.getMessage("StateCommon");
                    case LnConstants.LOCO_FREE:
                        return Bundle.getMessage("StateFree");
                    default:
                        return Bundle.getMessage("StateError");
                }
            case CONSCOLUMN:
                switch (s.consistStatus()) {
                    case LnConstants.CONSIST_MID:
                        t = Bundle.getMessage("SlotConsistMidX", s.speed());
                        return t;
                    case LnConstants.CONSIST_TOP:
                        return Bundle.getMessage("SlotConsistTop");
                    case LnConstants.CONSIST_SUB:
                        t = Bundle.getMessage("SlotConsistSubX", s.speed());
                        return t;
                    case LnConstants.CONSIST_NO:
                        return Bundle.getMessage("SlotConsistNone");
                    default:
                        return Bundle.getMessage("StateError");
                }
            case DISPCOLUMN:
                return Bundle.getMessage("ButtonRelease"); // will be name of button in default GUI
            case DIRCOLUMN:
                return s.isForward() ? Bundle.getMessage("DirColForward") : Bundle.getMessage("DirColReverse");
            case F0COLUMN:
                return s.isF0();
            case F1COLUMN:
                return s.isF1();
            case F2COLUMN:
                return s.isF2();
            case F3COLUMN:
                return s.isF3();
            case F4COLUMN:
                return s.isF4();
            case F5COLUMN:
                return s.isF5();
            case F6COLUMN:
                return s.isF6();
            case F7COLUMN:
                return s.isF7();
            case F8COLUMN:
                return s.isF8();
            case THROTCOLUMN:
                int upper = (s.id() >> 7) & 0x7F;
                int lower = s.id() & 0x7F;
                return StringUtil.twoHexFromInt(upper) + " " + StringUtil.twoHexFromInt(lower);

            default:
                log.error("internal state inconsistent with table requst for {} {}", row, col);
                return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case SLOTCOLUMN:
                return new JTextField(3).getPreferredSize().width;
            case ESTOPCOLUMN:
                return new JButton(Bundle.getMessage("ButtonEstop")).getPreferredSize().width;
            case ADDRCOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SPDCOLUMN:
            case STATCOLUMN:
                return new JTextField(6).getPreferredSize().width;
            case TYPECOLUMN:
                return new JTextField(12).getPreferredSize().width;
            case CONSCOLUMN:
                return new JTextField(4).getPreferredSize().width;
            case DIRCOLUMN:
                return new JLabel(Bundle.getMessage("DirectionCol")).getPreferredSize().width;
            case DISPCOLUMN:
                return new JButton(Bundle.getMessage("ButtonRelease")).getPreferredSize().width;
            case THROTCOLUMN:
                return new JTextField(7).getPreferredSize().width;
            case F0COLUMN:
            case F1COLUMN:
            case F2COLUMN:
            case F3COLUMN:
            case F4COLUMN:
            case F5COLUMN:
            case F6COLUMN:
            case F7COLUMN:
            case F8COLUMN:
                return new JLabel("       ").getPreferredSize().width; // to show checkboxes
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        int status;
        LocoNetMessage msg;
        LocoNetSlot s = memo.getSlotManager().slot(row);
        if (s == null) {
            log.error("slot pointer was null for slot row: {} col: {}", row, col);
            return;
        }

        switch (col) {
            case ESTOPCOLUMN:
                log.debug("Start estop in slot {}", row);
                if ((s.consistStatus() == LnConstants.CONSIST_SUB)
                        || (s.consistStatus() == LnConstants.CONSIST_MID)) {
                    Object[] options = {Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")};
                    int result
                            = JOptionPane.showOptionDialog(null,
                                    Bundle.getMessage("SlotEstopWarning"),
                                    Bundle.getMessage("WarningTitle"),
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE,
                                    null, options, options[1]);
                    if (result == 1) {
                        return;
                    }
                }
                msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SPD);
                msg.setElement(1, s.getSlot());
                msg.setElement(2, 1);       // 1 here is estop
                memo.getLnTrafficController().sendLocoNetMessage(msg);
                fireTableRowsUpdated(row, row);
                break;
            case F0COLUMN:
            case F1COLUMN:
            case F2COLUMN:
            case F3COLUMN:
            case F4COLUMN:
                log.debug("F0-F4 change requested {}", row);
                s = memo.getSlotManager().slot(row);
                if (s == null) {
                    log.error("slot pointer was null for slot row: {} col: {}", row, col);
                    return;
                }
                boolean tempF0 = (col == F0COLUMN) ? !s.isF0() : s.isF0();
                boolean tempF1 = (col == F1COLUMN) ? !s.isF1() : s.isF1();
                boolean tempF2 = (col == F2COLUMN) ? !s.isF2() : s.isF2();
                boolean tempF3 = (col == F3COLUMN) ? !s.isF3() : s.isF3();
                boolean tempF4 = (col == F4COLUMN) ? !s.isF4() : s.isF4();

                int new_dirf = ((s.isForward() ? 0 : LnConstants.DIRF_DIR)
                        | (tempF0 ? LnConstants.DIRF_F0 : 0)
                        | (tempF1 ? LnConstants.DIRF_F1 : 0)
                        | (tempF2 ? LnConstants.DIRF_F2 : 0)
                        | (tempF3 ? LnConstants.DIRF_F3 : 0)
                        | (tempF4 ? LnConstants.DIRF_F4 : 0));

                // set status to 'In Use' if other
                status = s.slotStatus();
                if (status != LnConstants.LOCO_IN_USE) {
                    memo.getLnTrafficController().sendLocoNetMessage(
                            s.writeStatus(LnConstants.LOCO_IN_USE
                            ));
                }
                msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
                msg.setElement(1, s.getSlot());
                msg.setElement(2, new_dirf);       // 1 here is estop
                memo.getLnTrafficController().sendLocoNetMessage(msg);
                // Delay here allows command station time to xmit on the rails.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    log.error(null, ex);
                }
                // reset status to original value if not previously 'in use'
                if (status != LnConstants.LOCO_IN_USE) {
                    memo.getLnTrafficController().sendLocoNetMessage(
                            s.writeStatus(status));
                }
                fireTableRowsUpdated(row, row);
                break;
            case F5COLUMN:
            case F6COLUMN:
            case F7COLUMN:
            case F8COLUMN:
                log.debug("F5-F8 change requested {}", row);

                boolean tempF5 = (col == F5COLUMN) ? !s.isF5() : s.isF5();
                boolean tempF6 = (col == F6COLUMN) ? !s.isF6() : s.isF6();
                boolean tempF7 = (col == F7COLUMN) ? !s.isF7() : s.isF7();
                boolean tempF8 = (col == F8COLUMN) ? !s.isF8() : s.isF8();

                int new_snd = ((tempF8 ? LnConstants.SND_F8 : 0)
                        | (tempF7 ? LnConstants.SND_F7 : 0)
                        | (tempF6 ? LnConstants.SND_F6 : 0)
                        | (tempF5 ? LnConstants.SND_F5 : 0));

                // set status to 'In Use' if other
                status = s.slotStatus();
                if (status != LnConstants.LOCO_IN_USE) {
                    memo.getLnTrafficController().sendLocoNetMessage(
                            s.writeStatus(LnConstants.LOCO_IN_USE
                            ));
                }

                msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SND);
                msg.setElement(1, s.getSlot());
                msg.setElement(2, new_snd);       // 1 here is estop
                memo.getLnTrafficController().sendLocoNetMessage(msg);
                // Delay here allows command station time to xmit on the rails.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    log.error(null, ex);
                }

                // reset status to original value if not previously 'in use'
                if (status != LnConstants.LOCO_IN_USE) {
                    memo.getLnTrafficController().sendLocoNetMessage(
                            s.writeStatus(status));
                }

                fireTableRowsUpdated(row, row);
                break;
            case DISPCOLUMN:
                log.debug("Start freeing slot {}", row);
                if (s.slotStatus() != LnConstants.LOCO_FREE) {
                    if (s.consistStatus() != LnConstants.CONSIST_NO) {
                        // Freeing a member takes it out of the consist
                        // entirely (i.e., while the slot is LOCO_FREE, it
                        // still reads the former consist information, but
                        // the next time that loco is selected, it comes
                        // back as CONSIST_NO).  Freeing the CONSIST_TOP
                        // will kill the entire consist.
                        Object[] options = {"OK", "Cancel"};
                        int result
                                = JOptionPane.showOptionDialog(null,
                                        "Freeing a consist member will destroy the consist.\n\nAre you sure you want to do that?",
                                        "Warning",
                                        JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.WARNING_MESSAGE,
                                        null, options, options[1]);
                        if (result == 1) {
                            return;
                        }
                    }
                    // send status to free
                    memo.getLnTrafficController().sendLocoNetMessage(
                            s.writeStatus(LnConstants.LOCO_FREE));
                } else {
                    log.debug("Slot not in use");
                }
                fireTableRowsUpdated(row, row);
                break;
            default:
                // nothing to do if column not recognized
                break;
        }
    }

    //Added by Jeffrey Machacek, date: 2013
    //changed 8/22/2013
    public void clearAllSlots() {
        int count = getRowCount();

        for (int row = 0; row < (count - 1); row++) {
            LocoNetSlot s = memo.getSlotManager().slot(row);

            if ((s.slotStatus() != LnConstants.LOCO_IN_USE) && (s.consistStatus() == LnConstants.CONSIST_NO)) {
                log.debug("Freeing {} from slot {}, old status: {}", s.locoAddr(), s.getSlot(), s.slotStatus());
                memo.getLnTrafficController().sendLocoNetMessage(
                        s.writeStatus(LnConstants.LOCO_FREE
                        ));
                fireTableRowsUpdated(row, row);
            }
            count = getRowCount();
        }
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     * @param slotTable the table to configure
     */
    public void configureTable(JTable slotTable) {
    }

    // methods to communicate with SlotManager
    @Override
    public synchronized void notifyChangedSlot(LocoNetSlot s) {
        // update model from this slot
        int slotNum = s.getSlot();
        int slotStatus2;

        if (slotNum == LnConstants.CFG_SLOT) {
            slotStatus2 = s.ss2() & 0x78; // Bit 3-6 of SS2 contains SW36-39 of the CFG_SLOT
            if (slotStatus2 > 0) {
                memo.getSlotManager().update();
            }
        } else {
            slotNum = -1; // all rows
        }

        // notify the JTable object that a row has changed; do that in the Swing thread!
        javax.swing.SwingUtilities.invokeLater(new Notify(slotNum, this));
    }

    static private class Notify implements Runnable {

        private final int _row;
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

    protected LocoNetSlot getSlot(int row) {
        return memo.getSlotManager().slot(row);
    }

    /**
     * This is a convenience method that makes it easier for classes using this
     * model to set all in-use slots to emergency stop
     */
    public void estopAll() {
        for (int slotNum = 0; slotNum < 120; slotNum++) {
            LocoNetSlot s = memo.getSlotManager().slot(slotNum);
            if (s.slotStatus() != LnConstants.LOCO_FREE
                    && (s.consistStatus() == LnConstants.CONSIST_NO
                    || s.consistStatus() == LnConstants.CONSIST_TOP)
                    && s.speed() != 1) {
                // send message to estop this loco
                LocoNetMessage msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SPD);
                msg.setElement(1, s.getSlot());
                msg.setElement(2, 1);  // emergency stop
                memo.getLnTrafficController().sendLocoNetMessage(msg);
            }
        }
    }

    public void dispose() {
        memo.getSlotManager().removeSlotListener(this);
    }

    private final static Logger log = LoggerFactory.getLogger(SlotMonDataModel.class);

}
