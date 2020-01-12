package jmri.jmrit.vsdecoder.swing;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * class VSDOptionsDialog
 *
 * Configuration dialog for setting up a new VSDecoder
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
public class VSDOptionsDialog extends JDialog {

    public static final String OPTIONS_PROPERTY = "Options"; // NOI18N

    private JComboBox<Train> opsTrainComboBox;

    public VSDOptionsDialog(JPanel parent, String title) {
        super(SwingUtilities.getWindowAncestor(parent), title);
        initComponents();
    }

    public void initComponents() {
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        JLabel x = new JLabel();
        x.setText(Bundle.getMessage("FieldSelectTrain"));
        this.add(x);
        opsTrainComboBox = InstanceManager.getDefault(TrainManager.class).getTrainComboBox();
        this.add(opsTrainComboBox);

        JButton closeButton = new JButton(Bundle.getMessage("ButtonOK"));
        closeButton.setEnabled(true);
        closeButton.setToolTipText(Bundle.getMessage("ToolTipCloseDialog"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
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

    // Log not used... yet...
    //    private static final Logger log = LoggerFactory.getLogger(VSDOptionsDialog.class);
}
