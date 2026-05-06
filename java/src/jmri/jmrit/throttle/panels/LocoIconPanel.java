package jmri.jmrit.throttle.panels;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom2.Element;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrit.consisttool.ConsistListCellRenderer;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.throttle.interfaces.AddressListener;

/**
 * A Panel that displays the loco icon and label as defined in its roster entry. 
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 */

public class LocoIconPanel extends JPanel implements AddressListener {

    private static final int IMAGE_HEIGHT = 32;
    private static final RosterIconFactory ICN_FACT = new RosterIconFactory(IMAGE_HEIGHT); // same instance reused

    AddressPanel addressPanel = null;
    String description;
    ImageIcon icon;
    JLabel iconLabel = new JLabel();

    public LocoIconPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setVerticalAlignment(JLabel.CENTER);
        updateLabel();
        add(iconLabel, BorderLayout.CENTER);        
    }

    public void setAddressPanel(AddressPanel addressPanel) {
        if (this.addressPanel != null) {
            this.addressPanel.removeAddressListener(this);
        }
        this.addressPanel = addressPanel;
        if (this.addressPanel != null) {
            this.addressPanel.addAddressListener(this);
        }
        updateLabel();
    }

    public String getDescription() {
        return description;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    private void updateLabel() {
        description = Bundle.getMessage("ThrottleNotAssigned");
        icon = null;
        if (addressPanel != null && (addressPanel.getThrottle() != null)) {
            if (addressPanel.getConsistAddress() != null) { 
                // consists                
                icon = ConsistListCellRenderer.getConsistIcon(addressPanel.getConsistAddress(), ICN_FACT);
                description = addressPanel.getConsistAddress().toString();
            } else if (addressPanel.getRosterEntry() != null) {  
                // regular locomotive                                   
                icon = ICN_FACT.getIcon(addressPanel.getRosterEntry());
                description = addressPanel.getRosterEntry().getId();
            } else if (addressPanel.getCurrentAddress() != null)  {
                switch (addressPanel.getCurrentAddress().getNumber()) {
                    case 0:
                        description = Bundle.getMessage("ThrottleDCControl") + " - " + addressPanel.getCurrentAddress();
                        break;
                    case 3:
                        description = Bundle.getMessage("ThrottleDCCControl") + " - " + addressPanel.getCurrentAddress();
                        break;
                    default:
                        description = Bundle.getMessage("ThrottleAddress") + " " + addressPanel.getCurrentAddress();
                        break;
                }
            }
        }
        iconLabel.setIcon(icon);
        iconLabel.setText(description);
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        updateLabel();
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {
        updateLabel();
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        updateLabel();
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle t) {
        updateLabel();
    }

    public void destroy() {        
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
    }

    public Element getXml() {
        Element me = new Element("LocoIconPanel"); // NOI18N
        // put nothing
        return me;
    }

    public void setXml(Element e) {
        // do nothing
    }

}
