//PowerPane.java

package jmri.jmrit.powerpanel;

import java.awt.*;

import javax.swing.*;

import jmri.*;

/**
 * Pane for power control
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version	    $Revision: 1.2 $
 */
public class PowerPane extends javax.swing.JPanel implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel onOffStatus 	= new JLabel("Unknown");
    JButton onButton 	= new JButton("on");
    JButton offButton 	= new JButton("off");
    
    public PowerPane() {
        // add listeners to buttons
        onButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onButtonPushed();
                }
            });
        offButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    offButtonPushed();
                }
            });
        
        // general GUI config
        setLayout(new GridLayout(2,2));
        
        // install items in GUI
        add(new JLabel("Layout power: "));
        add(onOffStatus);
        add(onButton);
        add(offButton);
        
    }
    
    private boolean mgrOK() {
        if (p==null) {
            p = InstanceManager.powerManagerInstance();
            if (p == null) {
                log.error("No power manager instance found, panel not active");
                return false;
            }
            else p.addPropertyChangeListener(this);
        }
        return true;
    }
    
    public void onButtonPushed() {
        if (mgrOK())
            try {
                p.setPower(PowerManager.ON);
            }
            catch (JmriException e) {
                log.error("Exception trying to turn power on " +e);
            }
    }
    
    public void offButtonPushed() {
        if (mgrOK())
            try {
                p.setPower(PowerManager.OFF);
            }
            catch (JmriException e) {
                log.error("Exception trying to turn power off " +e);
            }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        try {
            if (p.getPower()==PowerManager.ON) onOffStatus.setText("On");
            else if (p.getPower()==PowerManager.OFF) onOffStatus.setText("Off");
            else if (p.getPower()==PowerManager.UNKNOWN) onOffStatus.setText("Unknown");
            else {
                onOffStatus.setText("Unknown");
                log.error("Unexpected state value: +"+p.getPower());
            }
        } catch (JmriException ex) {
            onOffStatus.setText("unknown");
        }
    }
    
    public void dispose() {
        if (p!=null) p.removePropertyChangeListener(this);
    }
    
    PowerManager p = null;
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PowerPane.class.getName());
    
}
