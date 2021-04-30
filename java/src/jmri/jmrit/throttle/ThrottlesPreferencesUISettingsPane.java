package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jmri.InstanceManager;

/**
 * A preferences panel to display and edit JMRI throttle preferences
 * 
 * @author Lionel Jeanson - 2009 - 2021
 * 
 */
public class ThrottlesPreferencesUISettingsPane extends JPanel {

    private JCheckBox cbUseToolBar;
    private JCheckBox cbUseFunctionIcon;
    private JCheckBox cbUseLargeSpeedSlider;
    private JCheckBox cbHideSpeedStepSelector;
    private JCheckBox cbResizeWinImg;
    private JCheckBox cbUseExThrottle;
    private JCheckBox cbUseRosterImage;
    private JCheckBox cbEnableRosterSearch;
    private JCheckBox cbEnableAutoLoad;
    private JCheckBox cbHideUndefinedButtons;
    private JCheckBox cbIgnoreThrottlePosition;
    private JCheckBox cbSaveThrottleOnLayoutSave;
    private JCheckBox cbSilentSteal;
    private JCheckBox cbSilentShare;
    private boolean isDirty = false;

    /**
     * Creates new form ThrottlesPreferencesPane
     * @param tp the throttle preferences to init the component
     */
    public ThrottlesPreferencesUISettingsPane(ThrottlesPreferences tp) {
        initComponents();
        resetComponents(tp);
        checkConsistancy();
    }

    private void initComponents() {

        cbUseExThrottle = new JCheckBox();
        cbUseToolBar = new JCheckBox();
        cbUseFunctionIcon = new JCheckBox();
        cbUseLargeSpeedSlider = new JCheckBox();
        cbHideSpeedStepSelector = new JCheckBox();
        cbUseRosterImage = new JCheckBox();
        cbResizeWinImg = new JCheckBox();
        cbEnableRosterSearch = new JCheckBox();
        cbEnableAutoLoad = new JCheckBox();
        cbHideUndefinedButtons = new JCheckBox();
        cbIgnoreThrottlePosition = new JCheckBox();
        cbSaveThrottleOnLayoutSave = new JCheckBox();
        cbSilentSteal = new JCheckBox();
        cbSilentShare = new JCheckBox();

        cbUseExThrottle.setText(Bundle.getMessage("UseExThrottle"));
        cbResizeWinImg.setText(Bundle.getMessage("ExThrottleForceResize"));
        cbUseToolBar.setText(Bundle.getMessage("ExThrottleUseToolBar"));
        cbUseFunctionIcon.setText(Bundle.getMessage("ExThrottleUseFunctionIcons"));
        cbUseLargeSpeedSlider.setText(Bundle.getMessage("ExThrottleUseLargeSpeedSlider"));
        cbHideSpeedStepSelector.setText(Bundle.getMessage("ExThrottleHideSpeedStepSelector"));
        cbUseRosterImage.setText(Bundle.getMessage("ExThrottleUseRosterImageBkg"));
        cbEnableRosterSearch.setText(Bundle.getMessage("ExThrottleEnableRosterSearch"));
        cbEnableAutoLoad.setText(Bundle.getMessage("ExThrottleEnableAutoSave"));
        cbHideUndefinedButtons.setText(Bundle.getMessage("ExThrottleHideUndefinedFunctionButtons"));
        cbIgnoreThrottlePosition.setText(Bundle.getMessage("ExThrottleIgnoreThrottlePosition"));
        cbSaveThrottleOnLayoutSave.setText(Bundle.getMessage("ExThrottleSaveThrottleOnLayoutSave"));
        cbSilentSteal.setText(Bundle.getMessage("ExThrottleSilentSteal"));
        cbSilentShare.setText(Bundle.getMessage("ExThrottleSilentShare"));

        ActionListener dirtyAL = (ActionEvent evt) -> {        
            isDirty = true;
        };
        cbUseExThrottle.addActionListener(dirtyAL);
        cbResizeWinImg.addActionListener(dirtyAL);
        cbUseToolBar.addActionListener(dirtyAL);
        cbUseFunctionIcon.addActionListener(dirtyAL);
        cbUseLargeSpeedSlider.addActionListener(dirtyAL);
        cbHideSpeedStepSelector.addActionListener(dirtyAL);
        cbUseRosterImage.addActionListener(dirtyAL);
        cbEnableRosterSearch.addActionListener(dirtyAL);
        cbEnableAutoLoad.addActionListener(dirtyAL);
        cbHideUndefinedButtons.addActionListener(dirtyAL);
        cbIgnoreThrottlePosition.addActionListener(dirtyAL);     
        cbSaveThrottleOnLayoutSave.addActionListener(dirtyAL);     
        cbSilentSteal.addActionListener(dirtyAL);     
        cbSilentShare.addActionListener(dirtyAL);
        
        ActionListener al = (ActionEvent evt) -> {
            checkConsistancy();
        };
        cbUseExThrottle.addActionListener(al);
        cbUseToolBar.addActionListener(al);
        cbUseRosterImage.addActionListener(al);
        cbEnableAutoLoad.addActionListener(al);
        
        // only the steal checkbox OR the share checkbox should be selected
        ActionListener stealCheck = (ActionEvent evt) -> {
            checkStealButtonOk();
        };
        ActionListener shareCheck = (ActionEvent evt) -> {
            checkShareButtonOk();
        };
        cbSilentSteal.addActionListener(stealCheck);
        cbSilentShare.addActionListener(shareCheck);

        setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 5;
        constraints.ipady = 5;
        Insets x0 = new Insets(2, 2, 2, 2);
        Insets x1 = new Insets(2, 18, 2, 2);
        Insets x2 = new Insets(2, 32, 2, 2);        
        constraints.insets = x0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        
        constraints.gridx = 0;
        constraints.gridy = 0;                
        this.add(cbUseExThrottle, constraints);

        constraints.gridy++;
        constraints.insets = x1;
        this.add(cbSaveThrottleOnLayoutSave, constraints);
        
        constraints.gridy++;
        this.add(cbUseRosterImage, constraints);
        
        constraints.gridy++;
        constraints.insets = x2;
        this.add(cbResizeWinImg, constraints);
        
        constraints.gridy++;
        constraints.insets = x1;
        this.add(cbEnableRosterSearch, constraints);
        
        constraints.gridy++;
        this.add(cbEnableAutoLoad, constraints);

        constraints.gridy++;
        constraints.insets = x2;
        this.add(cbIgnoreThrottlePosition, constraints);
        
        constraints.gridy++;
        constraints.insets = x1;
        this.add(cbHideUndefinedButtons, constraints);

        constraints.gridy++;
        this.add(cbUseToolBar, constraints);

        constraints.gridy++;
        this.add(cbUseFunctionIcon, constraints);

        constraints.gridy++;
        this.add(cbUseLargeSpeedSlider, constraints);

        constraints.gridy++;
        this.add(cbHideSpeedStepSelector, constraints);

        constraints.gridy++;
        constraints.insets = x0;
        this.add(new JSeparator(),constraints );
        constraints.gridy++;
        this.add(cbSilentSteal,constraints );
        
        constraints.gridy++;
        this.add(cbSilentShare,constraints );
                        
        if (InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
            cbSilentSteal.setEnabled(InstanceManager.throttleManagerInstance().enablePrefSilentStealOption());
            cbSilentShare.setEnabled(InstanceManager.throttleManagerInstance().enablePrefSilentShareOption());
        }
        
    }

    public final void resetComponents(ThrottlesPreferences tp) {
        if (tp == null) {
            return;
        }
        cbSaveThrottleOnLayoutSave.setSelected(tp.isSavingThrottleOnLayoutSave());
        cbResizeWinImg.setSelected(tp.isResizingWindow());
        cbUseToolBar.setSelected(tp.isUsingToolBar());
        cbUseFunctionIcon.setSelected(tp.isUsingFunctionIcon());
        cbUseRosterImage.setSelected(tp.isUsingRosterImage());
        cbUseExThrottle.setSelected(tp.isUsingExThrottle());
        cbEnableRosterSearch.setSelected(tp.isEnablingRosterSearch());
        cbEnableAutoLoad.setSelected(tp.isAutoLoading());
        cbHideUndefinedButtons.setSelected(tp.isHidingUndefinedFuncButt());
        cbIgnoreThrottlePosition.setSelected(tp.isIgnoringThrottlePosition());
        cbUseLargeSpeedSlider.setSelected(tp.isUsingLargeSpeedSlider());
        cbHideSpeedStepSelector.setSelected(tp.isHidingSpeedStepSelector());
        cbSilentSteal.setSelected(tp.isSilentSteal());
        cbSilentShare.setSelected(tp.isSilentShare());
        checkConsistancy();
        isDirty = false;
    }

    public ThrottlesPreferences updateThrottlesPreferences(ThrottlesPreferences tp) {
        tp.setUseExThrottle(cbUseExThrottle.isSelected());
        tp.setUsingToolBar(cbUseToolBar.isSelected());
        tp.setUsingFunctionIcon(cbUseFunctionIcon.isSelected());
        tp.setResizeWindow(cbResizeWinImg.isSelected());
        tp.setUseRosterImage(cbUseRosterImage.isSelected());
        tp.setSaveThrottleOnLayoutSave(cbSaveThrottleOnLayoutSave.isSelected());
        tp.setSilentSteal(cbSilentSteal.isSelected());
        tp.setSilentShare(cbSilentShare.isSelected());
        tp.setEnableRosterSearch(cbEnableRosterSearch.isSelected());
        tp.setAutoLoad(cbEnableAutoLoad.isSelected());
        tp.setHideUndefinedFuncButt(cbHideUndefinedButtons.isSelected());
        tp.setIgnoreThrottlePosition(cbIgnoreThrottlePosition.isSelected());        
        tp.setUseLargeSpeedSlider(cbUseLargeSpeedSlider.isSelected());
        tp.setHideSpeedStepSelector(cbHideSpeedStepSelector.isSelected());
        return tp;
    }

    private void checkConsistancy() {
        cbSaveThrottleOnLayoutSave.setEnabled(cbUseExThrottle.isSelected());
        cbUseToolBar.setEnabled(cbUseExThrottle.isSelected());
        cbUseFunctionIcon.setEnabled(cbUseExThrottle.isSelected());
        cbEnableRosterSearch.setEnabled(cbUseExThrottle.isSelected());
        cbEnableAutoLoad.setEnabled(cbUseExThrottle.isSelected());
        cbUseRosterImage.setEnabled(cbUseExThrottle.isSelected());
        cbResizeWinImg.setEnabled(cbUseExThrottle.isSelected() && cbUseRosterImage.isSelected());
        cbHideUndefinedButtons.setEnabled(cbUseExThrottle.isSelected());
        cbIgnoreThrottlePosition.setEnabled(cbUseExThrottle.isSelected() && cbEnableAutoLoad.isSelected());
        cbUseLargeSpeedSlider.setEnabled(cbUseExThrottle.isSelected());
        cbHideSpeedStepSelector.setEnabled(cbUseExThrottle.isSelected());
        if (cbUseExThrottle.isSelected()) {
            if (cbUseToolBar.isSelected()) {
                cbIgnoreThrottlePosition.setSelected(true);
                cbIgnoreThrottlePosition.setEnabled(false);
            }
        }
    }
    
    private void checkStealButtonOk() {
        if (cbSilentShare.isSelected()){
            cbSilentShare.setSelected(false);
        }
    }
    
    private void checkShareButtonOk() {
        if (cbSilentSteal.isSelected()){
            cbSilentSteal.setSelected(false);
        }
    }

    boolean isDirty() {
        return isDirty;        
    }
    
}
