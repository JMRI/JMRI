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
 * Remove roster group.
 * <p>
 * If performAction(event) is being called in a context where the name of the
 * group to be removed is already known, call setParameter("group", groupName)
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
 * @version	$Revision$
  */
public class DeleteRosterGroupAction extends JmriAbstractAction {

    public DeleteRosterGroupAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public DeleteRosterGroupAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public DeleteRosterGroupAction(String s, Component who) {
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
                    "<html><b>Delete roster group</b><br>Roster entries in the group are not deleted.</html>",
                    "Delete Roster Group",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    Roster.instance().getRosterGroupList().toArray(),
                    Roster.getRosterGroup());
        }
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return;
        }
        // prompt for one last chance
        if (!userOK(group)) return;

        // delete the roster grouping
        Roster.instance().delRosterGroupList(group);
        Roster.writeRosterFile();

    }

    /**
     * Can provide some mechanism to prompt for user for one
     * last chance to change his/her mind
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeleteRosterGroupAction.class.getName());

}
