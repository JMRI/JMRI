package jmri.jmrit.vsdecoder.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.VSDecoderPreferences;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
class VSDecoderPreferencesPane extends javax.swing.JPanel implements PropertyChangeListener {

    private javax.swing.JCheckBox cbAutoStartEngine;
    private javax.swing.JCheckBox cbAutoLoadVSDFile;
    private javax.swing.JTextField tfDefaultVSDFilePath;
    private javax.swing.JTextField tfDefaultVSDFileName;
    private javax.swing.JLabel labelDefaultVSDFilePath;
    private javax.swing.JLabel labelDefaultVSDFileName;
    private javax.swing.JRadioButton audioModeRoomButton;
    private javax.swing.JRadioButton audioModeHeadphoneButton;
    private javax.swing.ButtonGroup audioModeGroup;

    private javax.swing.JLabel labelApplyWarning;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container = null;

    /**
     * Creates new form VSDecoderPreferencesPane
     */
    public VSDecoderPreferencesPane(VSDecoderPreferences tp) {
        initComponents();
        setComponents(tp);
        checkConsistency();
        tp.addPropertyChangeListener(this);
    }

    public VSDecoderPreferencesPane() {
        this(jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences());
    }

    private GridBagConstraints setConstraints(Insets i, int x, int y, int width, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = i;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = fill;
        return (gbc);
    }

    private void initComponents() {

        JPanel prefsPane = new JPanel();
        JPanel controlPane = new JPanel();

        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder());

        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbApply = new javax.swing.JButton();

        labelApplyWarning = new javax.swing.JLabel();
        labelDefaultVSDFilePath = new javax.swing.JLabel();
        labelDefaultVSDFileName = new javax.swing.JLabel();

        cbAutoStartEngine = new javax.swing.JCheckBox();
        cbAutoLoadVSDFile = new javax.swing.JCheckBox();
        tfDefaultVSDFilePath = new javax.swing.JTextField(40);
        tfDefaultVSDFileName = new javax.swing.JTextField(40);
        JButton jbPathBrowse = new javax.swing.JButton(Bundle.getMessage("Browse"));
        jbPathBrowse.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbPathBrowseActionPerformed(evt);
            }
        });
        JButton jbFileBrowse = new javax.swing.JButton(Bundle.getMessage("Browse"));
        jbFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFileBrowseActionPerformed(evt);
            }
        });

        // Audio Mode
        audioModeRoomButton = new JRadioButton(Bundle.getMessage("ButtonAudioModeRoom"));
        audioModeHeadphoneButton = new JRadioButton(Bundle.getMessage("ButtonAudioModeHeadphone"));
        audioModeGroup = new ButtonGroup();
        audioModeGroup.add(audioModeRoomButton);
        audioModeGroup.add(audioModeHeadphoneButton);
        JPanel amPanel = new JPanel();
        amPanel.setLayout(new BoxLayout(amPanel, BoxLayout.LINE_AXIS));
        amPanel.add(new JLabel("Audio Mode:"));
        amPanel.add(audioModeRoomButton);
        amPanel.add(audioModeHeadphoneButton);

        // Get label strings from the resource bundle and assign it.
        cbAutoStartEngine.setText(Bundle.getMessage("AutoStartEngine"));
        cbAutoLoadVSDFile.setText(Bundle.getMessage("AutoLoadVSDFile"));
        tfDefaultVSDFilePath.setColumns(30);
        tfDefaultVSDFilePath.setColumns(30);
        labelDefaultVSDFilePath.setText(Bundle.getMessage("DefaultVSDFilePath"));
        labelDefaultVSDFileName.setText(Bundle.getMessage("DefaultVSDFileName"));
        labelApplyWarning.setText(Bundle.getMessage("ExVSDecoderLabelApplyWarning"));

        // Set action listener to check consistency when the user makes changes.
        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkConsistency();
            }
        };
        cbAutoStartEngine.addActionListener(al);
        cbAutoLoadVSDFile.addActionListener(al);
        tfDefaultVSDFilePath.addActionListener(al);
        tfDefaultVSDFileName.addActionListener(al);

        // Set action listeners for save / cancel / reset buttons
        jbSave.setText(Bundle.getMessage("ButtonSave"));
        jbSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveActionPerformed(evt);
            }
        });
        jbSave.setVisible(false);

        jbCancel.setText(Bundle.getMessage("ButtonCancel"));
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jbApply.setText(Bundle.getMessage("ButtonApply"));
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });

        prefsPane.setLayout(new GridBagLayout());
        prefsPane.setBorder(BorderFactory.createEmptyBorder());
        controlPane.setLayout(new GridBagLayout());
        controlPane.setBorder(BorderFactory.createEmptyBorder());

        prefsPane.add(cbAutoStartEngine, setConstraints(new Insets(2, 10, 2, 2), 0, 0, 2, GridBagConstraints.NONE)); //1
        prefsPane.add(cbAutoLoadVSDFile, setConstraints(new Insets(2, 10, 2, 2), 0, 1, 2, GridBagConstraints.NONE)); //2

        prefsPane.add(labelDefaultVSDFilePath, setConstraints(new Insets(2, 10, 2, 2), 0, 2, 1, GridBagConstraints.NONE)); //5
        prefsPane.add(tfDefaultVSDFilePath, setConstraints(new Insets(2, 10, 2, 2), 1, 2, 3, GridBagConstraints.HORIZONTAL)); //3
        prefsPane.add(jbPathBrowse, setConstraints(new Insets(2, 2, 2, 2), 5, 2, 1, GridBagConstraints.NONE)); //3

        prefsPane.add(labelDefaultVSDFileName, setConstraints(new Insets(2, 10, 2, 2), 0, 3, 1, GridBagConstraints.NONE)); //6
        prefsPane.add(tfDefaultVSDFileName, setConstraints(new Insets(2, 10, 2, 2), 1, 3, 2, GridBagConstraints.HORIZONTAL)); //4
        prefsPane.add(jbFileBrowse, setConstraints(new Insets(2, 2, 2, 2), 5, 3, 1, GridBagConstraints.NONE)); //3

        prefsPane.add(amPanel, setConstraints(new Insets(2, 10, 2, 2), 0, 6, 2, GridBagConstraints.HORIZONTAL));

        controlPane.add(jbSave, setConstraints(new Insets(5, 3, 5, 2), 2, 100, 1, GridBagConstraints.NONE)); //7
        controlPane.add(jbCancel, setConstraints(new Insets(5, 3, 5, 2), 0, 100, 1, GridBagConstraints.NONE)); //8
        controlPane.add(jbApply, setConstraints(new Insets(5, 3, 5, 5), 1, 100, 1, GridBagConstraints.NONE)); //9

        this.add(prefsPane, setConstraints(new Insets(2, 2, 2, 2), 0, 0, 1, GridBagConstraints.NONE));
        this.add(controlPane, setConstraints(new Insets(2, 2, 2, 2), 0, 1, 1, GridBagConstraints.NONE));

        this.setVisible(true);
    }

    private void setComponents(VSDecoderPreferences tp) {
        if (tp == null) {
            return;
        }
        cbAutoStartEngine.setSelected(tp.isAutoStartingEngine());
        cbAutoLoadVSDFile.setSelected(tp.isAutoLoadingDefaultVSDFile());
        tfDefaultVSDFilePath.setText(tp.getDefaultVSDFilePath());
        tfDefaultVSDFileName.setText(tp.getDefaultVSDFileName());
        switch (tp.getAudioMode()) {
            case HEADPHONES:
                audioModeHeadphoneButton.setSelected(true);
                break;
            case ROOM_AMBIENT:
            default:
                audioModeRoomButton.setSelected(true);
                break;
        }
    }

    private VSDecoderPreferences getVSDecoderPreferences() {
        VSDecoderPreferences tp = new VSDecoderPreferences();
        tp.setAutoStartEngine(cbAutoStartEngine.isSelected());
        tp.setAutoLoadDefaultVSDFile(cbAutoLoadVSDFile.isSelected());
        tp.setDefaultVSDFilePath(tfDefaultVSDFilePath.getText());
        tp.setDefaultVSDFileName(tfDefaultVSDFileName.getText());
        tp.setListenerPosition(VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition());
        if (audioModeRoomButton.isSelected()) {
            tp.setAudioMode(VSDecoderPreferences.AudioMode.ROOM_AMBIENT);
        } else if (audioModeHeadphoneButton.isSelected()) {
            tp.setAudioMode(VSDecoderPreferences.AudioMode.HEADPHONES);
        }

        return tp;
    }

    private void checkConsistency() {
        /*
         cbSaveThrottleOnLayoutSave.setEnabled( cbUseExThrottle.isSelected() );
         cbUseToolBar.setEnabled( cbUseExThrottle.isSelected() );
         cbUseFunctionIcon.setEnabled( cbUseExThrottle.isSelected() );
         cbEnableRosterSearch.setEnabled( cbUseExThrottle.isSelected() );
         cbEnableAutoLoad.setEnabled( cbUseExThrottle.isSelected() );
         cbUseRosterImage.setEnabled( cbUseExThrottle.isSelected() );
         cbResizeWinImg.setEnabled( cbUseExThrottle.isSelected()  &&  cbUseRosterImage.isSelected() );
         cbHideUndefinedButtons.setEnabled( cbUseExThrottle.isSelected() );
         cbIgnoreThrottlePosition.setEnabled( cbUseExThrottle.isSelected() && cbEnableAutoLoad.isSelected() );
         cbCleanOnDispose.setEnabled( cbUseExThrottle.isSelected() );
         if ( cbUseExThrottle.isSelected() ) {
         if ( cbUseToolBar.isSelected() ) {
         cbIgnoreThrottlePosition.setSelected( true );
         cbIgnoreThrottlePosition.setEnabled( false );
         }
         }
         */
    }

    private void jbPathBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        // Browse for a path.  Update the UI 
        // use the path currently in the window text field, if possible.
        String path;
        if (tfDefaultVSDFilePath.getText() != null) {
            path = tfDefaultVSDFilePath.getText();
        } else {
            path = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
        }
        final JFileChooser fc = new JFileChooser(path);
        jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter(Bundle.getMessage("LoadVSDDirectoryChooserFilterLabel"));
        fc.setFileFilter(filt);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int rv = fc.showOpenDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            try {
                tfDefaultVSDFilePath.setText(fc.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                // do nothing.
            }
        }
    }

    private void jbFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        // Browse for a file. Update the UI
        String path;
        if (tfDefaultVSDFilePath.getText() != null) {
            path = tfDefaultVSDFilePath.getText();
        } else {
            path = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
        }
        final JFileChooser fc = new JFileChooser(path);
        jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter(Bundle.getMessage("LoadVSDFileChooserFilterLabel"));
        filt.addExtension("vsd");
        filt.addExtension("zip");
        fc.setFileFilter(filt);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rv = fc.showOpenDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            try {
                URI base = null;
                if (tfDefaultVSDFilePath.getText() != null) {
                    base = FileUtil.findURL(tfDefaultVSDFilePath.getText()).toURI();
                } else {
                    base = FileUtil.findURL(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath()).toURI();
                }
                URI absolute = fc.getSelectedFile().toURI();
                URI relative = base.relativize(absolute);
                log.debug("URI absolute = {} relative = {}", absolute.toString(), relative.toString());

                tfDefaultVSDFileName.setText(relative.getPath());
            } catch (URISyntaxException ex) {
                log.warn("Unable to get URI for {}", path, ex);
            }
        }
    }

    private void jbApplyActionPerformed(java.awt.event.ActionEvent evt) {
        jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().set(getVSDecoderPreferences());
    }

    public void jbSaveActionPerformed(java.awt.event.ActionEvent evt) {
        jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().set(getVSDecoderPreferences());
        jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().save();
        if (m_container != null) {
            jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().removePropertyChangeListener(this);
            m_container.setVisible(false); // should do with events...
            m_container.dispose();
        }
    }

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {
        setComponents(jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences());
        checkConsistency();
        if (m_container != null) {
            jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().removePropertyChangeListener(this);
            m_container.setVisible(false); // should do with events...
            m_container.dispose();
        }
    }

    public void setContainer(JFrame f) {
        m_container = f;
        jbSave.setVisible(true);
        jbCancel.setText(Bundle.getMessage("ButtonCancel"));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt == null) || (evt.getPropertyName() == null)) {
            return;
        }
        if (evt.getPropertyName().compareTo("VSDecoderPreferences") == 0) {
            if ((evt.getNewValue() == null) || (!(evt.getNewValue() instanceof VSDecoderPreferences))) {
                return;
            }
            setComponents((VSDecoderPreferences) evt.getNewValue());
            checkConsistency();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoderPreferencesPane.class);
}
