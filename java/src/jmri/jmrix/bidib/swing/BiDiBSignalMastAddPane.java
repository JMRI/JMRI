package jmri.jmrix.bidib.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.bidib.BiDiBSignalMast;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Nonnull;
import jmri.SystemConnectionMemo;
import jmri.jmrix.bidib.BiDiBAddress;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring BiDiBSignalMast objects
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 * @author Eckart Meyer Copyright (C) 2020
 * 
 * derived from DCCSignalMastAddPane.
 */
public class BiDiBSignalMastAddPane extends SignalMastAddPane {

    JScrollPane bidibMastScroll;
    JPanel bidibMastPanel = new JPanel();
    JLabel systemPrefixBoxLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BiDiBConnectionLabel")));
    JComboBox<String> systemPrefixBox = new JComboBox<>();
    JLabel bidibAccesoryAddressLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BiDiBMastAddress")));
    JTextField bidibAccesoryAddressField = new JTextField(20);

    JCheckBox allowUnLit = new JCheckBox();
    JTextField unLitAspectField = new JTextField(5);

    LinkedHashMap<String, BiDiBAspectPanel> bidibAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);

    BiDiBSignalMast currentMast = null;
    SignalSystem sigsys;
//    /* IMM Send Count */
//    JSpinner packetSendCountSpinner = new JSpinner();

    public BiDiBSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
//        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
//        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        bidibMastScroll = new JScrollPane(bidibMastPanel);
        bidibMastScroll.setBorder(BorderFactory.createEmptyBorder());
        add(bidibMastScroll);
    }


    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("BiDiBSignalMastPane");
    }
    
    /**
     * //Check all BiDiB connections if they have native (not DCC) Accessory functions.
     * Check all BiDiB connections if they are enabled.
     * Add those to the systemPrefixBox
     */
    protected void addUsableConnections() {
        systemPrefixBox.removeAllItems();
        List<jmri.SystemConnectionMemo> connList = jmri.InstanceManager.getList(jmri.SystemConnectionMemo.class);
        if (!connList.isEmpty()) {
            for (int x = 0; x < connList.size(); x++) {
                SystemConnectionMemo memo = connList.get(x);
                if (memo instanceof jmri.jmrix.bidib.BiDiBSystemConnectionMemo) {
                    //BiDiBTrafficController tc = ((BiDiBSystemConnectionMemo) memo).getBiDiBTrafficController();
                    //if (tc.hasAccessoryNode()) {
                    if (!memo.getDisabled()) {
                        systemPrefixBox.addItem(memo.getUserName());
                    }
                }
            }
        } else {
            systemPrefixBox.addItem("None");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, 
                               @Nonnull SignalSystem sigSystem) {
        log.trace("setAspectNames(...) start");

        bidibAspect.clear();
        
        Enumeration<String> aspects = map.getAspects();
        sigsys = map.getSignalSystem();

        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            BiDiBAspectPanel aPanel = new BiDiBAspectPanel(aspect);
            bidibAspect.put(aspect, aPanel);
            log.trace(" in loop, bidibAspect: {} ", map.getProperty(aspect, "dccAspect")); 
            aPanel.setAspectId((String) sigSystem.getProperty(aspect, "dccAspect"));
        }

        addUsableConnections();

        bidibMastPanel.removeAll();

        bidibMastPanel.add(systemPrefixBoxLabel);
        bidibMastPanel.add(systemPrefixBox);

        bidibMastPanel.add(bidibAccesoryAddressLabel);
        bidibAccesoryAddressField.setText("");
        bidibMastPanel.add(bidibAccesoryAddressField);

        for (Map.Entry<String, BiDiBAspectPanel> entry : bidibAspect.entrySet()) {
            log.trace("   aspect: {}", entry.getKey());
            bidibMastPanel.add(entry.getValue().getPanel());
        }

        bidibMastPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BiDiBMastCopyAspectId"))));
        bidibMastPanel.add(copyFromMastSelection());
        
        bidibMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 2)); // 0 means enough
        bidibMastPanel.revalidate();
        bidibMastScroll.revalidate();

        log.trace("setAspectNames(...) end");
    }



    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        // because that mast can be subtyped by something 
        // completely different, we text for exact here.
        return mast.getClass().getCanonicalName().equals(BiDiBSignalMast.class.getCanonicalName());
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.debug("setMast({}) start", mast);
        if (mast == null) { 
            currentMast = null; 
            log.debug("setMast() end early with null");
            return; 
        }
        
        if (! (mast instanceof BiDiBSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            log.debug("setMast({}) end early: wrong type", mast);
            return;
        }

        currentMast = (BiDiBSignalMast) mast;
        SignalAppearanceMap appMap = mast.getAppearanceMap();

        if (appMap != null) {
            Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                BiDiBAspectPanel aspectPanel = bidibAspect.get(key);
                aspectPanel.setAspectDisabled(currentMast.isAspectDisabled(key));
                if (!currentMast.isAspectDisabled(key)) {
                    aspectPanel.setAspectId(currentMast.getOutputForAppearance(key));
                }
            }
        }
        addUsableConnections();
        bidibAccesoryAddressField.setText(currentMast.getAccessoryAddress());
        systemPrefixBox.setSelectedItem(currentMast.getTrafficController().getUserName());

        systemPrefixBoxLabel.setEnabled(false);
        systemPrefixBox.setEnabled(false);
        bidibAccesoryAddressLabel.setEnabled(false);
        bidibAccesoryAddressField.setEnabled(false);
//        if (currentMast.allowUnLit()) {
//            unLitAspectField.setText("" + currentMast.getUnlitId());
//        }
        log.debug("setMast({}) end", mast);
    }

    /**
     * Check if the given aspect string is a valid BiDiB aspect.
     * Only numeric values between 0 and 31 are allowed
     * 
     * @param strAspect name of aspect
     * @return true if valid
     */
    static boolean validateAspectId(@Nonnull String strAspect) {
        int aspect;
        try {
            aspect = Integer.parseInt(strAspect.trim());
        } catch (java.lang.NumberFormatException e) {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAspectNumber"));
            return false;
        }
        if (aspect < 0 || aspect > 31) {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAspectOutOfRange"));
            log.error("invalid aspect {}", aspect);
            return false;
        }
        return true;
    }
    
    /**
     * Get the first part of the system name
     * for the specific mast type.
     * 
     * @return name prefix
     */
    protected @Nonnull String getNamePrefix() {
        return BiDiBSignalMast.getNamePrefix() + ":";
        //return "F$bsm:";
    }

    /** 
     * Create a mast of the specific subtype.
     * @param name system name to create
     * @return the new signal
     */
    protected BiDiBSignalMast constructMast(@Nonnull String name) {
        return new BiDiBSignalMast(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull
            String sigsysname, @Nonnull
                    String mastname, @Nonnull
                            String username) {
        log.debug("createMast({},{},{} start)", sigsysname, mastname, username);
        
        // are we already editing?  If no, create a new one.
        if (currentMast == null) {
            log.trace("Creating new mast");
            if (!validateBiDiBAddress()) {
                log.trace("validateBiDiBAddress failed, return from createMast");
                return false;
            }
            String systemNameText = ConnectionNameFromSystemName.getPrefixFromName((String) systemPrefixBox.getSelectedItem());
            if (systemNameText == null || systemNameText.isEmpty()) {
                systemNameText = "B";
            }
            systemNameText = systemNameText + getNamePrefix();

            String name = systemNameText
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "(" + bidibAccesoryAddressField.getText() + ")";
            currentMast = constructMast(name);
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }

        for (Map.Entry<String, BiDiBAspectPanel> entry : bidibAspect.entrySet()) {
            bidibMastPanel.add(entry.getValue().getPanel()); // update mast from aspect subpanel panel
            currentMast.setOutputForAppearance(entry.getKey(), entry.getValue().getAspectId());
            if (entry.getValue().isAspectDisabled()) {
                currentMast.setAspectDisabled(entry.getKey());
            } else {
                currentMast.setAspectEnabled(entry.getKey());
            }
        }
        if (!username.equals("")) {
            currentMast.setUserName(username);
        }

//        currentMast.setAllowUnLit(allowUnLit.isSelected());
//        if (allowUnLit.isSelected()) {
//            currentMast.setUnlitId(Integer.parseInt(unLitAspectField.getText()));
//        }

        log.debug("createMast({},{} end)", sigsysname, mastname);
        return true;
   }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("BiDiBSignalMastPane");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new BiDiBSignalMastAddPane();
        }
    }

    /**
     * Check if the contents of the accessory address field is syntactically correct and not already used.
     * Do not check if the accessory is currently available on the hardware.
     * 
     * @return true if valid
     */
    private boolean validateBiDiBAddress() {
        if (bidibAccesoryAddressField.getText().equals("")) {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAddressBlank"));
            return false;
        }
        char accessoryTypeLetter = 'T';
        BiDiBSystemConnectionMemo memo = (BiDiBSystemConnectionMemo)ConnectionNameFromSystemName.getSystemConnectionMemoFromUserName((String) systemPrefixBox.getSelectedItem());
        if (memo == null) {
            return false;
        }
        String accessorySystemName = memo.getSystemPrefix() + accessoryTypeLetter + bidibAccesoryAddressField.getText().trim();
        log.trace("validate Accessory Systemname: {}", accessorySystemName);
        // first, check validity
        if (!BiDiBAddress.isValidSystemNameFormat(accessorySystemName, accessoryTypeLetter, memo)) {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAddressInvalid"));
            return false;
        }
        BiDiBAddress addr = new BiDiBAddress(accessorySystemName, accessoryTypeLetter, memo);

// checks disabled
//        if (!addr.isValid()) {
//            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAddressInvalid"));
//            return false;
//        }
//        if (addr.isValid()  &&  !addr.isAccessoryAddr()  &&  !addr.isTrackAddr()) {
//            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BiDiBMastAddressWrongType"));
//            return false;
//        }

        // check if accessory address is already used
        if (BiDiBSignalMast.isAccessoryAddressUsed(addr) != null) {
            String msg = Bundle.getMessage("BiDiBMastAddressAssigned", new Object[]{bidibAccesoryAddressField.getText(), BiDiBSignalMast.isAccessoryAddressUsed(addr)});
            JmriJOptionPane.showMessageDialog(null, msg);
            return false;
        }

        return true;
    }

    @Nonnull JComboBox<String> copyFromMastSelection() {
        JComboBox<String> mastSelect = new JComboBox<>();
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof BiDiBSignalMast){
                mastSelect.addItem(mast.getDisplayName());
            }
        }
        if (mastSelect.getItemCount() == 0) {
            mastSelect.setEnabled(false);
        } else {
            mastSelect.insertItemAt("", 0);
            mastSelect.setSelectedIndex(0);
            mastSelect.addActionListener(new ActionListener() {
                @SuppressWarnings("unchecked") // e.getSource() cast from mastSelect source
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> eb = (JComboBox<String>) e.getSource();
                    String sourceMast = (String) eb.getSelectedItem();
                    if (sourceMast != null && !sourceMast.equals("")) {
                        copyFromAnotherBiDiBMastAspect(sourceMast);
                    }
                }
            });
        }
        return mastSelect;
    }

    /**
     * Copy aspects by name from another DccSignalMast.
     * @param strMast name
     */
    void copyFromAnotherBiDiBMastAspect(@Nonnull String strMast) {
        BiDiBSignalMast mast = (BiDiBSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(strMast);
        if (mast == null) {
            log.error("can't copy from another mast because {} doesn't exist", strMast);
            return;
        }
        Vector<String> validAspects = mast.getValidAspects();
        for (Map.Entry<String, BiDiBAspectPanel> entry : bidibAspect.entrySet()) {
            if (validAspects.contains(entry.getKey()) || mast.isAspectDisabled(entry.getKey())) { // valid doesn't include disabled
                // present, copy
                entry.getValue().setAspectId(mast.getOutputForAppearance(entry.getKey()));
                entry.getValue().setAspectDisabled(mast.isAspectDisabled(entry.getKey()));
            } else {
                // not present, log
                log.info("Can't get aspect \"{}\" from head \"{}\", leaving unchanged", entry.getKey(), mast);
            }
        }
    }

    /**
     * JPanel to define properties of an Aspect for a DCC Signal Mast.
     * <p>
     * Invoked from the AddSignalMastPanel class when a DCC Signal Mast is
     * selected.
     */
    static class BiDiBAspectPanel {

        String aspect = "";
        JCheckBox disabledCheck = new JCheckBox(Bundle.getMessage("DisableAspect"));
        JLabel aspectLabel = new JLabel(Bundle.getMessage("BiDiBMastSetAspectId") + ":");
        JTextField aspectId = new JTextField(5);

        BiDiBAspectPanel(String aspect) {
            this.aspect = aspect;
        }

        void setAspectDisabled(boolean boo) {
            disabledCheck.setSelected(boo);
            if (boo) {
                aspectLabel.setEnabled(false);
                aspectId.setEnabled(false);
            } else {
                aspectLabel.setEnabled(true);
                aspectId.setEnabled(true);
            }
        }

        boolean isAspectDisabled() {
            return disabledCheck.isSelected();
        }

        int getAspectId() {
            try {
                String value = aspectId.getText();
                return Integer.parseInt(value);

            } catch (Exception ex) {
                log.error("failed to convert aspect number");
            }
            return -1;
        }

        void setAspectId(int i) {
            aspectId.setText("" + i);
        }

        void setAspectId(String s) {
            aspectId.setText(s);
        }

        JPanel panel;

        JPanel getPanel() {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                JPanel dccDetails = new JPanel();
                dccDetails.add(aspectLabel);
                dccDetails.add(aspectId);
                panel.add(dccDetails);
                panel.add(disabledCheck);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);
                aspectId.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (aspectId.getText().equals("")) {
                            return;
                        }
                        if (!validateAspectId(aspectId.getText())) {
                            aspectId.requestFocusInWindow();
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }

                });
                disabledCheck.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setAspectDisabled(disabledCheck.isSelected());
                    }
                });

            }
            return panel;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BiDiBSignalMastAddPane.class);

}
