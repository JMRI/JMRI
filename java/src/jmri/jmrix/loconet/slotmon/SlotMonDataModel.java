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
    static public final int F9COLUMN = 19;
    static public final int F10COLUMN = 20;
    static public final int F11COLUMN = 21;
    static public final int F12COLUMN = 22;
    static public final int F13COLUMN = 23;
    static public final int F14COLUMN = 24;
    static public final int F15COLUMN = 25;
    static public final int F16COLUMN = 26;
    static public final int F17COLUMN = 27;
    static public final int F18COLUMN = 28;
    static public final int F19COLUMN = 29;
    static public final int F20COLUMN = 30;
    static public final int F21COLUMN = 31;
    static public final int F22COLUMN = 32;
    static public final int F23COLUMN = 33;
    static public final int F24COLUMN = 34;
    static public final int F25COLUMN = 35;
    static public final int F26COLUMN = 36;
    static public final int F27COLUMN = 37;
    static public final int F28COLUMN = 38;

    static public final int NUMCOLUMN = 39;

    private int numRows = 128;

    private final transient LocoNetSystemConnectionMemo memo;

    SlotMonDataModel(int row, int column, LocoNetSystemConnectionMemo memo) {
        this.memo = memo;

        // set number of rows;
        numRows = row;

        // connect to SlotManager for updates
        memo.getSlotManager().addSlotListener(this);

        // start update process
        memo.getSlotManager().update();
    }

    /**
     * Forces a refresh of the slots
     */
    public void refreshSlots() {
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
        return numRows;
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
            case F9COLUMN:
                return Throttle.F9;
            case F10COLUMN:
                return Throttle.F10;
            case F11COLUMN:
                return Throttle.F11;
            case F12COLUMN:
                return Throttle.F12;
            case F13COLUMN:
                return Throttle.F13;
            case F14COLUMN:
                return Throttle.F14;
            case F15COLUMN:
                return Throttle.F15;
            case F16COLUMN:
                return Throttle.F16;
            case F17COLUMN:
                return Throttle.F17;
            case F18COLUMN:
                return Throttle.F18;
            case F19COLUMN:
                return Throttle.F19;
            case F20COLUMN:
                return Throttle.F20;
            case F21COLUMN:
                return Throttle.F21;
            case F22COLUMN:
                return Throttle.F22;
            case F23COLUMN:
                return Throttle.F23;
            case F24COLUMN:
                return Throttle.F24;
            case F25COLUMN:
                return Throttle.F25;
            case F26COLUMN:
                return Throttle.F26;
            case F27COLUMN:
                return Throttle.F27;
            case F28COLUMN:
                return Throttle.F28;
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
            case F9COLUMN:
            case F10COLUMN:
            case F11COLUMN:
            case F12COLUMN:
            case F13COLUMN:
            case F14COLUMN:
            case F15COLUMN:
            case F16COLUMN:
            case F17COLUMN:
            case F18COLUMN:
            case F19COLUMN:
            case F20COLUMN:
            case F21COLUMN:
            case F22COLUMN:
            case F23COLUMN:
            case F24COLUMN:
            case F25COLUMN:
            case F26COLUMN:
            case F27COLUMN:
            case F28COLUMN:
                return Boolean.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        LocoNetSlot s = memo.getSlotManager().slot(row);
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
            case F9COLUMN:
            case F10COLUMN:
            case F11COLUMN:
            case F12COLUMN:
            case F13COLUMN:
            case F14COLUMN:
            case F15COLUMN:
            case F16COLUMN:
            case F17COLUMN:
            case F18COLUMN:
            case F19COLUMN:
            case F20COLUMN:
            case F21COLUMN:
            case F22COLUMN:
            case F23COLUMN:
            case F24COLUMN:
            case F25COLUMN:
            case F26COLUMN:
            case F27COLUMN:
            case F28COLUMN:
                // only loco slots to be marked writeable only, system slot are read only
                return !s.isSystemSlot();
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
            case F9COLUMN:
                return s.isF9();
            case F10COLUMN:
                return s.isF10();
            case F11COLUMN:
                return s.isF11();
            case F12COLUMN:
                return s.isF12();
            case F13COLUMN:
                return s.isF13();
            case F14COLUMN:
                return s.isF14();
            case F15COLUMN:
                return s.isF15();
            case F16COLUMN:
                return s.isF16();
            case F17COLUMN:
                return s.isF17();
            case F18COLUMN:
                return s.isF18();
            case F19COLUMN:
                return s.isF19();
            case F20COLUMN:
                return s.isF20();
            case F21COLUMN:
                return s.isF21();
            case F22COLUMN:
                return s.isF22();
            case F23COLUMN:
                return s.isF23();
            case F24COLUMN:
                return s.isF24();
            case F25COLUMN:
                return s.isF25();
            case F26COLUMN:
                return s.isF26();
            case F27COLUMN:
                return s.isF27();
            case F28COLUMN:
                return s.isF28();
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
            case F9COLUMN:
            case F10COLUMN:
            case F11COLUMN:
            case F12COLUMN:
            case F13COLUMN:
            case F14COLUMN:
            case F15COLUMN:
            case F16COLUMN:
            case F17COLUMN:
            case F18COLUMN:
            case F19COLUMN:
            case F20COLUMN:
            case F21COLUMN:
            case F22COLUMN:
            case F23COLUMN:
            case F24COLUMN:
            case F25COLUMN:
            case F26COLUMN:
            case F27COLUMN:
            case F28COLUMN:
                return new JLabel("       ").getPreferredSize().width; // to show checkboxes
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
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
                msg = s.writeSpeed(1);
                memo.getLnTrafficController().sendLocoNetMessage(msg);
                fireTableRowsUpdated(row, row);
                break;
            case F0COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup1(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F1COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup1(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F2COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup1(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F3COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup1(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F4COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup1(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F5COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup2(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F6COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup2(s, col, row);
                } else {
                    sendExpFunctionGroup1(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F7COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup2(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F8COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup2(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F9COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup3(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F10COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup3(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F11COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup3(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F12COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup3(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F13COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup2(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F14COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F15COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F16COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F17COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F18COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F19COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F20COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup4(s, col, row);
                } else {
                    sendExpFunctionGroup3(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F21COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F22COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F23COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F24COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F25COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F26COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F27COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
                }
                fireTableRowsUpdated(row, row);
                break;
            case F28COLUMN:
                if (s.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                    sendFunctionGroup5(s, col, row);
                } else {
                    sendExpFunctionGroup4(s, col, row);
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
                LocoNetMessage msg = s.writeSpeed(1) ; // emergency stop
                memo.getLnTrafficController().sendLocoNetMessage(msg);
            }
        }
    }

    /**
     * Send the LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendFunctionGroup1(LocoNetSlot slot, int col, int row) {
        log.debug("F0-F4 change requested {}", row);
        if (slot == null) {
            log.error("slot pointer was null for slot row: {} col: {}", row, col);
            return;
        }
        boolean tempF0 = (col == F0COLUMN) ? !slot.isF0() : slot.isF0();
        boolean tempF1 = (col == F1COLUMN) ? !slot.isF1() : slot.isF1();
        boolean tempF2 = (col == F2COLUMN) ? !slot.isF2() : slot.isF2();
        boolean tempF3 = (col == F3COLUMN) ? !slot.isF3() : slot.isF3();
        boolean tempF4 = (col == F4COLUMN) ? !slot.isF4() : slot.isF4();

        int new_dirf = ((slot.isForward() ? 0 : LnConstants.DIRF_DIR)
                | (tempF0 ? LnConstants.DIRF_F0 : 0)
                | (tempF1 ? LnConstants.DIRF_F1 : 0)
                | (tempF2 ? LnConstants.DIRF_F2 : 0)
                | (tempF3 ? LnConstants.DIRF_F3 : 0)
                | (tempF4 ? LnConstants.DIRF_F4 : 0));

        // set status to 'In Use' if other
        int status = slot.slotStatus();
        if (status != LnConstants.LOCO_IN_USE) {
            memo.getLnTrafficController().sendLocoNetMessage(
                    slot.writeStatus(LnConstants.LOCO_IN_USE
                    ));
        }
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
        msg.setElement(1, slot.getSlot());
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
                    slot.writeStatus(status));
        }
    }

    /**
     * Send the LocoNet message to set the state of functions F5, F6, F7, F8
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendFunctionGroup2(LocoNetSlot slot, int col, int row) {
        boolean tempF5 = (col == F5COLUMN) ? !slot.isF5() : slot.isF5();
        boolean tempF6 = (col == F6COLUMN) ? !slot.isF6() : slot.isF6();
        boolean tempF7 = (col == F7COLUMN) ? !slot.isF7() : slot.isF7();
        boolean tempF8 = (col == F8COLUMN) ? !slot.isF8() : slot.isF8();

        int new_snd = ((tempF8 ? LnConstants.SND_F8 : 0)
                | (tempF7 ? LnConstants.SND_F7 : 0)
                | (tempF6 ? LnConstants.SND_F6 : 0)
                | (tempF5 ? LnConstants.SND_F5 : 0));

        // set status to 'In Use' if other
        int status = slot.slotStatus();
        if (status != LnConstants.LOCO_IN_USE) {
            memo.getLnTrafficController().sendLocoNetMessage(
                    slot.writeStatus(LnConstants.LOCO_IN_USE
                    ));
        }

        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SND);
        msg.setElement(1, slot.getSlot());
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
                    slot.writeStatus(status));
        }
    }

    /**
     * Sends Function Group 3 values - F9 thru F12, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
     protected void sendFunctionGroup3(LocoNetSlot slot, int col, int row) {
        // LocoNet practice is to send F9-F12 as a DCC packet
        boolean tempF9 = (col == F9COLUMN) ? !slot.isF9() : slot.isF9();
        boolean tempF10 = (col == F10COLUMN) ? !slot.isF10() : slot.isF10();
        boolean tempF11 = (col == F11COLUMN) ? !slot.isF11() : slot.isF11();
        boolean tempF12 = (col == F12COLUMN) ? !slot.isF12() : slot.isF12();
        byte[] result = jmri.NmraPacket.function9Through12Packet(slot.locoAddr(), (slot.locoAddr() >= 128),
                tempF9, tempF10, tempF11, tempF12);

        log.debug("sendFunctionGroup3 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) memo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 4 values - F13 thru F20, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendFunctionGroup4(LocoNetSlot slot, int col, int row) {
        // LocoNet practice is to send F13-F20 as a DCC packet
        boolean tempF13 = (col == F13COLUMN) ? !slot.isF13() : slot.isF13();
        boolean tempF14 = (col == F14COLUMN) ? !slot.isF14() : slot.isF14();
        boolean tempF15 = (col == F15COLUMN) ? !slot.isF15() : slot.isF15();
        boolean tempF16 = (col == F16COLUMN) ? !slot.isF16() : slot.isF16();
        boolean tempF17 = (col == F17COLUMN) ? !slot.isF17() : slot.isF17();
        boolean tempF18 = (col == F18COLUMN) ? !slot.isF18() : slot.isF18();
        boolean tempF19 = (col == F19COLUMN) ? !slot.isF19() : slot.isF19();
        boolean tempF20 = (col == F20COLUMN) ? !slot.isF20() : slot.isF20();
        byte[] result = jmri.NmraPacket.function13Through20Packet(slot.locoAddr(), (slot.locoAddr() >= 128),
                tempF13, tempF14, tempF15, tempF16,
                tempF17, tempF18, tempF19, tempF20);

        log.debug("sendFunctionGroup4 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) memo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 5 values - F21 thru F28, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendFunctionGroup5(LocoNetSlot slot, int col, int row) {
        // LocoNet practice is to send F21-F28 as a DCC packet
        // LocoNet practice is to send F13-F20 as a DCC packet
        boolean tempF21 = (col == F21COLUMN) ? !slot.isF21() : slot.isF21();
        boolean tempF22 = (col == F22COLUMN) ? !slot.isF22() : slot.isF22();
        boolean tempF23 = (col == F23COLUMN) ? !slot.isF23() : slot.isF23();
        boolean tempF24 = (col == F24COLUMN) ? !slot.isF24() : slot.isF24();
        boolean tempF25 = (col == F25COLUMN) ? !slot.isF25() : slot.isF25();
        boolean tempF26 = (col == F26COLUMN) ? !slot.isF26() : slot.isF26();
        boolean tempF27 = (col == F27COLUMN) ? !slot.isF27() : slot.isF27();
        boolean tempF28 = (col == F28COLUMN) ? !slot.isF28() : slot.isF28();
        byte[] result = jmri.NmraPacket.function21Through28Packet(slot.locoAddr(), (slot.locoAddr() >= 128),
                tempF21, tempF22, tempF23, tempF24,
                tempF25, tempF26, tempF27, tempF28);

        log.debug("sendFunctionGroup5 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) memo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Send the Expanded LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4, F5, F6
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendExpFunctionGroup1(LocoNetSlot slot, int col, int row) {
        boolean tempF0 = (col == F0COLUMN) ? !slot.isF0() : slot.isF0();
        boolean tempF1 = (col == F1COLUMN) ? !slot.isF1() : slot.isF1();
        boolean tempF2 = (col == F2COLUMN) ? !slot.isF2() : slot.isF2();
        boolean tempF3 = (col == F3COLUMN) ? !slot.isF3() : slot.isF3();
        boolean tempF4 = (col == F4COLUMN) ? !slot.isF4() : slot.isF4();
        boolean tempF5 = (col == F5COLUMN) ? !slot.isF5() : slot.isF5();
        boolean tempF6 = (col == F6COLUMN) ? !slot.isF6() : slot.isF6();
        int new_F0F6 = ((tempF5 ? 0b00100000 : 0) | (tempF6 ? 0b01000000 : 0)
                | (tempF0 ? LnConstants.DIRF_F0 : 0)
                | (tempF1 ? LnConstants.DIRF_F1 : 0)
                | (tempF2 ? LnConstants.DIRF_F2 : 0)
                | (tempF3 ? LnConstants.DIRF_F3 : 0)
                | (tempF4 ? LnConstants.DIRF_F4 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xd5);
            msg.setElement(1, (slot.getSlot() / 128) | 0b00010000 );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F0F6);
            memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    /**
     * Send the Expanded LocoNet message to set the state of functions F7, F8, F8, F9, F10, F11, F12, F13
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendExpFunctionGroup2(LocoNetSlot slot, int col, int row) {
        boolean tempF7 = (col == F7COLUMN) ? !slot.isF7() : slot.isF7();
        boolean tempF8 = (col == F8COLUMN) ? !slot.isF8() : slot.isF8();
        boolean tempF9 = (col == F9COLUMN) ? !slot.isF9() : slot.isF9();
        boolean tempF10 = (col == F10COLUMN) ? !slot.isF10() : slot.isF10();
        boolean tempF11 = (col == F11COLUMN) ? !slot.isF11() : slot.isF11();
        boolean tempF12 = (col == F12COLUMN) ? !slot.isF12() : slot.isF12();
        boolean tempF13 = (col == F13COLUMN) ? !slot.isF13() : slot.isF13();
            int new_F7F13 = ((tempF7 ? 0b00000001 : 0) | (tempF8 ? 0b00000010 : 0)
                    | (tempF9  ? 0b00000100 : 0)
                    | (tempF10 ? 0b00001000 : 0)
                    | (tempF11 ? 0b00010000 : 0)
                    | (tempF12 ? 0b00100000 : 0)
                    | (tempF13 ? 0b01000000 : 0));
                LocoNetMessage msg = new LocoNetMessage(6);
                msg.setOpCode(0xd5);
                msg.setElement(1, (slot.getSlot() / 128) | 0b00011000 );
                msg.setElement(2,slot.getSlot() & 0b01111111);
                msg.setElement(3,slot.id() & 0x7F);
                msg.setElement(4, new_F7F13);
                memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    /**
     * Sends expanded loconet message F14 thru F20
     * Message.
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendExpFunctionGroup3(LocoNetSlot slot, int col, int row) {
        boolean tempF14 = (col == F14COLUMN) ? !slot.isF14() : slot.isF14();
        boolean tempF15 = (col == F15COLUMN) ? !slot.isF15() : slot.isF15();
        boolean tempF16 = (col == F16COLUMN) ? !slot.isF16() : slot.isF16();
        boolean tempF17 = (col == F17COLUMN) ? !slot.isF17() : slot.isF17();
        boolean tempF18 = (col == F18COLUMN) ? !slot.isF18() : slot.isF18();
        boolean tempF19 = (col == F19COLUMN) ? !slot.isF19() : slot.isF19();
        boolean tempF20 = (col == F20COLUMN) ? !slot.isF20() : slot.isF20();
        int new_F14F20 = ((tempF14 ? 0b00000001 : 0) | (tempF15 ? 0b00000010 : 0)
                | (tempF16  ? 0b00000100 : 0)
                | (tempF17 ? 0b00001000 : 0)
                | (tempF18 ? 0b00010000 : 0)
                | (tempF19 ? 0b00100000 : 0)
                | (tempF20 ? 0b01000000 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xd5);
            msg.setElement(1, (slot.getSlot() / 128) | 0b00100000 );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F14F20);
            memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    /**
     * Sends Expanded loconet message F21 thru F28 Message.
     * @param slot loconet slot
     * @param col  grid col
     * @param row  grid row
     */
    protected void sendExpFunctionGroup4(LocoNetSlot slot, int col, int row) {
        boolean tempF21 = (col == F21COLUMN) ? !slot.isF21() : slot.isF21();
        boolean tempF22 = (col == F22COLUMN) ? !slot.isF22() : slot.isF22();
        boolean tempF23 = (col == F23COLUMN) ? !slot.isF23() : slot.isF23();
        boolean tempF24 = (col == F24COLUMN) ? !slot.isF24() : slot.isF24();
        boolean tempF25 = (col == F25COLUMN) ? !slot.isF25() : slot.isF25();
        boolean tempF26 = (col == F26COLUMN) ? !slot.isF26() : slot.isF26();
        boolean tempF27 = (col == F27COLUMN) ? !slot.isF27() : slot.isF27();
        boolean tempF28 = (col == F28COLUMN) ? !slot.isF28() : slot.isF28();
        int new_F14F20 = ((tempF21 ? 0b00000001 : 0) |
                (tempF22 ? 0b00000010 : 0) |
                (tempF23 ? 0b00000100 : 0) |
                (tempF24 ? 0b00001000 : 0) |
                (tempF25 ? 0b00010000 : 0) |
                (tempF26 ? 0b00100000 : 0) |
                (tempF27 ? 0b01000000 : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(0xd5);
        if (tempF28) {
            msg.setElement(1, (slot.getSlot() / 128) | 0b00101000);
        } else {
            msg.setElement(1, (slot.getSlot() / 128) | 0b00110000);
        }
        msg.setElement(2, slot.getSlot() & 0b01111111);
        msg.setElement(3, slot.id() & 0x7F);
        msg.setElement(4, new_F14F20);
        memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    public void dispose() {
        memo.getSlotManager().removeSlotListener(this);
    }

    private final static Logger log = LoggerFactory.getLogger(SlotMonDataModel.class);

}
