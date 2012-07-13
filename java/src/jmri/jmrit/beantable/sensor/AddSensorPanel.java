// AddSensorPanel.java

package jmri.jmrit.beantable.sensor;

import jmri.*;
import jmri.util.ConnectionNameFromSystemName;
import jmri.jmrit.beantable.AddNewHardwareDevicePanel;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.List;

/**
 * JPanel to create a new Sensor
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision$
 * @deprecated  Replaced by {@link jmri.jmrit.beantable.AddNewHardwareDevicePanel}
 */
@Deprecated
public class AddSensorPanel extends jmri.util.swing.JmriPanel {
                             
    public AddSensorPanel(){
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            
        ActionListener rangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
        if (jmri.InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")){
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) jmri.InstanceManager.sensorManagerInstance();
            List<Manager> managerList = proxy.getManagerList();
            for(int x = 0; x<managerList.size(); x++){
                String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                Boolean addToPrefix = true;
                //Simple test not to add a system with a duplicate System prefix
                for (int i = 0; i<prefixBox.getItemCount(); i++){
                    if(((String)prefixBox.getItemAt(i)).equals(manuName))
                        addToPrefix=false;
                }
                if (addToPrefix)
                    prefixBox.addItem(manuName);

            }
            if(p.getComboBoxLastSelection(systemSelectionCombo)!=null)
                prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
            else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(jmri.InstanceManager.sensorManagerInstance().getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, rb.getString("ButtonAddSensor"), listener, rangeListener));
            canAddRange(null);
        
        //super.AddnewHardwareDevicePanel(sysName, userName, prefixBox, , rb.getString("ButtonAddSensor")
    }
    
    private void canAddRange(ActionEvent e){
        range.setEnabled(false);
        range.setSelected(false);
        if (senManager.getClass().getName().contains("ProxySensorManager")){
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) senManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for(int x = 0; x<managerList.size(); x++){
                jmri.SensorManager mgr = (jmri.SensorManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)){
                    range.setEnabled(true);
                    return;
                }
            }
        }
        else if (senManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))){
            range.setEnabled(true);
        }
    }

    /*public AddSensorPanel() {
            // to make location for accessibility & testing easier
            sysName.setName("sysName");
            userName.setName("userName");
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridwidth  = 1;
            c.gridheight = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.EAST;
            p.add(sysNameLabel,c);
            c.gridy = 1;
            p.add(userNameLabel,c);
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            p.add(sysName,c);
            c.gridy = 1;
            p.add(userName,c);
            add(p);

            JButton ok;
            add(ok = new JButton(rb.getString("ButtonAddSensor")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
    }
    */
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    
    protected SensorManager senManager = jmri.InstanceManager.sensorManagerInstance();
    
    JComboBox prefixBox = new JComboBox();
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox("Add a range");
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    String userNameError = this.getClass().getName()+".DuplicateUserName";
    jmri.UserPreferencesManager p;

    /*void okPressed(ActionEvent e) {
        String user = userName.getText();
        Sensor s = null;
        try {
            s = InstanceManager.sensorManagerInstance().provideSensor(sysName.getText());
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sysName.getText());
            return; // without creating       
        }
        if (user!= null && !user.equals("")) s.setUserName(user);
    }*/

    void okPressed(ActionEvent e) {
        /*String user = userName.getText();
        if (user.equals("")) user=null;*/

        int numberOfSensors = 1;

        if(range.isSelected()){
            try {
                numberOfSensors = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Number to Sensors to Add must be a number!",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
        } 
        if (numberOfSensors>=65){
            if(JOptionPane.showConfirmDialog(null,
                                                 "You are about to add " + numberOfSensors + " Sensors into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        String sensorPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());

        String sName = null;
        String curAddress = sysName.getText();

        for (int x = 0; x < numberOfSensors; x++){
            try {
                curAddress = jmri.InstanceManager.sensorManagerInstance().getNextValidAddress(curAddress, sensorPrefix);
            }  catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert '" + curAddress + "' to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
            if (curAddress==null){
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName=sensorPrefix+jmri.InstanceManager.sensorManagerInstance().typeLetter()+curAddress;
            Sensor s = null;
            try {
                s = jmri.InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (s!=null) {
                String user = userName.getText();
                if ((x!=0) && user != null && !user.equals(""))
                    user = userName.getText()+":"+x;
                if (user!= null && !user.equals("") && (jmri.InstanceManager.sensorManagerInstance().getByUserName(user)==null)){
                    s.setUserName(user);
                } else if (jmri.InstanceManager.sensorManagerInstance().getByUserName(user)!=null && !p.getPreferenceState(AddSensorPanel.class.getName(), "duplicateUserName")) {
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage("Duplicate UserName","The username " + user + " specified is already in use and therefore will not be set", AddSensorPanel.class.getName(), "duplicateUserName", false, true, org.apache.log4j.Level.ERROR);
                }
            }
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }
    
    
    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(AddSensorPanel.this,
                java.text.MessageFormat.format(
                    rb.getString("ErrorSensorAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddSensorPanel.class.getName());
}


/* @(#)AddSensorPanel.java */
