// ProgServiceModePane.java
package jmri.jmrit.progsupport;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JPanel to configure the service mode programmer.
 * <P>
 * The using code should get a configured programmer with getProgrammer. Since
 * there's only one service mode programmer, maybe this isn't critical, but it's
 * a good idea for the future.
 * <P>
 * A ProgModePane may "share" between one of these and a ProgOpsModePane, which
 * means that there might be _none_ of these buttons selected. When that
 * happens, the mode of the underlying programmer is left unchanged and no
 * message is propagated.
 * <P>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModePane object can disconnect its listeners.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001, 2014
 * @version	$Revision$
 */
public class ProgServiceModePane extends ProgModeSelector implements PropertyChangeListener, ActionListener {

    private static final long serialVersionUID = 9075947253729508706L;
    ButtonGroup modeGroup = new ButtonGroup();
    HashMap<ProgrammingMode, JRadioButton> buttonMap = new HashMap<ProgrammingMode, JRadioButton>();
    JComboBox<GlobalProgrammerManager> progBox;
    ArrayList<JRadioButton> buttonPool = new ArrayList<JRadioButton>();

    /**
     * Get the selected programmer
     */
    public Programmer getProgrammer() {
        if (progBox.getSelectedItem() == null) return null;
        return ((GlobalProgrammerManager) progBox.getSelectedItem()).getGlobalProgrammer();
    }

    /**
     * Are any of the modes selected?
     *
     * @return true is any button is selected
     */
    public boolean isSelected() {
        for (JRadioButton button : buttonMap.values()) {
            if (button.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgServiceModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * Get the list of global managers
     * @return empty list if none
     */
    protected List<GlobalProgrammerManager> getMgrList() {
        List<GlobalProgrammerManager> retval;
        
        retval = InstanceManager.getList(jmri.GlobalProgrammerManager.class);
        if (retval!=null) return retval;
        return new ArrayList<>();
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgServiceModePane(int direction, javax.swing.ButtonGroup group) {
        modeGroup = group;

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // create the programmer display combo box
        java.util.Vector<GlobalProgrammerManager> v = new java.util.Vector<GlobalProgrammerManager>();
        for (GlobalProgrammerManager pm : getMgrList()) {
            v.add(pm);
            // listen for changes
            if (pm.getGlobalProgrammer() != null) {
                pm.getGlobalProgrammer().addPropertyChangeListener(this);
            } else {
                log.warn("No GlobalProgrammer present in GlobalProgrammerManager, is there a problem with layout connection?");
            }
        }
        add(progBox = new JComboBox<GlobalProgrammerManager>(v));
        // if only one, don't show
        if (progBox.getItemCount() < 2) {
            // no choice, so don't display, don't monitor for changes
            progBox.setVisible(false);
        } else {
            progBox.setSelectedItem(InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)); // set default
            progBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // new programmer selection
                    programmerSelected();
                }
            });
        }

        // and execute the setup for 1st time
        programmerSelected();

    }

    /**
     * reload the interface with the new programmers
     */
    void programmerSelected() {
        log.debug("programmerSelected starts with {} buttons", buttonPool.size());
        // hide buttons
        for (JRadioButton button : buttonPool) {
            button.setVisible(false);
        }

        // clear map
        buttonMap.clear();

        // configure buttons
        int index = 0;
        if (getProgrammer() == null) {
            return;
        }
        List<ProgrammingMode> modes = getProgrammer().getSupportedModes();
        log.debug("   has {} modes", modes.size());
        for (ProgrammingMode mode : modes) {
            JRadioButton button;
            // need a new button?
            if (index >= buttonPool.size()) {
                log.debug("   add button");
                button = new JRadioButton();
                buttonPool.add(button);
                modeGroup.add(button);
                button.addActionListener(this);
                add(button); // add to GUI
            }
            // configure next button in pool
            log.debug("   set for {}", mode.toString());
            button = buttonPool.get(index++);
            button.setVisible(true);
            modeGroup.add(button);
            button.setText(mode.toString());
            buttonMap.put(mode, button);
        }
        setGuiFromProgrammer();
    }

    /**
     * Listen to buttons for mode changes
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // find selected button
        log.debug("Selected button: {}", e.getActionCommand());
        for (ProgrammingMode mode : buttonMap.keySet()) {
            if (mode.toString().equals(e.getActionCommand())) {
                log.debug("      set mode {} on {}", mode.toString(), getProgrammer());
                getProgrammer().setMode(mode);
                return; // 1st match
            }
        }
    }

    /**
     * Listen to programmer for mode changes
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("Mode".equals(e.getPropertyName()) && getProgrammer().equals(e.getSource())) {
            // mode changed in programmer, change GUI here if needed
            log.debug("Mode propertyChange with {}", isSelected());
            if (isSelected()) {  // only change mode if we have a selected mode, in case some other selector with shared group has the selection
                setGuiFromProgrammer();
            }
        }
    }

    void setGuiFromProgrammer() {
        ProgrammingMode mode = getProgrammer().getMode();
        JRadioButton button = buttonMap.get(mode);
        log.debug("  setting button for mode {}", mode);
        if (button == null) {
            log.debug("   didn't find button, returning");
            return;
        }
        button.setSelected(true);
    }

    // no longer needed, disconnect if still connected
    public void dispose() {
        for (GlobalProgrammerManager pm : getMgrList()) {
            pm.getGlobalProgrammer().removePropertyChangeListener(this);
        }
    }

    static Logger log = LoggerFactory.getLogger(ProgServiceModePane.class.getName());
}
