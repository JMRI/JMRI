// ProgServiceModePane.java

package jmri.jmrit.progsupport;

import javax.swing.BoxLayout;
import jmri.*;

/**
 * Provide a JPanel to configure the service mode programmer.
 * <P>
 * The using code should get a configured programmer with getProgrammer. Since
 * there's only one service mode programmer, maybe this isn't critical, but
 * it's a good idea for the future.
 * <P>
 * A ProgModePane may "share" between one of these and a ProgOpsModePane,
 * which means that there might be _none_ of these buttons selected.  When
 * that happens, the mode of the underlying programmer is left unchanged
 * and no message is propagated.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
 */
public class ProgServiceModePane extends ProgModeSelector implements java.beans.PropertyChangeListener {

    // GUI member declarations

    javax.swing.ButtonGroup modeGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton addressButton  	= new javax.swing.JRadioButton();
    javax.swing.JRadioButton pagedButton    	= new javax.swing.JRadioButton();
    javax.swing.JRadioButton directBitButton   	= new javax.swing.JRadioButton();
    javax.swing.JRadioButton directByteButton   = new javax.swing.JRadioButton();
    javax.swing.JRadioButton registerButton 	= new javax.swing.JRadioButton();

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance()!=null)
            return InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        else
            log.warn("request for service mode programmer with no ProgrammerManager configured");
        return null;
    }

    /**
     * Are any of the buttons selected?
     * @return true is any button is selected
     */
    public boolean isSelected() {
        return (addressButton.isSelected() || pagedButton.isSelected()
                || directBitButton.isSelected() || directByteButton.isSelected()
                || registerButton.isSelected() );
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public ProgServiceModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public ProgServiceModePane(int direction, javax.swing.ButtonGroup group) {
        modeGroup = group;

        // configure items for GUI
        pagedButton.setText(Bundle.getString("PagedMode"));
        directBitButton.setText(Bundle.getString("DirectBit"));
        directByteButton.setText(Bundle.getString("DirectByte"));
        registerButton.setText(Bundle.getString("RegisterMode"));
        addressButton.setText(Bundle.getString("AddressMode"));
        modeGroup.add(pagedButton);
        modeGroup.add(registerButton);
        modeGroup.add(directByteButton);
        modeGroup.add(directBitButton);
        modeGroup.add(addressButton);

        // if a programmer is available, disable buttons for unavailable modes
        if (InstanceManager.programmerManagerInstance()!=null
            && InstanceManager.programmerManagerInstance().getGlobalProgrammer()!=null) {
            Programmer p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
            if (!p.hasMode(Programmer.PAGEMODE)) pagedButton.setEnabled(false);
            if (!p.hasMode(Programmer.DIRECTBYTEMODE)) directByteButton.setEnabled(false);
            if (!p.hasMode(Programmer.DIRECTBITMODE)) directBitButton.setEnabled(false);
            if (!p.hasMode(Programmer.REGISTERMODE)) registerButton.setEnabled(false);
            if (!p.hasMode(Programmer.ADDRESSMODE)) addressButton.setEnabled(false);
        } else {
            log.info("No programmer available, so modes not set");
        }

        // add listeners to buttons
        pagedButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getSelectedMode());
                }
            });
        directBitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getSelectedMode());
                }
            });
        directByteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getSelectedMode());
                }
            });
        registerButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getSelectedMode());
                }
            });
        addressButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getSelectedMode());
                }
            });

        // load the state if a programmer exists
        connect();
        updateMode();

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // install items in GUI
        add(pagedButton);
        add(directBitButton);
        add(directByteButton);
        add(registerButton);
        add(addressButton);
    }

    /**
     * Determine the mode selected by these buttons
     * @return A mode constant or 0 is no button selected
     */
    private int getSelectedMode() {
        if (pagedButton.isSelected())
            return jmri.Programmer.PAGEMODE;
        else if (directBitButton.isSelected())
            return jmri.Programmer.DIRECTBITMODE;
        else if (directByteButton.isSelected())
            return jmri.Programmer.DIRECTBYTEMODE;
        else if (registerButton.isSelected())
            return jmri.Programmer.REGISTERMODE;
        else if (addressButton.isSelected())
            return jmri.Programmer.ADDRESSMODE;
        else
            return 0;
    }

    protected void setButtonMode(int mode) {
        switch (mode) {
        case jmri.Programmer.REGISTERMODE:
            registerButton.setSelected(true);
            break;
        case jmri.Programmer.PAGEMODE:
            pagedButton.setSelected(true);
            break;
        case jmri.Programmer.DIRECTBYTEMODE:
            directByteButton.setSelected(true);
            break;
        case jmri.Programmer.DIRECTBITMODE:
            directBitButton.setSelected(true);
            break;
        case jmri.Programmer.ADDRESSMODE:
            addressButton.setSelected(true);
            break;
        case 0:
            // don't change anything in this case
            break;
            // NMRA "Operations" or "Programming on the main" modes
        case jmri.Programmer.OPSACCBITMODE:
        case jmri.Programmer.OPSACCBYTEMODE:
        case jmri.Programmer.OPSACCEXTBITMODE:
        case jmri.Programmer.OPSACCEXTBYTEMODE:
        case jmri.Programmer.OPSBITMODE:
        case jmri.Programmer.OPSBYTEMODE:
        	break;
        default:
            log.warn("propertyChange without valid mode value");
            break;
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName() == "Mode") {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // if we're not holding a current mode, don't update
                int mode = ((Integer)e.getNewValue()).intValue();
                setButtonMode(mode);
            }
        } else log.warn("propertyChange with unexpected propertyName: "+e.getPropertyName());
    }

    // connect to the Programmer interface
    boolean connected = false;

    private void connect() {
        if (!connected) {
            if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null) {
                InstanceManager.programmerManagerInstance()
                    .getGlobalProgrammer().addPropertyChangeListener(this);
                connected = true;
                log.debug("Connecting to programmer");
            } else {
                log.debug("No programmer present to connect");
            }
        }
    }

    // set the programmer to the current mode
    private void setProgrammerMode(int mode) {
        log.debug("Setting programmer to mode "+mode);
        if (InstanceManager.programmerManagerInstance() != null
            && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null)
            InstanceManager.programmerManagerInstance().getGlobalProgrammer().setMode(mode);
    }

    /**
     * Internal routine to update the mode buttons to the
     * current state
     */
    void updateMode() {
        if (connected) {
            int mode = InstanceManager.programmerManagerInstance().getGlobalProgrammer().getMode();
            if (log.isDebugEnabled()) log.debug("setting mode buttons: "+mode);
            setButtonMode(mode);
        }
        else {
            log.debug("Programmer doesn't exist, can't set default mode");
        }
    }

    // no longer needed, disconnect if still connected
    public void dispose() {
        if (connected) {
            if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null)
                InstanceManager.programmerManagerInstance().getGlobalProgrammer().removePropertyChangeListener(this);
            connected = false;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProgServiceModePane.class.getName());
}
