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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
//import jmri.jmrit.vsdecoder.swing.Vector3dPanel;
//import jmri.jmrit.vsdecoder.swing.Vector2dPanel;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;

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
    //private jmri.util.PhysicalLocationPanel listenerPos;
    //private Vector3dPanel listenerPos;
    //private Vector2dPanel listenerOtn;
    private javax.swing.JRadioButton audioModeRoomButton;
    private javax.swing.JRadioButton audioModeHeadphoneButton;
    private javax.swing.ButtonGroup audioModeGroup;

    private javax.swing.JLabel	labelApplyWarning;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container = null;
       
    /** Creates new form VSDecoderPreferencesPane */
    public VSDecoderPreferencesPane(VSDecoderPreferences tp) {
        initComponents();
        setComponents(tp);
        checkConsistency();
        tp.addPropertyChangeListener(this);
    }
    
    public VSDecoderPreferencesPane() {
	this ( jmri.jmrit.vsdecoder.VSDecoderManager.instance().getVSDecoderPreferences() );
    }

    private GridBagConstraints setConstraints (Insets i, int x, int y, int width, int fill) {
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = i;
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = width;
	gbc.anchor = GridBagConstraints.LINE_START;
	gbc.fill = fill;
	return(gbc);
    } 
    
    private void initComponents() {

	JPanel prefsPane = new JPanel();
	JPanel controlPane = new JPanel();

	//this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	this.setLayout(new GridBagLayout());
	this.setBorder(BorderFactory.createEmptyBorder());
	
	
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

	//listenerPos = new Vector3dPanel("X", "Y", "Z", 0.0d, -1000.0d, 1000.0d, 1.0d);
	JPanel lpPanel = new JPanel();
	lpPanel.setLayout(new BoxLayout(lpPanel, BoxLayout.LINE_AXIS));
	lpPanel.add(new JLabel("Listener Position:"));
	//lpPanel.add(listenerPos);

	//listenerOtn = new Vector2dPanel("Bearing", "Azimuth", 0.0d, 0.0d, 360.0d, 1.0d);
	JPanel loPanel = new JPanel();
	loPanel.setLayout(new BoxLayout(loPanel, BoxLayout.LINE_AXIS));
	loPanel.add(new JLabel("Listener Orientation:"));
	//loPanel.add(listenerOtn);

	// Audio Mode
	audioModeRoomButton = new JRadioButton("Room Ambient");
	audioModeHeadphoneButton = new JRadioButton("Headphones");
	audioModeGroup = new ButtonGroup();
	audioModeGroup.add(audioModeRoomButton);
	audioModeGroup.add(audioModeHeadphoneButton);
	JPanel amPanel = new JPanel();
	amPanel.setLayout(new BoxLayout(amPanel, BoxLayout.LINE_AXIS));
	amPanel.add(new JLabel("Audio Mode:"));
	amPanel.add(audioModeRoomButton);
	amPanel.add(audioModeHeadphoneButton);

	// Get label strings from the resource bundle and assign it.
	cbAutoStartEngine.setText(vsdecoderBundle.getString("AutoStartEngine"));
	cbAutoLoadVSDFile.setText(vsdecoderBundle.getString("AutoLoadVSDFile"));
	tfDefaultVSDFilePath.setColumns(50);
	tfDefaultVSDFilePath.setColumns(50);
	labelDefaultVSDFilePath.setText(vsdecoderBundle.getString("DefaultVSDFilePath"));
	labelDefaultVSDFileName.setText(vsdecoderBundle.getString("DefaultVSDFileName"));
        labelApplyWarning.setText(vsdecoderBundle.getString("ExVSDecoderLabelApplyWarning"));

	// Set action listener to check consistency when the user makes changes.
        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	checkConsistency();
            }
        };
        cbAutoStartEngine.addActionListener(al);
        cbAutoLoadVSDFile.addActionListener(al);
        tfDefaultVSDFilePath.addActionListener(al);
        tfDefaultVSDFileName.addActionListener(al);

	// Set action listeners for save / cancel / reset buttons
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
        
        prefsPane.setLayout(new GridBagLayout());
	prefsPane.setBorder(BorderFactory.createEmptyBorder());
	//prefsPane.setMinimumSize(new Dimension(450, 250));
	//controlPane.setMinimumSize(new Dimension(450, 75));
	controlPane.setLayout(new GridBagLayout());
	controlPane.setBorder(BorderFactory.createEmptyBorder());
	//setMinimumSize(new Dimension(600, 1000));
	//setPreferredSize(new Dimension(600, 1000));
        
        prefsPane.add(cbAutoStartEngine, setConstraints(new Insets(2,10,2,2), 0, 0, 2, GridBagConstraints.NONE)); //1
        prefsPane.add(cbAutoLoadVSDFile, setConstraints(new Insets(2,10,2,2), 0, 1, 2, GridBagConstraints.NONE)); //2
        prefsPane.add(tfDefaultVSDFilePath, setConstraints(new Insets(2,10,2,2), 1, 2, 3, GridBagConstraints.HORIZONTAL)); //3
        prefsPane.add(tfDefaultVSDFileName, setConstraints(new Insets(2,10,2,2), 1, 3, 2, GridBagConstraints.HORIZONTAL)); //4
        prefsPane.add(labelDefaultVSDFilePath, setConstraints(new Insets(2,10,2,2), 0, 2, 1, GridBagConstraints.NONE)); //5
        prefsPane.add(labelDefaultVSDFileName, setConstraints(new Insets(2,10,2,2), 0, 3, 1, GridBagConstraints.NONE)); //6
	prefsPane.add(lpPanel, setConstraints(new Insets(2,10,2,2), 0, 4, 2, GridBagConstraints.HORIZONTAL)); //17
	prefsPane.add(loPanel, setConstraints(new Insets(2,10,2,2), 0, 5, 2, GridBagConstraints.HORIZONTAL)); //17
	prefsPane.add(amPanel, setConstraints(new Insets(2,10,2,2), 0, 6, 2, GridBagConstraints.HORIZONTAL));

	

        controlPane.add(jbSave, setConstraints(new Insets(5,3,5,2), 2, 100, 1, GridBagConstraints.NONE)); //7
        controlPane.add(jbCancel, setConstraints(new Insets(5,3,5,2), 0, 100, 1, GridBagConstraints.NONE)); //8
        controlPane.add(jbApply, setConstraints(new Insets(5,3,5,5), 1, 100, 1, GridBagConstraints.NONE)); //9

	this.add(prefsPane, setConstraints(new Insets(2,2,2,2), 0, 0, 1, GridBagConstraints.NONE ));
	this.add(controlPane, setConstraints(new Insets(2,2,2,2), 0, 1, 1, GridBagConstraints.NONE ));

	//this.setMinimumSize(new Dimension(Math.max(prefsPane.getPreferredSize().getWidth(), controlPane.getWidth())+20, prefsPane.getHeight()+controlPane.getHeight()+20));
	//this.setPreferredSize(new Dimension(Math.max(prefsPane.getPreferredSize().getWidth(), controlPane.getPreferredSize().getWidth())+40, 
	//				    prefsPane.getPreferredSize().getHeight()+controlPane.getPreferredSize().getHeight()+40));
	//this.setMinimumSize(new Dimension(800,600));
	this.setPreferredSize(new Dimension(800,600));
	this.setVisible(true);
    }

    private void setComponents(VSDecoderPreferences tp) {
    	if (tp==null) return;
    	cbAutoStartEngine.setSelected( tp.isAutoStartingEngine() );
        cbAutoLoadVSDFile.setSelected( tp.isAutoLoadingDefaultVSDFile() );
        tfDefaultVSDFilePath.setText( tp.getDefaultVSDFilePath() );
        tfDefaultVSDFileName.setText( tp.getDefaultVSDFileName() );
	switch(tp.getAudioMode()) {
	case HEADPHONES:
	    audioModeHeadphoneButton.setSelected(true);
	    break;
	case ROOM_AMBIENT:
	default:
	    audioModeRoomButton.setSelected(true);
	    break;
	}
	//tfListenerPosX.setText("" + tp.getListenerPosition().getX());
	//tfListenerPosY.setText("" + tp.getListenerPosition().getY());
	//tfListenerPosZ.setText("" + tp.getListenerPosition().getY());
    }
    
    private VSDecoderPreferences getVSDecoderPreferences()
    {
    	VSDecoderPreferences tp = new VSDecoderPreferences();
    	tp.setAutoStartEngine (cbAutoStartEngine.isSelected() );
    	tp.setAutoLoadDefaultVSDFile(cbAutoLoadVSDFile.isSelected() );
    	tp.setDefaultVSDFilePath(tfDefaultVSDFilePath.getText() );
    	tp.setDefaultVSDFileName(tfDefaultVSDFileName.getText());
	//tp.setListenerPosition(new ListeningSpot("Listener", listenerPos.getValue()));
	if (audioModeRoomButton.isSelected())
	    tp.setAudioMode(VSDecoderPreferences.AudioMode.ROOM_AMBIENT);
	else if (audioModeHeadphoneButton.isSelected())
	    tp.setAudioMode(VSDecoderPreferences.AudioMode.HEADPHONES);

    	return tp;
    }
    
    private void checkConsistency()
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
        jbCancel.setText(vsdecoderBundle.getString("VSDecoderPrefsCancel"));
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if ((evt == null) || (evt.getPropertyName() == null)) return;
		if (evt.getPropertyName().compareTo("VSDecoderPreferences") == 0) {
			if ((evt.getNewValue() == null) || (! (evt.getNewValue() instanceof VSDecoderPreferences))) return;
			setComponents((VSDecoderPreferences)evt.getNewValue());
			checkConsistency();
		}
	}

    // Unused - yet.
    //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPreferencesPane.class.getName());

}
