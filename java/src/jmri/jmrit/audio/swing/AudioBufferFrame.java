package jmri.jmrit.audio.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.beantable.AudioTableAction.AudioTableDataModel;
import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Defines a GUI to edit AudioBuffer objects.
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
public class AudioBufferFrame extends AbstractAudioFrame {

    private static int counter = 1;

    private boolean newBuffer;

    private final Object lock = new Object();

    // UI components for Add/Edit Buffer
    private final JLabel urlLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelURL")));
    private final JTextField url = new JTextField(40);
    private final JButton buttonBrowse = new JButton("...");
    private final JCheckBox stream = new JCheckBox(Bundle.getMessage("LabelStream"));
    // JLabel formatLabel = new JLabel(Bundle.getMessage("LabelFormat"));
    // JTextField format = new JTextField(20);
    private final JLabel loopStartLabel = new JLabel(Bundle.getMessage("LabelLoopStart"));
    private final JSpinner loopStart = new JSpinner();
    private final JLabel loopEndLabel = new JLabel(Bundle.getMessage("LabelLoopEnd"));
    private final JSpinner loopEnd = new JSpinner();
    private JFileChooser fileChooser;
    // AudioWaveFormPanel waveForm = new AudioWaveFormPanel();

    private static final String PREFIX = "IAB";

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AudioBufferFrame(String title, AudioTableDataModel model) {
        super(title, model);
        layoutFrame();

        // For now, disable editing of loop points
        // TODO: enable editing of looping points
        loopStart.setEnabled(false);
        loopStartLabel.setEnabled(false);
        loopEnd.setEnabled(false);
        loopEndLabel.setEnabled(false);
    }

    @Override
    public final void layoutFrame() {
        super.layoutFrame();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelSample")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(urlLabel);
        p2.add(url);
        buttonBrowse.addActionListener( e -> browsePressed());
        buttonBrowse.setToolTipText(Bundle.getMessage("ToolTipButtonBrowse"));
        p2.add(buttonBrowse);
        p.add(p2);
        p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(stream);
        p.add(p2);
//        p2 = new JPanel(); p2.setLayout(new FlowLayout());
//        p2.add(formatLabel);
//        p2.add(format);
//        p.add(p2);
        main.add(p);

//        p = new JPanel();
//        p.setBorder(BorderFactory.createCompoundBorder(
//                        BorderFactory.createTitledBorder("Waveforms"),
//                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//
//        JLabel label = new JLabel("Whole sample");
//        label.setUI(new VerticalLabelUI());
//        p.add(label);
//
//        waveForm.setPreferredSize(new Dimension(400, 120));
//        p.add(waveForm);
//
//        label = new JLabel("Loop-point detail");
//        label.setUI(new VerticalLabelUI());
//        p.add(label);
//
//        AudioWaveFormPanel waveFormLoop = new AudioWaveFormPanel();
//        waveFormLoop.setPreferredSize(new Dimension(80, 120));
//        p.add(waveFormLoop);
//
//        main.add(p);
//
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Bundle.getMessage("LabelLoopPoints")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        p.add(loopStartLabel);
        loopStart.setPreferredSize(new JTextField(8).getPreferredSize());
        loopStart.setModel(
                new SpinnerNumberModel(0L, 0L, Long.MAX_VALUE,1L));
        loopStart.addChangeListener( e ->
            loopEnd.setValue(
                    ((Long) loopStart.getValue()
                            < (Long) loopEnd.getValue())
                            ? loopEnd.getValue()
                            : loopStart.getValue()));
        p.add(loopStart);
        p.add(loopEndLabel);
        loopEnd.setPreferredSize(new JTextField(8).getPreferredSize());
        loopEnd.setModel(
                new SpinnerNumberModel(0L, 0L, Long.MAX_VALUE, 1L));
        loopEnd.addChangeListener( e ->
            loopStart.setValue(
                    ((Long) loopEnd.getValue()
                            < (Long) loopStart.getValue())
                            ? loopEnd.getValue()
                            : loopStart.getValue()));
        p.add(loopEnd);
        main.add(p);

        p = new JPanel();
        JButton apply = new JButton(Bundle.getMessage("ButtonApply"));
        p.add(apply);
        apply.addActionListener( e -> applyPressed());
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
        p.add(ok);
        ok.addActionListener((ActionEvent e) -> {
            if (applyPressed()) {
                frame.dispose();
            }
        });
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        p.add(cancel);
        cancel.addActionListener( e -> frame.dispose());
        frame.getContentPane().add(p);
    }

    /**
     * Populate the Edit Buffer frame with default values.
     */
    @Override
    public void resetFrame() {
        synchronized (lock) {
            sysName.setText(PREFIX + nextCounter()); // NOI18N
        }
        userName.setText(null);
        url.setText(null);
        // format.setText(null);
        stream.setSelected(false);
        stream.setEnabled(false);
        loopStart.setValue(0L);
        loopEnd.setValue(0L);

        this.newBuffer = true;
    }

    /**
     * Populate the Edit Buffer frame with current values.
     */
    @Override
    public void populateFrame(Audio a) {
        if (!(a instanceof AudioBuffer)) {
            throw new IllegalArgumentException(a + " is not an AudioBuffer object");
        }
        super.populateFrame(a);
        AudioBuffer b = (AudioBuffer) a;
        url.setText(b.getURL());
        // format.setText(b.toString());
        stream.setSelected(b.isStreamed());
        stream.setEnabled(false); //(!b.isStreamedForced());
        loopStart.setValue(b.getStartLoopPoint());
        loopEnd.setValue(b.getEndLoopPoint());
        loopStart.setEnabled(true);
        loopStartLabel.setEnabled(true);
        loopEnd.setEnabled(true);
        loopEndLabel.setEnabled(true);

        this.newBuffer = false;
    }

    private void browsePressed() {
        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser("resources" + File.separator + "sounds" + File.separator); // NOI18N
            fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav)", "wav")); // NOI18N
        }

        // Show dialog
        fileChooser.rescanCurrentDirectory();
        int retValue = fileChooser.showOpenDialog(this);

        // Process selection
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = FileUtil.getPortableFilename(file);
            if (!url.getText().equals(fileName)) {
                url.setText(fileName);
//                try {
//                    WaveFileReader wfr = new WaveFileReader(FileUtil.getExternalFilename(fileName));
//                    JmriJOptionPane.showMessageDialog(null, wfr.toString(), wfr.toString(), JmriJOptionPane.INFORMATION_MESSAGE);
//                } catch (AudioException ex) {
//                    JmriJOptionPane.showMessageDialog(this, ex.getMessage(), rba.getString("TitleReadError"), JmriJOptionPane.ERROR_MESSAGE);
//                }
            }
        }
    }

    private boolean applyPressed() {
        String sName = sysName.getText();
        if (entryError(sName, PREFIX, "" + counter)) {
            return false;
        }
        String user = userName.getText();
        if (user.isEmpty()) {
            user = null;
        }
        AudioBuffer b;
        try {
            AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);
            if (newBuffer && am.getBySystemName(sName) != null) {
                throw new AudioException(Bundle.getMessage("DuplicateSystemName"));
            }
            try {
                b = (AudioBuffer) am.provideAudio(sName);
            } catch (IllegalArgumentException ex) {
                throw new AudioException(Bundle.getMessage("ProblemCreatingBuffer"));
            }
            if ((user != null) && newBuffer && am.getByUserName(user) != null) {
                am.deregister(b);
                synchronized (lock) {
                    prevCounter();
                }
                throw new AudioException(Bundle.getMessage("DuplicateUserName"));
            }
            b.setUserName(user);
            b.setStreamed(stream.isSelected());
            if (newBuffer || !b.getURL().equals(url.getText())) {
                b.setURL(url.getText());
                log.debug("After load, end loop point = {}", b.getEndLoopPoint());
                //b.setStartLoopPoint((Long)loopStart.getValue());
                //b.setEndLoopPoint((Long)loopEnd.getValue());
            } else {
                if (!b.getURL().equals(url.getText())) {
                    log.debug("Sound changed from: {}", b.getURL());
                    b.setURL(url.getText());
                }
            }

            // Update streaming checkbox if necessary
            stream.setSelected(b.isStreamed());
            stream.setEnabled(false); //(!b.isStreamedForced());

            // Notify changes
            model.fireTableDataChanged();
        } catch (AudioException ex) {
            JmriJOptionPane.showMessageDialog(this, ex.getMessage(),
                Bundle.getMessage("AudioCreateErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        newBuffer = false;  // If the user presses Apply, the dialog stays visible.
        return true;
    }

    private static int nextCounter() {
        counter++;
        return counter-1;
    }

    private static void prevCounter() {
        counter--;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AudioBufferFrame.class);

}
