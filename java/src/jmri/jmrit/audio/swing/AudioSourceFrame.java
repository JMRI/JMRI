package jmri.jmrit.audio.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.vecmath.Vector3f;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;

/**
 * Defines a GUI for editing AudioSource objects.
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
public class AudioSourceFrame extends AbstractAudioFrame {

    private static int counter = 1;

    private boolean newSource;

    private final Object lock = new Object();

    // UI components for Add/Edit Source
    JLabel assignedBufferLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelAssignedBuffer")));
    JComboBox<String> assignedBuffer = new JComboBox<>();
    JLabel loopMinLabel = new JLabel(Bundle.getMessage("LabelLoopMin"));
    JSpinner loopMin = new JSpinner();
    JLabel loopMaxLabel = new JLabel(Bundle.getMessage("LabelLoopMax"));
    JSpinner loopMax = new JSpinner();
    //    JLabel loopMinDelayLabel = new JLabel(Bundle.getMessage("LabelLoopMin"));
    //    JSpinner loopMinDelay = new JSpinner();
    //    JLabel loopMaxDelayLabel = new JLabel(Bundle.getMessage("LabelLoopMax"));
    //    JSpinner loopMaxDelay = new JSpinner();
    //    JLabel loopDelayUnitsLabel = new JLabel(Bundle.getMessage("UnitMS"));
    JCheckBox loopInfinite = new JCheckBox(Bundle.getMessage("LabelLoopInfinite"));
    JPanelVector3f position = new JPanelVector3f("",
            Bundle.getMessage("UnitUnits"));
    JCheckBox positionRelative = new JCheckBox(Bundle.getMessage("LabelPositionRelative"));
    JPanelVector3f velocity = new JPanelVector3f(Bundle.getMessage("LabelVelocity"),
            Bundle.getMessage("UnitU/S"));
    JPanelSliderf gain = new JPanelSliderf(Bundle.getMessage("LabelGain"), 0.0f, 1.0f, 5, 4);
    JPanelSliderf pitch = new JPanelSliderf(Bundle.getMessage("LabelPitch"), 0.5f, 2.0f, 6, 5);
    JLabel refDistanceLabel = new JLabel(Bundle.getMessage("LabelReferenceDistance"));
    JSpinner refDistance = new JSpinner();
    JLabel maxDistanceLabel = new JLabel(Bundle.getMessage("LabelMaximumDistance"));
    JSpinner maxDistance = new JSpinner();
    JLabel distancesLabel = new JLabel(Bundle.getMessage("UnitUnits"));
    JLabel rollOffFactorLabel = new JLabel(Bundle.getMessage("LabelRollOffFactor"));
    JSpinner rollOffFactor = new JSpinner();
    JLabel fadeInTimeLabel = new JLabel(Bundle.getMessage("LabelFadeIn"));
    JSpinner fadeInTime = new JSpinner();
    JLabel fadeOutTimeLabel = new JLabel(Bundle.getMessage("LabelFadeOut"));
    JSpinner fadeOutTime = new JSpinner();
    JLabel fadeTimeUnitsLabel = new JLabel(Bundle.getMessage("UnitMS"));

    private final static String PREFIX = "IAS";

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AudioSourceFrame(String title, AudioTableDataModel model) {
        super(title, model);
        layoutFrame();
    }

    @Override
    @SuppressWarnings("UnnecessaryBoxing")
    public void layoutFrame() {
        super.layoutFrame();
        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(assignedBufferLabel);
        p.add(assignedBuffer);
        main.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelLoop")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(loopMinLabel);
        loopMin.setPreferredSize(new JTextField(8).getPreferredSize());
        loopMin.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        loopMin.addChangeListener((ChangeEvent e) -> {
            loopMax.setValue(
                    ((Integer) loopMin.getValue()
                            < (Integer) loopMax.getValue())
                            ? loopMax.getValue()
                            : loopMin.getValue());
        });
        p.add(loopMin);
        p.add(loopMaxLabel);
        loopMax.setPreferredSize(new JTextField(8).getPreferredSize());
        loopMax.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        loopMax.addChangeListener((ChangeEvent e) -> {
            loopMin.setValue(
                    ((Integer) loopMax.getValue()
                            < (Integer) loopMin.getValue())
                            ? loopMax.getValue()
                            : loopMin.getValue());
        });
        p.add(loopMax);
        loopInfinite.addChangeListener((ChangeEvent e) -> {
            loopMin.setEnabled(!loopInfinite.isSelected());
            loopMax.setEnabled(!loopInfinite.isSelected());
        });
        p.add(loopInfinite);
        main.add(p);

//        p = new JPanel(); p.setLayout(new FlowLayout());
//        p.setBorder(BorderFactory.createCompoundBorder(
//                        BorderFactory.createTitledBorder(Bundle.getMessage("LabelLoopDelay")),
//                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//        p.add(loopMinDelayLabel);
//        loopMinDelay.setPreferredSize(new JTextField(8).getPreferredSize());
//        loopMinDelay.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
//        loopMinDelay.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                loopMaxDelay.setValue(
//                        ((Integer)loopMinDelay.getValue()
//                        <(Integer)loopMaxDelay.getValue())
//                        ?loopMaxDelay.getValue()
//                        :loopMinDelay.getValue());
//            }
//        });
//        p.add(loopMinDelay);
//        p.add(loopMaxDelayLabel);
//        loopMaxDelay.setPreferredSize(new JTextField(8).getPreferredSize());
//        loopMaxDelay.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
//        loopMaxDelay.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                loopMinDelay.setValue(
//                        ((Integer)loopMaxDelay.getValue()
//                        <(Integer)loopMinDelay.getValue())
//                        ?loopMaxDelay.getValue()
//                        :loopMinDelay.getValue());
//            }
//        });
//        p.add(loopMaxDelay);
//        p.add(loopDelayUnitsLabel);
//        main.add(p);
//
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelPosition")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(position);
        p.add(positionRelative);
        main.add(p);

        main.add(velocity);
        main.add(gain);
        main.add(pitch);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelDistances")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel p2;
        p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(refDistanceLabel);
        refDistance.setPreferredSize(new JTextField(8).getPreferredSize());
        refDistance.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(Audio.MAX_DISTANCE), Float.valueOf(FLT_PRECISION)));
        refDistance.setEditor(new JSpinner.NumberEditor(refDistance, "0.00"));
        refDistance.addChangeListener((ChangeEvent e) -> {
            maxDistance.setValue(
                    ((Float) refDistance.getValue()
                            < (Float) maxDistance.getValue())
                            ? maxDistance.getValue()
                            : refDistance.getValue());
        });
        p2.add(refDistance);

        p2.add(maxDistanceLabel);
        maxDistance.setPreferredSize(new JTextField(8).getPreferredSize());
        maxDistance.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(Audio.MAX_DISTANCE), Float.valueOf(FLT_PRECISION)));
        maxDistance.setEditor(new JSpinner.NumberEditor(maxDistance, "0.00"));
        maxDistance.addChangeListener((ChangeEvent e) -> {
            refDistance.setValue(
                    ((Float) maxDistance.getValue()
                            < (Float) refDistance.getValue())
                            ? maxDistance.getValue()
                            : refDistance.getValue());
        });
        p2.add(maxDistance);
        p2.add(distancesLabel);
        p.add(p2);

        p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(rollOffFactorLabel);
        rollOffFactor.setPreferredSize(new JTextField(8).getPreferredSize());
        rollOffFactor.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(Audio.MAX_DISTANCE), Float.valueOf(FLT_PRECISION)));
        rollOffFactor.setEditor(new JSpinner.NumberEditor(rollOffFactor, "0.00"));
        p2.add(rollOffFactor);
        p.add(p2);
        main.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelFadeTimes")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        p.add(fadeInTimeLabel);
        fadeInTime.setPreferredSize(new JTextField(8).getPreferredSize());
        fadeInTime.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        p.add(fadeInTime);

        p.add(fadeOutTimeLabel);
        fadeOutTime.setPreferredSize(new JTextField(8).getPreferredSize());
        fadeOutTime.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        p.add(fadeOutTime);

        p.add(fadeTimeUnitsLabel);
        main.add(p);

        p = new JPanel();
        JButton apply;
        p.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener((ActionEvent e) -> {
            applyPressed(e);
        });
        JButton ok;
        p.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        JButton cancel;
        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        frame.getContentPane().add(p);
    }

    /**
     * Populate the Edit Source frame with default values.
     */
    @Override
    public void resetFrame() {
        synchronized (lock) {
            sysName.setText(PREFIX + nextCounter());
        }
        userName.setText(null);
        assignedBuffer.setSelectedIndex(0);
        loopInfinite.setSelected(false);
        loopMin.setValue(AudioSource.LOOP_NONE);
        loopMax.setValue(AudioSource.LOOP_NONE);
//        loopMinDelay.setValue(0);
//        loopMaxDelay.setValue(0);
        position.setValue(new Vector3f(0, 0, 0));
        positionRelative.setSelected(false);
        velocity.setValue(new Vector3f(0, 0, 0));
        gain.setValue(1.0f);
        pitch.setValue(1.0f);
        refDistance.setValue(1.0f);
        maxDistance.setValue(Audio.MAX_DISTANCE);
        rollOffFactor.setValue(1.0f);
        fadeInTime.setValue(1000);
        fadeOutTime.setValue(1000);

        this.newSource = true;
    }

    /**
     * Populate the Edit Source frame with current values.
     */
    @Override
    public void populateFrame(Audio a) {
        if (!(a instanceof AudioSource)) {
            throw new IllegalArgumentException(a.getSystemName() + " is not an AudioSource object");
        }
        super.populateFrame(a);
        AudioSource s = (AudioSource) a;
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);
        String ab = s.getAssignedBufferName();
        Audio b = am.getAudio(ab);
        if (b != null) {
            assignedBuffer.setSelectedItem(b.getUserName() == null ? ab : b.getUserName());
        }
        loopInfinite.setSelected((s.getMinLoops() == AudioSource.LOOP_CONTINUOUS));
        loopMin.setValue(loopInfinite.isSelected() ? 0 : s.getMinLoops());
        loopMax.setValue(loopInfinite.isSelected() ? 0 : s.getMaxLoops());
        //        loopMinDelay.setValue(s.getMinLoopDelay());
        //        loopMaxDelay.setValue(s.getMaxLoopDelay());
        position.setValue(s.getPosition());
        positionRelative.setSelected(s.isPositionRelative());
        velocity.setValue(s.getVelocity());
        gain.setValue(s.getGain());
        pitch.setValue(s.getPitch());
        refDistance.setValue(s.getReferenceDistance());
        maxDistance.setValue(s.getMaximumDistance());
        rollOffFactor.setValue(s.getRollOffFactor());
        fadeInTime.setValue(s.getFadeIn());
        fadeOutTime.setValue(s.getFadeOut());

        this.newSource = false;
    }

    public void updateBufferList() {
        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);
        assignedBuffer.removeAllItems();
        assignedBuffer.addItem("Select buffer from list");
        am.getNamedBeanSet(Audio.BUFFER).stream().forEach((s) -> {
            Audio a = am.getAudio(s.getSystemName());
            if (a != null) {
                String u = a.getUserName();
                if (u != null) {
                    assignedBuffer.addItem(u);
                } else {
                    assignedBuffer.addItem(s.getSystemName());
                }
            } else {
                assignedBuffer.addItem(s.getSystemName());
            }
        });
    }

    private void applyPressed(ActionEvent e) {
        String sName = sysName.getText();
        if (entryError(sName, PREFIX, "" + counter)) {
            return;
        }
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        AudioSource s;
        try {
            AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);
            try {
                s = (AudioSource) am.provideAudio(sName);
            } catch (IllegalArgumentException ex) {
                throw new AudioException("Problem creating source");
            }
            if ((user != null) && (newSource) && (am.getByUserName(user) != null)) {
                am.deregister(s);
                synchronized (lock) {
                    prevCounter();
                }
                throw new AudioException("Duplicate user name - please modify");
            }
            s.setUserName(user);
            if (assignedBuffer.getSelectedIndex() > 0) {
                String sel = (String) assignedBuffer.getSelectedItem();
                if (sel != null) {
                    Audio a = am.getAudio(sel);
                    if (a != null) {
                        s.setAssignedBuffer(a.getSystemName());
                    }
                }
            }
            s.setMinLoops(loopInfinite.isSelected() ? AudioSource.LOOP_CONTINUOUS : (Integer) loopMin.getValue());
            s.setMaxLoops(loopInfinite.isSelected() ? AudioSource.LOOP_CONTINUOUS : (Integer) loopMax.getValue());
            // s.setMinLoopDelay((Integer) loopMinDelay.getValue());
            // s.setMaxLoopDelay((Integer) loopMaxDelay.getValue());
            s.setPosition(position.getValue());
            s.setPositionRelative(positionRelative.isSelected());
            s.setVelocity(velocity.getValue());
            s.setGain(gain.getValue());
            s.setPitch(pitch.getValue());
            s.setReferenceDistance((Float) refDistance.getValue());
            s.setMaximumDistance((Float) maxDistance.getValue());
            s.setRollOffFactor((Float) rollOffFactor.getValue());
            s.setFadeIn((Integer) fadeInTime.getValue());
            s.setFadeOut((Integer) fadeOutTime.getValue());

            // Notify changes
            model.fireTableDataChanged();
        } catch (AudioException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("AudioCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int nextCounter() {
        return counter++;
    }

    private static void prevCounter() {
        counter--;
    }

    //private static final Logger log = LoggerFactory.getLogger(AudioSourceFrame.class);

}
