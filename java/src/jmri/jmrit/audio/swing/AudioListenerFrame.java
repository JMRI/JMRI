// AudioListenerFrame.java

package jmri.jmrit.audio.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.Audio;
import jmri.AudioException;
import jmri.InstanceManager;
import jmri.implementation.AbstractAudio;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;

/**
 * Define a GUI to edit AudioListener objects
 *
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
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public class AudioListenerFrame extends AbstractAudioFrame {

    JPanelVector3f position = new JPanelVector3f(rba.getString("LabelPosition"),
                                                 rba.getString("UnitUnits"));
    JPanelVector3f velocity = new JPanelVector3f(rba.getString("LabelVelocity"),
                                                 rba.getString("UnitU/S"));
    JLabel oriAtLabel = new JLabel(rba.getString("LabelOrientationAt"));
    JPanelVector3f oriAt = new JPanelVector3f("",rba.getString("UnitUnits"));
    JLabel oriUpLabel = new JLabel(rba.getString("LabelOrientationUp"));
    JPanelVector3f oriUp = new JPanelVector3f("",rba.getString("UnitUnits"));
    JPanelSliderf gain = new JPanelSliderf(rba.getString("LabelGain"), 0.0f, 1.0f, 5, 4);
    JSpinner metersPerUnit = new JSpinner();
    JLabel metersPerUnitLabel = new JLabel(rba.getString("UnitM/U"));

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AudioListenerFrame(String title, AudioTableDataModel model) {
        super(title, model);
        layoutFrame();
    }

    @Override
    public void layoutFrame() {
        super.layoutFrame();
        JPanel p;

        main.add(position);
        main.add(velocity);

        p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(rba.getString("LabelOrientation")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(oriAtLabel);
        p.add(oriAt);
        p.add(oriUpLabel);
        p.add(oriUp);
        main.add(p);

        main.add(gain);

        p = new JPanel(); p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(rba.getString("LabelMetersPerUnit")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        metersPerUnit.setPreferredSize(new JTextField(8).getPreferredSize());
        metersPerUnit.setModel(
                new SpinnerNumberModel(new Float(0f), new Float(0f), new Float(65536f), new Float(0.0001f)));
        metersPerUnit.setEditor(new JSpinner.NumberEditor(metersPerUnit, "0.0000"));
        p.add(metersPerUnit);
        p.add(metersPerUnitLabel);
        main.add(p);

        JButton ok;
        frame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        });
    }

    @Override
    public void resetFrame() {
        // Not required
    }

    /**
     * Method to populate the Edit Listener frame with current values
     */
    @Override
    public void populateFrame(Audio a) {
        super.populateFrame(a);
        AudioListener l = (AudioListener) a;
        position.setValue(l.getPosition());
        velocity.setValue(l.getVelocity());
        oriAt.setValue(l.getOrientation(Audio.AT));
        oriUp.setValue(l.getOrientation(Audio.UP));
        gain.setValue(l.getGain());
        metersPerUnit.setValue(l.getMetersPerUnit());
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        AudioListener l;
        try {
            l = (AudioListener) InstanceManager.audioManagerInstance().provideAudio(sName);
            l.setUserName(user);
            l.setPosition(position.getValue());
            l.setVelocity(velocity.getValue());
            l.setOrientation(oriAt.getValue(), oriUp.getValue());
            l.setGain(gain.getValue());
            l.setMetersPerUnit(AbstractAudio.roundDecimal((Float)metersPerUnit.getValue(),4d));

            // Notify changes
            model.fireTableDataChanged();
        } catch (AudioException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), rb.getString("AudioCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    //private static final Logger log = LoggerFactory.getLogger(AudioListenerFrame.class.getName());

}

/* @(#)AudioListenerFrame.java */
