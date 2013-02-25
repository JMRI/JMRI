// RemoveRosterEntryToGroupAction.java

package jmri.jmrit.roster.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.event.ActionEvent;
import jmri.util.JmriJFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;

import javax.swing.JPanel;
import java.awt.event.ActionListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Remove a locomotive from a roster grouping.
 *
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
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version	$Revision$
 */
public class RemoveRosterEntryToGroupAction extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public RemoveRosterEntryToGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    String curRosterGroup;
    JmriJFrame frame = null;
    JComboBox typeBox;
    JLabel jLabel = new JLabel("Select the Group");
    RosterEntrySelectorPanel rosterBox;
    JButton okButton = new JButton("Remove");
    JButton cancelButton = new JButton("Exit");
    
    public void actionPerformed(ActionEvent event) {
        frame = new JmriJFrame("Remove Loco from Group");
        rosterBox = new RosterEntrySelectorPanel();
        rosterBox.getRosterGroupComboBox().setAllEntriesEnabled(false);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        JPanel p;
        p = new JPanel(); p.setLayout(new FlowLayout());
        p.add(jLabel);
        p.add(rosterBox);
        frame.getContentPane().add(p);
        
        p = new JPanel(); p.setLayout(new FlowLayout());
        p.add(okButton);
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed();
                }
            });
        p.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        frame.getContentPane().add(p);
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * Can provide some mechanism to prompt for user for one
     * last chance to change his/her mind
     * @return true if user says to continue
     */
    boolean userOK(String entry) {
        return ( JOptionPane.YES_OPTION ==
                 JOptionPane.showConfirmDialog(_who,
                                               "Delete roster group "+entry ,
                                               "Delete roster group "+entry+"?", JOptionPane.YES_NO_OPTION));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(RemoveRosterEntryToGroupAction.class.getName());

    public void okPressed() {
        String group = rosterBox.getSelectedRosterGroup();
        log.info("Selected " + group);
        if (group != null && !group.equals(Roster.ALLENTRIES)) {
            if (rosterBox.getSelectedRosterEntries().length != 0) {
                RosterEntry re = rosterBox.getSelectedRosterEntries()[0];
                log.info("Preparing to remove " + re.getId() + " from " + group);
                re.deleteAttribute(Roster.getRosterGroupProperty(group));
                re.updateFile();
                Roster.writeRosterFile();
                rosterBox.getRosterEntryComboBox().update();
            }
        }
        frame.pack();
    
    }
    
    public void dispose(){
        frame.dispose();
    
    }
    
}
