// PreferencesPane.java

package jmri.jmrix.ecos.swing.preferences;

//import jmri.InstanceManager;
//import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.jmrix.ecos.EcosPreferences;



import javax.swing.*;

/**
 * Pane to show ECoS preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 1.2 $
 */
public class PreferencesPane extends javax.swing.JPanel {


    JPanel throttletabpanel = new JPanel();
    JPanel rostertabpanel = new JPanel();
    JPanel turnouttabpanel = new JPanel();
    JCheckBox _addTurnoutsEcos;
    JCheckBox _removeTurnoutsEcos;
    JCheckBox _addTurnoutsJmri;
    JCheckBox _removeTurnoutsJmri;
    JComboBox _masterControl;
    JCheckBox _addLocosEcos;
    JCheckBox _removeLocosEcos;
    JCheckBox _addLocosJmri;
    JCheckBox _removeLocosJmri;
    JTextField _ecosDescription;
    JRadioButton _adhocLocoEcosAsk;
    JRadioButton _adhocLocoEcosLeave;
    JRadioButton _adhocLocoEcosRemove;
    ButtonGroup _adhocLocoEcos;
    JCheckBox _rememberAdhocLocosEcos;
    JComboBox _defaultProtocol;
    EcosPreferences ep;
    
    public PreferencesPane() {
        super();
        ep = EcosPreferences.instance();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update");
        
        updateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                updateButtonPressed();
            }
        });
        buttonPanel.add(updateButton);
        
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTabbedPane tab = new JTabbedPane();
        tab.add(rosterTab(), "Roster");
        tab.add(throttleTab(), "Throttle");
        tab.add(turnoutTab(), "Turnouts");
        
        add(tab);
        add(buttonPanel);
    }

    private JPanel turnoutTab(){
        turnouttabpanel.setLayout(new BoxLayout(turnouttabpanel, BoxLayout.Y_AXIS));
        
        _addTurnoutsEcos  = new JCheckBox("Add Turnouts to the ECoS");
        _addTurnoutsEcos.setSelected(ep.getAddTurnoutsToEcos());
        _addTurnoutsEcos.setEnabled(false);
        turnouttabpanel.add(_addTurnoutsEcos);
        
        _removeTurnoutsEcos  = new JCheckBox("Remove Turnouts from the ECoS");
        _removeTurnoutsEcos.setSelected(ep.getRemoveTurnoutsFromEcos());
        _removeTurnoutsEcos.setEnabled(false);
        turnouttabpanel.add(_removeTurnoutsEcos);
        
        _addTurnoutsJmri  = new JCheckBox("Add Turnouts to JMRI");
        _addTurnoutsJmri.setSelected(ep.getAddTurnoutsToJMRI());
        _addTurnoutsJmri.setEnabled(false);
        turnouttabpanel.add(_addTurnoutsJmri);
        
        _removeTurnoutsJmri  = new JCheckBox("Remove Turnouts from JMRI");
        _removeTurnoutsJmri.setSelected(ep.getRemoveTurnoutsFromJMRI());
        _removeTurnoutsJmri.setEnabled(false);
        turnouttabpanel.add(_removeTurnoutsJmri);
        return turnouttabpanel;
    
    }
    
    private JPanel rosterTab(){
        //rostertabpanel = new JPanel(new BoxLayout());
        
        rostertabpanel.setLayout(new BoxLayout(rostertabpanel, BoxLayout.PAGE_AXIS));
        
        JLabel _rosterLabel = new JLabel("These option control the Syncronisation of the JMRI Roster Database and the ECOS Database");
        _rosterLabel.setLayout(new BoxLayout(_rosterLabel, BoxLayout.X_AXIS));
        rostertabpanel.add(_rosterLabel);
        
        JPanel _locomaster = new JPanel();
        JLabel _masterLocoLabel = new JLabel("Resolve conflicts between JMRI and the ECOS");
        _locomaster.add(_masterLocoLabel);
        _masterControl = new JComboBox();
        initializeMasterControlCombo(_masterControl);
        if (ep.getLocoMaster()!=0x00)
            setMasterControlType(_masterControl, ep.getLocoMaster());
        _masterControl.setEnabled(false);
        _locomaster.add(_masterControl);
        rostertabpanel.add(_locomaster);
        
        
        _addLocosEcos  = new JCheckBox("Add Locos to the ECoS");
        _addLocosEcos.setSelected(ep.getAddLocoToEcos());
        _addLocosEcos.setEnabled(false);
        rostertabpanel.add(_addLocosEcos);
        
        _removeLocosEcos  = new JCheckBox("Remove Locos from the ECoS");
        _removeLocosEcos.setSelected(ep.getRemoveLocoFromEcos());
        _removeLocosEcos.setEnabled(false);
        rostertabpanel.add(_removeLocosEcos);
        
        _addLocosJmri  = new JCheckBox("Add Locos to JMRI Roster");
        _addLocosJmri.setSelected(ep.getAddLocoToJMRI());
        _addLocosJmri.setEnabled(false);
        rostertabpanel.add(_addLocosJmri);
        
        _removeLocosJmri  = new JCheckBox("Remove Locos from JMRI Roster");
        _removeLocosJmri.setSelected(ep.getRemoveLocoFromJMRI());
        _removeLocosJmri.setEnabled(false);
        rostertabpanel.add(_removeLocosJmri);
        
        JPanel ecosDescriptionPanel = new JPanel();
        
        JLabel _ecosDesLabel = new JLabel("Ecos Loco Description Format");
        ecosDescriptionPanel.add(_ecosDesLabel);
        
        _ecosDescription = new JTextField(20);
        _ecosDescription.setText(ep.getEcosLocoDescription());
        ecosDescriptionPanel.add(_ecosDescription);
        
        rostertabpanel.add(ecosDescriptionPanel);
        
        JLabel _descriptionformat = new JLabel("%i - Roster Id, %r - Road Name, %n - Road Number, %m - Manufacturer");
        rostertabpanel.add(_descriptionformat);
        JLabel _descriptionformat2 = new JLabel("%o - Owner, %l - Model, %c - Comment");
        rostertabpanel.add(_descriptionformat2);
    
        return rostertabpanel;
    }
    
    private JPanel throttleTab(){
    
            throttletabpanel.setLayout(new BoxLayout(throttletabpanel, BoxLayout.Y_AXIS));
        
        JLabel _throttleLabel = new JLabel("This option control what happens to a loco on the ECoS Database which has been");
        throttletabpanel.add(_throttleLabel);
        _throttleLabel = new JLabel("specifically created to enable a throttle to be used");
        throttletabpanel.add(_throttleLabel);        
        _adhocLocoEcosAsk = new JRadioButton("Always ask when releasing loco");
        
        _adhocLocoEcosLeave = new JRadioButton("Always leave the Loco in the Ecos");
        
        _adhocLocoEcosRemove = new JRadioButton("Always remove the Loco from the Ecos");
        switch (ep.getAdhocLocoFromEcos()){
            case 0  :   _adhocLocoEcosAsk.setSelected(true);
                        break;
            case 1  :   _adhocLocoEcosLeave.setSelected(true);
                        break;
            case 2  :   _adhocLocoEcosRemove.setSelected(true);
                        break;
            default :   _adhocLocoEcosAsk.setSelected(true);
                        break;
        }
        _adhocLocoEcos = new ButtonGroup();
        _adhocLocoEcos.add(_adhocLocoEcosAsk);
        _adhocLocoEcos.add(_adhocLocoEcosLeave);
        _adhocLocoEcos.add(_adhocLocoEcosRemove);
        /*_addAdhocLocosEcos  = new JCheckBox("Remove Adhoc Created Locos no the ECoS");
        _addAdhocLocosEcos.setSelected(ep.getRemoveAdhocLocoFromEcos());*/

        throttletabpanel.add(_adhocLocoEcosAsk);
        throttletabpanel.add(_adhocLocoEcosLeave);
        throttletabpanel.add(_adhocLocoEcosRemove);
        //_rememberAdhocLocosEcos  = new JCheckBox("Always use this setting for all operations");
        //_rememberAdhocLocosEcos.setSelected(ep.getAdhocLocoFromEcos());
        //throttletabpanel.add(_rememberAdhocLocosEcos);
        
        JPanel _defaultprotocolpanel = new JPanel();

        JLabel _defaultprotocolLabel = new JLabel("Sets the Default protocol to use for an Adhoc Loco");
        _defaultprotocolpanel.add(_defaultprotocolLabel);
        _defaultProtocol = new JComboBox();
        initializeEcosProtocolCombo(_defaultProtocol);
        //if (ep.getLocoMaster()!=0x00)
        setEcosProtocolType(_defaultProtocol, ep.getDefaultEcosProtocol());
        _defaultprotocolpanel.add(_defaultProtocol);
        throttletabpanel.add(_defaultprotocolpanel);

        return throttletabpanel;
    
    }
    
    private void updateButtonPressed(){
        EcosPreferences ep = EcosPreferences.instance();
        ep.setRemoveLocoFromJMRI(_removeLocosJmri.isSelected());
        ep.setAddLocoToJMRI(_addLocosJmri.isSelected());
        ep.setRemoveLocoFromEcos(_removeLocosEcos.isSelected());
        ep.setAddLocoToEcos(_addLocosEcos.isSelected());
        ep.setRemoveTurnoutsFromJMRI(_removeTurnoutsJmri.isSelected());
        ep.setAddTurnoutsToJMRI(_addTurnoutsJmri.isSelected());
        ep.setRemoveTurnoutsFromEcos(_removeTurnoutsEcos.isSelected());
        ep.setAddTurnoutsToEcos(_addTurnoutsEcos.isSelected());
        ep.setLocoMaster(getMasterControlType(_masterControl));
        ep.setDefaultEcosProtocol(getEcosProtocol(_defaultProtocol));
        ep.setEcosLocoDescription(_ecosDescription.getText());
        if (_adhocLocoEcosAsk.isSelected()) ep.setAdhocLocoFromEcos(0);
        else if (_adhocLocoEcosLeave.isSelected()) ep.setAdhocLocoFromEcos(1);
        else if (_adhocLocoEcosRemove.isSelected()) ep.setAdhocLocoFromEcos(2);
        else ep.setAdhocLocoFromEcos(0);
        
        /*//Taken directly from the AppConfigPanel
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        // decide whether name is absolute or relative
        File file = new File(mConfigFilename);
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+mConfigFilename);
        }

        InstanceManager.configureManagerInstance().storePrefs(file);*/
        JOptionPane.showMessageDialog(null,"ECOS/JMRI Preferences Updated\nTo save the changes from the Main menu, \n goto Edit|Preferences and click on 'Save'","Update",javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    String[] masterControlTypes = {"NOSYNC","WARNING","JMRI","ECoS"};
    int[] masterControlCode = {0x00,0x01,0x02,0x03};
    int numTypes = 4;  // number of entries in the above arrays
    
    private void initializeMasterControlCombo(JComboBox masterCombo) {
		masterCombo.removeAllItems();
		for (int i = 0;i<numTypes;i++) {
			masterCombo.addItem(masterControlTypes[i]);
		}
	}
    private void setMasterControlType(JComboBox masterBox, int master){
        for (int i = 0;i<numTypes;i++) {
			if (master==masterControlCode[i]) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }
    
    private int getMasterControlType(JComboBox masterBox){
        return masterControlCode[masterBox.getSelectedIndex()];
    
    }

    String[] ecosProtocolTypes = {"DCC14","DCC28", "DCC128", "MM14", "MM27", "MM28", "SX32", "MMFKT"};
    int numProtocolTypes = 8;  // number of entries in the above arrays

    private void initializeEcosProtocolCombo(JComboBox protocolCombo) {
		protocolCombo.removeAllItems();
		for (int i = 0;i<numProtocolTypes;i++) {
			protocolCombo.addItem(ecosProtocolTypes[i]);
		}
	}
    private void setEcosProtocolType(JComboBox masterBox, String protocol){
        for (int i = 0;i<numProtocolTypes;i++) {
			if (protocol.equals(ecosProtocolTypes[i])) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }

    private String getEcosProtocol(JComboBox masterBox){
        return ecosProtocolTypes[masterBox.getSelectedIndex()];

    }
}


/* @(#)PreferencesPane.java */
