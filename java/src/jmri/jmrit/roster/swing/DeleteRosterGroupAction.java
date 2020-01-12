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
 * Remove roster group.
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
public class DeleteRosterGroupAction extends JmriAbstractAction {

    public DeleteRosterGroupAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DeleteRosterGroupAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public DeleteRosterGroupAction(String s, Component who) {
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
        if (Beans.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) Beans.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        // null might be valid output from getting the selectedRosterGroup,
        // so we have to check for null again.
        if (group == null) {
            group = (String) JOptionPane.showInputDialog(_who,
                    Bundle.getMessage("DeleteRosterGroupDialog"),
                    Bundle.getMessage("DeleteRosterGroupTitle", ""),
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.getDefault().getRosterGroupList().toArray(),
                    null);
        }
        // can't delete the roster itself (ALLENTRIES and null represent the roster)
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return;
        }
        // prompt for one last chance
        if (!userOK(group)) {
            return;
        }

        // delete the roster grouping
        Roster.getDefault().delRosterGroupList(group);
        Roster.getDefault().writeRoster();
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean userOK(String entry) {
        String[] titles = {Bundle.getMessage("ButtonDelete"), Bundle.getMessage("ButtonCancel")};
        // TODO: replace "Are you sure..." string with JPanel containing string
        //       and checkbox silencing this message in the future
        return (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("DeleteRosterGroupSure", entry),
                Bundle.getMessage("DeleteRosterGroupTitle", entry),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                titles,
                null));
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
