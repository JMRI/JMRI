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
import java.awt.*;
import jmri.util.swing.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class VSDSoundsPanel extends JmriPanel {

    String decoder_id;
    VSDecoderPane main_pane;

    public VSDSoundsPanel() {
	super();
    }

    public VSDSoundsPanel(String dec, VSDecoderPane dad) {
	super();
	decoder_id = dec;
	main_pane = dad;
	initComponents();

    }

    public void init() {}

    public void initContext(Object context) {
	initComponents();
    }

    public void initComponents() {

	this.setLayout(new GridLayout(0, 3));

	if (main_pane.getDecoder() == null) {
	    log.debug("No decoder!");
	    return;
	}
	
	ArrayList<SoundEvent> elist = new ArrayList<SoundEvent>(main_pane.getDecoder().getEventList());
	for (SoundEvent e : elist) {
	    if (e.getButton() != null)
		log.debug("adding button " + e.getButton().toString());
		this.add(e.getButton());
	}
    }

    private static final Logger log = Logger.getLogger(VSDSoundsPanel.class.getName());

}
