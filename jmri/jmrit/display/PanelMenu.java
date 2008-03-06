// PanelMenu.java

package jmri.jmrit.display;

import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JSeparator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.ArrayList;

/**
 * Create the default "Panels" menu for use in a menubar.
 *
 * Also manages the Show Panel menu for both PanelEditor panels and LayoutEditor panels.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004
 * @author  Dave Duchamp   Copyright 2007
 * @version     $Revision: 1.9 $
 */
public class PanelMenu extends JMenu {
    public PanelMenu() {

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

        this.setText(rb.getString("MenuPanels"));

		add(new jmri.jmrit.display.NewPanelAction(rb.getString("MenuItemNew")));
        add(new jmri.configurexml.LoadXmlUserAction(rb.getString("MenuItemLoad")));
        add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        add(new JSeparator());
		panelsSubMenu = new JMenu(rb.getString("MenuShowPanel"));
		add(panelsSubMenu);
        add(new JSeparator());
        add(new jmri.jmrit.jython.RunJythonScript(rb.getString("MenuItemScript")));
        add(new jmri.jmrit.automat.monitor.AutomatTableAction(rb.getString("MenuItemMonitor")));
        add(new jmri.jmrit.jython.JythonWindow(rb.getString("MenuItemScriptLog")));
        add(new jmri.jmrit.jython.InputWindowAction(rb.getString("MenuItemScriptInput")));
		thisMenu = this;
    }
	
	// operational variables
	private JMenu panelsSubMenu	= null;
	static private PanelMenu thisMenu = null;
    private ArrayList panelsList = new ArrayList();  
		
	/** 
	 * Provide method to reference this panel menu
	 */	
	static public PanelMenu instance() {
		if (thisMenu==null) thisMenu = new PanelMenu();
		return thisMenu;
	}
	
	/**
	 * Utility routine for getting the number of panels in the Panels sub menu
	 */
	public int getNumberOfPanels() {return panelsList.size();}
	
	/**
	 * Delete a panel from Show Panel sub menu
	 */
	public void deletePanel (Object panel) {
		if (panelsList.size()==0) return;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			Object o = panelsList.get(i);
			if (o == panel) {
				found = true;
				panelsList.remove(i);
				panelsSubMenu.remove(i);
			}
		}
	}
	
	/**
	 * Add LayoutEditor type panel to Show Panels sub menu
	 */
    public void addLayoutEditorPanel(final LayoutEditor panel) {
		panelsList.add((Object)panel);
        ActionListener a = new ActionListener() {
				final LayoutEditor thisPanel = panel;
				public void actionPerformed(ActionEvent e) {
					panel.setVisible(true);
					panel.repaint();
					updateLayoutEditorPanel(panel);
				}
			};
        JCheckBoxMenuItem r = new JCheckBoxMenuItem(panel.getTitle());
        r.addActionListener(a);
        if (panel.isVisible()) r.setSelected(true);
        else r.setSelected(false);
        panelsSubMenu.add(r);
    }
	
	/**
	 * Update LayoutEditor type panel in Show Panels sub menu
	 */
	public void updateLayoutEditorPanel (LayoutEditor panel) {
		if (panelsList.size()==0) return;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			Object o = panelsList.get(i);
			if (o == (Object)panel) {
				found = true;
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
				if (panel.isVisible()) r.setSelected(false);
				else r.setSelected(true);
			}
		}
	}	
	
	/**
	 * Rename LayoutEditor type panel in Show Panels sub menu
	 */
	public void renameLayoutEditorPanel (LayoutEditor panel) {
		if (panelsList.size()==0) return;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			Object o = panelsList.get(i);
			if (o == (Object)panel) {
				found = true;
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
				r.setText(panel.getTitle());
			}
		}
	}
	
	/**
	 * Add PanelEditor type panel to Show Panels sub menu
	 */
    public void addPanelEditorPanel(final PanelEditor panel) {
		panelsList.add((Object)panel);
        ActionListener a = new ActionListener() {
				final PanelEditor thisPanel = panel;
				public void actionPerformed(ActionEvent e) {
					panel.getFrame().setVisible(true);
//					panel.repaint();
					updatePanelEditorPanel(panel);
				}
			};
		
        JCheckBoxMenuItem r = new JCheckBoxMenuItem(panel.getFrame().getTitle());
        r.addActionListener(a);
        if (panel.getFrame().isVisible()) r.setSelected(true);
        else r.setSelected(false);
        panelsSubMenu.add(r);
    }
	
	/**
	 * Update PanelEditor type panel in Show Panels sub menu
	 */
	public void updatePanelEditorPanel (PanelEditor panel) {
		if (panelsList.size()==0) return;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			Object o = panelsList.get(i);
			if (o == (Object)panel) {
				found = true;
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
				if (panel.getFrame().isVisible()) r.setSelected(false);
				else r.setSelected(true);
			}
		}
	}	
	
	/**
	 * Rename PanelEditor type panel in Show Panels sub menu
	 */
	public void renamePanelEditorPanel (PanelEditor panel) {
		if (panelsList.size()==0) return;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			Object o = panelsList.get(i);
			if (o == (Object)panel) {
				found = true;
				JCheckBoxMenuItem r = (JCheckBoxMenuItem)panelsSubMenu.getItem(i);
				r.setText(panel.getFrame().getTitle());
			}
		}
	}
	/**
	 * Determine if named panel already exists
	 * returns true if named panel already loaded
	 */
	public boolean isPanelNameUsed (String name) {
		if (panelsList.size()==0) return false;
		boolean found = false;
		for (int i = 0; (i<panelsList.size()) && !found ; i++) {
			try{
				PanelEditor pe = (PanelEditor)panelsList.get(i);
				if (pe.getFrame().getTitle().equals(name)) {
					found = true;
				}
			} catch(Exception e){
			}
			try{
				LayoutEditor le = (LayoutEditor)panelsList.get(i);
				if (le.getTitle().equals(name)) {
					found = true;
				}
			} catch(Exception e){
			}
		}
		return found;
	}
	
}


