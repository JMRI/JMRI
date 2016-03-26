// DeleteRosterGroupAction.java
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
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision$
 */
public class DeleteRosterGroupAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5645990386314165982L;

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
     * @param event
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
                    "<html><b>Delete roster group</b><br>Roster entries in the group are not deleted.</html>",
                    "Delete Roster Group",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.instance().getRosterGroupList().toArray(),
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
        Roster.instance().delRosterGroupList(group);
        Roster.writeRosterFile();
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean userOK(String entry) {
        String[] titles = {"Delete", "Cancel"};
        // TODO: I18N
        // TODO: replace "Are you sure..." string with JPanel containing string
        //       and checkbox silencing this message in the future
        return (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(_who,
                "Are you sure you want to delete roster group \"" + entry + "\"?",
                "Delete Roster Group \"" + entry + "\"",
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
