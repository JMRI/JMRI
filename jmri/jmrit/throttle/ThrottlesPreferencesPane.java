/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ThrottlesPreferencesPane.java
 *
 * Created on 17 juil. 2009, 15:05:26
 */

package jmri.jmrit.throttle;

import java.util.ResourceBundle;
import javax.swing.JFrame;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 *
 * @author lionel
 */
public class ThrottlesPreferencesPane extends javax.swing.JPanel {
	private static final long serialVersionUID = -5473594799045080011L;
	
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");
    private javax.swing.JCheckBox cb1Win4all;
    private javax.swing.JCheckBox cbResizeWinImg;
    private javax.swing.JCheckBox cbUseAdvTransition;
    private javax.swing.JCheckBox cbUseExThrottle;
    private javax.swing.JCheckBox cbUseRosterImage;
    private javax.swing.JCheckBox cbUseTransparentCtl;
    private javax.swing.JCheckBox cbEnableRosterSearch;
    private javax.swing.JCheckBox cbEnableAutoLoad;
    private javax.swing.JCheckBox cbHideUndefinedButtons;
    private javax.swing.JCheckBox cbIgnoreThrottlePosition;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container;
    
    /** Creates new form ThrottlesPreferencesPane */
    public ThrottlesPreferencesPane(ThrottlesPreferences tp) {
        initComponents();
        setComponents(tp);
        checkConsistancy();
    }
    
    public ThrottlesPreferencesPane() {
	    this ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences() );
	}

    private void initComponents() {

    	GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
    	gridBagConstraints12.gridx = 0;
        gridBagConstraints12.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints12.anchor = GridBagConstraints.WEST;
        gridBagConstraints12.gridy = 8;
    	
    	GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints11.anchor = GridBagConstraints.WEST;
        gridBagConstraints11.gridy = 7;

        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints6.gridy = 5;
        gridBagConstraints6.anchor = GridBagConstraints.WEST;
        gridBagConstraints6.gridx = 0;
        GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
        gridBagConstraints10.insets = new Insets(2, 43, 2, 2);
        gridBagConstraints10.gridy = 6;
        gridBagConstraints10.anchor = GridBagConstraints.WEST;
        gridBagConstraints10.gridx = 0;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints5.gridy = 4;
        gridBagConstraints5.anchor = GridBagConstraints.WEST;
        gridBagConstraints5.gridx = 0;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.insets = new Insets(2, 43, 2, 2);
        gridBagConstraints4.gridy = 3;
        gridBagConstraints4.anchor = GridBagConstraints.WEST;
        gridBagConstraints4.gridx = 0;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.anchor = GridBagConstraints.WEST;
        gridBagConstraints3.gridx = 0;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.gridx = 0;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.insets = new Insets(8, 5, 2, 2);
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 0;
        
        // last line: buttons
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
        gridBagConstraints9.insets = new Insets(5, 3, 5, 5);
        gridBagConstraints9.gridy = 9;
        gridBagConstraints9.gridx = 1;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints8.gridy = 9;
        gridBagConstraints8.anchor = GridBagConstraints.WEST;
        gridBagConstraints8.gridx = 0;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints7.gridy = 9;
        gridBagConstraints7.gridx = 8;
        
        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbApply = new javax.swing.JButton();
        
        cbUseExThrottle = new javax.swing.JCheckBox();
        cbUseTransparentCtl = new javax.swing.JCheckBox();
        cb1Win4all = new javax.swing.JCheckBox();
        cbUseAdvTransition = new javax.swing.JCheckBox();
        cbUseRosterImage = new javax.swing.JCheckBox();
        cbResizeWinImg = new javax.swing.JCheckBox();
        cbEnableRosterSearch = new javax.swing.JCheckBox();
        cbEnableAutoLoad  = new javax.swing.JCheckBox();
        cbHideUndefinedButtons = new javax.swing.JCheckBox();
        cbIgnoreThrottlePosition = new javax.swing.JCheckBox();

        
        cbUseExThrottle.setText(throttleBundle.getString("UseExThrottle"));
        cbUseTransparentCtl.setText(throttleBundle.getString("ExThrottleTransparence"));
        cbUseAdvTransition.setText(throttleBundle.getString("ExThrottleAdvTransition"));
        cbResizeWinImg.setText(throttleBundle.getString("ExThrottleForceResize"));
        cb1Win4all.setText(throttleBundle.getString("ExThrottle1win4all"));
        cbUseRosterImage.setText(throttleBundle.getString("ExThrottleUseRosterImageBkg"));
        cbEnableRosterSearch.setText(throttleBundle.getString("ExThrottleEnableRosterSearch"));
        cbEnableAutoLoad.setText(throttleBundle.getString("ExThrottleEnableAutoSave"));
        cbHideUndefinedButtons.setText(throttleBundle.getString("ExThrottleHideUndefinedFunctionButtons")); 
        cbIgnoreThrottlePosition.setText(throttleBundle.getString("ExThrottleIgnoreThrottlePosition"));
                
        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	checkConsistancy();
            }
        };
        cbUseExThrottle.addActionListener(al);
        cb1Win4all.addActionListener(al);
        cbUseRosterImage.addActionListener(al);
        cbEnableAutoLoad.addActionListener(al);

        jbSave.setText(throttleBundle.getString("ThrottlesPrefsSave"));
        jbSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveActionPerformed(evt);
            }
        });
        
        jbCancel.setText(throttleBundle.getString("ThrottlesPrefsCancel"));
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
       
        jbApply.setText(throttleBundle.getString("ThrottlesPrefsApply"));
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });
        
        setLayout(new GridBagLayout());
        
        this.add(cbUseExThrottle, gridBagConstraints1);
        this.add(cbUseTransparentCtl, gridBagConstraints2);
        this.add(cbUseRosterImage, gridBagConstraints3);
        this.add(cbResizeWinImg, gridBagConstraints4);
        this.add(cbEnableRosterSearch, gridBagConstraints5);
        this.add(cbEnableAutoLoad, gridBagConstraints6);
        this.add(jbSave, gridBagConstraints7);
        this.add(jbCancel, gridBagConstraints8);
        this.add(jbApply, gridBagConstraints9);
        this.add(cbHideUndefinedButtons, gridBagConstraints11);
        this.add(cbIgnoreThrottlePosition, gridBagConstraints10);
        this.add(cb1Win4all, gridBagConstraints12);
     // TODO       add(cbUseAdvTransition, gridBagConstraints);
    }

    private void setComponents(ThrottlesPreferences tp) {
        cbUseTransparentCtl.setSelected( tp.isUsingTransparentCtl() );
        cbUseAdvTransition.setSelected( tp.isUsingAdvTransition() );
        cbResizeWinImg.setSelected( tp.isResizingWindow() );
        cb1Win4all.setSelected( tp.isOneWindowForAll() );
        cbUseRosterImage.setSelected( tp.isUsingRosterImage() );
        cbUseExThrottle.setSelected( tp.isUsingExThrottle() );
        cbEnableRosterSearch.setSelected( tp.isEnablingRosterSearch() );
        cbEnableAutoLoad.setSelected( tp.isAutoLoading() );
        cbHideUndefinedButtons.setSelected( tp.isHidingUndefinedFuncButt() );
        cbIgnoreThrottlePosition.setSelected( tp.isIgnoringThrottlePosition() );
    }
    
    private ThrottlesPreferences getThrottlesPreferences()
    {
    	ThrottlesPreferences tp = new ThrottlesPreferences();
    	tp.setUseExThrottle (cbUseExThrottle.isSelected() );
    	tp.setOneWindowForAll(cb1Win4all.isSelected() );
    	tp.setResizeWindow(cbResizeWinImg.isSelected());
    	tp.setUseAdvTransition(cbUseAdvTransition.isSelected());
    	tp.setUseRosterImage(cbUseRosterImage.isSelected());
    	tp.setUseTransparentCtl( cbUseTransparentCtl.isSelected() ); 
    	tp.setEnableRosterSearch( cbEnableRosterSearch.isSelected() );
    	tp.setAutoLoad( cbEnableAutoLoad.isSelected() );
    	tp.setHideUndefinedFuncButt( cbHideUndefinedButtons.isSelected() );
    	tp.setIgnoreThrottlePosition( cbIgnoreThrottlePosition.isSelected() );
    	return tp;
    }
    
    private void checkConsistancy()
    {
        cbUseTransparentCtl.setEnabled( cbUseExThrottle.isSelected() );
        cb1Win4all.setEnabled( cbUseExThrottle.isSelected() );
        cbEnableRosterSearch.setEnabled( cbUseExThrottle.isSelected() );
        cbEnableAutoLoad.setEnabled( cbUseExThrottle.isSelected() );
        cbUseAdvTransition.setEnabled( cbUseExThrottle.isSelected() && cb1Win4all.isSelected() );
        cbUseRosterImage.setEnabled( cbUseExThrottle.isSelected() );
        cbResizeWinImg.setEnabled( cbUseExThrottle.isSelected() &&  cbUseRosterImage.isSelected() );
        cbHideUndefinedButtons.setEnabled( cbUseExThrottle.isSelected() );
        cbIgnoreThrottlePosition.setEnabled( cbUseExThrottle.isSelected() && cbEnableAutoLoad.isSelected() );
        if ( cbUseExThrottle.isSelected() ) {
        	if ( cb1Win4all.isSelected() ) {
        		cbResizeWinImg.setSelected( false );
        		cbResizeWinImg.setEnabled( false );
        		cbIgnoreThrottlePosition.setSelected( true );
        		cbIgnoreThrottlePosition.setEnabled( false );
        	}
        }
    }

    private void jbApplyActionPerformed(java.awt.event.ActionEvent evt) {
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().set(getThrottlesPreferences());
    }

    private void jbSaveActionPerformed(java.awt.event.ActionEvent evt) {
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().set(getThrottlesPreferences());
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().save();
    	m_container.setVisible(false); // should do with events...
    	m_container.dispose();
    }

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {
    	m_container.setVisible(false); // should do with events...
    	m_container.dispose();
    }

	public void setContainer(JFrame f) {
		m_container = f;
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottlesPreferencesPane.class.getName());
}
