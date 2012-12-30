package jmri.jmrit.vsdecoder.swing;

/** class VSDOptionsDialog
 * 
 * Configuration dialog for setting up a new VSDecoder
 */

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
 * @version			$Revision: 21510 $
 */

import java.util.ResourceBundle;
import javax.swing.*;
import jmri.jmrit.operations.trains.TrainManager;

public class VSDOptionsDialog extends JDialog {

    public static final String OPTIONS_PROPERTY = "Options"; // NOI18N


    private JComboBox opsTrainComboBox;

    public VSDOptionsDialog(JPanel parent, String title) {
	super(SwingUtilities.getWindowAncestor(parent), title);
	initComponents();
    }

    public void initComponents() {
	this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

	JLabel x = new JLabel();
	x.setText(Bundle.getString("FieldSelectTrain"));
	this.add(x);
	opsTrainComboBox = TrainManager.instance().getComboBox();
	this.add(opsTrainComboBox);

	JButton closeButton = new JButton(Bundle.getString("ButtonClose"));
	closeButton.setEnabled(true);
	closeButton.setToolTipText(Bundle.getString("ToolTipCloseDialog"));
	closeButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    closeButtonActionPerformed(e);
		}
	    });
	this.add(closeButton);
	this.pack();
	this.setVisible(true);
    }

    private void closeButtonActionPerformed(java.awt.event.ActionEvent ae) {
	if (opsTrainComboBox.getSelectedItem() != null) {
	    firePropertyChange(OPTIONS_PROPERTY, null, opsTrainComboBox.getSelectedItem().toString());
	}
	dispose();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDOptionsDialog.class.getName());
}
