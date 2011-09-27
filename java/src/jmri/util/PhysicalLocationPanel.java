package jmri.util;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

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
 * @version			$Revision: 18568 $
 */


/* PhysicalLocationPanel
 *
 * Provides a Swing component to show and/or edit a PhysicalLocation
 */

public class PhysicalLocationPanel extends JPanel {

    JTextField xt, yt, zt;
    TitledBorder tb;

    public PhysicalLocationPanel() {
	super();
	initComponents("");
    }

    public PhysicalLocationPanel(String title) {
	super();
	initComponents(title);
    }

    protected void initComponents(String title) {

	//tb = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), title);
	//tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), title);
	tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title);
	tb.setTitlePosition(TitledBorder.DEFAULT_POSITION);
	this.setBorder(tb);

	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	
	xt = new JTextField(20);
	xt.setColumns(20);
	yt = new JTextField(20);
	yt.setColumns(20);
	zt = new JTextField(20);
	zt.setColumns(20);

	this.add(new JLabel("X"));
	this.add(xt);
	this.add(new JLabel("Y"));
	this.add(yt);
	this.add(new JLabel("Z"));
	this.add(zt);

	this.setVisible(true);
	log.debug("initComponents() complete");
    }

    public void setTitle(String t) {
	tb.setTitle(t);
    }

    public String getTitle() {
	return(tb.getTitle());
    }

    public void setValue(PhysicalLocation p) {
	xt.setText("" + p.getX());
	yt.setText("" + p.getY());
	zt.setText("" + p.getZ());
    }

    public void setValue(String s) {
	PhysicalLocation p = PhysicalLocation.parse(s);
	if (p != null) {
	    this.setValue(p);
	}
    }
    
    public PhysicalLocation getValue() {
	return(new PhysicalLocation(Float.parseFloat(xt.getText()),
				    Float.parseFloat(yt.getText()),
				    Float.parseFloat(zt.getText()))
	       );
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhysicalLocationPanel.class.getName());


}