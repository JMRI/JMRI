//SelectRosterGroupAction.java

package jmri.jmrit.roster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import javax.swing.JLabel;
import jmri.util.swing.JmriPanel;

import javax.swing.JComboBox;
import java.beans.PropertyChangeListener;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
    
    JComboBox selections;
    JmriPanel container;
    boolean init = false;
    
    public JmriPanel makePanel() {
        if(!init){
            Roster roster = Roster.instance();

            container = new JmriPanel();
            container.add(new JLabel("Select Roster Group"));
            
            // create a dialog to select the roster entry
            selections = roster.rosterGroupBox();
            container.add(selections);
            
            comboListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {    
                    JComboBox combo = (JComboBox)e.getSource();
                    String entry = (String)combo.getSelectedItem();
                    Roster.instance().setRosterGroup(entry);
                }
            };
            
            selections.addActionListener(comboListener);
            
            roster.addPropertyChangeListener(  new PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if ((e.getPropertyName().equals("RosterGroupRemoved")) || 
                            (e.getPropertyName().equals("RosterGroupAdded")) ||
                               (e.getPropertyName().equals("ActiveRosterGroup"))){
                        updateComboBox();
                    }
                }
            });
            
            if(selections.getItemCount()<=1)
                container.setVisible(false);
            else
                container.setVisible(true);
            init = true;
        }
        return container;
    }

    JList groupsList;

    public JmriPanel makeListPanel() {
        if (!init) {
            Roster roster = Roster.instance();

            groupsList = roster.rosterGroupList();
            container = new JmriPanel();
            container.add(groupsList);

            listListener = new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    JList list = (JList)e.getSource();
                    String entry = (String)list.getSelectedValue();
                    Roster.instance().setRosterGroup(entry);
                }
            };

            groupsList.addListSelectionListener(listListener);

            roster.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if ((e.getPropertyName().equals("RosterGroupRemoved")) ||
                            (e.getPropertyName().equals("RosterGroupAdded")) ||
                            (e.getPropertyName().equals("ActiveRosterGroup"))) {
                        updateGroupList();
                    }
                }
            });
            init = true;
        }
        return container;
    }

    /**
     * Allow direct manipulation of the JList contained in the JmriPanel returned
     * by makeListPanel
     * 
     * @return JList
     */
    public JList getList() {
        return groupsList;
    }

    void updateComboBox(){
        selections.removeActionListener(comboListener);
        Roster.instance().updateGroupBox(selections);
        if(selections.getItemCount()<=1)
            container.setVisible(false);
        else{
            container.setVisible(true);
            selections.addActionListener(comboListener);
        }
    }

    void updateGroupList() {
        groupsList.removeListSelectionListener(listListener);
        Roster.instance().updateGroupList(groupsList);
        groupsList.addListSelectionListener(listListener);
    }

    ActionListener comboListener;
    ListSelectionListener listListener;

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SelectRosterGroupAction.class.getName());

}
