package jmri.jmrit.throttle.panels;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.swing.ResizableImagePanel;

/**
 * A panel to be used as background for JMRI throttle frames 
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
 * @author Lionel Jeanson - 2009, 2026
 * 
 */

public class BackgroundPanel extends ResizableImagePanel implements AddressListener, PropertyChangeListener  {

    AddressPanel addressPanel = null;

    public BackgroundPanel() {
        super();
        initGUI();
        InstanceManager.getDefault(ThrottlesPreferences.class).addPropertyChangeListener(this);
        applyPreferences();
    }
    
    private void initGUI() {
        setBackground(Color.GRAY);
        setRespectAspectRatio(true);
    }
    
    private void applyPreferences() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        
        setResizingContainer(preferences.isResizingWindow());
        setVisible(  (preferences.isUsingExThrottle()) && (preferences.isUsingRosterImage()));
    }

    public void setAddressPanel(AddressPanel ap) {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        addressPanel = ap;
        if (addressPanel != null) {
            addressPanel.addAddressListener(this);
            updateImage(addressPanel.getRosterEntry());
        }        
    }

    private void updateImage(RosterEntry rosterEntry) {
        setImagePath(null);
        if (rosterEntry != null) {
            setImagePath(rosterEntry.getImagePath());
        } else if (addressPanel != null) {
            DccThrottle t = addressPanel.getThrottle();
            if (t != null) {               
                if (t.getLocoAddress().toString().compareTo("3(S)") == 0) { // default DCC address
                    setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCCImage.png"));
                }
                if (t.getLocoAddress().toString().compareTo("0(S)") == 0) { // default DC address
                    setImagePath(jmri.util.FileUtil.getExternalFilename("resources/icons/throttles/DCImage.png"));
                }
            }
        }
    }
    

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }       
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {
        updateImage(re);
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        setImagePath(null);
    } 

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress l) {
        updateImage(null);
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle t) {
        if (addressPanel != null) {
            updateImage(addressPanel.getRosterEntry());
        }
    }

    public void dispose() {
        InstanceManager.getDefault(ThrottlesPreferences.class).removePropertyChangeListener(this);
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e == null) {
            return;
        }        
//        log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
        if (ThrottlesPreferences.prefPopertyName.compareTo(e.getPropertyName()) == 0) {
            applyPreferences();
        }
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackgroundPanel.class);    
}
