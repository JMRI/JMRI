// RosterEntryToGroupAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Associate a Roster Entry to a Roster Group
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
public class RosterEntryToGroupAction extends AbstractAction {

    /**
     * @param s Name of this action, e.g. in menus
     * @param who Component that action is associated with, used
     *              to ensure proper position in of dialog boxes
     */
    public RosterEntryToGroupAction(String s, Component who) {
        super(s);
        _who = who;
    }

    Component _who;
    JComboBox rosterEntry = new JComboBox();
    JComboBox selections;
    Roster roster;
    String lastGroupSelect = null;
    
    public void actionPerformed(ActionEvent event) {
        
        roster = Roster.instance();
        String curRosterGroup = Roster.getRosterGroup();
        Roster.setRosterGroup(null);

        selections = roster.rosterGroupBox();
        if (lastGroupSelect!=null){
            selections.setSelectedItem(lastGroupSelect);
        }

        rosterEntryUpdate();
        selections.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rosterEntryUpdate();
            }
        });
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Select the roster entry and the group to assign it to\nA Roster Entry can belong to multiple groups. " , "Associate Roster Entry with Group",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", selections, rosterEntry}, null );
        log.debug("Dialog value "+retval+" selected "+selections.getSelectedIndex()+":"
                  +selections.getSelectedItem()+ ", " + rosterEntry.getSelectedIndex()+":" + rosterEntry.getSelectedItem());
        if (retval != 1) {
            Roster.setRosterGroup(curRosterGroup);
            return;
        }
        
        String selEntry = (String) rosterEntry.getSelectedItem();
        lastGroupSelect = (String) selections.getSelectedItem();
        RosterEntry re = roster.entryFromTitle(selEntry);
        String selGroup = roster.getRosterGroupPrefix()+(String) selections.getSelectedItem();
        re.putAttribute(selGroup, "yes");
        Roster.writeRosterFile();
        re.updateFile();
        Roster.setRosterGroup(curRosterGroup);
        actionPerformed(event);
    }

    void rosterEntryUpdate(){
        if (rosterEntry!=null)
            rosterEntry.removeAllItems();
        String group = roster.getRosterGroupPrefix()+selections.getSelectedItem();
        for(int i=0; i<roster.numEntries(); i++){
            RosterEntry r = roster.getEntry(i);
            if(r.getAttribute(group)==null)
                rosterEntry.addItem(r.titleString());
        }
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterEntryToGroupAction.class.getName());
}
