package jmri.jmrix.vsdecoder;

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
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class VSDSoundsPanel extends JmriPanel {

    private javax.swing.JButton bellButton;
    private javax.swing.JButton coupleButton;
    private javax.swing.JButton hornButton;
    private javax.swing.JButton brakeButton;
    private javax.swing.JButton dynamicBrakeButton;
    private javax.swing.JButton airReleaseButton;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton uncoupleButton;

    VSDecoder decoder;

    public VSDSoundsPanel() {
	this(null);
    }

    public VSDSoundsPanel(VSDecoder dec) {
	super();
	decoder = dec;
	initComponents();

    }

    public void setDecoder(VSDecoder dec) {
	decoder = dec;
    }

    public VSDecoder getDecoder() {
	return(decoder);
    }

    public void init() {}

    public void initContext(Object context) {
	initComponents();
    }

    public void initComponents() {

	this.setLayout(new GridLayout(0, 3));

	if (decoder == null) {
	    return;
	}
	
	ArrayList<SoundEvent> elist = new ArrayList<SoundEvent>(decoder.getEventList());
	for (SoundEvent e : elist) {
	    if (e.getButton() != null)
		log.debug("adding button " + e.getButton().toString());
		this.add(e.getButton());
	}
	
	//this.invalidate();
	//JPanel test = new DieselPane();
	/*
	JButton testb = new JButton();
	JSlider tests = new JSlider(JSlider.HORIZONTAL, 1, 8, 1);
	tests.setMajorTickSpacing(1);
	tests.setPaintTicks(true);
	testb.setText("Test");
	test.setVisible(true);
	testb.setVisible(true);
	test.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	test.add(testb);
	test.add(tests);
	*/
	//this.add(test);
	
	//revalidate();
	//setVisible(true);
	//repaint();
    }

    private void bellButtonActionPerformed(java.awt.event.ActionEvent evt) {
        decoder.toggleBell();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void hornButtonPressed(java.awt.event.MouseEvent evt) {
	log.debug("hornButtonPressed");
        decoder.playHorn();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void hornButtonReleased(java.awt.event.MouseEvent evt) {
	log.debug("hornButtonReleased");
        decoder.stopHorn();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void hornButtonClicked(java.awt.event.MouseEvent evt) {
	log.debug("hornButtonClicked");
        decoder.shortHorn();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void brakeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void dynamicBrakeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void airReleaseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDSoundsPanel.class.getName());

}