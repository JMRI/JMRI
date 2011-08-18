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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jmri.jmrit.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.util.swing.*;

import java.io.File;
import jmri.jmrit.XmlFile;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;


/**
 * Virtual Sound Decoder for playing sounds off of LocoNet messages.
 * Based on the LocoMon tool by Bob Jacobsen
 * @author	   Mark Underwood   Copyright (C) 2011
 * @version   $Revision$
 */
public class VSDecoderPane extends JmriPanel {

    //private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    VSDecoder decoder;
    VSDecoderManager decoder_mgr;

    final static String BASICPANEL = "Basic";
    final static String COMBOPANEL = "Sounds";
    final static String OPTIONPANEL = "Options";

    // GUI Components

    private VSDecoderFrame parent;

    private JTabbedPane tabbedPane;
    private VSDConfigPanel configPanel;
    private JPanel soundsPanel;
    private JPanel optionPanel;

    private static String VSDecoderFileLocation = null;

    //private List<JMenu> menuList;
    

    public VSDecoderPane(VSDecoderFrame p) {
        super();
	parent = p;
	decoder_mgr = VSDecoderManager.instance();
    }

    /*
    public String getHelpTarget() { return "package.jmri.jmrix.vsdecoder.VSDecoderPane"; }
    public String getTitle() { 
        return LocoNetBundle.bundle().getString("MenuItemVirtualSoundDecoder");
    }
    */

    public static String getDefaultVSDecoderFolder() {
        if (VSDecoderFileLocation == null)
            return XmlFile.prefsDir()+"vsdecoder"+File.separator ;
        return VSDecoderFileLocation;
    }



    public void init() {}
    
    public void initContext(Object context) {
	//initComponents();
	/*
        if (context instanceof LocoNetSystemConnectionMemo ) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
	*/
    }

    public void initComponents() {
	log.debug("initComponents()");
	//buildMenu();

	setLayout(new BorderLayout(10, 10));
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	// Add the tabbed pane to the VSDecoderPane.  The tabbedPane will contain all the other panes.
	tabbedPane = new JTabbedPane();
	add(tabbedPane);

	//-------------------------------------------------------------------
	// configPanel
	// The configPanel holds the stuff for addressing and configuration.
        configPanel = new VSDConfigPanel(decoder, this);

	tabbedPane.addTab("Config", configPanel);

	//-------------------------------------------------------------------
	// soundsPanel
	// The optionPanel holds controls for selecting sound options.
	optionPanel = new VSDOptionPanel(decoder, this);

	tabbedPane.addTab("Options", optionPanel);
	

	//-------------------------------------------------------------------
	// soundsPanel
	// The soundsPanel holds buttons for specific sounds.
        soundsPanel = new VSDSoundsPanel(decoder);
	tabbedPane.addTab("Sounds", soundsPanel);
    }

    public void setDecoder(VSDecoder dec) {
	if (dec != null) {
	    // Store the new decoder
	    decoder = dec;
	    // Update the sounds pane
	    tabbedPane.remove(soundsPanel);
	    soundsPanel = new VSDSoundsPanel(decoder);
	    tabbedPane.addTab("Sounds", soundsPanel);
	    tabbedPane.revalidate();
	    tabbedPane.repaint();
	}
	
    }

    public void setTitle(DccLocoAddress a) {
	if (a != null) {
	    parent.setTitle("VSDecoder - " + a.toString());
	}
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPane.class.getName());
}
