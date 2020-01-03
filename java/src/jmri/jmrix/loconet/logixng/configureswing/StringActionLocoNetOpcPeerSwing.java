package jmri.jmrix.loconet.logixng.configureswing;

import java.awt.event.ActionEvent;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.text.NumberFormatter;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.string.actions.configureswing.AbstractActionSwing;
import jmri.jmrix.loconet.logixng.StringActionLocoNetOpcPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class StringActionLocoNetOpcPeerSwing extends AbstractActionSwing {

    private JFormattedTextField _manufacturerID;
    private JFormattedTextField _developerID;
    private JFormattedTextField _sourceAddress;
    private JFormattedTextField _destAddress;
    private JFormattedTextField _svAddress;
    private final JCheckBox _charsetIncludeAll = new JCheckBox();
    private final JComboBox<Charset> _charset = new JComboBox<>();
    
    private final JLabel _manufacturerIDLabel = new JLabel(Bundle.getMessage("ManufacturerID") + ":");  // NOI18N
    private final JLabel _developerIDLabel = new JLabel(Bundle.getMessage("DeveloperID") + ":");  // NOI18N
    private final JLabel _sourceAddressLabel = new JLabel(Bundle.getMessage("SourceAddress") + ":");  // NOI18N
    private final JLabel _destAddressLabel = new JLabel(Bundle.getMessage("DestAddress") + ":");  // NOI18N
    private final JLabel _svAddressLabel = new JLabel(Bundle.getMessage("SV_Address") + ":");  // NOI18N
    private final JLabel _charsetIncludeAllLabel = new JLabel(Bundle.getMessage("CharsetIncludeAll") + ":");  // NOI18N
    private final JLabel _charsetLabel = new JLabel(Bundle.getMessage("Charset") + ":");  // NOI18N
    
    
    private void updateCharsetCombobox() {
        _charset.removeAll();
        
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
    protected void createPanel(Base object) {
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text.isEmpty()) return null;
                else return super.stringToValue(text);
            }
        };
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        
        _manufacturerID = new JFormattedTextField(formatter);
        _developerID = new JFormattedTextField(formatter);
        _sourceAddress = new JFormattedTextField(formatter);
        _destAddress = new JFormattedTextField(formatter);
        _svAddress = new JFormattedTextField(formatter);
        
        if (action != null) {
            _manufacturerID.setText(Integer.toString(action.getManufacturerID()));
            _developerID.setText(Integer.toString(action.getDeveloperID()));
            _sourceAddress.setText(Integer.toString(action.getSourceAddress()));
            _destAddress.setText(Integer.toString(action.getDestAddress()));
            _svAddress.setText(Integer.toString(action.get_SV_Address()));
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
        p.add(_sourceAddressLabel, c);
        c.gridy = 3;
        p.add(_destAddressLabel, c);
        c.gridy = 4;
        p.add(_svAddressLabel, c);
        c.gridy = 5;
        p.add(_charsetIncludeAllLabel, c);
        c.gridy = 6;
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
        p.add(_sourceAddress, c);
        c.gridy = 3;
        p.add(_destAddress, c);
        c.gridy = 4;
        p.add(_svAddress, c);
        c.gridy = 5;
        p.add(_charsetIncludeAll, c);
        c.gridy = 6;
        p.add(_charset, c);
        
        _manufacturerID.setToolTipText(Bundle.getMessage("ManufacturerIDHint"));    // NOI18N
        _developerID.setToolTipText(Bundle.getMessage("DeveloperIDHint"));    // NOI18N
        _sourceAddress.setToolTipText(Bundle.getMessage("SourceAddressHint"));    // NOI18N
        _destAddress.setToolTipText(Bundle.getMessage("DestAddressHint"));    // NOI18N
        _svAddress.setToolTipText(Bundle.getMessage("SV_AddressHint"));    // NOI18N
        _charset.setToolTipText(Bundle.getMessage("CharsetHint"));    // NOI18N
//        _discoverButton.setToolTipText(Bundle.getMessage("DiscoverHint"));    // NOI18N
        panel.add(p);
        
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
    
    private boolean validate(String value, String errorMessage, @Nonnull List<String> errorMessages) {
        try {
            Integer.parseUnsignedInt(value);
        } catch (NumberFormatException e) {
            errorMessages.add(Bundle.getMessage(errorMessage));
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        boolean result = true;
        result &= validate(_manufacturerID.getText(), "ErrorManufacturerID_BadValue", errorMessages); // NOI18N
        result &= validate(_developerID.getText(), "ErrorDeveloperID_BadValue", errorMessages); // NOI18N
        result &= validate(_sourceAddress.getText(), "ErrorSourceAddressBadValue", errorMessages); // NOI18N
        result &= validate(_destAddress.getText(), "ErrorDestAddressBadValue", errorMessages); // NOI18N
        result &= validate(_svAddress.getText(), "ErrorSV_Address_BadValue", errorMessages); // NOI18N
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
            action.setManufacturerID(Integer.parseUnsignedInt(_manufacturerID.getText()));
            action.setManufacturerID(Integer.parseUnsignedInt(_developerID.getText()));
            action.setManufacturerID(Integer.parseUnsignedInt(_sourceAddress.getText()));
            action.setManufacturerID(Integer.parseUnsignedInt(_destAddress.getText()));
            action.setManufacturerID(Integer.parseUnsignedInt(_svAddress.getText()));
        } catch (NumberFormatException e) {
            throw e;
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
    
    
    private final static Logger log = LoggerFactory.getLogger(StringActionLocoNetOpcPeerSwing.class);
    
}
