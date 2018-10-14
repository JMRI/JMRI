package jmri.jmrix.loconet.locoio;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to display and program a LocoIO configuration.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Egbert Broerse 2018
 */
public class LocoIOPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements java.beans.PropertyChangeListener {

    public LocoIOPanel() {
        super();

    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        ln = memo.getLnTrafficController();
        // creating the table (done here to ensure order OK)
        data = new LocoIOData(Integer.valueOf(addrField.getText(), 16).intValue(),
                Integer.valueOf(subAddrField.getText(), 16).intValue(),
                ln);
        model = new LocoIOTableModel(data);
        table = new JTable(model);
        scroll = new JScrollPane(table);
        empty = new EmptyBorder(5, 5, 5, 5);

        data.addPropertyChangeListener(this);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowHorizontalLines(true);
        table.setAutoCreateColumnsFromModel(true);

        TableColumnModel tcm = table.getColumnModel();
        // install a ComboBox editor on the OnMode column
        JComboBox<String> comboOnBox = new JComboBox<String>(data.getLocoIOModeList().getValidModes());
        comboOnBox.setEditable(true);
        DefaultCellEditor modeEditor = new DefaultCellEditor(comboOnBox);
        tcm.getColumn(LocoIOTableModel.MODECOLUMN).setCellEditor(modeEditor);

        // install a button renderer & editor in the Read, Write and Compare columns
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(LocoIOTableModel.READCOLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(LocoIOTableModel.WRITECOLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(LocoIOTableModel.CAPTURECOLUMN).setCellRenderer(buttonRenderer);

        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(LocoIOTableModel.READCOLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(LocoIOTableModel.WRITECOLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(LocoIOTableModel.CAPTURECOLUMN).setCellEditor(buttonEditor);

        // ensure the table rows, columns have enough room for buttons and comboBox contents
        table.setRowHeight(new JButton(Bundle.getMessage("ButtonCapture")).getPreferredSize().height);
        for (int col = 0; col < LocoIOTableModel.HIGHESTCOLUMN; col++) {
            table.getColumnModel().getColumn(col).setPreferredWidth(model.getPreferredWidth(col));
        }

        // Top (config) row for SV0, SV1, SV2, the board sub address and the PIC version
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JPanel p1a = new JPanel();
        p1a.add(new JLabel(Bundle.getMessage("LocoioAddressLabel")));
        p1a.add(addrField);
        addrField.setPreferredSize(spacer.getPreferredSize());
        addrField.setToolTipText(Bundle.getMessage("AddressToolTip"));
        p1a.add(new JLabel("/"));
        p1a.add(subAddrField);
        subAddrField.setPreferredSize(spacer.getPreferredSize());
        subAddrField.setToolTipText(Bundle.getMessage("SubAddressToolTip"));
        p1.add(p1a);
        p1.add(new JLabel("   ")); // prevent the glue pulling on the address fields
        p1.add(Box.createGlue());  // -------------------
        probeButton = new JButton(Bundle.getMessage("ButtonProbe"));
        probeButton.addActionListener(new ActionListener() {
            @Override
                    public void actionPerformed(ActionEvent a) {
                        data.setLIOVersion("<Not found>"); // NOI18N
                        LocoIO.probeLocoIOs(ln);
                    }
                });
        p1.add(probeButton);
        p1.add(Box.createGlue());  // -------------------
        readAllButton = new JButton(Bundle.getMessage("ButtonReadAll"));
        readAllButton.addActionListener(new ActionListener() {
            @Override
                    public void actionPerformed(ActionEvent a) {
                        data.readAll();
                    }
                });
        p1.add(readAllButton);
        writeAllButton = new JButton(Bundle.getMessage("ButtonWriteAll"));
        writeAllButton.addActionListener(new ActionListener() {
            @Override
                    public void actionPerformed(ActionEvent a) {
                        data.writeAll();
                    }
                });
        p1.add(writeAllButton);
        p1.add(Box.createGlue());  // -------------------
        addrSetButton = new JButton(Bundle.getMessage("ButtonSetAddress"));
        p1.add(addrSetButton);
        addrSetButton.addActionListener(new ActionListener() {
            @Override
                    public void actionPerformed(ActionEvent a) {
                        addrSet();
                    }
                });
        p1.add(Box.createGlue());  // -------------------

        /*
         openButton = new JButton("Load...");
         openButton.setEnabled(false);
         p1.add(openButton);

         saveButton = new JButton("Save...");
         saveButton.setEnabled(false);
         p1.add(saveButton);
         */
        // bottom (status) row, smaller text size display in gray
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(revLabel);
        p2.add(locobuffer);
        revLabel.setFont(revLabel.getFont().deriveFont(0.9f * addrField.getFont().getSize())); // a bit smaller
        revLabel.setForeground(Color.gray);
        locobuffer.setFont(locobuffer.getFont().deriveFont(0.9f * addrField.getFont().getSize()));
        locobuffer.setForeground(Color.gray);
        p2.add(Box.createGlue());  // -------------------
        p2.add(statusLabel);
        p2.add(status);
        statusLabel.setFont(statusLabel.getFont().deriveFont(0.9f * addrField.getFont().getSize()));
        statusLabel.setForeground(Color.gray);
        status.setFont(status.getFont().deriveFont(0.9f * addrField.getFont().getSize()));
        status.setForeground(Color.gray);
        p2.add(Box.createGlue());  // -------------------
        p2.add(fwLabel);
        p2.add(firmware);
        fwLabel.setFont(fwLabel.getFont().deriveFont(0.9f * addrField.getFont().getSize()));
        fwLabel.setForeground(Color.gray);
        firmware.setFont(firmware.getFont().deriveFont(0.9f * addrField.getFont().getSize()));
        firmware.setForeground(Color.gray);

        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        p3.add(p1);
        scroll.setBorder(empty);
        p3.add(scroll);

        add(p3);
        add(p2);

        // updating the Board address needs to be conveyed to the table
        ActionListener al4UnitAddress = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                log.debug("address =|{}|", addrField.getText());
                addrField.setText(addrField.getText().trim());
                subAddrField.setText(subAddrField.getText().trim());
                // check for empty address fields
                if (addrField.getText() == null || addrField.getText().length() < 1) {
                    addrField.setText("1");
                    log.warn("empty Address, set to 1");
                    return;
                }
                if (subAddrField.getText() == null || subAddrField.getText().length() < 1) {
                    subAddrField.setText("0");
                    log.warn("empty SubAddress, set to 0");
                    return;
                }
                try {
                    data.setUnitAddress(
                            Integer.valueOf(addrField.getText(), 16).intValue(),
                            Integer.valueOf(subAddrField.getText(), 16).intValue());
                } catch (NullPointerException e) {
                    log.error("Caught NullPointerException", e); // NOI18N
                } catch (NumberFormatException ne) {
                    log.error("Caught NumberFormatException", ne); // NOI18N
                }
            }
        };
        FocusListener fl4UnitAddress = new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
            }

            @Override
            public void focusLost(FocusEvent event) {
                log.debug("address =|{}|", addrField.getText());
                addrField.setText(addrField.getText().trim());
                subAddrField.setText(subAddrField.getText().trim());
                // check for empty address fields
                if (addrField.getText().length() < 1) {
                    addrField.setText("1");
                    log.warn("empty LocoIO Address, set to 1");
                    return;
                }
                if (subAddrField.getText().length() < 1) {
                    subAddrField.setText("0");
                    log.warn("empty LocoIO SubAddress, set to 0");
                    return;
                }
                try {
                    data.setUnitAddress(
                            Integer.valueOf(addrField.getText(), 16).intValue(),
                            Integer.valueOf(subAddrField.getText(), 16).intValue());
                } catch (NullPointerException e) {
                    log.error("Caught NullPointerException", e); // NOI18N
                }
            }
        };

        addrField.addActionListener(al4UnitAddress);
        subAddrField.addActionListener(al4UnitAddress);
        addrField.addFocusListener(fl4UnitAddress);
        subAddrField.addFocusListener(fl4UnitAddress);

        try {
            data.setUnitAddress(0x51, 0x00);
        } catch (NullPointerException e) {
            log.error("Caught NullPointerException", e); // NOI18N
        }
    }

    LnTrafficController ln;

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locoio.LocoIOFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemLocoIOProgrammer"));
    }

    /**
     * The SET LOCOIO ADDRESS button was pressed. Since this does a broadcast
     * program-all to every LocoIO board on the LocoNet, it needs to be used
     * with caution.
     */
    protected int cautionAddrSet() {
        log.info("Caution: 'Set LocoIO Address' is a broadcast operation to ALL boards on this connection"); // NOI18N
        return JOptionPane.showOptionDialog(this,
                Bundle.getMessage("LocoIoSetAddressWarnDialog"),
                Bundle.getMessage("WarningTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK")}, null);
    }

    /**
     * Change the hardware address of all connected LocoIO boards using inputs from UI.
     * Checks validity of entries.
     * <p>
     * Clips off any address value over 126 (trimmed and prechecked on entry in UI)
     */
    protected void addrSet() {
        // caution user
        int retval = cautionAddrSet();
        if (retval != 1) {
            return; // user cancelled
        }
        int fullAddress;
        int address = Integer.valueOf(addrField.getText(), 16).intValue();
        int subAddress = Integer.valueOf(subAddrField.getText(), 16).intValue();

        if (address > 126) {
            log.warn("Address must be [1..126], was {}", // NOI18N
                    address);
        }
        if ((address & 0xFF) == 0x80) {
            log.warn("Only a LocoBuffer may use address 0x80"); // NOI18N
            return;
        }

        if (subAddress > 126) {
            log.warn("subAddress must be [1..126], was {}", // NOI18N
                    subAddress);
        }
        fullAddress = 0x0100 | (address & 0x07F); // range is [1..79, 81..127]
        subAddress = subAddress & 0x07F; // range is [1..126] clipping off any higher value
        LocoIO.programLocoIOAddress(fullAddress, subAddress, ln); // broadcast new address values
        // need to update UI to show values used, was input validated upon entry in UI pane?
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // these messages can arrive without a complete
        // GUI, in which case we just ignore them
        if (evt.getPropertyName().equals("UnitAddress")) { // NOI18N
            Integer i = (Integer) evt.getNewValue();
            int v = i.intValue();
            v = v & 0xFF;
            if (addrField != null) {
                addrField.setText(Integer.toHexString(v));
            }
            if (firmware != null) {
                firmware.setText("<" + Bundle.getMessage("BeanStateUnknown").toLowerCase() + ">  "); // some trailing space at bottom right corner of pane
            }
        }
        if (evt.getPropertyName().equals("UnitSubAddress")) { // NOI18N
            Integer i = (Integer) evt.getNewValue();
            int v = i.intValue();
            if (subAddrField != null) {
                subAddrField.setText(Integer.toHexString(v));
            }
            if (firmware != null) { // remove former fw info + some trailing space at bottom right corner of pane
                firmware.setText("<" + Bundle.getMessage("BeanStateUnknown").toLowerCase() + ">  ");
            }
        }
        if (evt.getPropertyName().equals("LBVersionChange")) { // NOI18N
            String v = (String) evt.getNewValue();
            if (locobuffer != null) {
                locobuffer.setText(v);
            }
        }
        if (evt.getPropertyName().equals("LIOVersionChange")) { // NOI18N
            String v = (String) evt.getNewValue();
            if (firmware != null) {
                firmware.setText(v + "  "); // add some trailing space at bottom right corner of pane
            }
        }
        if (evt.getPropertyName().equals("StatusChange")) { // NOI18N
            String v = (String) evt.getNewValue();
            if (status != null) {
                status.setText(v);
            }
        }
    }

    JTextField addrField = new JTextField("00");
    JTextField subAddrField = new JTextField("00");
    final static JTextField spacer = new JTextField("123");
    JLabel status = new JLabel(Bundle.getMessage("StateUnknown"));
    JLabel firmware = new JLabel(Bundle.getMessage("StateUnknown"));
    JLabel locobuffer = new JLabel(Bundle.getMessage("StateUnknown"));
    JLabel revLabel = new JLabel(" LocoBuffer rev: "); // a bit of room on the left NOI18N
    JLabel statusLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("StatusCol")) + " ");
    JLabel fwLabel = new JLabel("LocoIO Firmware rev: "); // NOI18N

    JButton addrSetButton = null;
    JButton probeButton = null;
    JButton readAllButton = null;
    JButton writeAllButton = null;
    JButton saveButton = null;
    JButton openButton = null;

    LocoIOData data;
    LocoIOTableModel model;
    JTable table;
    JScrollPane scroll;
    EmptyBorder empty;

    @Override
    public void dispose() {
        // dispose of the model
        model.dispose();
        // take apart the JFrame
        super.dispose();
        model = null;
        table = null;
        scroll = null;
        readAllButton = null;
        writeAllButton = null;
        addrField = null;
        subAddrField = null;
        status = null;
        firmware = null;
        locobuffer = null;
        saveButton = null;
        openButton = null;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoIOPanel.class);

}
