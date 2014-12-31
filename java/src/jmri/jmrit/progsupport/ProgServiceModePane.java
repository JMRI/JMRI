// ProgServiceModePane.java

package jmri.jmrit.progsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
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

    /**
	 * 
	 */
	private static final long serialVersionUID = 9075947253729508706L;
	ButtonGroup modeGroup 		    = new ButtonGroup();
    JRadioButton addressButton  	= new JRadioButton();
    JRadioButton pagedButton    	= new JRadioButton();
    JRadioButton directBitButton   	= new JRadioButton();
    JRadioButton directByteButton   = new JRadioButton();
    JRadioButton registerButton 	= new JRadioButton();
    JComboBox<GlobalProgrammerManager>   progBox;

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance()!=null)
            return InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
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
        pagedButton.setText(Bundle.getMessage("PagedMode"));
        directBitButton.setText(Bundle.getMessage("DirectBit"));
        directByteButton.setText(Bundle.getMessage("DirectByte"));
        registerButton.setText(Bundle.getMessage("RegisterMode"));
        addressButton.setText(Bundle.getMessage("AddressMode"));
        modeGroup.add(pagedButton);
        modeGroup.add(registerButton);
        modeGroup.add(directByteButton);
        modeGroup.add(directBitButton);
        modeGroup.add(addressButton);

        // create the display combo box
        java.util.Vector<GlobalProgrammerManager> v = new java.util.Vector<GlobalProgrammerManager>();
        for (Object e : InstanceManager.getList(jmri.GlobalProgrammerManager.class))
            v.add((GlobalProgrammerManager)e);
        add(progBox = new JComboBox<GlobalProgrammerManager>(v));
        // if only one, don't show
        if (progBox.getItemCount()<2) progBox.setVisible(false);
        progBox.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new selection
                //setModes((jmri.GlobalProgrammerManager)progBox.getSelectedItem());
            }
        });
        progBox.setSelectedItem(InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)); // set default


        setModes();
        
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

    void setModes() {
        // if a programmer is available, disable buttons for unavailable modes
        if (InstanceManager.programmerManagerInstance()!=null
            && InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer()!=null) {
            Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
            if (!p.hasMode(Programmer.PAGEMODE)) pagedButton.setEnabled(false);
            if (!p.hasMode(Programmer.DIRECTBYTEMODE)) directByteButton.setEnabled(false);
            if (!p.hasMode(Programmer.DIRECTBITMODE)) directBitButton.setEnabled(false);
            if (!p.hasMode(Programmer.REGISTERMODE)) registerButton.setEnabled(false);
            if (!p.hasMode(Programmer.ADDRESSMODE)) addressButton.setEnabled(false);
        } else {
            log.info("No programmer available, so modes not set");
        }
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
                && InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer() != null) {
                InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
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
        if (InstanceManager.getDefault(jmri.GlobalProgrammerManager.class) != null
            && InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer() != null)
            InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer().setMode(mode);
    }

    /**
     * Internal routine to update the mode buttons to the
     * current state
     */
    void updateMode() {
        if (connected) {
            int mode = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer().getMode();
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
            if (InstanceManager.getDefault(jmri.GlobalProgrammerManager.class) != null
                && InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer() != null)
                InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer().removePropertyChangeListener(this);
            connected = false;
        }
    }

    static Logger log = LoggerFactory.getLogger(ProgServiceModePane.class.getName());
}
