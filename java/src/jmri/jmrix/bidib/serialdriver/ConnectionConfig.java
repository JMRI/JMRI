package jmri.jmrix.bidib.serialdriver;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.bidib.jbidibc.messages.utils.ByteUtils;
//import org.bidib.jbidibc.scm.ScmPortIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring a layout connection via a BiDiB
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Eckart Meyer Copyright (C) 2019
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = Bundle.getMessage("TypeSerial");
//    protected JCheckBox useAutoScan = new JCheckBox(Bundle.getMessage("Use Autoscan"));
    // TODO: use Bundle for localization of the field text
    protected JCheckBox useAutoScan = new JCheckBox("Use Autoscan");
    protected JLabel rootNodeLabel = new JLabel("Root Node (hex):");
    protected JTextField rootNodeField = new JTextField(16);
    protected JLabel portNameFilterLabel = new JLabel("Port Name Filter:");
    protected JTextField portNameFilterField = new JTextField(15);

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p SerialPortAdapter for existing adapter
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        log.debug("BiDiB Simulator ConnectionConfig.setInstance: {}", adapter);
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
            log.debug("-- adapter created: {}", adapter);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
//    @SuppressWarnings("UseOfObsoleteCollectionType")
    public Vector<String> getPortNames() {
        // used for SCM - will prepend "/dev" fpr Linux
        Vector<String> portNameVector = new Vector<>();
        try {
            List<String> portNameList = ((SerialDriverAdapter)adapter).getPortIdentifiers();
            for (String portName : portNameList) {
                portNameVector.addElement(portName);
            }
        }
        catch (Exception ex) {
            log.error("Serial adapter not set: ", ex); // NOSONAR
        }
//        List<String> portNameList = SerialDriverAdapter.getPortIdentifiers();
        log.trace("getPortNames done {}", portNameVector);
        return portNameVector;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        super.checkInitDone();
        log.debug("checkInitDone");
        if (adapter.getSystemConnectionMemo() != null) {
            SerialDriverAdapter a = (SerialDriverAdapter)adapter;
            rootNodeField.setText(ByteUtils.formatHexUniqueId(a.getRootNodeUid()));
            portNameFilterField.setText(a.getPortNameFilter());
            useAutoScan.setSelected(a.getUseAutoScan());
            if (a.getUseAutoScan()) {
                rootNodeField.setEnabled(true);
                portBox.setEnabled(false);
            }
            else {
                rootNodeField.setEnabled(false);
                portBox.setEnabled(true);
            }
            // add listeners
            // portBox combobox
            portBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    log.debug("portBox action!");
                    portBoxChanged(e);
                }
            });
            // rootNode field
            rootNodeField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    log.debug("rootNodeUID action!");
                    rootNodeUidChanged(e);
                }
            });
            rootNodeField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    log.debug("rootNodeUID focus lost!");
                    rootNodeUidChanged(e);
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            // portNameFilter field
            portNameFilterField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    log.debug("portNameFilter action!");
                    portNameFilterChanged(e);
                }
            });
            portNameFilterField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    log.debug("portNameFilter focus lost!");
                    portNameFilterChanged(e);
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            // useAutoScan checkbox
            useAutoScan.addItemListener((ItemEvent e) -> {
                log.debug("useAutoScan changed!");
                a.setUseAutoScan(useAutoScan.isSelected());
                rootNodeField.setEnabled(useAutoScan.isSelected());
                portBox.setEnabled(!useAutoScan.isSelected());
            });
            
            baudBox.setEnabled(false);// Bidib SCM always tries to find the the baudrate itself, the user cannot select
        }
    }

    @SuppressWarnings (value="unchecked") //because of the unchecked cast of the event object. but we can be sure here...
    private void portBoxChanged(AWTEvent e) {
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        String fieldtext = ((JComboBox<String>)e.getSource()).getSelectedItem().toString();
        log.debug("portBox selected: {}", fieldtext);
        if (!a.getUseAutoScan()) {
            //Long uid = SerialDriverAdapter.checkPort(fieldtext); //call static function
            Long uid = a.checkPort(fieldtext); //call static function
            if (uid == null) {
                a.setRootNodeUid(null);
                rootNodeField.setText("");
            }
            else {
                a.setRootNodeUid(uid);
                //rootNodeField.setText(String.format("0x%X", a.getRootNodeUid() & 0xffffffffffffffL));
                rootNodeField.setText(ByteUtils.formatHexUniqueId(a.getRootNodeUid()));
            }
        }
    }
    
    private void rootNodeUidChanged(AWTEvent e) {
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        String fieldtext = ((JTextField)e.getSource()).getText();
        if (fieldtext.isEmpty()) {
            //a.setRootNodeUid(null); //don't set
        }
        else {
            try {
                if (a.getUseAutoScan()) {
                    Long uid = ByteUtils.parseHexUniqueId(fieldtext);
                    String err = a.findPortbyUniqueID(uid);
                    if (err == null) {
                        log.info("found port name for UID {} is {}",
                                    ByteUtils.formatHexUniqueId(a.getRootNodeUid()), a.getCurrentPortName());
                        a.setRootNodeUid(uid);
                        //rootNodeField.setText(String.format("0x%X", a.getRootNodeUid() & 0x0000ffffffffffL));
                        rootNodeField.setText(ByteUtils.formatHexUniqueId(a.getRootNodeUid()));
                        // TODO set portBox
                        rootNodeField.setForeground(Color.black);
                        portBox.setSelectedItem(a.getCurrentPortName());
                    }
                    else {
                        log.warn(err);
                        rootNodeField.setForeground(Color.red);
                    }
                }
            }
            catch (NumberFormatException ex) {
                a.setRootNodeUid(null);
                rootNodeField.setText("");
                log.warn("Exception:", ex);
            }
        }
    }
    
    private void portNameFilterChanged(AWTEvent e) {
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        // String s = a.getCurrentPortName();
        String fieldtext = ((JTextField)e.getSource()).getText();
        a.setPortNameFilter(fieldtext);
        refreshPortBox();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems(); // we're adding to the normal advanced items.
        log.debug("showAdvancedItems");
        if (adapter.getSystemConnectionMemo() != null) {
            cR.gridy += 2;
            cL.gridy += 2;
            gbLayout.setConstraints(rootNodeLabel, cL);
            gbLayout.setConstraints(rootNodeField, cR);
            _details.add(rootNodeLabel);
            //rootNodeField.setEnabled(false);
            _details.add(rootNodeField);
            
            cR.gridy += 2;
            cL.gridy += 2;
            gbLayout.setConstraints(useAutoScan, cL);
            useAutoScan.setFont(useAutoScan.getFont().deriveFont(9f));
            useAutoScan.setForeground(Color.blue);
            _details.add(useAutoScan);

            cR.gridy += 2;
            cL.gridy += 2;
            gbLayout.setConstraints(portNameFilterLabel, cL);
            gbLayout.setConstraints(portNameFilterField, cR);
            _details.add(portNameFilterLabel);
            _details.add(portNameFilterField);
        }
        if (_details.getParent() != null) {
            _details.getParent().revalidate();
            _details.getParent().repaint();
        }
    }
    
    @Override
    public void updateAdapter() {
        super.updateAdapter(); // we're adding more details to the connection.
        log.debug("updateAdapter");
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        if (adapter.getSystemConnectionMemo() != null) {
            a.setUseAutoScan(useAutoScan.isSelected());
            a.setRootNodeUid(ByteUtils.parseHexUniqueId(rootNodeField.getText()));
            a.setPortNameFilter(portNameFilterField.getText());
        }
    }


    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);
}
