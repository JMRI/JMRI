// DeleteRosterGroupAction.java
package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import jmri.beans.Beans;
import jmri.jmrit.roster.Roster;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Rename a roster group.
 * <p>
 * This action prevents a user from renaming a roster group to same name as an
 * existing roster group.
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
 * @author Randall Wood Copyright (C) 2011
 * @version	$Revision$
 * @see Roster
 */
public class RenameRosterGroupAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1370317330367764168L;

    public RenameRosterGroupAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RenameRosterGroupAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s   Name of this action, e.g. in menus
     * @param who Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public RenameRosterGroupAction(String s, Component who) {
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
        if (Beans.hasProperty(wi, "selectedRosterGroup")) {
            group = (String) Beans.getProperty(wi, "selectedRosterGroup");
        }
        // null might be valid output from getting the selectedRosterGroup,
        // so we have to check for null again.
        if (group == null) {
            group = (String) JOptionPane.showInputDialog(_who,
                    "<html><b>Rename roster group</b><br>Select the roster group to rename.</html>",
                    "Rename Roster Group",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.instance().getRosterGroupList().toArray(),
                    null);
        }
        // can't rename the groups that represent the entire roster 
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return;
        }

        String entry = (String) JOptionPane.showInputDialog(_who,
                "<html><b>Rename roster group</b><br>Enter the new name for roster group \"" + group + "\".</html>",
                "Rename Roster Group " + group,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                null);
        if (entry == null || entry.equals(Roster.ALLENTRIES)) {
            return;
        } else if (Roster.instance().getRosterGroupList().contains(entry)) {
            JOptionPane.showMessageDialog(_who,
                    "<html><b>Unable to rename roster group</b><br>The roster group named \"" + entry + "\" already exists.",
                    "Rename Roster Group " + group,
                    JOptionPane.ERROR_MESSAGE);
        }

        // rename the roster grouping
        Roster.instance().renameRosterGroupList(group, entry);
        Roster.writeRosterFile();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
