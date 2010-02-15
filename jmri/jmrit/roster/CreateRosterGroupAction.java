// CreateRosterGroupAction.java

package jmri.jmrit.roster;

//import jmri.jmrit.XmlFile;
import java.awt.Component;
import java.awt.event.ActionEvent;
// import java.io.File;

import javax.swing.AbstractAction;
//import javax.swing.Action;
import javax.swing.JTextField;
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
 * @version	$Revision: 1.3 $
 */
public class CreateRosterGroupAction extends AbstractAction {

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

    public void actionPerformed(ActionEvent event) {

        Roster roster = Roster.instance();

        JTextField _newGroup = new JTextField(20);
        int retval = JOptionPane.showOptionDialog(_who,
                                                  "Create new roster Group", "Create new roster group",
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[]{"Cancel", "OK", _newGroup}, null );

        if (retval != 1) return;
        String entry = _newGroup.getText();
        if(entry.equals("Global")){
            return;
        }

        roster.addRosterGroupList(entry);
        Roster.writeRosterFile();
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateRosterGroupAction.class.getName());
}
