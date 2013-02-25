// AddSensorPanel.java

package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JPanel to create a new JMRI devices
 * HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @author  Pete Cressman    Copyright (C) 2010
 * @version     $Revision$
 */

public class AddNewDevicePanel extends jmri.util.swing.JmriPanel {

    public AddNewDevicePanel(JTextField sys, JTextField userName,
                             String addButtonLabel, ActionListener listener) {
            sysName = sys;
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

            add(ok = new JButton(rb.getString(addButtonLabel)));
            ok.addActionListener(listener);
            ok.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        reset();
                    }
            });

            reset();
            sysName.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (sysName.getText().length() > 0) {
                            ok.setEnabled(true);
                            ok.setToolTipText(null);
                        }
                    }
                });
    }

    void reset() {
        ok.setEnabled(false);
        ok.setToolTipText(rb.getString("ToolTipWillActivate"));
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysNameLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }

    JButton ok;
    JTextField sysName;
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final Logger log = LoggerFactory.getLogger(AddNewDevicePanel.class.getName());
}


/* @(#)AddNewDevicePanel.java */
