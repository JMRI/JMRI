// AddSensorPanel.java

package jmri.jmrit.beantable;

import jmri.*;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JPanel to create a new JMRI devices
 * HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @author  Pete Cressman    Copyright (C) 2010
 * @version     $Revision: 1.1 $
 */

public class AddNewDevicePanel extends jmri.util.swing.JmriPanel {

    private  AddNewDevicePanel() {
    }

    public AddNewDevicePanel(JTextField sysName, JTextField userName,
                             String addButtonLabel, ActionListener listener) {
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
            add(ok = new JButton(rb.getString(addButtonLabel)));
            ok.addActionListener(listener);
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysNameLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }
    
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddNewDevicePanel.class.getName());
}


/* @(#)AddNewDevicePanel.java */
