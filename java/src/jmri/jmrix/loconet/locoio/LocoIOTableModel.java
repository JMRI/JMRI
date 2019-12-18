package jmri.jmrix.loconet.locoio;

import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.jmrix.loconet.LnConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Configurer for LocoIO hardware.
 * <p>
 * This code derves the SV values from the user-selected mode and address; this
 * is different from earlier versions where the user was expected to do the
 * derivation manually. This derivation is complicated by the fact that the
 * "mode" SV[port.0] in the LocoIO doesn't fully specify the operation being
 * done - additional bits in "v2" SV[port.2] are also used. For example, 0x80 is
 * both turnout closed and turnout high. We read and write the mode SV _last_ to
 * handle this.
 * <p>
 * The "addr" field is constructed from (or causes the construction of,
 * depending on whether we are reading or writing...) value1 and value2. In
 * particular, value2 requires knowledge of the mode being set. When "capturing"
 * a turnout address (where we don't have a mode setting) we presume that the
 * address seen in the OPC_SW_REQ packet is for a fixed contact, and interpret
 * the bits in that context.
 * <p>
 * The timeout code is modelled after that in jmri.jmrix.AbstractProgrammer,
 * though there are significant modifications.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LocoIOTableModel
        extends javax.swing.table.AbstractTableModel
        implements java.beans.PropertyChangeListener {

    private LocoIOData liodata;
    private boolean inHex;
    //private String maxSizeMode = "";

    /**
     * Define the number of rows in the table, which is also the number of
     * "channels" in a single LocoIO unit.
     */
    private int _numRows = 16;

    /**
     * Define the contents of the individual columns.
     */
    public static final int PINCOLUMN = 0;     // pin number
    public static final int MODECOLUMN = 1;    // what makes this happen?
    public static final int ADDRCOLUMN = 2;    // what address is involved?
    public static final int SV0COLUMN = 3;     //  SV config code
    public static final int SV1COLUMN = 4;     //  SV Value1
    public static final int SV2COLUMN = 5;     //  SV Value2
    public static final int CAPTURECOLUMN = 6; // "capture" button
    public static final int READCOLUMN = 7;    // "read" button
    public static final int WRITECOLUMN = 8;   // "write" button
    public static final int HIGHESTCOLUMN = WRITECOLUMN + 1;

    private String[] msg = new String[_numRows];

    /**
     * Reference to the JTextField which should receive status info.
     */
    private JTextField status = null;

    /**
     * Reference to JLabel for firmware version.
     */
    //private JLabel     firmware = null;
    //private JLabel     locobuffer = null;
    /**
     * Primary constructor. Initializes all the arrays.
     */
    public LocoIOTableModel(LocoIOData ldata) {
        super();

        inHex = true;
        // references to external resources
        liodata = ldata;
        ldata.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // String s = "LocoIOTableModel: " + evt.getPropertyName() + " := " + evt.getNewValue() + " from " + evt.getSource();
        if (evt.getPropertyName().equals("PortChange")) { // NOI18N
            Integer i = (Integer) evt.getNewValue();
            int v = i.intValue();
            // log.debug("{} ROW = {}", i, v);
            fireTableRowsUpdated(v, v);
        }
    }

    // basic methods for AbstractTableModel implementation
    @Override
    public int getRowCount() {
        return _numRows;
    }

    @Override
    public int getColumnCount() {
        return HIGHESTCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case PINCOLUMN:
                return Bundle.getMessage("ColumnPort");
            case MODECOLUMN:
                return Bundle.getMessage("ColumnAction");
            case ADDRCOLUMN:
                return Bundle.getMessage("AddressCol");
            case SV0COLUMN:
                return "SV"; // NOI18N
            case SV1COLUMN:
                return "Value1"; // NOI18N
            case SV2COLUMN:
                return "Value2"; // NOI18N
            case CAPTURECOLUMN:
                return "";
            case READCOLUMN:
                return "";
            case WRITECOLUMN:
                return "";
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case PINCOLUMN:
                return String.class;
            case MODECOLUMN:
                return String.class;
            case ADDRCOLUMN:
                return String.class;
            case SV0COLUMN:
                return String.class;
            case SV1COLUMN:
                return String.class;
            case SV2COLUMN:
                return String.class;
            case CAPTURECOLUMN:
                return JButton.class;
            case READCOLUMN:
                return JButton.class;
            case WRITECOLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case PINCOLUMN:
                return false;
            case MODECOLUMN:
                return true;
            case ADDRCOLUMN:
                return true;
            case SV0COLUMN:
                return false;
            case SV1COLUMN:
                return false;
            case SV2COLUMN:
                return false;
            case CAPTURECOLUMN:
                return true;
            case READCOLUMN:
                return true;
            case WRITECOLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case PINCOLUMN:
                return Integer.toString(row + 1);  // Ports 1 to 16
            case MODECOLUMN:
                return liodata.getMode(row);
            case ADDRCOLUMN:
                return (liodata.getAddr(row) == 0 ? ("<" + Bundle.getMessage("None").toLowerCase() + ">") : Integer.toString(liodata.getAddr(row)));
            case SV0COLUMN:
                return (inHex) ? "0x" + Integer.toHexString(liodata.getSV(row)) : "" + liodata.getSV(row);
            case SV1COLUMN:
                return (inHex) ? "0x" + Integer.toHexString(liodata.getV1(row)) : "" + liodata.getV1(row);
            case SV2COLUMN:
                return (inHex) ? "0x" + Integer.toHexString(liodata.getV2(row)) : "" + liodata.getV2(row);
            case CAPTURECOLUMN:
                return Bundle.getMessage("ButtonCapture");
            case READCOLUMN:
                return Bundle.getMessage("ButtonRead");
            case WRITECOLUMN:
                return Bundle.getMessage("ButtonWrite");
            default:
                return "unknown"; // NOI18N
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case PINCOLUMN:
                return new JLabel(" 16 ").getPreferredSize().width; // NOI18N
            case MODECOLUMN:
                return new JLabel("1234567890123456789012345678901234567890").getPreferredSize().width; // NOI18N
            case ADDRCOLUMN:
                return new JLabel(getColumnName(ADDRCOLUMN)).getPreferredSize().width;
            case SV0COLUMN:
            case SV1COLUMN:
            case SV2COLUMN:
                return new JLabel(" 0xFF ").getPreferredSize().width; // NOI18N
            case CAPTURECOLUMN:
                return new JButton(Bundle.getMessage("ButtonCapture")).getPreferredSize().width;
            case READCOLUMN:
                return new JButton(Bundle.getMessage("ButtonRead")).getPreferredSize().width;
            case WRITECOLUMN:
                return new JButton(Bundle.getMessage("ButtonWrite")).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == MODECOLUMN) {
            if (liodata.getLocoIOModeList().isValidModeValue(value)) {
                liodata.setMode(row, (String) value);
                liodata.setLIM(row, (String) value);
                LocoIOMode l = liodata.getLIM(row);
                if (l != null) {
                    liodata.setSV(row, l.getSV());
                    liodata.setV1(row, l, liodata.getAddr(row));
                    liodata.setV2(row, l, liodata.getAddr(row));

                    msg[row] = "Packet: " + LnConstants.OPC_NAME(l.getOpCode()) + " " // NOI18N
                            + Integer.toHexString(liodata.getV1(row)) + " "
                            + Integer.toHexString(liodata.getV2(row)) + " <CHK>"; // NOI18N
                    if (status != null) {
                        status.setText(msg[row]);
                    }
                    fireTableRowsUpdated(row, row);
                }
            }
        } else if (col == ADDRCOLUMN) {
            int a;
            if (((String) (value)).startsWith("0x")) {
                a = Integer.valueOf(((String) value).substring(2), 16).intValue();
            } else {
                try {
                    a = Integer.valueOf((String) value, 10).intValue();
                } catch (NumberFormatException ne) {
                    log.warn("Enter a hex or decimal number for the Port Address first");
                    return;
                }
            }
            if (a < 1) {
                a = 1;
            }
            if (a > 0xFFF) {
                a = 0xFFF;
            }
            liodata.setAddr(row, a);
            // ignore default start-up entry, created in #getValueAt(int, int)
            if (!(("<" + Bundle.getMessage("None").toLowerCase() + ">").equals(liodata.getMode(row)))) {
                LocoIOMode l = liodata.getLIM(row);
                liodata.setV1(row, l, a);
                liodata.setV2(row, l, a);

                int opcode = (l == null) ? 0 : l.getOpCode();
                msg[row] = "Packet: " + LnConstants.OPC_NAME(opcode) // NOI18N
                        + " " + Integer.toHexString(liodata.getV1(row))
                        + " " + Integer.toHexString(liodata.getV2(row))
                        + " <CHK>"; // NOI18N

                if (status != null) {
                    status.setText(msg[row]);
                }
            } else {
                log.warn("Select an option from the Mode drop down first");
            }
            fireTableRowsUpdated(row, row);
        } else if (col == CAPTURECOLUMN) {
            // start a capture operation
            liodata.captureValues(row);
        } else if (col == READCOLUMN) {
            // start a read operation
            liodata.readValues(row);

        } else if (col == WRITECOLUMN) {
            // start a write operation
            liodata.writeValues(row);
        }
    }

    // public static String[] getValidOnModes() { return validmodes.getValidModes(); }
    public void dispose() {
        log.debug("dispose"); // NOI18N
    }

    private final static Logger log = LoggerFactory.getLogger(LocoIOTableModel.class);

}
