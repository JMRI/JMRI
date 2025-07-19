package jmri.jmrit.audio.swing;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import jmri.util.swing.JmriJOptionPane;

/**
 * Define a GUI to edit AudioListener objects
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public class AudioListenerFrame extends AbstractAudioFrame {

    private final JPanelVector3f position = new JPanelVector3f(Bundle.getMessage("LabelPosition"),
            Bundle.getMessage("UnitUnits"));
    private final JPanelVector3f velocity = new JPanelVector3f(Bundle.getMessage("LabelVelocity"),
            Bundle.getMessage("UnitU/S"));
    private final JLabel oriAtLabel = new JLabel(Bundle.getMessage("LabelOrientationAt"));
    private final JPanelVector3f oriAt = new JPanelVector3f("", Bundle.getMessage("UnitUnits"));
    private final JLabel oriUpLabel = new JLabel(Bundle.getMessage("LabelOrientationUp"));
    private final JPanelVector3f oriUp = new JPanelVector3f("", Bundle.getMessage("UnitUnits"));
    private final JPanelSliderf gain = new JPanelSliderf(Bundle.getMessage("LabelGain"), 0.0f, 1.0f, 5, 4);
    private final JSpinner metersPerUnit = new JSpinner();
    private final JLabel metersPerUnitLabel = new JLabel(Bundle.getMessage("UnitM/U"));

    private static final String PREFIX = "IAL";

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AudioListenerFrame(String title, AudioTableDataModel model) {
        super(title, model);
        layoutFrame();
    }

    @Override
    public final void layoutFrame() {
        super.layoutFrame();
        JPanel p;

        main.add(position);
        main.add(velocity);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelOrientation")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(oriAtLabel);
        p.add(oriAt);
        p.add(oriUpLabel);
        p.add(oriUp);
        main.add(p);

        main.add(gain);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelMetersPerUnit")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        metersPerUnit.setPreferredSize(new JTextField(8).getPreferredSize());
        metersPerUnit.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f),
                    Float.valueOf(65536f), Float.valueOf(0.0001f)));
        // TODO - I18N of format
        metersPerUnit.setEditor(new JSpinner.NumberEditor(metersPerUnit, "0.0000"));
        p.add(metersPerUnit);
        p.add(metersPerUnitLabel);
        main.add(p);

        p = new JPanel();
        JButton apply = new JButton(Bundle.getMessage("ButtonApply"));
        p.add(apply);
        apply.addActionListener( e -> applyPressed());
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
        p.add(ok);
        ok.addActionListener( e -> {
            applyPressed();
            frame.dispose();
        });
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        p.add(cancel);
        cancel.addActionListener( e -> frame.dispose());
        frame.getContentPane().add(p);
    }

    @Override
    public void resetFrame() {
        // Not required
    }

    /**
     * Populate the Edit Listener frame with current values.
     */
    @Override
    public void populateFrame(Audio a) {
        if (!(a instanceof AudioListener)) {
            throw new IllegalArgumentException(a + " is not an AudioListener object");
        }
        super.populateFrame(a);
        AudioListener l = (AudioListener) a;
        position.setValue(l.getPosition());
        velocity.setValue(l.getVelocity());
        oriAt.setValue(l.getOrientation(Audio.AT));
        oriUp.setValue(l.getOrientation(Audio.UP));
        gain.setValue(l.getGain());
        metersPerUnit.setValue(l.getMetersPerUnit());
    }

    private void applyPressed() {
        String sName = sysName.getText();
        if (entryError(sName, PREFIX, "$")) { // no index for AudioListener
            return;
        }
        String user = userName.getText();
        if (user.isEmpty()) {
            user = null;
        }
        AudioListener l;
        try {
            l = (AudioListener) InstanceManager.getDefault(jmri.AudioManager.class).provideAudio(sName);
            l.setUserName(user);
            l.setPosition(position.getValue());
            l.setVelocity(velocity.getValue());
            l.setOrientation(oriAt.getValue(), oriUp.getValue());
            l.setGain(gain.getValue());
            l.setMetersPerUnit(AbstractAudio.roundDecimal((Float) metersPerUnit.getValue(), 4d));

            // Notify changes
            model.fireTableDataChanged();
        } catch (AudioException | IllegalArgumentException ex) {
            JmriJOptionPane.showMessageDialog(this, ex.getMessage(),
                Bundle.getMessage("AudioCreateErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    //private static final Logger log = LoggerFactory.getLogger(AudioListenerFrame.class);

}
