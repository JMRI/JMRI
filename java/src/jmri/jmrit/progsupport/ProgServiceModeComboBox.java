// ProgServiceModeComboBox.java
package jmri.jmrit.progsupport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import jmri.*;

/**
 * Provide a JPanel with a JComboBox to configure the service mode programmer.
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
public class ProgServiceModeComboBox extends ProgModeSelector implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JComboBox box;
    ArrayList<Integer> modes = new ArrayList<Integer>();

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance() != null) {
            return InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        } else {
            log.warn("request for service mode programmer with no ProgrammerManager configured");
        }
        return null;
    }

    /**
     * Are any of the buttons selected?
     * @return true
     */
    public boolean isSelected() {
        return true;
    }

    public ProgServiceModeComboBox() {
        box = new JComboBox();

        // install items in GUI
        add(new JLabel(Bundle.getMessage("ProgrammingMode")));
        add(box);

        if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null) {
            Programmer p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
            if (p.hasMode(Programmer.PAGEMODE)) {
                box.addItem(Bundle.getMessage("PagedMode"));
                modes.add(modes.size(), Programmer.PAGEMODE);
            }
            if (p.hasMode(Programmer.REGISTERMODE)) {
                box.addItem(Bundle.getMessage("RegisterMode"));
                modes.add(modes.size(), Programmer.REGISTERMODE);
            }
            if (p.hasMode(Programmer.DIRECTBYTEMODE)) {
                box.addItem(Bundle.getMessage("DirectByte"));
                modes.add(modes.size(), Programmer.DIRECTBYTEMODE);
            }
            if (p.hasMode(Programmer.DIRECTBITMODE)) {
                box.addItem(Bundle.getMessage("DirectBit"));
                modes.add(modes.size(), Programmer.DIRECTBITMODE);
            }
            if (p.hasMode(Programmer.ADDRESSMODE)) {
                box.addItem(Bundle.getMessage("AddressMode"));
                modes.add(modes.size(), Programmer.ADDRESSMODE);
            }
        } else {
            box.addItem(Bundle.getMessage("NotAvailable"));
            log.info("No programmer available, so modes not set");
        }
        box.setEnabled((!modes.isEmpty()));

        ActionListener boxListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connect();
                if (connected) {
                    try {
                        setProgrammerMode(modes.get(box.getSelectedIndex()));
                    } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                        //Can be considered normal if there is no service mode programmer available
                    }
                }
            }
        };
        box.addActionListener(boxListener);

        // load the state if a programmer exists
        connect();
        updateMode();

    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("Mode".equals(e.getPropertyName())) {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // if we're not holding a current mode, don't update
                int mode = ((Integer) e.getNewValue()).intValue();
                box.setSelectedIndex(modes.indexOf(mode));
            }
        } else {
            log.warn("propertyChange with unexpected propertyName: " + e.getPropertyName());
        }
    }
    // connect to the Programmer interface
    boolean connected = false;

    private void connect() {
        if (!connected) {
            if (InstanceManager.programmerManagerInstance() != null
                    && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null) {
                InstanceManager.programmerManagerInstance().getGlobalProgrammer().addPropertyChangeListener(this);
                connected = true;
                log.debug("Connecting to programmer");
            } else {
                log.debug("No programmer present to connect");
            }
        }
    }

    // set the programmer to the current mode
    private void setProgrammerMode(int mode) {
        if (log.isDebugEnabled()) {
            log.debug("Setting programmer to mode " + mode);
        }
        if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null) {
            InstanceManager.programmerManagerInstance().getGlobalProgrammer().setMode(mode);
        }
    }

    // Internal routine to update the comboBox to the current state
    private void updateMode() {
        if (connected) {
            int mode = InstanceManager.programmerManagerInstance().getGlobalProgrammer().getMode();
            if (log.isDebugEnabled()) {
                log.debug("setting mode buttons: " + mode);
            }
            box.setSelectedIndex(modes.indexOf(mode));
        } else {
            log.debug("Programmer doesn't exist, can't set default mode");
        }
    }

    // no longer needed, disconnect if still connected
    public void dispose() {
        if (connected) {
            if (InstanceManager.programmerManagerInstance() != null
                    && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null) {
                InstanceManager.programmerManagerInstance().getGlobalProgrammer().removePropertyChangeListener(this);
            }
            connected = false;
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProgServiceModeComboBox.class.getName());
}
