package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove a locomotive from a roster grouping.
 *
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
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class RemoveRosterEntryToGroupAction extends AbstractAction {


    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public RemoveRosterEntryToGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    String curRosterGroup;
    JmriJFrame frame = null;
//    JComboBox typeBox;
    JLabel jLabel = new JLabel(Bundle.getMessage("SelectTheGroup"));
    RosterEntrySelectorPanel rosterBox;
    JButton okButton = new JButton(Bundle.getMessage("ButtonRemove"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonDone"));

    @Override
    public void actionPerformed(ActionEvent event) {
        frame = new JmriJFrame(Bundle.getMessage("DeleteFromGroup"));
        rosterBox = new RosterEntrySelectorPanel();
        rosterBox.getRosterGroupComboBox().setAllEntriesEnabled(false);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(jLabel);
        p.add(rosterBox);
        frame.getContentPane().add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        p.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        frame.getContentPane().add(p);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean userOK(String entry, String group) {
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(_who,
                        Bundle.getMessage("DeleteEntryFromGroupDialog", entry, group),
                        Bundle.getMessage("DeleteEntryFromGroupTitle", entry, group),
                        JOptionPane.YES_NO_OPTION));
    }

    public void okPressed() {
        String group = rosterBox.getSelectedRosterGroup();
        log.info("Selected " + group);
        if (group != null && !group.equals(Roster.ALLENTRIES)) {
            if (rosterBox.getSelectedRosterEntries().length != 0) {
                RosterEntry re = rosterBox.getSelectedRosterEntries()[0];
                log.info("Preparing to remove " + re.getId() + " from " + group);
                if (userOK(re.getId(), group)) {
                    re.deleteAttribute(Roster.getRosterGroupProperty(group));
                    re.updateFile();
                    Roster.getDefault().writeRoster();
                    rosterBox.getRosterEntryComboBox().update();
                }
            }
        }
        frame.pack();
    }

    public void dispose() {
        frame.dispose();

    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RemoveRosterEntryToGroupAction.class);

}
