// DeleteRosterGroupAction.java
package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.JOptionPane;
import jmri.jmrit.roster.Roster;

/**
 * Rename a roster group.
 * <p>
 * This action prevents a user from renaming a roster group to same name as an
 * existing roster group.
 * <p>
 * If performAction(event) is being called in a context where the name of the
 * group to be renamed is already known, call setParameter("group", groupName)
 * prior to calling performAction(event) to bypass the group selection dialog.
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
 * @author	Kevin Dickerson  Copyright (C) 2009
 * @author      Randall Wood     Copyright (C) 2011
 * @version	$Revision$
 * @see         Roster
 */
public class RenameRosterGroupAction extends JmriAbstractAction {

    public RenameRosterGroupAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RenameRosterGroupAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public RenameRosterGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }
    Component _who;
    String group;

    /**
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (group == null) {
            group = (String) JOptionPane.showInputDialog(_who,
                    "<html><b>Rename roster group</b><br>Select the roster group to rename.</html>",
                    "Rename Roster Group",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.instance().getRosterGroupList().toArray(),
                    Roster.getRosterGroup());
        }
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
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    @Override
    public void setParameter(String key, String value) {
        if (key.equals("group")) {
            group = value;
        }
    }
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RenameRosterGroupAction.class.getName());
}
