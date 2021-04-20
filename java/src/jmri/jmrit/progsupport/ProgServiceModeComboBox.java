package jmri.jmrit.progsupport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JPanel with a JComboBox to configure the service mode (Global) programmer.
 * <p>
 * The using code should get a configured programmer with {@link #getProgrammer()}.
 * <p>
 * A ProgModePane may "share" between one of these and a ProgOpsModePane, which
 * means that there might be _none_ of these buttons selected. When that
 * happens, the mode of the underlying programmer is left unchanged and no
 * message is propagated.
 * <p>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModePane object can disconnect its listeners.
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class ProgServiceModeComboBox extends ProgModeSelector implements PropertyChangeListener, ActionListener {

    // GUI member declarations
    JLabel progLabel = new JLabel(Bundle.getMessage("ProgrammerLabel"));
    JComboBox<GlobalProgrammerManager> progBox;
    JComboBox<ProgrammingMode> modeBox;
    ArrayList<Integer> modes = new ArrayList<Integer>();

    /**
     * Get the configured programmer
     */
    @Override
    public Programmer getProgrammer() {
        if (progBox == null) {
            return null;
        }
        GlobalProgrammerManager pm = (GlobalProgrammerManager) progBox.getSelectedItem();
        if (pm == null) {
            return null;
        }
        return pm.getGlobalProgrammer();
    }

    /**
     * Are any of the modes selected?
     *
     * @return true
     */
    @Override
    public boolean isSelected() {
        return true;
    }

    public ProgServiceModeComboBox() {
        this(BoxLayout.X_AXIS);
    }

    /**
     * Get the list of Global ProgrammingManagers.
     *
     * @return empty list if none
     */
    protected List<GlobalProgrammerManager> getMgrList() {
        return InstanceManager.getList(jmri.GlobalProgrammerManager.class);
    }

    public ProgServiceModeComboBox(int direction) {
        log.trace("ctor starts");
        modeBox = new JComboBox<ProgrammingMode>();
        modeBox.addActionListener(this);

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // create the programmer display combo box
        progBox = new JComboBox<>();
        Vector<GlobalProgrammerManager> v = new Vector<>();
        for (GlobalProgrammerManager pm : getMgrList()) {
            Programmer globProg = null;
            if (pm != null) {
                globProg = pm.getGlobalProgrammer();
            }
            if (globProg != null) {
                v.add(pm);
                log.debug("ProgSMCombo added programmer {} as item {}",
                        (pm != null ? pm.getClass() : "null"), v.size());
                // listen for changes
                globProg.addPropertyChangeListener(this);
            }
        }

        add(progLabel);
        add(progBox = new JComboBox<>(v));
        // if only one, don't show is confusing to user, so show combo with just 1 choice)
        progBox.setSelectedItem(InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)); // set default
        progBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // new programmer selection
                programmerSelected();
            }
        });

        // install items in GUI
        add(new JLabel(Bundle.getMessage("ProgrammingModeLabel")));

        add(modeBox);

        // and execute the setup for 1st time
        programmerSelected();
    }

    /**
     * Reload the interface with the new programming modes
     */
    void programmerSelected() {
        DefaultComboBoxModel<ProgrammingMode> model = new DefaultComboBoxModel<>();
        Programmer p = getProgrammer();
        if (p != null) {
            for (ProgrammingMode mode : getProgrammer().getSupportedModes()) {
                model.addElement(mode);
            }
        }
        log.trace("programmerSelected sets model");
        modeBox.setModel(model);
        ProgrammingMode mode = (getProgrammer() != null) ? getProgrammer().getMode() : null;
        log.trace("programmerSelected sets mode {}", mode);
        modeBox.setSelectedItem(mode);
    }

    /**
     * Listen to modeBox for mode changes
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // convey change to programmer
        log.debug("Selected mode: {}", modeBox.getSelectedItem());
        if (modeBox.getSelectedItem() != null) {
            getProgrammer().setMode((ProgrammingMode) modeBox.getSelectedItem());
        }
    }

    /**
     * Listen to programmer for mode changes
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("Mode".equals(e.getPropertyName()) && getProgrammer().equals(e.getSource())) {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // if we're not holding a current mode, don't update
                modeBox.setSelectedItem(e.getNewValue());
            }
        }
    }

    // no longer needed, disconnect if still connected
    @Override
    public void dispose() {
    }
    private final static Logger log = LoggerFactory.getLogger(ProgServiceModeComboBox.class);
}
