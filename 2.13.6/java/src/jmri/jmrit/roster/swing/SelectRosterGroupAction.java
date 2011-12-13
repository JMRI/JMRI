// SelectRosterGroupAction.java

package jmri.jmrit.roster.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.JOptionPane;
import jmri.jmrit.roster.Roster;

/**
 * Selects a Roster group to work with
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
public class SelectRosterGroupAction extends JmriAbstractAction {

    public SelectRosterGroupAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public SelectRosterGroupAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public SelectRosterGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;

    public void actionPerformed(ActionEvent event) {

        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        ArrayList<String> groups = Roster.instance().getRosterGroupList();
        groups.add(0, Roster.ALLENTRIES);
        String currentGroup = Roster.getRosterGroup();
        if (currentGroup == null) {
            currentGroup = Roster.ALLENTRIES;
        }
        String entry = (String)JOptionPane.showInputDialog(_who,
                "<html><b>Select active roster group</b><br>You can work with locomotives in the active group.</html>",
                "Active Roster Group",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                groups.toArray(),
                currentGroup);
        if (entry == null) {
            return;
        }

        Roster.instance().setRosterGroup(entry);
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SelectRosterGroupAction.class.getName());

}
