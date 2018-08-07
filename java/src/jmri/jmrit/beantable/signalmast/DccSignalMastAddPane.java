package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.DccSignalMast;
import jmri.util.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring MatrixSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class DccSignalMastAddPane extends SignalMastAddPane {

    public DccSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        dccMastScroll = new JScrollPane(dccMastPanel);
        dccMastScroll.setBorder(BorderFactory.createEmptyBorder());
        add(dccMastScroll);

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("DCCMast");
    }

    JScrollPane dccMastScroll;
    JPanel dccMastPanel = new JPanel();
    JLabel systemPrefixBoxLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DCCSystem")));
    JComboBox<String> systemPrefixBox = new JComboBox<>();
    JLabel dccAspectAddressLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DCCMastAddress")));
    JTextField dccAspectAddressField = new JTextField(5);

    JCheckBox allowUnLit = new JCheckBox();
    JTextField unLitAspectField = new JTextField(5);

    LinkedHashMap<String, DCCAspectPanel> dccAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);

    DccSignalMast currentMast = null;
    SignalSystem sigsys;

    /**
     * Check if a command station will work for this subtype
     */
    protected boolean usableCommandStation(CommandStation cs) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, 
                               @Nonnull SignalSystem sigSystem) {
        log.trace("setAspectNames(...) start");

        dccAspect.clear();
        
        Enumeration<String> aspects = map.getAspects();
        sigsys = map.getSignalSystem();

        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            DCCAspectPanel aPanel = new DCCAspectPanel(aspect);
            dccAspect.put(aspect, aPanel);
            log.trace(" in loop, dccAspect: {} ", map.getProperty(aspect, "dccAspect")); 
            aPanel.setAspectId((String) sigSystem.getProperty(aspect, "dccAspect"));
        }

        systemPrefixBox.removeAllItems();
        List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
        if (!connList.isEmpty()) {
            for (int x = 0; x < connList.size(); x++) {
                jmri.CommandStation station = connList.get(x);
                if (usableCommandStation(station)) {
                    systemPrefixBox.addItem(station.getUserName());
                }
            }
        } else {
            systemPrefixBox.addItem("None");
        }

        dccMastPanel.removeAll();

        dccMastPanel.add(systemPrefixBoxLabel);
        dccMastPanel.add(systemPrefixBox);

        dccMastPanel.add(dccAspectAddressLabel);
        dccAspectAddressField.setText("");
        dccMastPanel.add(dccAspectAddressField);

        for (String aspect : dccAspect.keySet()) {
            log.trace("   aspect: {}", aspect);
            dccMastPanel.add(dccAspect.get(aspect).getPanel());
        }

        dccMastPanel.add(new JLabel(Bundle.getMessage("DCCMastCopyAspectId") + ":"));
        dccMastPanel.add(copyFromMastSelection());
        
        dccMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 2)); // 0 means enough
        dccMastPanel.revalidate();
        dccMastScroll.revalidate();

        log.trace("setAspectNames(...) end");
    }


    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        // because that mast can be subtyped by something 
        // completely different, we text for exact here.
        return mast.getClass().getCanonicalName().equals(DccSignalMast.class.getCanonicalName());
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.debug("setMast({}) start", mast);
        if (mast == null) { 
            currentMast = null; 
            log.debug("setMast({}) end early with null", mast);
            return; 
        }
        
        if (! (mast instanceof DccSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            log.debug("setMast({}) end early: wrong type", mast);
            return;
        }

        currentMast = (DccSignalMast) mast;
        SignalAppearanceMap appMap = mast.getAppearanceMap();

        if (appMap != null) {
            Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                DCCAspectPanel dccPanel = dccAspect.get(key);
                dccPanel.setAspectDisabled(currentMast.isAspectDisabled(key));
                if (!currentMast.isAspectDisabled(key)) {
                    dccPanel.setAspectId(currentMast.getOutputForAppearance(key));
                }

            }
        }
        List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
        if (!connList.isEmpty()) {
            for (int x = 0; x < connList.size(); x++) {
                jmri.CommandStation station = connList.get(x);
                if (usableCommandStation(station)) {
                    systemPrefixBox.addItem(station.getUserName());
                }
            }
        } else {
            systemPrefixBox.addItem("None");
        }
        dccAspectAddressField.setText("" + currentMast.getDccSignalMastAddress());
        systemPrefixBox.setSelectedItem(currentMast.getCommandStation().getUserName());

        systemPrefixBoxLabel.setEnabled(false);
        systemPrefixBox.setEnabled(false);
        dccAspectAddressLabel.setEnabled(false);
        dccAspectAddressField.setEnabled(false);
        if (currentMast.allowUnLit()) {
            unLitAspectField.setText("" + currentMast.getUnlitId());
        }

        log.debug("setMast({}) end", mast);
    }

    static boolean validateAspectId(String strAspect) {
        int aspect;
        try {
            aspect = Integer.parseInt(strAspect.trim());
        } catch (java.lang.NumberFormatException e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAspectNumber"));
            return false;
        }
        if (aspect < 0 || aspect > 31) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAspectOutOfRange"));
            log.error("invalid aspect {}", aspect);
            return false;
        }
        return true;
    }
    
    /**
     * Return the first part of the system name 
     * for the specific mast type
     */
    protected String getNamePrefix() {
        return "F$dsm:";
    }

    /** 
     * Create a mast of the specific subtype
     */
    protected DccSignalMast constructMast(String name) {
        return new DccSignalMast(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull
            String sigsysname, @Nonnull
                    String mastname, @Nonnull
                            String username) {
        log.debug("createMast({},{} start)", sigsysname, mastname);
        
        // are we already editing?  If no, create a new one.
        if (currentMast == null) {
            log.trace("Creating new mast");
            if (!validateDCCAddress()) {
                log.trace("validateDCCAddress failed, return from createMast");
                return false;
            }
            String systemNameText = ConnectionNameFromSystemName.getPrefixFromName((String) systemPrefixBox.getSelectedItem());
            // if we return a null string then we will set it to use internal, thus picking up the default command station at a later date.
            if (systemNameText.equals("\0")) {
                systemNameText = "I";
            }
            systemNameText = systemNameText + getNamePrefix();

            String name = systemNameText
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "(" + dccAspectAddressField.getText() + ")";
            currentMast = constructMast(name);
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }

        for (String aspect : dccAspect.keySet()) {
            dccMastPanel.add(dccAspect.get(aspect).getPanel()); // update mast from aspect subpanel panel
            currentMast.setOutputForAppearance(aspect, dccAspect.get(aspect).getAspectId());
            if (dccAspect.get(aspect).isAspectDisabled()) {
                currentMast.setAspectDisabled(aspect);
            } else {
                currentMast.setAspectEnabled(aspect);
            }
        }
        if (!username.equals("")) {
            currentMast.setUserName(username);
        }

        currentMast.setAllowUnLit(allowUnLit.isSelected());
        if (allowUnLit.isSelected()) {
            currentMast.setUnlitId(Integer.parseInt(unLitAspectField.getText()));
        }

        log.debug("createMast({},{} end)", sigsysname, mastname);
        return true;
   }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("DCCMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new DccSignalMastAddPane();
        }
    }

    private boolean validateDCCAddress() {
        if (dccAspectAddressField.getText().equals("")) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAddressBlank"));
            return false;
        }
        int address;
        try {
            address = Integer.parseInt(dccAspectAddressField.getText().trim());
        } catch (java.lang.NumberFormatException e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAddressNumber"));
            return false;
        }

        if (address < NmraPacket.accIdLowLimit || address > NmraPacket.accIdAltHighLimit) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAddressOutOfRange"));
            log.error("invalid address {}", address);
            return false;
        }
        if (DccSignalMast.isDCCAddressUsed(address) != null) {
            String msg = Bundle.getMessage("DCCMastAddressAssigned", new Object[]{dccAspectAddressField.getText(), DccSignalMast.isDCCAddressUsed(address)});
            JOptionPane.showMessageDialog(null, msg);
            return false;
        }
        return true;
    }

    JComboBox<String> copyFromMastSelection() {
        JComboBox<String> mastSelect = new JComboBox<>();
        List<String> names = InstanceManager.getDefault(jmri.SignalMastManager.class).getSystemNameList();
        for (String name : names) {
            if (log.isTraceEnabled()) log.trace("copyFromMastSelection comparing to {}", InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name).getSignalSystem().getSystemName());
            if ((InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(name) instanceof DccSignalMast)){
                mastSelect.addItem(InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(name).getDisplayName());
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
                        copyFromAnotherDCCMastAspect(sourceMast);
                    }
                }
            });
        }
        return mastSelect;
    }

    /**
     * Copy aspects by name from another DccSignalMast
     */
    void copyFromAnotherDCCMastAspect(String strMast) {
        DccSignalMast mast = (DccSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(strMast);
        Vector<String> validAspects = mast.getValidAspects();
        for (String aspect : dccAspect.keySet()) {
            if (validAspects.contains(aspect) || mast.isAspectDisabled(aspect)) { // valid doesn't include disabled
                // present, copy
                dccAspect.get(aspect).setAspectId(mast.getOutputForAppearance(aspect));
                dccAspect.get(aspect).setAspectDisabled(mast.isAspectDisabled(aspect));
            } else {
                // not present, log
                log.info("Can't get aspect \"{}\" from head \"{}\", leaving unchanged", aspect, mast);
            }
        }
    }

    /**
     * JPanel to define properties of an Aspect for a DCC Signal Mast.
     * <p>
     * Invoked from the AddSignalMastPanel class when a DCC Signal Mast is
     * selected.
     */
    static class DCCAspectPanel {

        String aspect = "";
        JCheckBox disabledCheck = new JCheckBox(Bundle.getMessage("DisableAspect"));
        JLabel aspectLabel = new JLabel(Bundle.getMessage("DCCMastSetAspectId") + ":");
        JTextField aspectId = new JTextField(5);

        DCCAspectPanel(String aspect) {
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
                log.error("failed to convert DCC number");
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DccSignalMastAddPane.class);
}
