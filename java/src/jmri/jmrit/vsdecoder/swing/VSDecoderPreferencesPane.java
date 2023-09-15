package jmri.jmrit.vsdecoder.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.VSDecoderPreferences;

/**
 * Pane to show VSDecoder Preferences.
 *
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
 * @author Mark Underwood Copyright (C) 2011
 */
class VSDecoderPreferencesPane extends JPanel implements PropertyChangeListener {

    private javax.swing.JCheckBox cbAutoStartEngine;
    private javax.swing.JCheckBox cbAutoLoadVSDFile;
    private javax.swing.JCheckBox cbUseBlocks;
    private javax.swing.JTextField tfDefaultVSDFilePath;
    private javax.swing.JLabel labelDefaultVSDFilePath;

    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container = null;

    /**
     * Creates new form VSDecoderPreferencesPane
     * @param tp Preferences information
     */
    public VSDecoderPreferencesPane(VSDecoderPreferences tp) {
        initComponents();
        setComponents(tp);
        tp.addPropertyChangeListener(this);
    }

    public VSDecoderPreferencesPane() {
        this(VSDecoderManager.instance().getVSDecoderPreferences());
    }

    private GridBagConstraints setConstraints(Insets i, int x, int y, int width, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = i;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = fill;
        return gbc;
    }

    private void initComponents() {

        JPanel prefsPane = new JPanel();
        JPanel controlPane = new JPanel();

        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder());

        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbApply = new javax.swing.JButton();

        labelDefaultVSDFilePath = new javax.swing.JLabel();

        cbAutoStartEngine = new javax.swing.JCheckBox();
        cbAutoLoadVSDFile = new javax.swing.JCheckBox();
        cbUseBlocks       = new javax.swing.JCheckBox();
        tfDefaultVSDFilePath = new javax.swing.JTextField(40);
        JButton jbPathBrowse = new javax.swing.JButton(Bundle.getMessage("Browse"));
        jbPathBrowse.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbPathBrowseActionPerformed(evt);
            }
        });

        // Get label strings from the resource bundle and assign it.
        cbAutoStartEngine.setText(Bundle.getMessage("AutoStartEngine"));
        cbAutoLoadVSDFile.setText(Bundle.getMessage("AutoLoadVSDFile"));
        cbUseBlocks.setText(Bundle.getMessage("UseBlocks"));
        tfDefaultVSDFilePath.setColumns(30);
        labelDefaultVSDFilePath.setText(Bundle.getMessage("DefaultVSDFilePath"));

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
        prefsPane.add(cbUseBlocks, setConstraints(new Insets(2, 10, 2, 2), 0, 2, 2, GridBagConstraints.NONE)); //3

        prefsPane.add(labelDefaultVSDFilePath, setConstraints(new Insets(2, 10, 2, 2), 0, 3, 1, GridBagConstraints.NONE)); //4
        prefsPane.add(tfDefaultVSDFilePath, setConstraints(new Insets(2, 10, 2, 2), 1, 3, 3, GridBagConstraints.HORIZONTAL)); //4
        prefsPane.add(jbPathBrowse, setConstraints(new Insets(2, 2, 2, 2), 5, 3, 1, GridBagConstraints.NONE)); //4

        controlPane.add(jbSave, setConstraints(new Insets(5, 3, 5, 2), 2, 100, 1, GridBagConstraints.NONE)); //5
        controlPane.add(jbCancel, setConstraints(new Insets(5, 3, 5, 2), 0, 100, 1, GridBagConstraints.NONE)); //6
        controlPane.add(jbApply, setConstraints(new Insets(5, 3, 5, 5), 1, 100, 1, GridBagConstraints.NONE)); //7

        this.add(prefsPane, setConstraints(new Insets(2, 2, 2, 2), 0, 0, 1, GridBagConstraints.NONE));
        this.add(controlPane, setConstraints(new Insets(2, 2, 2, 2), 0, 1, 1, GridBagConstraints.NONE));

        this.setVisible(true);
    }

    private void setComponents(VSDecoderPreferences tp) {
        if (tp == null) {
            return;
        }
        cbAutoStartEngine.setSelected(tp.isAutoStartingEngine());
        cbAutoLoadVSDFile.setSelected(tp.isAutoLoadingVSDFile());
        cbUseBlocks.setSelected(tp.getUseBlocksSetting());
        tfDefaultVSDFilePath.setText(tp.getDefaultVSDFilePath());
    }

    private VSDecoderPreferences getVSDecoderPreferences() {
        VSDecoderPreferences tp = new VSDecoderPreferences();
        tp.setAutoStartEngine(cbAutoStartEngine.isSelected());
        tp.setAutoLoadVSDFile(cbAutoLoadVSDFile.isSelected());
        tp.setUseBlocksSetting(cbUseBlocks.isSelected());
        tp.setDefaultVSDFilePath(tfDefaultVSDFilePath.getText());
        tp.setListenerPosition(VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition());
        tp.setMasterVolume(VSDecoderManager.instance().getVSDecoderPreferences().getMasterVolume());
        return tp;
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
        final JFileChooser fc = new jmri.util.swing.JmriJFileChooser(jmri.util.FileUtil.getExternalFilename(path));
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

    private void jbApplyActionPerformed(java.awt.event.ActionEvent evt) {
        VSDecoderManager.instance().getVSDecoderPreferences().set(getVSDecoderPreferences());
    }

    private void jbSaveActionPerformed(java.awt.event.ActionEvent evt) {
        VSDecoderManager.instance().getVSDecoderPreferences().set(getVSDecoderPreferences());
        VSDecoderManager.instance().getVSDecoderPreferences().save();
        if (m_container != null) {
            VSDecoderManager.instance().getVSDecoderPreferences().removePropertyChangeListener(this);
            m_container.setVisible(false); // should do with events...
            m_container.dispose();
        }
    }

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {
        setComponents(VSDecoderManager.instance().getVSDecoderPreferences());
        if (m_container != null) {
            VSDecoderManager.instance().getVSDecoderPreferences().removePropertyChangeListener(this);
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
        if (evt.getPropertyName().equals("VSDecoderPreferences")) {
            if ((evt.getNewValue() != null) && (evt.getNewValue() instanceof VSDecoderPreferences)) {
                setComponents((VSDecoderPreferences) evt.getNewValue());
            }
        }
    }

}
