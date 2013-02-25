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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JFrame;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author lionel
 */
public class ThrottlesPreferencesPane extends javax.swing.JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = -5473594799045080011L;
	
    private javax.swing.JCheckBox cbUseToolBar;
    private javax.swing.JCheckBox cbUseFunctionIcon;
    private javax.swing.JCheckBox cbResizeWinImg;
    private javax.swing.JCheckBox cbUseExThrottle;
    private javax.swing.JCheckBox cbUseRosterImage;
    private javax.swing.JCheckBox cbEnableRosterSearch;
    private javax.swing.JCheckBox cbEnableAutoLoad;
    private javax.swing.JCheckBox cbHideUndefinedButtons;
    private javax.swing.JCheckBox cbIgnoreThrottlePosition;
    private javax.swing.JCheckBox cbSaveThrottleOnLayoutSave;
    private javax.swing.JLabel	labelApplyWarning;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbSave;
    private JFrame m_container = null;
       
    /** Creates new form ThrottlesPreferencesPane */
    public ThrottlesPreferencesPane() {
        if(jmri.InstanceManager.getDefault(jmri.jmrit.throttle.ThrottlesPreferences.class)==null){
            jmri.InstanceManager.store(new jmri.jmrit.throttle.ThrottlesPreferences(), jmri.jmrit.throttle.ThrottlesPreferences.class);
        }
        ThrottlesPreferences tp = jmri.InstanceManager.getDefault(jmri.jmrit.throttle.ThrottlesPreferences.class);
        initComponents();
        setComponents(tp);
        checkConsistancy();
        tp.addPropertyChangeListener(this);
	}

    private void initComponents() {

        
    	GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
    	gridBagConstraints13.gridx = 0;
        gridBagConstraints13.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints13.ipady = 16;
        gridBagConstraints13.anchor = GridBagConstraints.WEST;
        gridBagConstraints13.gridy = 99;
        
        GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
    	gridBagConstraints14.gridx = 0;
        gridBagConstraints14.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints14.anchor = GridBagConstraints.WEST;
        gridBagConstraints14.gridy = 10;   	
       
    	GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
    	gridBagConstraints12.gridx = 0;
        gridBagConstraints12.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints12.anchor = GridBagConstraints.WEST;
        gridBagConstraints12.gridy = 9;
        
        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
    	gridBagConstraints15.gridx = 0;
        gridBagConstraints15.insets = new Insets(2, 23, 2, 2);
        gridBagConstraints15.anchor = GridBagConstraints.WEST;
        gridBagConstraints15.gridy = 8;
        
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
        gridBagConstraints9.gridy = 100;
        gridBagConstraints9.gridx = 1;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints8.gridy = 100;
        gridBagConstraints8.anchor = GridBagConstraints.WEST;
        gridBagConstraints8.gridx = 0;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.insets = new Insets(5, 3, 5, 2);
        gridBagConstraints7.gridy = 100;
        gridBagConstraints7.gridx = 8;
        
        jbCancel = new javax.swing.JButton();
        jbSave = new javax.swing.JButton();
        jbApply = new javax.swing.JButton();
        
        cbUseExThrottle = new javax.swing.JCheckBox();
        cbUseToolBar = new javax.swing.JCheckBox();
        cbUseFunctionIcon = new javax.swing.JCheckBox();
        cbUseRosterImage = new javax.swing.JCheckBox();
        cbResizeWinImg = new javax.swing.JCheckBox();
        cbEnableRosterSearch = new javax.swing.JCheckBox();
        cbEnableAutoLoad  = new javax.swing.JCheckBox();
        cbHideUndefinedButtons = new javax.swing.JCheckBox();
        cbIgnoreThrottlePosition = new javax.swing.JCheckBox();
        cbSaveThrottleOnLayoutSave = new javax.swing.JCheckBox();

        labelApplyWarning = new javax.swing.JLabel();
        
        cbUseExThrottle.setText(Bundle.getMessage("UseExThrottle"));
        cbResizeWinImg.setText(Bundle.getMessage("ExThrottleForceResize"));
        cbUseToolBar.setText(Bundle.getMessage("ExThrottleUseToolBar"));
        cbUseFunctionIcon.setText(Bundle.getMessage("ExThrottleUseFunctionIcons"));
        cbUseRosterImage.setText(Bundle.getMessage("ExThrottleUseRosterImageBkg"));
        cbEnableRosterSearch.setText(Bundle.getMessage("ExThrottleEnableRosterSearch"));
        cbEnableAutoLoad.setText(Bundle.getMessage("ExThrottleEnableAutoSave"));
        cbHideUndefinedButtons.setText(Bundle.getMessage("ExThrottleHideUndefinedFunctionButtons")); 
        cbIgnoreThrottlePosition.setText(Bundle.getMessage("ExThrottleIgnoreThrottlePosition"));         
        labelApplyWarning.setText(Bundle.getMessage("ExThrottleLabelApplyWarning"));
        cbSaveThrottleOnLayoutSave.setText(Bundle.getMessage("ExThrottleSaveThrottleOnLayoutSave"));

        java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	checkConsistancy();
            }
        };
        cbUseExThrottle.addActionListener(al);
        cbUseToolBar.addActionListener(al);
        cbUseRosterImage.addActionListener(al);
        cbEnableAutoLoad.addActionListener(al);

        jbSave.setText(Bundle.getMessage("ThrottlesPrefsSave"));
        jbSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveActionPerformed(evt);
            }
        });
        jbSave.setVisible(false);
        
        jbCancel.setText(Bundle.getMessage("ThrottlesPrefsReset"));
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
       
        jbApply.setText(Bundle.getMessage("ThrottlesPrefsApply"));
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });
        
        setLayout(new GridBagLayout());
        
        this.add(cbUseExThrottle, gridBagConstraints1);
        this.add(cbSaveThrottleOnLayoutSave, gridBagConstraints2);
        this.add(cbUseRosterImage, gridBagConstraints3);
        this.add(cbResizeWinImg, gridBagConstraints4);
        this.add(cbEnableRosterSearch, gridBagConstraints5);
        this.add(cbEnableAutoLoad, gridBagConstraints6);
        this.add(jbSave, gridBagConstraints7);
        this.add(jbCancel, gridBagConstraints8);
        this.add(jbApply, gridBagConstraints9);
        this.add(cbHideUndefinedButtons, gridBagConstraints11);
        this.add(cbIgnoreThrottlePosition, gridBagConstraints10);
        this.add(cbUseToolBar, gridBagConstraints12);
        this.add(cbUseFunctionIcon, gridBagConstraints14);
        this.add(labelApplyWarning, gridBagConstraints13);
    }

    private void setComponents(ThrottlesPreferences tp) {
    	if (tp==null) return;
    	cbSaveThrottleOnLayoutSave.setSelected( tp.isSavingThrottleOnLayoutSave() );
        cbResizeWinImg.setSelected( tp.isResizingWindow() );
        cbUseToolBar.setSelected( tp.isUsingToolBar() );
        cbUseFunctionIcon.setSelected( tp.isUsingFunctionIcon() );
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
    	tp.setUsingToolBar(cbUseToolBar.isSelected() );
    	tp.setUsingFunctionIcon(cbUseFunctionIcon.isSelected() );
    	tp.setResizeWindow(cbResizeWinImg.isSelected());
    	tp.setUseRosterImage(cbUseRosterImage.isSelected());
    	tp.setSaveThrottleOnLayoutSave( cbSaveThrottleOnLayoutSave.isSelected() ); 
    	tp.setEnableRosterSearch( cbEnableRosterSearch.isSelected() );
    	tp.setAutoLoad( cbEnableAutoLoad.isSelected() );
    	tp.setHideUndefinedFuncButt( cbHideUndefinedButtons.isSelected() );
    	tp.setIgnoreThrottlePosition( cbIgnoreThrottlePosition.isSelected() );
    	return tp;
    }
    
    private void checkConsistancy()
    {
    	cbSaveThrottleOnLayoutSave.setEnabled( cbUseExThrottle.isSelected() );
        cbUseToolBar.setEnabled( cbUseExThrottle.isSelected() );
        cbUseFunctionIcon.setEnabled( cbUseExThrottle.isSelected() );
        cbEnableRosterSearch.setEnabled( cbUseExThrottle.isSelected() );
        cbEnableAutoLoad.setEnabled( cbUseExThrottle.isSelected() );
        cbUseRosterImage.setEnabled( cbUseExThrottle.isSelected() );
        cbResizeWinImg.setEnabled( cbUseExThrottle.isSelected()  &&  cbUseRosterImage.isSelected() );
        cbHideUndefinedButtons.setEnabled( cbUseExThrottle.isSelected() );
        cbIgnoreThrottlePosition.setEnabled( cbUseExThrottle.isSelected() && cbEnableAutoLoad.isSelected() );
        if ( cbUseExThrottle.isSelected() ) {
        	if ( cbUseToolBar.isSelected() ) {
        		cbIgnoreThrottlePosition.setSelected( true );
        		cbIgnoreThrottlePosition.setEnabled( false );
        	}
        }
    }

    private void jbApplyActionPerformed(java.awt.event.ActionEvent evt) {
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().set(getThrottlesPreferences());
    }

    public void jbSaveActionPerformed(java.awt.event.ActionEvent evt) {
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().set(getThrottlesPreferences());
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().save();
    	if (m_container != null) {
    		jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().removePropertyChangeListener(this);
    		m_container.setVisible(false); // should do with events...
    		m_container.dispose();
    	}
    }

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {
        setComponents(jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences());
        checkConsistancy();
    	if (m_container != null) {
    		jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().removePropertyChangeListener(this);
    		m_container.setVisible(false); // should do with events...
    		m_container.dispose();
    	}
    }

	public void setContainer(JFrame f) {
		m_container = f;
        jbSave.setVisible(true);
        jbCancel.setText(Bundle.getMessage("ThrottlesPrefsCancel"));
	}
	
	static Logger log = LoggerFactory.getLogger(ThrottlesPreferencesPane.class.getName());

	public void propertyChange(PropertyChangeEvent evt) {
		if ((evt == null) || (evt.getPropertyName() == null)) return;
		if (evt.getPropertyName().compareTo("ThrottlePreferences") == 0) {
			if ((evt.getNewValue() == null) || (! (evt.getNewValue() instanceof ThrottlesPreferences))) return;
			setComponents((ThrottlesPreferences)evt.getNewValue());
			checkConsistancy();
		}
	}
}
