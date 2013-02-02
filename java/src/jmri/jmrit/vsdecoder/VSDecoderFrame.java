package jmri.jmrit.vsdecoder;

/*
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
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import org.apache.log4j.Logger;
import jmri.util.JmriJFrame;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.Dimension;
import jmri.jmrit.vsdecoder.swing.VSDPreferencesAction;

@SuppressWarnings("serial")
class VSDecoderFrame extends JmriJFrame {

    private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    VSDecoderPane decpane;

    private List<JMenu> menuList;

    public VSDecoderFrame() {
	super();
	initGUI();
    }

    public void initGUI() {
	log.debug("initGUI");
	this.setTitle("VSDecoder - (no loco)");
	this.buildMenu();
	this.setLayout(new BorderLayout());
	decpane = new VSDecoderPane(this);
	decpane.initComponents();
	this.getContentPane().add(decpane, BorderLayout.CENTER);
	this.setSize(new Dimension(decpane.getPreferredSize().width + 20, decpane.getPreferredSize().height + 20));
	log.debug("pane size + " + decpane.getPreferredSize());
	this.setVisible(true);

	//this.pack();
	log.debug("done...");
    }

    private void buildMenu() {
	JMenu fileMenu = new JMenu(vsdBundle.getString("VSDecoderFileMenu"));

        fileMenu.add(new LoadVSDFileAction(vsdBundle.getString("VSDecoderFileMenuLoadVSDFile" )));
        fileMenu.add(new StoreXmlVSDecoderAction(vsdBundle.getString("VSDecoderFileMenuSaveProfile" )));
        fileMenu.add(new LoadXmlVSDecoderAction(vsdBundle.getString("VSDecoderFileMenuLoadProfile")));
	fileMenu.add(new VSDPreferencesAction(vsdBundle.getString("VSDecoderFileMenuPreferences")));

	fileMenu.getItem(1).setEnabled(false); // disable XML store
	fileMenu.getItem(2).setEnabled(false); // disable XML load

	menuList = new ArrayList<JMenu>(2);

	menuList.add(fileMenu);

	this.setJMenuBar(new JMenuBar());
	this.getJMenuBar().add(fileMenu);
	this.addHelpMenu("package.jmri.jmrit.vsdecoder.VSDecoderFrame", true);
	
    }

    public void windowClosing(java.awt.event.WindowEvent e) {
	// Call the superclass function
        super.windowClosing(e);

	log.debug("VSDecoderFrame windowClosing() called... " + e.toString());

	log.debug("Calling decpane.windowClosing() directly " + e.toString());
	decpane.windowClosing(e);
    }


    public List<JMenu> getMenus() { return menuList; }
    
    static Logger log = Logger.getLogger(VSDecoderFrame.class.getName());
}
