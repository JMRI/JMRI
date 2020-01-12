package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Associate a Roster Entry to a Roster Group
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
public class RosterEntryToGroupAction extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     * proper position in of dialog boxes
     */
    public RosterEntryToGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    JComboBox<String> rosterEntry = new JComboBox<String>();
    RosterGroupComboBox selections;
    Roster roster;
    String lastGroupSelect = null;

    @Override
    public void actionPerformed(ActionEvent event) {

        roster = Roster.getDefault();

        selections = new RosterGroupComboBox();
        selections.setAllEntriesEnabled(false);
        if (lastGroupSelect != null) {
            selections.setSelectedItem(lastGroupSelect);
        }

        rosterEntryUpdate();
        selections.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rosterEntryUpdate();
            }
        });
        int retval = JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("AddEntryToGroupDialog"), Bundle.getMessage("AddEntryToGroupTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonDone"), Bundle.getMessage("ButtonOK"), selections, rosterEntry}, null);
        log.debug("Dialog value " + retval + " selected " + selections.getSelectedIndex() + ":"
                + selections.getSelectedItem() + ", " + rosterEntry.getSelectedIndex() + ":" + rosterEntry.getSelectedItem());
        if (retval != 1) {
            return;
        }

        String selEntry = (String) rosterEntry.getSelectedItem();
        lastGroupSelect = selections.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        String selGroup = Roster.getRosterGroupProperty(selections.getSelectedItem());
        re.putAttribute(selGroup, "yes");
        Roster.getDefault().writeRoster();
        re.updateFile();
        actionPerformed(event);
    }

    void rosterEntryUpdate() {
        if (rosterEntry != null) {
            rosterEntry.removeAllItems();
        }
        String group = Roster.ROSTER_GROUP_PREFIX + selections.getSelectedItem();
        roster.getAllEntries().stream().filter((r) -> (r.getAttribute(group) == null)).forEachOrdered((r) -> {
            rosterEntry.addItem(r.titleString());
        });
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RosterEntryToGroupAction.class);
}
