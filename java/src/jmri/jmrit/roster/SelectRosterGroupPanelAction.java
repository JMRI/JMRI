//SelectRosterGroupAction.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import javax.swing.JLabel;
import jmri.util.swing.JmriPanel;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.beans.PropertyChangeListener;

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
 * @version	$Revision: 18302 $
 */
public class SelectRosterGroupPanelAction extends JmriAbstractAction {

    public SelectRosterGroupPanelAction(String s) {
    	super(s);
    }
    
    public SelectRosterGroupPanelAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public SelectRosterGroupPanelAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    

    public void actionPerformed(ActionEvent event) {
        throw new IllegalArgumentException("Should not be invoked");

    }
    
    public JmriPanel makePanel() {
        
        final Roster roster = Roster.instance();

        // get parent object if there is one
        //Component parent = null;
        //if ( event.getSource() instanceof Component) parent = (Component)event.getSource();
        final JmriPanel container = new JmriPanel();
        container.add(new JLabel("Select Roster Group"));
        
        // create a dialog to select the roster entry
        final JComboBox selections = roster.rosterGroupBox();
        container.add(selections);
        
        selections.addActionListener(comboListener);
        
        roster.addPropertyChangeListener(  new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if ((e.getPropertyName().equals("RosterGroupRemoved")) || 
                        (e.getPropertyName().equals("RosterGroupAdded"))){
                    selections.removeActionListener(comboListener);
                    roster.updateGroupBox(selections);
                    if(selections.getItemCount()<=1)
                        container.setVisible(false);
                    else{
                        container.setVisible(true);
                        selections.addActionListener(comboListener);
                    }
                    
                } 
            }
        });
        
        if(selections.getItemCount()<=1)
            container.setVisible(false);
        else
            container.setVisible(true);
        
        return container;
    }
    
    ActionListener comboListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {    
                    JComboBox combo = (JComboBox)e.getSource();
                    String entry = (String)combo.getSelectedItem();
                    Roster.instance().setRosterGroup(entry);
                }
            };


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SelectRosterGroupAction.class.getName());

}
