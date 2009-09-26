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
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottlesPreferencesPane.class.getName());

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
        java.awt.GridBagConstraints gridBagConstraints;

        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints11.anchor = GridBagConstraints.WEST;
        gridBagConstraints11.gridy = 6;
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
        gridBagConstraints9.insets = new Insets(5, 3, 5, 5);
        gridBagConstraints9.gridy = 7;
        gridBagConstraints9.gridx = 1;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints8.gridy = 7;
        gridBagConstraints8.anchor = GridBagConstraints.WEST;
        gridBagConstraints8.gridx = 0;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints7.gridy = 7;
        gridBagConstraints7.gridx = 8;
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints6.gridy = 5;
        gridBagConstraints6.anchor = GridBagConstraints.WEST;
        gridBagConstraints6.gridx = 0;
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

        setLayout(new GridBagLayout());

        cbUseExThrottle.setText(throttleBundle.getString("UseExThrottle")); // NOI18N
        cbUseExThrottle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbUseExThrottleActionPerformed(evt);
            }
        });
        cbUseTransparentCtl.setText(throttleBundle.getString("ExThrottleTransparence")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 20, 0, 0);
        cb1Win4all.setText("One window for all throttle");
        cb1Win4all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb1Win4allActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 20, 0, 0);
 // TODO       add(cb1Win4all, gridBagConstraints);

        cbUseAdvTransition.setText(throttleBundle.getString("ExThrottleAdvTransition")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 40, 0, 0);
// TODO        add(cbUseAdvTransition, gridBagConstraints);

        cbUseRosterImage.setText("Use roster image as background");
        cbUseRosterImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbUseRosterImageActionPerformed(evt);
            }
        });
        cbResizeWinImg.setText(throttleBundle.getString("ExThrottleForceResize")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 40, 0, 0);
        
        cbEnableRosterSearch.setText(throttleBundle.getString("ExThrottleEnableRosterSearch")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 20, 0, 0);
        cbEnableAutoLoad.setText(throttleBundle.getString("ExThrottleEnableAutoSave")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 20, 0, 0);
        
        cbHideUndefinedButtons.setText(throttleBundle.getString("ExThrottleHideUndefinedFunctionButtons")); // NOI18N
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 3, 18, 9);
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 3, 18, 0);
       
        jbApply.setText(throttleBundle.getString("ThrottlesPrefsApply"));
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 18, 0);
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
    }
    
    private void cbUseExThrottleActionPerformed(java.awt.event.ActionEvent evt) {
    	 checkConsistancy();
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

    private void cbUseRosterImageActionPerformed(java.awt.event.ActionEvent evt) {
    	checkConsistancy();
    }

    private void cb1Win4allActionPerformed(java.awt.event.ActionEvent evt) {
        checkConsistancy();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cb1Win4all;
    private javax.swing.JCheckBox cbResizeWinImg;
    private javax.swing.JCheckBox cbUseAdvTransition;
    private javax.swing.JCheckBox cbUseExThrottle;
    private javax.swing.JCheckBox cbUseRosterImage;
    private javax.swing.JCheckBox cbUseTransparentCtl;
    private javax.swing.JCheckBox cbEnableRosterSearch;
    private javax.swing.JCheckBox cbEnableAutoLoad;
    private javax.swing.JCheckBox cbHideUndefinedButtons;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container;
    // End of variables declaration//GEN-END:variables

	public void setContainer(JFrame f) {
		m_container = f;
	}

}  //  @jve:decl-index=0:visual-constraint="72,45"
