package jmri.jmrix.can.cbus.swing.preferences;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import jmri.swing.PreferencesPanel;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Pane to edit MERG CBUS Preferences
 * Based on ECoS preferences
 * @author Kevin Dickerson Copyright (C) 2009
 * @author Steve Young Copyright (C) 2019
 * @see CbusPreferences
  */
public class CbusPreferencesPane extends jmri.jmrix.can.swing.CanPanel implements PreferencesPanel {

    JPanel reporterTabPanel;

    JRadioButton _allowAutoSensorCreationTrue;
    JRadioButton _allowAutoSensorCreationFalse;
    
    JRadioButton _ddesType0;
    JRadioButton _ddesType1;
    JRadioButton _ddesType2;

    boolean updateButtonPressed = false;
    boolean _isDirty = false;
    
    ActionListener l;
    JButton updateButton;
    
    private CanSystemConnectionMemo memo;
    private CbusPreferences p;

    public CbusPreferencesPane() {
        super();
        p = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
    }
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        if ( p == null) {
            log.error("Unable to load CbusPreferences");
        }

        // listener for all options for enabling Update button
        l = ae -> {
            checkIfOptionsChanged();
        };
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));

        // button listener
        ActionListener buttonListener = ae -> {
            updateButtonPressed();
        };
        updateButton.addActionListener(buttonListener);
        
        JLabel updateButtonLabel = new JLabel(Bundle.getMessage("PreferencesTip"));
        updateButtonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(updateButton);
        buttonPanel.add(updateButtonLabel);
        JTabbedPane tab = new JTabbedPane();
        
        tab.add(reporterTab(), Bundle.getMessage("Reporters"));

        add(tab);
        add(buttonPanel);
        
        updateValues();
        
    }    
    
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemCbusPreferences"));
        }
        return Bundle.getMessage("MenuItemCbusPreferences");
    }    
    
    private JPanel reporterTab() {
        reporterTabPanel = new JPanel();
        reporterTabPanel.setLayout(new BoxLayout(reporterTabPanel, BoxLayout.Y_AXIS));

        JLabel _allowAutoSensorCreationLabel = new JLabel(Bundle.getMessage("AutoSensorCreation"));
        _allowAutoSensorCreationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reporterTabPanel.add(_allowAutoSensorCreationLabel);
        _allowAutoSensorCreationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _allowAutoSensorCreationTrue = new JRadioButton(Bundle.getMessage("SensorCreationTrue"));
        _allowAutoSensorCreationFalse = new JRadioButton(Bundle.getMessage("SensorCreationFalse"));
        _allowAutoSensorCreationTrue.addActionListener(l);
        _allowAutoSensorCreationFalse.addActionListener(l);

        ButtonGroup _reporterSensorCreationGroup = new ButtonGroup();
        _reporterSensorCreationGroup.add(_allowAutoSensorCreationFalse);
        _reporterSensorCreationGroup.add(_allowAutoSensorCreationTrue);

        JPanel reporterSensorCreationGroup = new JPanel();
        reporterSensorCreationGroup.setLayout(new BoxLayout(reporterSensorCreationGroup, BoxLayout.Y_AXIS));
        reporterSensorCreationGroup.add(_allowAutoSensorCreationFalse);
        reporterSensorCreationGroup.add(_allowAutoSensorCreationTrue);
        reporterSensorCreationGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        reporterTabPanel.add(reporterSensorCreationGroup);

        reporterTabPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        JLabel _ddesLabel = new JLabel(Bundle.getMessage("DDESType"));
        _ddesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reporterTabPanel.add(_ddesLabel);
        _ddesType0 = new JRadioButton(Bundle.getMessage("ReporterType0"));
        _ddesType1 = new JRadioButton(Bundle.getMessage("ReporterType1"));
        _ddesType2 = new JRadioButton(Bundle.getMessage("ReporterType2"));
        _ddesType0.addActionListener(l);
        _ddesType1.addActionListener(l);
        _ddesType2.addActionListener(l);
        
        ButtonGroup _ddesType = new ButtonGroup();
        _ddesType.add(_ddesType0);
        _ddesType.add(_ddesType1);
        _ddesType.add(_ddesType2);
        
        JPanel ddesTypeGroup = new JPanel();
        ddesTypeGroup.setLayout(new BoxLayout(ddesTypeGroup, BoxLayout.Y_AXIS));
        ddesTypeGroup.add(_ddesType0);
        ddesTypeGroup.add(_ddesType1);
        ddesTypeGroup.add(_ddesType2);
        ddesTypeGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        reporterTabPanel.add(ddesTypeGroup);
        
        return reporterTabPanel;
    }

    void updateValues() {
        
        // reporter options
        if ( p.getAllowAutoSensorCreation() ) {
            _allowAutoSensorCreationTrue.setSelected(true);
        } else {
            _allowAutoSensorCreationFalse.setSelected(true);
        }
        switch (p.getReporterMode()) {
            case 1:
                _ddesType1.setSelected(true);
                break;
            case 2:
                _ddesType2.setSelected(true);
                break;
            default:
                _ddesType0.setSelected(true);
        }
        checkIfOptionsChanged();
    }

    private void checkIfOptionsChanged(){
        Boolean changed = false;
        if ( p.getAllowAutoSensorCreation() != _allowAutoSensorCreationTrue.isSelected() ) {
            changed = true;
        }
        switch (p.getReporterMode()) {
            case 1:
                if ( ! _ddesType1.isSelected() ) { changed = true; }
                break;
            case 2:
                if ( ! _ddesType2.isSelected() ) { changed = true; }
                break;
            default:
                if ( ! _ddesType0.isSelected() ) { changed = true; }
        }
        updateButton.setEnabled(changed);
        _isDirty = changed;
    }

    private void updateButtonPressed() {
        
        p.setAllowAutoSensorCreation(_allowAutoSensorCreationTrue.isSelected());
        int reporterType = 0;
        if (_ddesType1.isSelected()){
            reporterType = 1;
        }
        if (_ddesType2.isSelected()){
            reporterType = 2;
        }
        p.setReporterMode(reporterType);

        p.savePreferences();
        checkIfOptionsChanged();

    }

    @Override
    public String getPreferencesItem() {
        return "MERG"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return getTitle();
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getTitle();
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
        this.updateButtonPressed();
    }

    @Override
    public boolean isDirty() {
        return _isDirty;
    }

    @Override
    public boolean isRestartRequired() {
        return true;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
    
    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemCbusPreferences"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusPreferencesPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusPreferencesPane.class);
    
}
