package jmri.jmrit.vsdecoder;

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

import java.util.ResourceBundle;
import javax.swing.JFrame;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author lionel
 * Modified for VSDecoder by twindad
 */
public class VSDecoderPreferencesPane extends javax.swing.JPanel implements PropertyChangeListener {
    private static final long serialVersionUID = -5473594799045080011L;
	
    private static final ResourceBundle vsdecoderBundle = VSDecoderBundle.bundle();

    private javax.swing.JCheckBox cbAutoStartEngine;
    private javax.swing.JCheckBox cbAutoLoadVSDFile;
    private javax.swing.JTextField  tfDefaultVSDFilePath;
    private javax.swing.JTextField  tfDefaultVSDFileName;
    private javax.swing.JLabel labelDefaultVSDFilePath;
    private javax.swing.JLabel labelDefaultVSDFileName;

    private javax.swing.JLabel	labelApplyWarning;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container = null;
       
    /** Creates new form VSDecoderPreferencesPane */
    public VSDecoderPreferencesPane(VSDecoderPreferences tp) {
        initComponents();
        setComponents(tp);
        checkConsistancy();
        tp.addPropertyChangeListener(this);
    }
    
    public VSDecoderPreferencesPane() {
	this ( jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences() );
    }
    
    private void initComponents() {
	
	GridBagConstraints gridBagConstraints1 = new GridBagConstraints(); // Auto Start Engine
        gridBagConstraints1.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridwidth = 2;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints(); // Auto Load Default VSD File
        gridBagConstraints2.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints2.gridx = 0;
	gridBagConstraints2.gridwidth = 2;
	GridBagConstraints gridBagConstraints5 = new GridBagConstraints(); // Default VSD File Path label
        gridBagConstraints5.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints5.gridx = 0;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints(); // Default VSD File Path textfield
        gridBagConstraints3.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints3.gridx = 1;
	gridBagConstraints3.gridwidth = 2;
	gridBagConstraints3.fill=GridBagConstraints.HORIZONTAL;
	GridBagConstraints gridBagConstraints6 = new GridBagConstraints(); // Default VSD File name label
        gridBagConstraints6.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints6.gridy = 3;
        gridBagConstraints6.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints6.gridx = 0;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints(); // Default VSD File Name textfield
        gridBagConstraints4.insets = new Insets(2, 10, 2, 2);
        gridBagConstraints4.gridy = 3;
        gridBagConstraints4.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints4.gridx = 1;
	gridBagConstraints4.gridwidth = 2;
	gridBagConstraints4.fill=GridBagConstraints.HORIZONTAL;
        
        // last line: buttons
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints(); // Apply
        gridBagConstraints9.insets = new Insets(5, 3, 5, 5);
        gridBagConstraints9.gridy = 100;
        gridBagConstraints9.gridx = 1;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints(); // Cancel
        gridBagConstraints8.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints8.gridy = 100;
        gridBagConstraints8.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints8.gridx = 0;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints(); // Save
        gridBagConstraints7.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints7.gridy = 100;
        gridBagConstraints7.gridx = 2;
        
        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbApply = new javax.swing.JButton();

	cbAutoStartEngine = new javax.swing.JCheckBox();
	cbAutoLoadVSDFile = new javax.swing.JCheckBox();
	tfDefaultVSDFilePath = new javax.swing.JTextField(40);
	tfDefaultVSDFileName = new javax.swing.JTextField(40);
        
        labelApplyWarning = new javax.swing.JLabel();
	labelDefaultVSDFilePath = new javax.swing.JLabel();
	labelDefaultVSDFileName = new javax.swing.JLabel();

	cbAutoStartEngine.setText(vsdecoderBundle.getString("AutoStartEngine"));
	cbAutoLoadVSDFile.setText(vsdecoderBundle.getString("AutoLoadVSDFile"));
	tfDefaultVSDFilePath.setColumns(50);
	tfDefaultVSDFilePath.setColumns(50);
	labelDefaultVSDFilePath.setText(vsdecoderBundle.getString("DefaultVSDFilePath"));
	labelDefaultVSDFileName.setText(vsdecoderBundle.getString("DefaultVSDFileName"));
        
        labelApplyWarning.setText(vsdecoderBundle.getString("ExVSDecoderLabelApplyWarning"));

        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	checkConsistancy();
            }
        };
        cbAutoStartEngine.addActionListener(al);
        cbAutoLoadVSDFile.addActionListener(al);
        tfDefaultVSDFilePath.addActionListener(al);
        tfDefaultVSDFileName.addActionListener(al);

        jbSave.setText(vsdecoderBundle.getString("VSDecoderPrefsSave"));
        jbSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveActionPerformed(evt);
            }
        });
        jbSave.setVisible(false);
        
        jbCancel.setText(vsdecoderBundle.getString("VSDecoderPrefsReset"));
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
       
        jbApply.setText(vsdecoderBundle.getString("VSDecoderPrefsApply"));
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });
        
        setLayout(new GridBagLayout());
        
        this.add(cbAutoStartEngine, gridBagConstraints1);
        this.add(cbAutoLoadVSDFile, gridBagConstraints2);
        this.add(tfDefaultVSDFilePath, gridBagConstraints3);
        this.add(tfDefaultVSDFileName, gridBagConstraints4);
        this.add(labelDefaultVSDFilePath, gridBagConstraints5);
        this.add(labelDefaultVSDFileName, gridBagConstraints6);
        this.add(jbSave, gridBagConstraints7);
        this.add(jbCancel, gridBagConstraints8);
        this.add(jbApply, gridBagConstraints9);
    }

    private void setComponents(VSDecoderPreferences tp) {
    	if (tp==null) return;
    	cbAutoStartEngine.setSelected( tp.isAutoStartingEngine() );
        cbAutoLoadVSDFile.setSelected( tp.isAutoLoadingDefaultVSDFile() );
        tfDefaultVSDFilePath.setText( tp.getDefaultVSDFilePath() );
        tfDefaultVSDFileName.setText( tp.getDefaultVSDFileName() );
    }
    
    private VSDecoderPreferences getVSDecoderPreferences()
    {
    	VSDecoderPreferences tp = new VSDecoderPreferences();
    	tp.setAutoStartEngine (cbAutoStartEngine.isSelected() );
    	tp.setAutoLoadDefaultVSDFile(cbAutoLoadVSDFile.isSelected() );
    	tp.setDefaultVSDFilePath(tfDefaultVSDFilePath.getText() );
    	tp.setDefaultVSDFileName(tfDefaultVSDFileName.getText());
    	return tp;
    }
    
    private void checkConsistancy()
    {
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
        checkConsistancy();
    	if (m_container != null) {
    		jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences().removePropertyChangeListener(this);
    		m_container.setVisible(false); // should do with events...
    		m_container.dispose();
    	}
    }

	public void setContainer(JFrame f) {
		m_container = f;
        jbSave.setVisible(true);
        jbCancel.setText(vsdecoderBundle.getString("VSDecoderPrefsCancel"));
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPreferencesPane.class.getName());

	public void propertyChange(PropertyChangeEvent evt) {
		if ((evt == null) || (evt.getPropertyName() == null)) return;
		if (evt.getPropertyName().compareTo("VSDecoderPreferences") == 0) {
			if ((evt.getNewValue() == null) || (! (evt.getNewValue() instanceof VSDecoderPreferences))) return;
			setComponents((VSDecoderPreferences)evt.getNewValue());
			checkConsistancy();
		}
	}
}
