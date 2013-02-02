// AddSensorPanel.java

package jmri.jmrit.beantable;

import org.apache.log4j.Logger;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

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

public class AddNewHardwareDevicePanel extends jmri.util.swing.JmriPanel {

    public AddNewHardwareDevicePanel(JTextField sysAddress, JTextField userName, JComboBox prefixBox, JTextField endRange, JCheckBox addRange,
                             String addButtonLabel, ActionListener listener, ActionListener rangeListener) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            _endRange=endRange;
            _range=addRange;
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
            c.gridx =0;
            c.gridy = 1;
            p.add(sysAddressLabel,c);
            c.gridy = 2;
            p.add(userNameLabel,c);
            c.gridx = 2;
            c.gridy = 1;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            c.gridy = 0;
            p.add(prefixBox,c);
            c.gridx = 3;
            p.add(addRange,c);
            c.gridx = 2;
            c.gridy = 1;
            p.add(sysAddress,c);
            c.gridx = 3;
            p.add(finishLabel,c);
            c.gridx = 4;
            p.add(endRange,c);
            c.gridx=2;
            c.gridy = 2;
            p.add(userName,c);
            add(p);
            JButton ok;
            add(ok = new JButton(rb.getString(addButtonLabel)));
            ok.addActionListener(listener);
            addRange.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e){
                    rangeState();
                }
            });
            prefixBox.addActionListener(rangeListener);

            finishLabel.setEnabled(false);
            _endRange.setEnabled(false);
           /* System.out.println(jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class));
            java.util.List<Object> list 
                = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
            if (list != null) {
                for (Object memo : list) {
                    System.out.println(((jmri.jmrix.SystemConnectionMemo)memo).getUserName());
                    //if (menu != null) m.add(menu);
                }
            }*/
    }
    public void addLabels(String labelSystemName, String labelUserName) {
        sysAddressLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }
    
    private void rangeState(){
        if (_range.isSelected()){
            finishLabel.setEnabled(true);
            _endRange.setEnabled(true);
        } else {
            finishLabel.setEnabled(false);
            _endRange.setEnabled(false);
        }       
    }
       
    JTextField _endRange;
    JCheckBox _range;
    JLabel sysNameLabel = new JLabel("System");
    JLabel sysAddressLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    JLabel finishLabel = new JLabel("Number to Add");

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final Logger log = Logger.getLogger(AddNewHardwareDevicePanel.class.getName());
}


/* @(#)AddNewHardwareDevicePanel.java */
