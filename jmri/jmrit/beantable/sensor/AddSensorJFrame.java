// AddSensorJFrame.java

package jmri.jmrit.beantable.sensor;

import jmri.*;
import jmri.util.JmriJFrame;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JFrame to create a new Sensor
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */

public class AddSensorJFrame extends JmriJFrame {

    public AddSensorJFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle")
                .getString("TitleAddSensor"));
        
        addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSensorPanel());
        pack();
    }
    
}


/* @(#)AddSensorJFrame.java */
