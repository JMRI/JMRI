// DeleteRosterGroupAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Remove roster group.
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

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();

        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();

        // create a dialog to select the roster entry
        JComboBox selections = roster.rosterGroupBox();
        selections.removeItem(Roster.ALLENTRIES);
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Select one roster Group\nThis does not delete the roster entries within a group", "Delete roster group entry",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":"
                  +selections.getSelectedItem());
        if (retval != 1) return;
        String entry = (String) selections.getSelectedItem();
        if(entry == null || entry.equals(Roster.ALLENTRIES)){
            return;
        }
        // prompt for one last chance
        if (!userOK(entry)) return;

        // delete the roster grouping
        roster.delRosterGroupList(entry);
        Roster.writeRosterFile();

    }

    /**
     * Can provide some mechanism to prompt for user for one
     * last chance to change his/her mind
     * @return true if user says to continue
     */
    boolean userOK(String entry) {
        return ( JOptionPane.YES_OPTION ==
                 JOptionPane.showConfirmDialog(_who,
                                               "Delete roster group "+entry ,
                                               "Delete roster group "+entry+"?", JOptionPane.YES_NO_OPTION));
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeleteRosterGroupAction.class.getName());

}
