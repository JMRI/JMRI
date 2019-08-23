package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import jmri.beans.Beans;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Duplicate roster group.
 * <p>
 * This action prevents a user from creating a new roster group with the same
 * name as an existing roster group.
 * <p>
 * If performAction(event) is being called in a context where the name of the
 * group to be duplicated is already known, call setContext(groupName) prior to
 * calling performAction(event) to bypass the group selection dialog.
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
public class CopyRosterGroupAction extends JmriAbstractAction {

    public CopyRosterGroupAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public CopyRosterGroupAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public CopyRosterGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }
    Component _who;

    /**
     * Call setParameter("group", oldName) prior to calling
     * actionPerformed(event) to bypass the roster group selection dialog if the
     * name of the group to be copied is already known and is not the
     * selectedRosterGroup property of the WindowInterface.
     *
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String group = null;
        // only query wi for group if group was not set using setParameter
        // prior to call
        if (Beans.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) Beans.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        // null might be valid output from getting the selectedRosterGroup,
        // so we have to check for null again.
        if (group == null) {
            group = (String) JOptionPane.showInputDialog(_who,
                    Bundle.getMessage("DuplicateRosterGroupDialog"),
                    Bundle.getMessage("DuplicateRosterGroupTitle", ""),
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.getDefault().getRosterGroupList().toArray(),
                    null);
        }
        // don't duplicate the null and ALLENTRIES groups (they are the entire roster)
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return;
        }

        String entry = (String) JOptionPane.showInputDialog(_who,
                Bundle.getMessage("DuplicateRosterGroupNewName"),
                Bundle.getMessage("DuplicateRosterGroupTitle",group),
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                null);
        if (entry != null) {
            entry = entry.trim(); // remove white space around name, also prevent "Space" as a Group name
        }
        if (entry == null || entry.length() == 0 || entry.equals(Roster.ALLENTRIES)) {
            return;
        } else if (Roster.getDefault().getRosterGroupList().contains(entry)) {
            JOptionPane.showMessageDialog(_who,
                    Bundle.getMessage("DuplicateRosterGroupSameName", entry),
                    Bundle.getMessage("DuplicateRosterGroupTitle", group),
                    JOptionPane.ERROR_MESSAGE);
        }

        // rename the roster grouping
        Roster.getDefault().copyRosterGroupList(group, entry);
        Roster.getDefault().writeRoster();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
