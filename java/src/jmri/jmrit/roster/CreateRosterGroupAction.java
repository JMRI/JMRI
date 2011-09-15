// CreateRosterGroupAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.JOptionPane;

/**
 * Create a roster group.
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
public class CreateRosterGroupAction extends JmriAbstractAction {

    public CreateRosterGroupAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public CreateRosterGroupAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public CreateRosterGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    String[] IDs;

    @Override
    public void actionPerformed(ActionEvent event) {

        String entry = (String)JOptionPane.showInputDialog(_who,
                                     "<html><b>Create new roster group</b></html>",
                                     "New Roster Group",
                                     JOptionPane.INFORMATION_MESSAGE,
                                     null, // icon
                                     null, // initial values
                                     null);// preselected initial value
        if(entry == null || entry.equals(Roster.ALLENTRIES)){
            return;
        }
        if (IDs != null) {
            for (String ID : IDs) {
                if (!"".equals(ID)) {
                    RosterEntry re = Roster.instance().entryFromTitle(ID);
                    if (re == null) {
                        log.warn("Attempted to create RosterEntry from invalid title: " + ID);
                    } else {
                        log.debug("Adding RosterEntry " + ID + " to new group " + entry);
                        re.putAttribute(Roster.instance().getRosterGroupPrefix() + entry, "yes");
                        re.updateFile();
                    }
                }
            }
        }
        Roster.instance().addRosterGroupList(entry);
        Roster.writeRosterFile();
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    /**
     * Set a String parameter
     * <p>
     * This method accepts the following key, with the following value:
     * <dl>
     * <dt>RosterEntryIDs</dt>
     * <dd>A string of RosterEntry IDs separated by # characters.
     * </dl>
     * @param key
     * @param value
     */
    @Override
    public void setParameter(String key, String value) {
        if (key.equals("RosterEntryIDs")) {
            IDs = value.split("#");
        }
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateRosterGroupAction.class.getName());
}
