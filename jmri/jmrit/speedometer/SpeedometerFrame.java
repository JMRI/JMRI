// SpeedometerFrame.java

package jmri.jmrit.speedometer;

import java.awt.*;
import javax.swing.*;
import jmri.*;

/**
 * Frame providing access to a speedometer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class SpeedometerFrame extends javax.swing.JFrame {

    JTextField startSensor = new JTextField(5);
    javax.swing.ButtonGroup startGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton startOnEntry  	= new javax.swing.JRadioButton("On entry");
    javax.swing.JRadioButton startOnExit    = new javax.swing.JRadioButton("On exit");
    
    JTextField stopSensor = new JTextField(5);
    javax.swing.ButtonGroup stopGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry  	= new javax.swing.JRadioButton("On entry");
    javax.swing.JRadioButton stopOnExit    = new javax.swing.JRadioButton("On exit");
    
    JTextField distance = new JTextField(5);
    
    JButton startButton = new JButton("Start");
    JLabel result = new JLabel("    ");
    
    public SpeedometerFrame() {
        
        startGroup.add(startOnEntry);
        startGroup.add(startOnExit);
        stopGroup.add(stopOnEntry);
        stopGroup.add(stopOnExit);
        
        // general GUI config
        setTitle("Speedometer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add items to GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel("1st sensor:"));
        stopSensor.setToolTipText("Number of sensor starting the timer");
        pane1.add(startSensor);
        pane1.add(startOnEntry);
        pane1.add(startOnExit);
        getContentPane().add(pane1);
        
        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel("2nd sensor:"));
        stopSensor.setToolTipText("Number of sensor ending the timer");
        pane2.add(stopSensor);
        pane2.add(stopOnEntry);
        pane2.add(stopOnExit);
        getContentPane().add(pane2);
        
        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel("Distance (scale feet):"));
        pane3.add(distance);
        getContentPane().add(pane3);
        
        getContentPane().add(startButton);
        
        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
        pane4.add(new JLabel("Speed (scale MPH):"));
        pane4.add(result);
        getContentPane().add(pane4);
        
        // add the actions to the buttons
        startButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setup();
                }
            });
        
        pack();
    }
    
    long startTime = 0;
    long stopTime = 0;
    
    void setup() {
        startButton.setEnabled(false);
        startButton.setToolTipText("You can only configure this once");
        // set start sensor
        Sensor s = InstanceManager.sensorManagerInstance().
            newSensor(null, startSensor.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("start sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && startOnEntry.isSelected())
                             || (now==Sensor.INACTIVE && startOnExit.isSelected()) ) {
                            startTime = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set start "+startTime);
                        }
                    }
                }
            });
        // set stop sensor
        s = InstanceManager.sensorManagerInstance().
            newSensor(null, stopSensor.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("stop sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && stopOnEntry.isSelected())
                             || (now==Sensor.INACTIVE && stopOnExit.isSelected()) ) {
                            stopTime = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set stop "+stopTime);
                            // calculate and show speed
                            double secs = (stopTime-startTime)/1000.;
                            double feet = Integer.parseInt(distance.getText());
                            double speed = (feet/5280.)*(3600./secs);
                            if (log.isDebugEnabled()) log.debug("calc from "+secs+","+feet+":"+speed);
                            result.setText(String.valueOf(speed).substring(0,5));
                        }
                    }
                }
            });
        
    }
    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        // dispose();
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpeedometerFrame.class.getName());
}

