// UserMessagePreferencesPane.java

package jmri.jmrit.beantable.usermessagepreferences;

//import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.jmrix.ecos.EcosPreferences;
import java.awt.Component;
import java.io.File;



import javax.swing.*;

/**
 * Pane to show User Message Preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision: 1.1 $
 */
public class UserMessagePreferencesPane extends javax.swing.JPanel {

    JPanel applicationTabPanel = new JPanel();
    JCheckBox _routeSaveMsg;
    JComboBox _quitAfterSave;
    JComboBox _warnTurnoutInUse;


    JCheckBox _rememberAdhocLocosEcos;
    JComboBox _defaultProtocol;
    jmri.managers.DefaultUserMessagePreferences p;
    
    public UserMessagePreferencesPane() {
        super();
        p = jmri.managers.DefaultUserMessagePreferences.instance();
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
        tab.add(routeTab(), "Routes");
        tab.add(applicationTab(), "Application");
        tab.add(turnoutTab(), "Turnouts");
        add(tab);
        add(buttonPanel);
    }

    private JPanel routeTab(){
        JPanel routeTabPanel = new JPanel();
        routeTabPanel.setLayout(new BoxLayout(routeTabPanel, BoxLayout.Y_AXIS));
        
        _routeSaveMsg  = new JCheckBox("Always Display Save Message");
        _routeSaveMsg.setSelected(p.getRouteSaveMsg());
        routeTabPanel.add(_routeSaveMsg);
        
        return routeTabPanel;
    }
    
    private JPanel applicationTab(){
        //applicationtabpanel = new JPanel(new BoxLayout());
        JPanel applicationTabPanel = new JPanel();
        applicationTabPanel.setLayout(new BoxLayout(applicationTabPanel, BoxLayout.Y_AXIS));
        
        JPanel _quitAfterSavePanel = new JPanel();
        JLabel _quitAfterSaveLabel = new JLabel("Quit after Saving Preferences");
        
        _quitAfterSave = new JComboBox();
        _quitAfterSavePanel.add(_quitAfterSaveLabel);
        _quitAfterSavePanel.add(_quitAfterSave);
        initializeChoiceCombo(_quitAfterSave);
        if (p.getQuitAfterSave()!=0x00)
            setChoiceType(_quitAfterSave, p.getQuitAfterSave());
        applicationTabPanel.add(_quitAfterSavePanel);
        
        return applicationTabPanel;
    }
    
    private JPanel turnoutTab(){
        //applicationtabpanel = new JPanel(new BoxLayout());
        JPanel turnoutTabPanel = new JPanel();
        turnoutTabPanel.setLayout(new BoxLayout(turnoutTabPanel, BoxLayout.Y_AXIS));
        
        JPanel _warnTurnoutInUsePanel = new JPanel();
        JLabel _warnTurnoutInUseLabel = new JLabel("Quit after Saving Preferences");
        
        _warnTurnoutInUse = new JComboBox();
        _warnTurnoutInUsePanel.add(_warnTurnoutInUseLabel);
        _warnTurnoutInUsePanel.add(_warnTurnoutInUse);
        initializeChoiceCombo(_warnTurnoutInUse);
        if (p.getWarnTurnoutInUse()!=0x00)
            setChoiceType(_warnTurnoutInUse, p.getWarnTurnoutInUse());
        turnoutTabPanel.add(_warnTurnoutInUsePanel);
        
        return turnoutTabPanel;
    }
    
    
    private void updateButtonPressed(){
        p.setRouteSaveMsg(_routeSaveMsg.isSelected());
        p.setQuitAfterSave(getChoiceType(_quitAfterSave));
        p.setWarnTurnoutInUse(getChoiceType(_warnTurnoutInUse));
        jmri.InstanceManager.configureManagerInstance().storePrefs();
    }
    
    String[] choiceTypes = {"Always Ask","No","Yes"};
    int[] masterChoiceCode = {0x00,0x01,0x02};
    int numChoiceTypes = 3;  // number of entries in the above arrays
    
    private void initializeChoiceCombo(JComboBox masterCombo) {
		masterCombo.removeAllItems();
		for (int i = 0;i<numChoiceTypes;i++) {
			masterCombo.addItem(choiceTypes[i]);
		}
	}
    private void setChoiceType(JComboBox masterBox, int master){
        for (int i = 0;i<numChoiceTypes;i++) {
			if (master==masterChoiceCode[i]) {
				masterBox.setSelectedIndex(i);
				return;
			}
		}
    }
    
    private int getChoiceType(JComboBox masterBox){
        return masterChoiceCode[masterBox.getSelectedIndex()];
    
    }
}


/* @(#)UserMessagePreferencesPane.java */
