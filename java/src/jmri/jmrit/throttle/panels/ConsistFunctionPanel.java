package jmri.jmrit.throttle.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jdom2.Element;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.throttle.UIImplementation.SimpleThrottlePanel;
import jmri.jmrit.throttle.interfaces.AddressListener;

/**
 * A panel do be used withing a throttle UI, allows for independantly 
 * controling functions of each locomotive in a consist
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
 * @author Lionel Jeanson Copyright (C) 2026
 * 
 */

public class ConsistFunctionPanel extends JPanel implements AddressListener {

    private AddressPanel addressPanel = null;    
    private JTabbedPane consistFunctionsPanels;
    private JLabel errorLabel = new JLabel(Bundle.getMessage("ThrottleConsistsFunctionPanelError"));
    private static final RosterIconFactory ICN_FACT = InstanceManager.getDefault(RosterIconFactory.class);
    
    public ConsistFunctionPanel() {
        initGUI();        
        updateFunctionPanels();
    }
    
    private void initGUI() {
        setLayout(new BorderLayout());
        consistFunctionsPanels = new JTabbedPane();    
        add(errorLabel, BorderLayout.NORTH);       
        add(consistFunctionsPanels, BorderLayout.CENTER);
    }

    public void dispose() {        
        if (consistFunctionsPanels!=null) {
            for (Component cmp : consistFunctionsPanels.getComponents()) {
                if (cmp instanceof SimpleThrottlePanel) {
                    ((SimpleThrottlePanel) cmp).dispose();
                }
            }
            consistFunctionsPanels.removeAll();
        }
        consistFunctionsPanels = null;
    }

    public void setAddressPanel(AddressPanel addressPanel) {
        if (this.addressPanel != null) {
            this.addressPanel.removeAddressListener(this);
        }
        this.addressPanel = addressPanel;
        if (this.addressPanel != null) {
            this.addressPanel.addAddressListener(this);
        }
        updateFunctionPanels();
    }

    private void updateFunctionPanels() {
        if (consistFunctionsPanels == null) {
            errorLabel.setVisible(true);
            return; // we're self desctructing
        }
        // clean up       
        consistFunctionsPanels.removeAll();        
        for (Component cmp : consistFunctionsPanels.getComponents()) {
            if (cmp instanceof SimpleThrottlePanel) {
                ((SimpleThrottlePanel) cmp).dispose();
            }
        }        
        // shall we?
        if ((addressPanel == null) || (addressPanel.getThrottle() == null)) {
            return;
        }
        Consist consist = addressPanel.getConsistEntry();
        if (consist == null) {
            return;
        }
        // let's go
        errorLabel.setVisible(false);
        ArrayList<DccLocoAddress> consistList = consist.getConsistList(); 
        // go backward, we want the head on the right (added last)
        for (int i=consistList.size()-1; i>=0; i--) {
            SimpleThrottlePanel stp = new SimpleThrottlePanel(null, InstanceManager.getDefault(jmri.ThrottleManager.class), false, true, false);
            stp.setAddress(consistList.get(i));
            // do we have a matching roster entry
            Icon tabIcon = null;
            String tabText = Bundle.getMessage("ThrottleAddress") + " " + consistList.get(i);
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + consistList.get(i).getNumber(), null, null, null, null);
            if (!l.isEmpty()) {                
                tabText = l.get(0).getId();
                if (consist.getLocoDirection(consistList.get(i))) {
                    tabIcon = ICN_FACT.getIcon(l.get(0));
                } else {
                    tabIcon = ICN_FACT.getReversedIcon(l.get(0));
                }
            }           
            consistFunctionsPanels.addTab(tabText, tabIcon, stp);
            consistFunctionsPanels.setBackgroundAt(consistList.size()-i-1, Color.RED);
        }
        consistFunctionsPanels.setSelectedIndex(consistList.size()-1); // select last (head)
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress address) {
       updateFunctionPanels();
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        updateFunctionPanels();
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress address) {
        // do nothing
    }

    @Override
    public void notifyAddressChosen(LocoAddress address) {
        // do nothing
    }

    @Override
    public void notifyAddressReleased(LocoAddress address) {
        // do nothing
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle throttle) {
        // do nothing
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {
        updateFunctionPanels();
    }

    public Element getXml() {
        Element me = new Element("ConsistFunctionsPanel"); // NOI18N
        // put nothing
        return me;
    }

    public void setXml(Element e) {
        // do nothing
    }
    
}
