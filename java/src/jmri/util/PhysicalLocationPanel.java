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
    JSpinner xs, ys, zs;
    SpinnerNumberModel spinnerModel;

    static final Float min_spin = -1000.0f;
    static final Float max_spin = 1000.0f;
    static final Float spin_value = 0.0f;
    static final Float spin_inc = 0.1f;

    public PhysicalLocationPanel() {
	super();
	initComponents("");
    }

    public PhysicalLocationPanel(String title) {
	super();
	initComponents(title);
    }

    private GridBagConstraints setConstraints(int x, int y, boolean fill) {
	GridBagConstraints gbc1 = new GridBagConstraints();
	gbc1.insets = new Insets(2, 2, 2, 2);
	gbc1.gridx = GridBagConstraints.RELATIVE;
	gbc1.gridy = y;
	gbc1.weightx = 100.0;
	gbc1.weighty = 100.0;
	gbc1.gridwidth = 1;
	gbc1.anchor = GridBagConstraints.LINE_START;
	if (fill && false) {
	    gbc1.fill = GridBagConstraints.HORIZONTAL;
        }
	return(gbc1);
    }

    protected void initComponents(String title) {

	tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title);
	tb.setTitlePosition(TitledBorder.DEFAULT_POSITION);
	this.setBorder(tb);

	this.setLayout(new GridBagLayout());
	
	xt = new JTextField(20);
	xt.setColumns(20);
	yt = new JTextField(20);
	yt.setColumns(20);
	zt = new JTextField(20);
	zt.setColumns(20);

	xs = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));
	ys = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));
	zs = new JSpinner(new SpinnerNumberModel(spin_value, min_spin, max_spin, spin_inc));
	xs.setMaximumSize(new Dimension(10, xs.getHeight()));
	ys.setMaximumSize(new Dimension(10, ys.getHeight()));
	zs.setMaximumSize(new Dimension(10, zs.getHeight()));
	
	JLabel xl = new JLabel("X");
	xl.setMaximumSize(new Dimension(10, xl.getHeight()));
	JLabel yl = new JLabel("Y");
	yl.setMaximumSize(new Dimension(10, yl.getHeight()));
	JLabel zl = new JLabel("Z");
	zl.setMaximumSize(new Dimension(10, zl.getHeight()));


	this.add(xl, setConstraints(0,0, false));
	this.add(xs, setConstraints(1,0, true));
	this.add(yl, setConstraints(2,0, false));
	this.add(ys, setConstraints(3,0, true));
	this.add(zl, setConstraints(3,0, false));
	this.add(zs, setConstraints(5,0, true));

	this.setPreferredSize(new Dimension(300, xl.getHeight()+100));
	this.setMaximumSize(new Dimension(350, xl.getHeight()+100));
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
	xs.setValue(p.getX());
	ys.setValue(p.getY());
	zs.setValue(p.getZ());
    }

    public void setValue(String s) {
	PhysicalLocation p = PhysicalLocation.parse(s);
	if (p != null) {
	    this.setValue(p);
	}
    }
    
    public PhysicalLocation getValue() {
	Float x = (Float)((SpinnerNumberModel)xs.getModel()).getNumber();
	Float y = (Float)((SpinnerNumberModel)ys.getModel()).getNumber();
	Float z = (Float)((SpinnerNumberModel)zs.getModel()).getNumber();
	return(new PhysicalLocation(x, y, z));
	      
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhysicalLocationPanel.class.getName());


}
