// SpeedometerFrame.java

package jmri.jmrit.speedometer;

import java.awt.*;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.*;

/**
 * Frame providing access to a speedometer.
 * <P>
 * The logic is actually here, so that this mixes the speedometer's model
 * and presentation; that should be improved.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.11 $
 */
public class SpeedometerFrame extends javax.swing.JFrame {

    final String blank = "       ";
    JTextField startSensor = new JTextField(5);
    javax.swing.ButtonGroup startGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton startOnEntry  	= new javax.swing.JRadioButton("entry");
    javax.swing.JRadioButton startOnExit    = new javax.swing.JRadioButton("exit");

    JTextField stopSensor1 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup1 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry1  	= new javax.swing.JRadioButton("entry");
    javax.swing.JRadioButton stopOnExit1    = new javax.swing.JRadioButton("exit");

    JTextField stopSensor2 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup2 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry2  	= new javax.swing.JRadioButton("entry");
    javax.swing.JRadioButton stopOnExit2    = new javax.swing.JRadioButton("exit");

    JTextField distance1 = new JTextField(5);
    JTextField distance2 = new JTextField(5);

    JButton startButton = new JButton("Start");
    JButton clearButton = new JButton("Clear");

    JLabel result1 = new JLabel(blank);
    JLabel time1 = new JLabel(blank);
    JLabel result2 = new JLabel(blank);
    JLabel time2 = new JLabel(blank);

    SensorIcon startSensorIcon;
    SensorIcon stopSensorIcon1;
    SensorIcon stopSensorIcon2;

    public SpeedometerFrame() {

        startOnEntry.setSelected(true);
        stopOnEntry1.setSelected(true);
        stopOnEntry2.setSelected(true);

        startGroup.add(startOnEntry);
        startGroup.add(startOnExit);
        stopGroup1.add(stopOnEntry1);
        stopGroup1.add(stopOnExit1);
        stopGroup2.add(stopOnEntry2);
        stopGroup2.add(stopOnExit2);

        // general GUI config
        setTitle("Speedometer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel("Sensor "));
        startSensor.setToolTipText("Number of sensor starting the timer");
        pane1.add(startSensor);
        pane1.add(new JLabel(" starts timers on "));
        pane1.add(startOnEntry);
        pane1.add(startOnExit);
        startSensorIcon = new SensorIcon();
        startSensorIcon.setToolTipText("Shows sensor state; click to change");
        pane1.add(startSensorIcon);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel("Sensor "));
        stopSensor1.setToolTipText("Number of sensor ending the 1st timer");
        pane2.add(stopSensor1);
        pane2.add(new JLabel(" stops timer 1 on "));
        pane2.add(stopOnEntry1);
        pane2.add(stopOnExit1);
        stopSensorIcon1 = new SensorIcon();
        stopSensorIcon1.setToolTipText("Shows sensor state; click to change");
        pane2.add(stopSensorIcon1);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel("Sensor "));
        stopSensor2.setToolTipText("Number of sensor ending the 2nd timer");
        pane3.add(stopSensor2);
        pane3.add(new JLabel(" stops timer 2 on "));
        pane3.add(stopOnEntry2);
        pane3.add(stopOnExit2);
        stopSensorIcon2 = new SensorIcon();
        stopSensorIcon2.setToolTipText("Shows sensor state; click to change");
        pane3.add(stopSensorIcon2);
        getContentPane().add(pane3);

        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
        pane4.add(new JLabel("Distance 1 (scale feet):"));
        pane4.add(distance1);
        getContentPane().add(pane4);

        JPanel pane5 = new JPanel();
        pane5.setLayout(new FlowLayout());
        pane5.add(new JLabel("Distance 2 (scale feet):"));
        pane5.add(distance2);
        getContentPane().add(pane5);

        getContentPane().add(startButton);
        getContentPane().add(clearButton);
        clearButton.setVisible(false);

        // see if there's a sensor manager, if not disable
        if (null == InstanceManager.sensorManagerInstance()) {
            startButton.setEnabled(false);
            startButton.setToolTipText("Sensors are not supported with this DCC connection");
        }

        JPanel pane6 = new JPanel();
        pane6.setLayout(new FlowLayout());
        pane6.add(new JLabel("Timer1 speed (scale MPH):"));
        pane6.add(result1);
        pane6.add(new JLabel("  Time (seconds):"));
        pane6.add(time1);
        getContentPane().add(pane6);

        JPanel pane7 = new JPanel();
        pane7.setLayout(new FlowLayout());
        pane7.add(new JLabel("Timer2 speed (scale MPH):"));
        pane7.add(result2);
        pane7.add(new JLabel("  Time (seconds):"));
        pane7.add(time2);
        getContentPane().add(pane7);


        // add the actions to the config button
        startButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setup();
                }
            });

        clearButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    time1.setText(blank);
                    time2.setText(blank);
                    result1.setText(blank);
                    result2.setText(blank);
                }
            });
        // start displaying the sensor status when the number is entered
        startSensor.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startSensorIcon.setSensor(null, startSensor.getText());
                }
            });
        stopSensor1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopSensorIcon1.setSensor(null, stopSensor1.getText());
                }
            });

        stopSensor2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopSensorIcon2.setSensor(null, stopSensor2.getText());
                }
            });

        // and get ready to display
        pack();
    }

    long startTime = 0;
    long stopTime1 = 0;
    long stopTime2 = 0;

    void setup() {
        startButton.setEnabled(false);
        startButton.setToolTipText("You can only configure this once");
        startButton.setVisible(false);

        clearButton.setEnabled(true);
        clearButton.setVisible(true);

        // set start sensor
        Sensor s;
        try {
            s = InstanceManager.sensorManagerInstance().
                 newSensor(null, startSensor.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Start sensor NFG: "+startSensor.getText());
            startButton.setEnabled(true);
            return;
        }
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
        startSensorIcon.setSensor(null, startSensor.getText());

        // set stop sensor1
        try {
            s = InstanceManager.sensorManagerInstance().
                 newSensor(null, stopSensor1.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Stop 1 sensor NFG: "+stopSensor1.getText());
            startButton.setEnabled(true);
            return;
        }
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("stop sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && stopOnEntry1.isSelected())
                             || (now==Sensor.INACTIVE && stopOnExit1.isSelected()) ) {
                            stopTime1 = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set stop "+stopTime1);
                            // calculate and show speed
                            double secs = (stopTime1-startTime)/1000.;
                            double feet = Integer.parseInt(distance1.getText());
                            double speed = (feet/5280.)*(3600./secs);
                            if (log.isDebugEnabled()) log.debug("calc from "+secs+","+feet+":"+speed);
                            result1.setText(String.valueOf(speed).substring(0,4));
                            String time = String.valueOf(secs);
                            int offset = time.indexOf(".");
                            if (offset==-1) offset=time.length();
                            offset=offset+2;  // the decimal point, plus tenths digit
                            if (offset>time.length()) offset=time.length();
                            time1.setText(time.substring(0,offset));
                        }
                    }
                }
            });
        stopSensorIcon1.setSensor(null, stopSensor1.getText());

        // set stop sensor2
        try {
            s = InstanceManager.sensorManagerInstance().
                 newSensor(null, stopSensor2.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error, but for the second
            // one that is no big deal
            log.error("Stop 2 sensor NFG: "+stopSensor2.getText());
            return;
        }
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("stop sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && stopOnEntry2.isSelected())
                             || (now==Sensor.INACTIVE && stopOnExit2.isSelected()) ) {
                            stopTime2 = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set stop "+stopTime2);
                            // calculate and show speed
                            double secs = (stopTime2-startTime)/1000.;
                            double feet = Integer.parseInt(distance2.getText());
                            double speed = (feet/5280.)*(3600./secs);
                            if (log.isDebugEnabled()) log.debug("calc from "+secs+","+feet+":"+speed);
                            result2.setText(String.valueOf(speed).substring(0,4));
                            String time = String.valueOf(secs);
                            int offset = time.indexOf(".");
                            if (offset==-1) offset=time.length();
                            offset=offset+2;  // the decimal point, plus tenths digit
                            if (offset>time.length()) offset=time.length();
                            time2.setText(time.substring(0,offset));
                        }
                    }
                }
            });
        stopSensorIcon2.setSensor(null, stopSensor2.getText());

    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        // dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpeedometerFrame.class.getName());
}


