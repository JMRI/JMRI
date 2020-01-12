package jmri.jmrix.ecos.swing.preferences;

//import jmri.InstanceManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jmri.ConfigureManager;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.swing.PreferencesPanel;

/**
 * Pane to show ECoS preferences
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class PreferencesPane extends javax.swing.JPanel implements PropertyChangeListener, PreferencesPanel {

    JPanel throttletabpanel = new JPanel();
    JPanel rostertabpanel = new JPanel();
    JPanel turnouttabpanel = new JPanel();
    JComboBox<String> _addTurnoutsEcos;
    JComboBox<String> _removeTurnoutsEcos;
    JComboBox<String> _addTurnoutsJmri;
    JComboBox<String> _removeTurnoutsJmri;
    JComboBox<String> _masterControl;
    JComboBox<String> _addLocoEcos;
    JComboBox<String> _removeLocosEcos;
    JComboBox<String> _addLocoJmri;
    JComboBox<String> _removeLocosJmri;
    JTextField _ecosAttSufText;
    JTextField _ecosDescription;
    JRadioButton _adhocLocoEcosAsk;
    JRadioButton _adhocLocoEcosLeave;
    JRadioButton _adhocLocoEcosRemove;
    JRadioButton _forceControlLocoEcosAsk;
    JRadioButton _forceControlLocoEcosNever;
    JRadioButton _forceControlLocoEcosAlways;
    JRadioButton _locoControlNormal;
    JRadioButton _locoControlForce;
    ButtonGroup _adhocLocoEcos;
    ButtonGroup _locoEcosControl;
    ButtonGroup _locoControl;
    JCheckBox _rememberAdhocLocosEcos;
    //JComboBox<String> _defaultProtocol;
    EcosPreferences ep;
    boolean updateButtonPressed = false;

    public PreferencesPane(EcosPreferences epref) {
        super();
        ep = epref;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtonPressed();
            }
        });
        updateButton.setToolTipText(Bundle.getMessage("UpdateEcosPrefsToolTip"));
        buttonPanel.add(updateButton);

        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTabbedPane tab = new JTabbedPane();
        tab.add(rosterTab(), Bundle.getMessage("RosterTitle"));
        tab.add(throttleTab(), Bundle.getMessage("ThrottleTitle"));
        tab.add(turnoutTab(), Bundle.getMessage("Turnouts"));

        add(tab);
        add(buttonPanel);

        ep.addPropertyChangeListener(this);

    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (updateButtonPressed) {
            return;
        }
        if (e.getPropertyName().equals("update")) {
            updateValues();
        }
    }

    void updateValues() {
        setChoiceType(_removeTurnoutsEcos, ep.getRemoveTurnoutsFromEcos());
        setChoiceType(_addTurnoutsJmri, ep.getAddTurnoutsToJMRI());
        setChoiceType(_removeTurnoutsJmri, ep.getRemoveTurnoutsFromJMRI());
        setChoiceType(_addLocoEcos, ep.getAddLocoToEcos());
        setChoiceType(_removeLocosEcos, ep.getRemoveLocoFromEcos());
        setChoiceType(_addLocoJmri, ep.getAddLocoToJMRI());
        setChoiceType(_removeLocosJmri, ep.getRemoveLocoFromJMRI());
        switch (ep.getAdhocLocoFromEcos()) {
            case 1:
                _adhocLocoEcosLeave.setSelected(true);
                break;
            case 2:
                _adhocLocoEcosRemove.setSelected(true);
                break;
            case 0:
            default:
                _adhocLocoEcosAsk.setSelected(true);
                break;
        }

        _ecosAttSufText.setText(ep.getRosterAttributeSuffix());
        _ecosDescription.setText(ep.getEcosLocoDescription());

        switch (ep.getForceControlFromEcos()) {
            case 0x01:
                _forceControlLocoEcosNever.setSelected(true);
                break;
            case 0x02:
                _forceControlLocoEcosAlways.setSelected(true);
                break;
            case 0x00:
            default:
                _forceControlLocoEcosAsk.setSelected(true);
                break;
        }

        if (ep.getLocoControl()) {
            _locoControlForce.setSelected(true);
        } else {
            _locoControlNormal.setSelected(true);
        }
        //setEcosProtocolType(_defaultProtocol, ep.getDefaultEcosProtocol());
    }

    private JPanel turnoutTab() {
        turnouttabpanel.setLayout(new BoxLayout(turnouttabpanel, BoxLayout.Y_AXIS));

        JPanel _removeTurnoutsEcosPanel = new JPanel();
        JLabel _removeTurnoutsEcosLabel = new JLabel(Bundle.getMessage("RemoveTurnoutsEPref"));
        _removeTurnoutsEcos = new JComboBox<String>();
        _removeTurnoutsEcosPanel.add(_removeTurnoutsEcosLabel);
        initializeChoiceCombo(_removeTurnoutsEcos);
        if (ep.getRemoveTurnoutsFromEcos() != 0x00) {
            setChoiceType(_removeTurnoutsEcos, ep.getRemoveTurnoutsFromEcos());
        }
        _removeTurnoutsEcosPanel.add(_removeTurnoutsEcos);
        turnouttabpanel.add(_removeTurnoutsEcosPanel);

        JPanel _addTurnoutsJMRIPanel = new JPanel();
        JLabel _addTurnoutsJMRILabel = new JLabel(Bundle.getMessage("AddTurnoutsJPref"));
        _addTurnoutsJmri = new JComboBox<String>();
        _addTurnoutsJMRIPanel.add(_addTurnoutsJMRILabel);
        initializeChoiceCombo(_addTurnoutsJmri);
        if (ep.getAddTurnoutsToJMRI() != 0x00) {
            setChoiceType(_addTurnoutsJmri, ep.getAddTurnoutsToJMRI());
        }
        _addTurnoutsJMRIPanel.add(_addTurnoutsJmri);
        turnouttabpanel.add(_addTurnoutsJMRIPanel);

        JPanel _removeTurnoutsJMRIPanel = new JPanel();
        JLabel _removeTurnoutsJMRILabel = new JLabel(Bundle.getMessage("RemoveTurnoutsJPref"));
        _removeTurnoutsJmri = new JComboBox<String>();
        _removeTurnoutsJMRIPanel.add(_removeTurnoutsJMRILabel);
        initializeChoiceCombo(_removeTurnoutsJmri);
        if (ep.getRemoveTurnoutsFromJMRI() != 0x00) {
            setChoiceType(_removeTurnoutsJmri, ep.getRemoveTurnoutsFromJMRI());
        }
        _removeTurnoutsJMRIPanel.add(_removeTurnoutsJmri);
        turnouttabpanel.add(_removeTurnoutsJMRIPanel);

        return turnouttabpanel;
    }

    private JPanel rosterTab() {

        rostertabpanel.setLayout(new BoxLayout(rostertabpanel, BoxLayout.Y_AXIS));

        JLabel _rosterLabel = new JLabel(Bundle.getMessage("RosterPrefToolTip"));
        _rosterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rostertabpanel.add(_rosterLabel);

        JPanel _locomaster = new JPanel();
        JLabel _masterLocoLabel = new JLabel(Bundle.getMessage("RosterPrefLabel"));
        _masterLocoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _locomaster.add(_masterLocoLabel);
        _masterControl = new JComboBox<String>();
        initializeMasterControlCombo(_masterControl);
        if (ep.getLocoMaster() != 0x00) {
            setMasterControlType(_masterControl, ep.getLocoMaster());
        }
        //_masterControl.setEnabled(false);
        _locomaster.add(_masterControl);
        rostertabpanel.add(_locomaster);

        JPanel _addlocoecospanel = new JPanel();
        JLabel _addLocosEcosLabel = new JLabel(Bundle.getMessage("AddLocosEPref"));
        _addLocoEcos = new JComboBox<String>();
        _addlocoecospanel.add(_addLocosEcosLabel);
        initializeChoiceCombo(_addLocoEcos);
        if (ep.getAddLocoToEcos() != 0x00) {
            setChoiceType(_addLocoEcos, ep.getAddLocoToEcos());
        }
        _addlocoecospanel.add(_addLocoEcos);
        rostertabpanel.add(_addlocoecospanel);
        //_addLocosEcos  = new JCheckBox("Add Locos to the ECoS");
        //_addLocosEcos.setSelected(ep.getAddLocoToEcos());
        //_addLocosEcos.setEnabled(false);
        //rostertabpanel.add(_addLocosEcos);

        /*_removeLocosEcos  = new JCheckBox("Remove Locos from the ECoS");
         _removeLocosEcos.setSelected(ep.getRemoveLocoFromEcos());
         //_removeLocosEcos.setEnabled(false);
         rostertabpanel.add(_removeLocosEcos);*/
        JPanel _removelocosecospanel = new JPanel();
        JLabel _removeLocosEcosLabel = new JLabel(Bundle.getMessage("RemoveLocosEPref"));
        _removeLocosEcos = new JComboBox<String>();
        _removelocosecospanel.add(_removeLocosEcosLabel);
        initializeChoiceCombo(_removeLocosEcos);
        if (ep.getRemoveLocoFromEcos() != 0x00) {
            setChoiceType(_removeLocosEcos, ep.getRemoveLocoFromEcos());
        }
        _removelocosecospanel.add(_removeLocosEcos);
        rostertabpanel.add(_removelocosecospanel);

        /*_addLocosJmri  = new JCheckBox("Add Locos to JMRI Roster");
         _addLocosJmri.setSelected(ep.getAddLocoToJMRI());
         //_addLocosJmri.setEnabled(false);
         rostertabpanel.add(_addLocosJmri);*/
        JPanel _addlocosjmripanel = new JPanel();
        JLabel _addLocoJmriLabel = new JLabel(Bundle.getMessage("AddLocosJPref"));
        _addLocoJmri = new JComboBox<String>();
        _addlocosjmripanel.add(_addLocoJmriLabel);
        initializeChoiceCombo(_addLocoJmri);
        if (ep.getAddLocoToJMRI() != 0x00) {
            setChoiceType(_addLocoJmri, ep.getAddLocoToJMRI());
        }
        _addlocosjmripanel.add(_addLocoJmri);
        rostertabpanel.add(_addlocosjmripanel);

        /*_removeLocosJmri  = new JCheckBox("Remove Locos from JMRI Roster");
         _removeLocosJmri.setSelected(ep.getRemoveLocoFromJMRI());
         _removeLocosJmri.setEnabled(false);
         rostertabpanel.add(_removeLocosJmri);*/
        JPanel _removelocosjmripanel = new JPanel();
        JLabel _removeLocosJmriLabel = new JLabel(Bundle.getMessage("RemoveLocosJPref"));
        _removeLocosJmri = new JComboBox<String>();
        _removelocosjmripanel.add(_removeLocosJmriLabel);
        initializeChoiceCombo(_removeLocosJmri);
        if (ep.getRemoveLocoFromJMRI() != 0x00) {
            setChoiceType(_removeLocosJmri, ep.getRemoveLocoFromJMRI());
        }
        _removelocosjmripanel.add(_removeLocosJmri);
        rostertabpanel.add(_removelocosjmripanel);

        JPanel ecosAttributeSuffix = new JPanel();
        JLabel _ecosAttSufLabel = new JLabel(Bundle.getMessage("EcosRosterSuffixLabel"));
        ecosAttributeSuffix.add(_ecosAttSufLabel);
        _ecosAttSufText = new JTextField(10);
        _ecosAttSufText.setToolTipText(Bundle.getMessage("EcosRosterSuffixToolTip"));
        _ecosAttSufText.setText(ep.getRosterAttributeSuffix());
        ecosAttributeSuffix.add(_ecosAttSufText);
        rostertabpanel.add(ecosAttributeSuffix);

        JPanel ecosDescriptionPanel = new JPanel();

        JLabel _ecosDesLabel = new JLabel(Bundle.getMessage("EcosLocoDescrLabel"));
        ecosDescriptionPanel.add(_ecosDesLabel);

        _ecosDescription = new JTextField(20);
        _ecosDescription.setText(ep.getEcosLocoDescription());
        _ecosDescription.setToolTipText(Bundle.getMessage("EcosLocoDescrToolTip"));
        ecosDescriptionPanel.add(_ecosDescription);

        rostertabpanel.add(ecosDescriptionPanel);

        JLabel _descriptionformat = new JLabel(Bundle.getMessage("EcosTagsHelp1"));
        rostertabpanel.add(_descriptionformat);
        JLabel _descriptionformat2 = new JLabel(Bundle.getMessage("EcosTagsHelp2"));
        rostertabpanel.add(_descriptionformat2);

        return rostertabpanel;
    }

    private JPanel throttleTab() {

        throttletabpanel.setLayout(new BoxLayout(throttletabpanel, BoxLayout.Y_AXIS));

        JLabel _throttleLabel = new JLabel(Bundle.getMessage("EcosTempLocoHelp"));
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(_throttleLabel);
        /*_throttleLabel = new JLabel("specifically created to enable a throttle to be used");
         throttletabpanel.add(_throttleLabel);*/
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _adhocLocoEcosAsk = new JRadioButton(Bundle.getMessage("ButtonAlwaysAsk"));
        _adhocLocoEcosLeave = new JRadioButton(Bundle.getMessage("ButtonAlwaysLeave"));
        _adhocLocoEcosRemove = new JRadioButton(Bundle.getMessage("ButtonAlwaysRemove"));
        switch (ep.getAdhocLocoFromEcos()) {
            case 1:
                _adhocLocoEcosLeave.setSelected(true);
                break;
            case 2:
                _adhocLocoEcosRemove.setSelected(true);
                break;
            case 0:
            default:
                _adhocLocoEcosAsk.setSelected(true);
                break;
        }
        _adhocLocoEcos = new ButtonGroup();
        _adhocLocoEcos.add(_adhocLocoEcosAsk);
        _adhocLocoEcos.add(_adhocLocoEcosLeave);
        _adhocLocoEcos.add(_adhocLocoEcosRemove);

        JPanel adhocEcosGroup = new JPanel();
        adhocEcosGroup.setLayout(new BoxLayout(adhocEcosGroup, BoxLayout.Y_AXIS));
        adhocEcosGroup.add(_adhocLocoEcosAsk);
        adhocEcosGroup.add(_adhocLocoEcosLeave);
        adhocEcosGroup.add(_adhocLocoEcosRemove);
        adhocEcosGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(adhocEcosGroup);

        /*throttletabpanel.add(_adhocLocoEcosAsk);
         throttletabpanel.add(_adhocLocoEcosLeave);
         throttletabpanel.add(_adhocLocoEcosRemove);*/
        throttletabpanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        _throttleLabel = new JLabel(Bundle.getMessage("EcosControlLocoHelp"));
        _throttleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(_throttleLabel);
        _forceControlLocoEcosAsk = new JRadioButton(Bundle.getMessage("ButtonControlAlwaysAsk"));
        _forceControlLocoEcosNever = new JRadioButton(Bundle.getMessage("ButtonControlNeverForce"));
        _forceControlLocoEcosAlways = new JRadioButton(Bundle.getMessage("ButtonControlAlwaysForce"));
        switch (ep.getForceControlFromEcos()) {
            case 0x01:
                _forceControlLocoEcosNever.setSelected(true);
                break;
            case 0x02:
                _forceControlLocoEcosAlways.setSelected(true);
                break;
            case 0x00:
            default:
                _forceControlLocoEcosAsk.setSelected(true);
                break;
        }
        _locoEcosControl = new ButtonGroup();
        _locoEcosControl.add(_forceControlLocoEcosAsk);
        _locoEcosControl.add(_forceControlLocoEcosNever);
        _locoEcosControl.add(_forceControlLocoEcosAlways);
        JPanel locoEcosControlGroup = new JPanel();
        locoEcosControlGroup.setLayout(new BoxLayout(locoEcosControlGroup, BoxLayout.Y_AXIS));
        locoEcosControlGroup.add(_forceControlLocoEcosAsk);
        locoEcosControlGroup.add(_forceControlLocoEcosNever);
        locoEcosControlGroup.add(_forceControlLocoEcosAlways);
        locoEcosControlGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(locoEcosControlGroup);
        /*throttletabpanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
         JPanel _defaultprotocolpanel = new JPanel();

         JLabel _defaultprotocolLabel = new JLabel("Sets the Default protocol to use for an Adhoc Loco");
         _defaultprotocolpanel.add(_defaultprotocolLabel);
         _defaultProtocol = new JComboBox<String>();
         initializeEcosProtocolCombo(_defaultProtocol);
         if (ep.getLocoMaster()!=0x00)
         setEcosProtocolType(_defaultProtocol, ep.getDefaultEcosProtocol());
         defaultprotocolpanel.add(_defaultProtocol);
         throttletabpanel.add(_defaultprotocolpanel);*/
        throttletabpanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        _locoControl = new ButtonGroup();
        _locoControlNormal = new JRadioButton(Bundle.getMessage("ButtonControlAlwaysGain"));
        _locoControlForce = new JRadioButton(Bundle.getMessage("ButtonControlAlwaysForce"));
        _locoControl.add(_locoControlNormal);
        _locoControl.add(_locoControlForce);
        if (ep.getLocoControl()) {
            _locoControlForce.setSelected(true);
        } else {
            _locoControlNormal.setSelected(true);
        }
        JLabel _locoControlLabel = new JLabel(Bundle.getMessage("EcosTakeControlHelp"));
        JPanel locoControlGroup = new JPanel();
        locoControlGroup.add(_locoControlLabel);
        locoControlGroup.setLayout(new BoxLayout(locoControlGroup, BoxLayout.Y_AXIS));
        locoControlGroup.add(_locoControlNormal);
        locoControlGroup.add(_locoControlForce);
        locoControlGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        throttletabpanel.add(locoControlGroup);

        return throttletabpanel;
    }

    private void updateButtonPressed() {
        //Disable any action on the listener.
        updateButtonPressed = true;
        //EcosPreferences ep = adaptermemo.getPreferenceManager();
        ep.setRemoveLocoFromJMRI(getChoiceType(_removeLocosJmri));
        ep.setAddLocoToJMRI(getChoiceType(_addLocoJmri));
        ep.setRemoveLocoFromEcos(getChoiceType(_removeLocosEcos));
        ep.setAddLocoToEcos(getChoiceType(_addLocoEcos));
        ep.setRemoveTurnoutsFromJMRI(getChoiceType(_removeTurnoutsJmri));
        ep.setAddTurnoutsToJMRI(getChoiceType(_addTurnoutsJmri));
        ep.setRemoveTurnoutsFromEcos(getChoiceType(_removeTurnoutsEcos));
        //ep.setAddTurnoutsToEcos(getChoiceType(_addTurnoutsEcos));
        ep.setLocoMaster(getMasterControlType(_masterControl));
        //ep.setDefaultEcosProtocol(getEcosProtocol(_defaultProtocol));
        ep.setEcosLocoDescription(_ecosDescription.getText());
        ep.setRosterAttribute(_ecosAttSufText.getText());

        if (_adhocLocoEcosAsk.isSelected()) {
            ep.setAdhocLocoFromEcos(0);
        } else if (_adhocLocoEcosLeave.isSelected()) {
            ep.setAdhocLocoFromEcos(1);
        } else if (_adhocLocoEcosRemove.isSelected()) {
            ep.setAdhocLocoFromEcos(2);
        } else {
            ep.setAdhocLocoFromEcos(0);
        }

        if (_locoControlForce.isSelected()) {
            ep.setLocoControl(true);
        } else {
            ep.setLocoControl(false);
        }

        ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.storePrefs();
        }
        updateButtonPressed = false;
    }

    /**
     * Options in the ECoS Sync prefs drop down.
     * <p>
     * Keep masterControlTypes array identical to {@link jmri.jmrix.ecos.EcosPreferences#setLocoMaster(String)}
     */
    String[] masterControlTypes = {Bundle.getMessage("NOSYNC"), Bundle.getMessage("WARNING"), "JMRI", "ECoS"};
    int[] masterControlCode = {0x00, 0x01, 0x02, 0x03};
    int numTypes = 4;  // number of entries in the above arrays

    private void initializeMasterControlCombo(JComboBox<String> masterCombo) {
        masterCombo.removeAllItems();
        for (int i = 0; i < numTypes; i++) {
            masterCombo.addItem(masterControlTypes[i]);
        }
    }

    private void setMasterControlType(JComboBox<String> masterBox, int master) {
        for (int i = 0; i < numTypes; i++) {
            if (master == masterControlCode[i]) {
                masterBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private int getMasterControlType(JComboBox<String> masterBox) {
        return masterControlCode[masterBox.getSelectedIndex()];
    }

    String[] choiceTypes = {Bundle.getMessage("DeleteAsk"),
            Bundle.getMessage("ButtonNo"),
            Bundle.getMessage("ButtonYes")};
    int[] masterChoiceCode = {0x00, 0x01, 0x02};
    int numChoiceTypes = 3;  // number of entries in the above arrays

    private void initializeChoiceCombo(JComboBox<String> masterCombo) {
        masterCombo.removeAllItems();
        for (int i = 0; i < numChoiceTypes; i++) {
            masterCombo.addItem(choiceTypes[i]);
        }
    }

    private void setChoiceType(JComboBox<String> masterBox, int master) {
        for (int i = 0; i < numChoiceTypes; i++) {
            if (master == masterChoiceCode[i]) {
                masterBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private int getChoiceType(JComboBox<String> masterBox) {
        return masterChoiceCode[masterBox.getSelectedIndex()];
    }

    // String[] ecosProtocolTypes = {"DCC14","DCC28", "DCC128", "MM14", "MM27", "MM28", "SX32", "MMFKT"};
    // int numProtocolTypes = 8;  // number of entries in the above arrays

    /*private void initializeEcosProtocolCombo(JComboBox<String> protocolCombo) {
     protocolCombo.removeAllItems();
     for (int i = 0;i<numProtocolTypes;i++) {
     protocolCombo.addItem(ecosProtocolTypes[i]);
     }
     }*/
    /*private void setEcosProtocolType(JComboBox<String> masterBox, String protocol){
     for (int i = 0;i<numProtocolTypes;i++) {
     if (protocol.equals(ecosProtocolTypes[i])) {
     masterBox.setSelectedIndex(i);
     return;
     }
     }
     }*/

    /*private String getEcosProtocol(JComboBox<String> masterBox){
     return ecosProtocolTypes[masterBox.getSelectedIndex()];

     }*/
    @Override
    public String getPreferencesItem() {
        return "ECoS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuItemECoSPrefs");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return this.ep.getAdaptermemo().getUserName();
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
        return this.ep.getChangeMade();
    }

    @Override
    public boolean isRestartRequired() {
        // is this correct? If not, we would need to set a flag in
        // updateButtonPressed() to set this true.
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}

