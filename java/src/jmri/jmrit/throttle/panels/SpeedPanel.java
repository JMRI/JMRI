package jmri.jmrit.throttle.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.interfaces.AddressListener;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Panel that contains a label to display scale speed if available
 * for forward, reverse and STOP. TODO: fix speed increments (14, 28)
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
 * @author glen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2008
 * @author Steve Gigiel Copyright (C) 2017
 * @author Lionel Jeanson 2026
 * 
 */
public class SpeedPanel extends JPanel implements java.beans.PropertyChangeListener, AddressListener {

    private DccThrottle throttle;

    private JPanel speedDisplayPanel;

    private JLabel scaleSpeedLabel = new JLabel("", JLabel.CENTER);

    // tracks whether we are using speed profiles
    private boolean useSpeedProfile = false;

    // last known direction
    private boolean currentIsForward = true;
    private float currentThrottleVol = 0.0f;

    //for access to roster entry
    private AddressPanel addressPanel; //for access to roster entry

    /**
     * Constructor.
     */
    public SpeedPanel() {
        initGUI();
    }

    /**
     * Set the AddressPanel this throttle control is listening for new throttle
     * event
     *
     * @param ap  reference to the addresspanel
     */
    public void setAddressPanel(AddressPanel ap) {
        if (throttle != null) {
            notifyAddressReleased(throttle.getLocoAddress());
        }
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        addressPanel = ap;
        if (addressPanel != null) {
            addressPanel.addAddressListener(this);        
            if (addressPanel.getThrottle() != null ) {
                notifyAddressThrottleFound(addressPanel.getThrottle());
            } else {
                notifyAddressReleased(addressPanel.getCurrentAddress());
            }
        }
    }

    /**
     * "Destructor"
     */
    public void dispose() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
            throttle = null;
        }
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI() {
        setLayout(new BorderLayout());
        speedDisplayPanel = new JPanel();
        speedDisplayPanel.setFont(new Font("", Font.PLAIN, 32));
        speedDisplayPanel.setLayout(new BoxLayout(speedDisplayPanel, BoxLayout.X_AXIS));
        speedDisplayPanel.setOpaque(false);
        add(speedDisplayPanel, BorderLayout.CENTER);
        speedDisplayPanel.add(scaleSpeedLabel);
    }

    /**
     * update the state of this panel if direction or speed change
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            currentThrottleVol = ((Float) e.getNewValue()).floatValue();
            scaleSpeedLabel.setText(updateSpeedLabel(useSpeedProfile, currentThrottleVol, currentIsForward));
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            currentIsForward = (boolean) e.getNewValue();
            scaleSpeedLabel.setText(updateSpeedLabel(useSpeedProfile, currentThrottleVol, currentIsForward));
        }
        if (log.isDebugEnabled()) {
            log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
        }
    }

    /**
     *
     * @param useSpeedProfile  are we using speed profile
     * @param throttleVolume   throttle position (percent of 1)
     * @param isForward        true if going forward.
     * @return a string for displaying speed if available
     */
    private String updateSpeedLabel(boolean useSpeedProfile, float throttleVolume, boolean isForward) {
        RosterEntry re = addressPanel.getRosterEntry();
        if (re != null && useSpeedProfile) {
            return (re.getSpeedProfile().convertThrottleSettingToScaleSpeedWithUnits(throttleVolume, isForward));
        } else {
            return (Bundle.getMessage("ThrottleSpeedPanelError"));
        }

    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {     
    }    

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        if (throttle == null) {
            log.debug("notifyAddressReleased() throttle alreaday null, called for loc {}",la);
            return;
        }        
        this.setEnabled(false);
        throttle.removePropertyChangeListener(this);
        throttle = null;
        updateSpeedLabel(useSpeedProfile, 0, true);
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {       
        if (log.isDebugEnabled()) {
            log.debug("control panel received new throttle {}",t);
        }
        this.throttle = t;
        this.throttle.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            jmri.DccLocoAddress Address = (jmri.DccLocoAddress) throttle.getLocoAddress();
            log.debug("new address is {}", Address.toString());
        }

        useSpeedProfile = false;  //posit false
        RosterEntry re = addressPanel.getRosterEntry();
        if (re != null
                && re.getSpeedProfile() != null
                && re.getSpeedProfile().getProfileSize() > 0) {
            useSpeedProfile = true;
        }
        updateSpeedLabel(useSpeedProfile, t.getSpeedSetting(), t.getIsForward());
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        notifyAddressChosen(l);
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress l) {
        notifyAddressReleased(l);
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received consist throttle");
        }
        notifyAddressThrottleFound(throttle);
    }

    public Element getXml() {
        Element me = new Element("SpeedPanel"); // NOI18N
        // put nothing
        return me;
    }

    public void setXml(Element e) {
        // do nothing
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(SpeedPanel.class);
}
