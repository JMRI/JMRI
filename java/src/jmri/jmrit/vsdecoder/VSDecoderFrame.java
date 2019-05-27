package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrit.vsdecoder.swing.VSDPreferencesAction;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VSDecoderFrame extends JmriJFrame {

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
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

        fileMenu.add(new LoadVSDFileAction(Bundle.getMessage("VSDecoderFileMenuLoadVSDFile")));
        fileMenu.add(new StoreXmlVSDecoderAction(Bundle.getMessage("VSDecoderFileMenuSaveProfile")));
        fileMenu.add(new LoadXmlVSDecoderAction(Bundle.getMessage("VSDecoderFileMenuLoadProfile")));
        fileMenu.addSeparator();
        fileMenu.add(new VSDPreferencesAction(Bundle.getMessage("VSDecoderFileMenuPreferences")));

        fileMenu.getItem(1).setEnabled(false); // disable XML store
        fileMenu.getItem(2).setEnabled(false); // disable XML load

        menuList = new ArrayList<JMenu>(2);

        menuList.add(fileMenu);

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
        this.addHelpMenu("package.jmri.jmrit.vsdecoder.VSDecoderFrame", true);

    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        // Call the superclass function
        super.windowClosing(e);

        log.debug("VSDecoderFrame windowClosing() called... " + e.toString());

        log.debug("Calling decpane.windowClosing() directly " + e.toString());
        decpane.windowClosing(e);
    }

    public List<JMenu> getMenus() {
        return menuList;
    }

    private final static Logger log = LoggerFactory.getLogger(VSDecoderFrame.class);
}
