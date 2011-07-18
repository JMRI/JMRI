// AddSensorPanel.java

package jmri.jmrit.beantable.sensor;

import jmri.*;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JPanel to create a new Sensor
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision$
 * @deprecated  Replaced by {@link jmri.jmrit.beantable.AddNewHardwareDevicePanel}
 */
@Deprecated
public class AddSensorPanel extends jmri.util.swing.JmriPanel {

    public AddSensorPanel() {
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
    
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void okPressed(ActionEvent e) {
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
