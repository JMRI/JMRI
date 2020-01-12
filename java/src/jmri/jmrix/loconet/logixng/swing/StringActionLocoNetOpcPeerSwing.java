package jmri.jmrix.loconet.logixng.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.text.NumberFormatter;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.string.actions.swing.AbstractActionSwing;
import jmri.jmrix.loconet.logixng.StringActionLocoNetOpcPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class StringActionLocoNetOpcPeerSwing extends AbstractActionSwing {

    private NumberFormatter formatter7bits;
    private NumberFormatter formatter16bits;
    private JFormattedTextField _manufacturerID;
    private JFormattedTextField _developerID;
    private JFormattedTextField _serialNumber;
    private JFormattedTextField _sourceAddress;
    private JFormattedTextField _destAddress;
    private JFormattedTextField _svAddress;
    private final JCheckBox _charsetIncludeAll = new JCheckBox();
    private final JComboBox<Charset> _charset = new JComboBox<>();
    
    private final JLabel _manufacturerIDLabel = new JLabel(Bundle.getMessage("ManufacturerID") + ":");  // NOI18N
    private final JLabel _developerIDLabel = new JLabel(Bundle.getMessage("DeveloperID") + ":");  // NOI18N
    private final JLabel _serialNumberLabel = new JLabel(Bundle.getMessage("SerialNumber") + ":");  // NOI18N
    private final JLabel _sourceAddressLabel = new JLabel(Bundle.getMessage("SourceAddress") + ":");  // NOI18N
    private final JLabel _destAddressLabel = new JLabel(Bundle.getMessage("DestAddress") + ":");  // NOI18N
    private final JLabel _svAddressLabel = new JLabel(Bundle.getMessage("SV_Address") + ":");  // NOI18N
    private final JLabel _charsetIncludeAllLabel = new JLabel(Bundle.getMessage("CharsetIncludeAll") + ":");  // NOI18N
    private final JLabel _charsetLabel = new JLabel(Bundle.getMessage("Charset") + ":");  // NOI18N
    
    
    private void updateCharsetCombobox() {
        _charset.removeAllItems();
        
        if (_charsetIncludeAll.isSelected()) {
            Charset.availableCharsets().values().forEach((charset) -> {
                _charset.addItem(charset);
            });
        } else {
            _charset.addItem(StandardCharsets.ISO_8859_1);
            _charset.addItem(StandardCharsets.US_ASCII);
            _charset.addItem(StandardCharsets.UTF_8);
        }
    }
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        NumberFormat format = NumberFormat.getInstance();
        
        formatter7bits = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text.isEmpty()) return null;
                else return super.stringToValue(text);
            }
        };
        formatter7bits.setValueClass(Integer.class);
        formatter7bits.setMinimum(0);
        formatter7bits.setMaximum(0x7F);
        formatter7bits.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter7bits.setCommitsOnValidEdit(true);
        
        formatter16bits = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text.isEmpty()) return null;
                else return super.stringToValue(text);
            }
        };
        formatter16bits.setValueClass(Integer.class);
        formatter16bits.setMinimum(0);
        formatter16bits.setMaximum(0xFFFF);
        formatter16bits.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter16bits.setCommitsOnValidEdit(true);
        
        _manufacturerID = new JFormattedTextField(formatter7bits);
        _developerID = new JFormattedTextField(formatter7bits);
        _serialNumber = new JFormattedTextField(formatter16bits);
        _sourceAddress = new JFormattedTextField(formatter7bits);
        _destAddress = new JFormattedTextField(formatter16bits);
        _svAddress = new JFormattedTextField(formatter16bits);
        
        if (action != null) {
            try {
                _manufacturerID.setText(formatter16bits.valueToString(action.getManufacturerID()));
                _developerID.setText(formatter16bits.valueToString(action.getDeveloperID()));
                _serialNumber.setText(formatter16bits.valueToString(action.getSerialNumber()));
                _sourceAddress.setText(formatter16bits.valueToString(action.getSourceAddress()));
                _destAddress.setText(formatter16bits.valueToString(action.getDestAddress()));
                _svAddress.setText(formatter16bits.valueToString(action.get_SV_Address()));
            } catch (ParseException e) {
                // if we are here, we have a runtime error.
                throw new RuntimeException(e);
            }
            _charsetIncludeAll.setSelected(action.getShowAllCharsets());
        }
        
        updateCharsetCombobox();
        
        if (action != null) {
            _charset.setSelectedItem(action.getCharset());
        }
        
        _charsetIncludeAll.addChangeListener((ChangeEvent e) -> {
            Charset currentCharset = (Charset)_charset.getSelectedItem();
            updateCharsetCombobox();
            _charset.setSelectedItem(currentCharset);
        });
        
        JPanel p;
        p = new JPanel();
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(_manufacturerIDLabel, c);
        c.gridy = 1;
        p.add(_developerIDLabel, c);
        c.gridy = 2;
        p.add(_serialNumberLabel, c);
        c.gridy = 3;
        p.add(_sourceAddressLabel, c);
        c.gridy = 4;
        p.add(_destAddressLabel, c);
        c.gridy = 5;
        p.add(_svAddressLabel, c);
        c.gridy = 6;
        p.add(_charsetIncludeAllLabel, c);
        c.gridy = 7;
        p.add(_charsetLabel, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_manufacturerID, c);
        c.gridy = 1;
        p.add(_developerID, c);
        c.gridy = 2;
        p.add(_serialNumber, c);
        c.gridy = 3;
        p.add(_sourceAddress, c);
        c.gridy = 4;
        p.add(_destAddress, c);
        c.gridy = 5;
        p.add(_svAddress, c);
        c.gridy = 6;
        p.add(_charsetIncludeAll, c);
        c.gridy = 7;
        p.add(_charset, c);
        
        _manufacturerID.setToolTipText(Bundle.getMessage("ManufacturerIDHint"));    // NOI18N
        _developerID.setToolTipText(Bundle.getMessage("DeveloperIDHint"));    // NOI18N
        _serialNumber.setToolTipText(Bundle.getMessage("SerialNumberHint"));    // NOI18N
        _sourceAddress.setToolTipText(Bundle.getMessage("SourceAddressHint"));    // NOI18N
        _destAddress.setToolTipText(Bundle.getMessage("DestAddressHint"));    // NOI18N
        _svAddress.setToolTipText(Bundle.getMessage("SV_AddressHint"));    // NOI18N
        _charset.setToolTipText(Bundle.getMessage("CharsetHint"));    // NOI18N
        panel.add(p);
        
        
        // Add button
        // Cancel
        JButton discover = new JButton(Bundle.getMessage("ButtonDiscover"));    // NOI18N
        buttonPanel.add(discover);
        discover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show discover dialog
            }
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        discover.setToolTipText("DiscoverButtonHint");      // NOI18N
        
/*        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel panel_AA = new JPanel();
        panel_AA.add(new JLabel("InitExMessageListHeader"));
//        panel_AA.add(new JLabel(Bundle.getMessage("InitExMessageListHeader")));
        panel.add(panel_AA);

        JPanel marginPanel = new JPanel();
        marginPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        marginPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(marginPanel);
        JPanel borderPanel = new JPanel();
        borderPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        borderPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
        marginPanel.add(borderPanel);
        panel_AA = new JPanel();
        panel_AA.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel_AA.setLayout(new BoxLayout(panel_AA, BoxLayout.Y_AXIS));
        panel_AA.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
//        for (String s : list) {
            // Remove html
//            s = s.replaceAll("\\<html\\>.*\\<\\/html\\>", "");
            String s = "Hej";
            JLabel label = new JLabel(s);
            label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel_AA.add(label);
//        }
        borderPanel.add(panel_AA);

        panel_AA = new JPanel();
        JButton button = new JButton("ErrorDialogButtonExitProgram");
//        JButton button = new JButton(Bundle.getMessage("ErrorDialogButtonExitProgram"));
        button.addActionListener((ActionEvent a) -> {
//            result = Result.EXIT_PROGRAM;
            dispose();
        });
        panel_AA.add(button);

        button = new JButton("ErrorDialogButtonRestartProgram");
//        button = new JButton(Bundle.getMessage("ErrorDialogButtonRestartProgram"));
        button.addActionListener((ActionEvent a) -> {
//            result = Result.RESTART_PROGRAM;
            dispose();
        });
        panel_AA.add(button);

        button = new JButton("ErrorDialogButtonNewProfile");
//        button = new JButton(Bundle.getMessage("ErrorDialogButtonNewProfile"));
        button.addActionListener((ActionEvent a) -> {
//            result = Result.NEW_PROFILE;
            dispose();
        });
        panel_AA.add(button);

        button = new JButton("ErrorDialogButtonEditConnections");
//        button = new JButton(Bundle.getMessage("ErrorDialogButtonEditConnections"));
        button.addActionListener((ActionEvent a) -> {
//            result = Result.EDIT_CONNECTIONS;
            dispose();
        });
        panel_AA.add(button);
        
        panel.add(panel_AA);
*/        
    }
/*    
    private boolean validate(String value, String errorMessage, @Nonnull List<String> errorMessages) {
        try {
            Integer.parseUnsignedInt(value);
        } catch (NumberFormatException e) {
            errorMessages.add(Bundle.getMessage(errorMessage));
            return false;
        }
        return true;
    }
*/    
    private boolean validate(AbstractFormatter formatter, String value, String errorMessage, @Nonnull List<String> errorMessages) {
        try {
            formatter.stringToValue(value);
        } catch (ParseException e) {
            errorMessages.add(Bundle.getMessage(errorMessage));
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        boolean result = true;
        result &= validate(formatter7bits, _manufacturerID.getText(), "ErrorManufacturerID_BadValue", errorMessages); // NOI18N
        result &= validate(formatter7bits, _developerID.getText(), "ErrorDeveloperID_BadValue", errorMessages); // NOI18N
        result &= validate(formatter16bits, _serialNumber.getText(), "ErrorSerialNumber_BadValue", errorMessages); // NOI18N
        result &= validate(formatter7bits, _sourceAddress.getText(), "ErrorSourceAddressBadValue", errorMessages); // NOI18N
        result &= validate(formatter16bits, _destAddress.getText(), "ErrorDestAddressBadValue", errorMessages); // NOI18N
        result &= validate(formatter16bits, _svAddress.getText(), "ErrorSV_Address_BadValue", errorMessages); // NOI18N
        return result;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringActionLocoNetOpcPeer action = new StringActionLocoNetOpcPeer(systemName, userName);
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)object;
        
        try {
            action.setManufacturerID((int) formatter7bits.stringToValue(_manufacturerID.getText()));
            action.setDeveloperID((int) formatter7bits.stringToValue(_developerID.getText()));
            action.setSerialNumber((int) formatter16bits.stringToValue(_serialNumber.getText()));
            action.setSourceAddress((int) formatter7bits.stringToValue(_sourceAddress.getText()));
            action.setDestAddress((int) formatter16bits.stringToValue(_destAddress.getText()));
            action.set_SV_Address((int) formatter16bits.stringToValue(_svAddress.getText()));
/*            
            action.setManufacturerID(Integer.parseUnsignedInt(_manufacturerID.getText()));
            action.setDeveloperID(Integer.parseUnsignedInt(_developerID.getText()));
            action.setSourceAddress(Integer.parseUnsignedInt(_sourceAddress.getText()));
            action.setDestAddress(Integer.parseUnsignedInt(_destAddress.getText()));
            action.set_SV_Address(Integer.parseUnsignedInt(_svAddress.getText()));
*/            
//        } catch (NumberFormatException e) {
        } catch (ParseException e) {
            // If we are here, the panel is probably not validated and we
            // have a runtime error.
            // Or, the panel is validated, but there is an error in the
            // validation check, so we have a runtime error.
            throw new RuntimeException(e);
        }
        action.setShowAllCharsets(_charsetIncludeAll.isSelected());
        action.setCharset((Charset) _charset.getSelectedItem());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringActionLocoNet_OPC_PEER_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNetOpcPeerSwing.class);
    
}
