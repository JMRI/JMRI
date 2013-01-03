// ProgDeferredServiceModePane.java

package jmri.jmrit.progsupport;

import java.awt.*;
import javax.swing.*;
import jmri.*;

import jmri.Programmer;

/**
 * Provide a JPanel to configure the service mode programmer via a
 * "simple until you need it".  This consists of a label with the
 * current mode, plus a "set" button.
 * <P>
 * The using code should get a configured programmer with getProgrammer. Since
 * there's only one service mode programmer, maybe this isn't critical, but
 * it's a good idea for the future.
 * <P>
 * A ProgModePane may "share" between one of these and an ops-mode selection,
 * which means that there might be _none_ of these modes selected.  When
 * that happens, the mode of the underlying programmer is left unchanged
 * and no message is propagated.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * <P>
 * The implementation relies on a captive ProgServiceModePane which handles
 * "set" operations by changing the actual service mode programmer state.
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
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
 */
public class ProgDeferredServiceModePane extends ProgModeSelector implements java.beans.PropertyChangeListener {

    ProgServiceModePane servicePane;
    JFrame setFrame;
    JLabel currentMode = new JLabel();
    protected JButton setButton = new JButton(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("SET..."));

    /**
     * Enable/Disable the "set" button in GUI
     * @param enabled false disables button
     */
    public void setEnabled(boolean enabled) {
        setButton.setEnabled(enabled);
    }

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
     *
     * @return true always, as we expect to always be selected
     */
    public boolean isSelected() {
        return true;
    }

    /**
     * Create the object.  There are no parameters, as there's
     * only a single layout now.
     */
    public ProgDeferredServiceModePane() {
        servicePane = new ProgServiceModePane(BoxLayout.Y_AXIS);

        // update to current status & watch for changes
        int mode = 0;
        if (InstanceManager.programmerManagerInstance() != null
            && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null)
            mode = InstanceManager.programmerManagerInstance().getGlobalProgrammer().getMode();
        updateStatus(mode);
        connect();

        // arrange activation
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // pop the frame
                setFrame.setVisible(true);
            }
        });

        // create the set frame
        setFrame = new JFrame(Bundle.getMessage("TitleSetProgrammingMode"));
        setFrame.getContentPane().add(servicePane);
        setFrame.pack();

        // create the main GUI
        setLayout(new FlowLayout());
        add(currentMode);
        add(setButton);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName() == "Mode") {
            // mode changed in programmer, change GUI here if needed
            // take the mode from the message, not the programmer, to get
            // proper synchronization
            int mode = ((Integer)e.getNewValue()).intValue();
            updateStatus(mode);
        } else log.warn("propertyChange with unexpected propertyName: "+e.getPropertyName());
    }

    /**
     * Update the display to the current status
     */
    private void updateStatus(int mode) {
        currentMode.setText(decodeMode(mode));
        invalidate();
    }

    private String decodeMode(int mode) {
        switch (mode) {
        case Programmer.ADDRESSMODE:    return Bundle.getMessage("AddressMode");
        case Programmer.DIRECTBITMODE:  return Bundle.getMessage("DirectBit");
        case Programmer.DIRECTBYTEMODE: return Bundle.getMessage("DirectByte");
        case Programmer.PAGEMODE:       return Bundle.getMessage("PagedMode");
        case Programmer.REGISTERMODE:   return Bundle.getMessage("RegisterMode");
        default:                        return Bundle.getMessage("UnknownMode");
        }
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

    // no longer needed, disconnect if still connected
    public void dispose() {
        if (connected) {
            if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null)
                InstanceManager.programmerManagerInstance().getGlobalProgrammer().removePropertyChangeListener(this);
            connected = false;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProgDeferredServiceModePane.class.getName());
}
