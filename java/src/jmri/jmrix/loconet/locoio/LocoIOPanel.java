// LocoIOPanel.java

package jmri.jmrix.loconet.locoio;

import org.apache.log4j.Logger;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.beans.*;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnTrafficController;

/**
 * Panel displaying and programming a LocoIO configuration.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision$
 */

public class LocoIOPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements java.beans.PropertyChangeListener {

        public LocoIOPanel() {
            super();
        
        }
        
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        ln = memo.getLnTrafficController();
        // creating the table (done here to ensure order OK)
        data        = new LocoIOData(Integer.valueOf(addrField.getText(),16).intValue(),
                                     Integer.valueOf(subAddrField.getText(),16).intValue(),
                                     memo.getLnTrafficController());
        model       = new LocoIOTableModel(data);
        table       = new JTable(model);
        scroll      = new JScrollPane(table);

        data.addPropertyChangeListener(this);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setShowHorizontalLines(true);
        table.setAutoCreateColumnsFromModel(true);

        TableColumnModel tcm = table.getColumnModel();
        // install a ComboBox editor on the OnMode column
        JComboBox comboOnBox = new JComboBox(data.getLocoIOModeList().getValidModes());
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
        table.setRowHeight(new JButton("Capture").getPreferredSize().height);
        for (int col = 0; col < LocoIOTableModel.HIGHESTCOLUMN; col++) {
            table.getColumnModel().getColumn(col).setPreferredWidth(model.getPreferredWidth(col));
        }

        // A pane for SV0, SV1, SV2, the board sub address and the PIC version
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(new JLabel("LocoIO address: 0x"));
            addrField.setMaximumSize(addrField.getPreferredSize());
            subAddrField.setMaximumSize(subAddrField.getPreferredSize());
        p1.add(addrField);
        p1.add(new JLabel("/"));
        p1.add(subAddrField);
        p1.add(Box.createGlue());  // -------------------
        probeButton = new JButton("Probe");
        probeButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    data.setLIOVersion("<Not found>");
                    LocoIO.probeLocoIOs(ln);
                }
            });
        p1.add(probeButton);
        p1.add(Box.createGlue());  // -------------------
        readAllButton = new JButton("Read All");
        readAllButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent a) { data.readAll();}
            });
        p1.add(readAllButton);
        writeAllButton = new JButton("Write All");
        writeAllButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent a) { data.writeAll();}
            });
        p1.add(writeAllButton);
        p1.add(Box.createGlue());  // -------------------
            addrSetButton = new JButton("Set address");
        p1.add(addrSetButton);
            addrSetButton.addActionListener(
                new ActionListener() {
                public void actionPerformed(ActionEvent a) { addrSet(); }
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

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(new JLabel("Locobuffer rev: "));
        p2.add(locobuffer);
        p2.add(Box.createGlue());  // -------------------
        p2.add(new JLabel("Status: "));
        p2.add(status);
        p2.add(Box.createGlue());  // -------------------
        p2.add(new JLabel("LocoIO Firmware rev: "));
        p2.add(firmware);

        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        p3.add(p1);
        p3.add(table);


        add(p3);
        add(p2);

        // updating the Board address needs to be conveyed to the table

        ActionListener al4UnitAddress = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                try {
                    data.setUnitAddress(
                            Integer.valueOf(addrField.getText(),16).intValue(),
                            Integer.valueOf(subAddrField.getText(),16).intValue());
                } catch (NullPointerException e) {
                    log.error("Caught NullPointerException", e);
                }
            }
        };
        FocusListener fl4UnitAddress = new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost(FocusEvent event) {
                try {
                    data.setUnitAddress(
                            Integer.valueOf(addrField.getText(),16).intValue(),
                            Integer.valueOf(subAddrField.getText(),16).intValue());
                } catch (NullPointerException e) {
                    log.error("Caught NullPointerException", e);
                }
            }
        };

        addrField.addActionListener(al4UnitAddress);
        subAddrField.addActionListener(al4UnitAddress);
        addrField.addFocusListener(fl4UnitAddress);
        subAddrField.addFocusListener(fl4UnitAddress);

        try {
            data.setUnitAddress(0x51,0x00);
        } catch (NullPointerException e) {
            log.error("Caught NullPointerException", e);
        }

    }

    LnTrafficController ln;
    
    public String getHelpTarget() { return "package.jmri.jmrix.loconet.locoio.LocoIOFrame"; }
    public String getTitle() { 
        return getTitle(jmri.jmrix.loconet.LocoNetBundle.bundle().getString("MenuItemLocoIOProgrammer")); 
    }
    
    /**
     * the SET LOCOIO ADDRESS button was pressed
     * Since this does a broadcast program-all to every
     * LocoIO board on the LocoNet, it needs to be used
     * with caution.
     */

    protected int cautionAddrSet() {
        log.info("Caution: Set locoio address is a broadcast operation");
        return JOptionPane.showOptionDialog(this,
                                "This will set the address of all attached LocoIO boards",
                                "Global operation!",
                                0, JOptionPane.INFORMATION_MESSAGE, null,
                                new Object[]{"Cancel", "OK"}, null );
    }

    protected void addrSet() {
        // caution user
        int retval = cautionAddrSet();
        if (retval != 1 ) return; // user cancelled
        int address = Integer.valueOf(addrField.getText(),16).intValue();
        int subAddress = Integer.valueOf(subAddrField.getText(),16).intValue();

        if ((address&0x7F00) != 0x0100) log.warn("High part of address should be 0x01, was "
                                            +(address&0x7F00)/256);
        if ((address&0x7FFF) == 0x0180) log.warn("Only a LocoBuffer can use address 0x80");

        if (subAddress > 126) log.warn("subAddress must be [1..126]" +
			", was " + subAddress);
	    address    = 0x0100 | (address&0x07F);  // range is [1..79, 81..127]
	    subAddress = subAddress & 0x07F;	// range is [1..126]
        LocoIO.programLocoIOAddress(address, subAddress, ln);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // these messages can arrive without a complete
        // GUI, in which case we just ignore them
        if (evt.getPropertyName().equals("UnitAddress")) {
            Integer i = (Integer)evt.getNewValue();
            int v = i.intValue();
            v = v & 0xFF;
            if (addrField!=null) addrField.setText(Integer.toHexString(v));
            if (firmware!=null) firmware.setText("unknown  ");
        }
        if (evt.getPropertyName().equals("UnitSubAddress")) {
            Integer i = (Integer)evt.getNewValue();
            int v = i.intValue();
            if (subAddrField!=null) subAddrField.setText(Integer.toHexString(v));
            if (firmware!=null) firmware.setText("unknown  ");
        }
        if (evt.getPropertyName().equals("LBVersionChange")) {
            String v = (String)evt.getNewValue();
            if (locobuffer!=null) locobuffer.setText(" " + v);
        }
        if (evt.getPropertyName().equals("LIOVersionChange")) {
            String v = (String)evt.getNewValue();
            if (firmware!=null) firmware.setText(v + "    ");
        }
        if (evt.getPropertyName().equals("StatusChange")) {
            String v = (String)evt.getNewValue();
            if (status!=null) status.setText(v + " ");
        }
    }

    JTextField addrField    = new JTextField("00");
    JTextField subAddrField = new JTextField("00");
    JLabel     status       = new JLabel("<unknown>");
    JLabel     firmware     = new JLabel("<unknown>");
    JLabel     locobuffer   = new JLabel("<unknown>");

    JButton addrSetButton  = null;
    JButton probeButton    = null;
    JButton readAllButton  = null;
    JButton writeAllButton = null;
    JButton saveButton     = null;
    JButton openButton     = null;

    LocoIOData          data;
    LocoIOTableModel	model;
    JTable		table;
    JScrollPane 	scroll;

    public void dispose() {
        // dispose of the model
        model.dispose();
        // take apart the JFrame
        super.dispose();
        model          = null;
        table          = null;
        scroll         = null;
        readAllButton  = null;
        writeAllButton = null;
        addrField      = null;
        subAddrField   = null;
        status         = null;
        firmware       = null;
        locobuffer     = null;
        saveButton     = null;
        openButton     = null;
    }

    static Logger log = Logger.getLogger(LocoIOPanel.class.getName());

}
