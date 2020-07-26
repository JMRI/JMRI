package jmri.jmrit.throttle;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lionel
 */
@ServiceProvider(service = PreferencesPanel.class)
public class ThrottlesPreferencesPane extends JPanel implements PropertyChangeListener, PreferencesPanel {

    private JCheckBox cbUseToolBar;
    private JCheckBox cbUseFunctionIcon;
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
    private JLabel labelApplyWarning;
    private JButton jbApply;
    private JButton jbCancel;
    private JButton jbSave;
    private JFrame m_container = null;

    /**
     * Creates new form ThrottlesPreferencesPane
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ThrottlesPreferencesPane() {
        if (InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        ThrottlesPreferences tp = InstanceManager.getDefault(ThrottlesPreferences.class);
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
        
        GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
        gridBagConstraints16.gridx = 0;
        gridBagConstraints16.insets = new Insets(2, 5, 2, 2);
        gridBagConstraints16.anchor = GridBagConstraints.WEST;
        gridBagConstraints16.gridy = 12;

        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
        gridBagConstraints15.gridx = 0;
        gridBagConstraints15.insets = new Insets(8, 5, 2, 2);
        gridBagConstraints15.anchor = GridBagConstraints.WEST;
        gridBagConstraints15.gridy = 11;
        
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

        jbCancel = new JButton();
        jbSave = new JButton();
        jbApply = new JButton();

        cbUseExThrottle = new JCheckBox();
        cbUseToolBar = new JCheckBox();
        cbUseFunctionIcon = new JCheckBox();
        cbUseRosterImage = new JCheckBox();
        cbResizeWinImg = new JCheckBox();
        cbEnableRosterSearch = new JCheckBox();
        cbEnableAutoLoad = new JCheckBox();
        cbHideUndefinedButtons = new JCheckBox();
        cbIgnoreThrottlePosition = new JCheckBox();
        cbSaveThrottleOnLayoutSave = new JCheckBox();
        cbSilentSteal = new JCheckBox();
        cbSilentShare = new JCheckBox();

        labelApplyWarning = new JLabel();

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
        cbSilentSteal.setText(Bundle.getMessage("ExThrottleSilentSteal"));
        cbSilentShare.setText(Bundle.getMessage("ExThrottleSilentShare"));

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

        jbSave.setText(Bundle.getMessage("ButtonSave"));
        jbSave.addActionListener(this::jbSaveActionPerformed);
        jbSave.setVisible(false);

        jbCancel.setText(Bundle.getMessage("ThrottlesPrefsReset"));
        jbCancel.addActionListener(this::jbCancelActionPerformed);

        jbApply.setText(Bundle.getMessage("ButtonApply"));
        jbApply.addActionListener(this::jbApplyActionPerformed);

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
        this.add(cbSilentSteal,gridBagConstraints15 );
        this.add(cbSilentShare,gridBagConstraints16 );
        this.add(labelApplyWarning, gridBagConstraints13);
        
        if (InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
            cbSilentSteal.setEnabled(InstanceManager.throttleManagerInstance().enablePrefSilentStealOption());
            cbSilentShare.setEnabled(InstanceManager.throttleManagerInstance().enablePrefSilentShareOption());
        }
        
    }

    private void setComponents(ThrottlesPreferences tp) {
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
        cbSilentSteal.setSelected(tp.isSilentSteal());
        cbSilentShare.setSelected(tp.isSilentShare());
    }

    private ThrottlesPreferences getThrottlesPreferences() {
        ThrottlesPreferences tp = new ThrottlesPreferences();
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

    private void jbApplyActionPerformed(ActionEvent evt) {
        InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().set(getThrottlesPreferences());
    }

    public void jbSaveActionPerformed(ActionEvent evt) {
        InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().set(getThrottlesPreferences());
        InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().save();
        if (m_container != null) {
            InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().removePropertyChangeListener(this);
            m_container.setVisible(false); // should do with events...
            m_container.dispose();
        }
    }

    private void jbCancelActionPerformed(ActionEvent evt) {
        setComponents(InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences());
        checkConsistancy();
        if (m_container != null) {
            InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().removePropertyChangeListener(this);
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
        if (evt.getPropertyName().compareTo("ThrottlePreferences") == 0) {
            if ((evt.getNewValue() == null) || (!(evt.getNewValue() instanceof ThrottlesPreferences))) {
                return;
            }
            setComponents((ThrottlesPreferences) evt.getNewValue());
            checkConsistancy();
        }
    }

    @Override
    public String getPreferencesItem() {
        return "THROTTLE";
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuThrottle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        this.jbSaveActionPerformed(null);
    }

    @Override
    public boolean isDirty() {
        return InstanceManager.getDefault(ThrottlesPreferences.class).isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
