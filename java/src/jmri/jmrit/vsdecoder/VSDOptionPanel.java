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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.JmriPanel;

public class VSDOptionPanel extends JmriPanel {

    private javax.swing.JComboBox<Object> hornOptionComboBox;
    private javax.swing.JComboBox<Train> opsTrainComboBox;

    private Train selected_train;

    String decoder_id;
    VSDecoderPane main_frame;

    public VSDOptionPanel() {
        this(null, null);
    }

    public VSDOptionPanel(String dec, VSDecoderPane dad) {
        super();
        decoder_id = dec;
        main_frame = dad;
        selected_train = null;
        initComponents();
    }

    public void init() {
    }

    @Override
    public void initContext(Object context) {
        initComponents();
    }

    @Override
    public void initComponents() {

        // Below is mostly just "filler" stuff until we implement the real thing
        this.setLayout(new GridLayout(0, 2));

        JLabel x = new JLabel();
        x.setText("Operations Train: ");
        this.add(x);
        opsTrainComboBox = InstanceManager.getDefault(TrainManager.class).getTrainComboBox();
        this.add(opsTrainComboBox);
        opsTrainComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opsTrainSelectAction(e);
            }
        });

        hornOptionComboBox = new javax.swing.JComboBox<Object>();
        hornOptionComboBox.setModel(new javax.swing.DefaultComboBoxModel<Object>(new String[]{"3-Chime Leslie", "5-Chime Leslie", "4-Chime Nathan"}));
        x = new JLabel();
        x.setText("Horn Option: ");
        this.add(x);
        this.add(hornOptionComboBox);
        x = new JLabel();
        x.setText("Engine Option: ");
        this.add(x);
        JComboBox<Object> y = new javax.swing.JComboBox<Object>();
        y.setModel(new javax.swing.DefaultComboBoxModel<Object>(new String[]{"Non-Turbo", "Turbo"}));
        this.add(y);
    }

    public void opsTrainSelectAction(ActionEvent e) {
        if (opsTrainComboBox.getSelectedItem() != null) {
            if (selected_train != null) {
                selected_train.removePropertyChangeListener(main_frame.getDecoder());
            }
            String opsTrain = opsTrainComboBox.getSelectedItem().toString();
            if ((selected_train = InstanceManager.getDefault(TrainManager.class).getTrainByName(opsTrain)) != null) {
                selected_train.addPropertyChangeListener(main_frame.getDecoder());
            }
        }
    }

    // Unused as yet.  Commented out to hide the compiler warning.
    //private static final Logger log = LoggerFactory.getLogger(VSDOptionPanel.class);
}
