// SpeedometerFrame.java

package jmri.jmrit.speedometer;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.SensorIcon;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Frame providing access to a speedometer.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.16 $
 *
 * Adapted for metric system - S.K. Bosch
 *
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

    JButton dimButton = new JButton("");   // content will be set to English during startup
    JButton startButton = new JButton("Start");

    JLabel text1 = new JLabel("Distance 1 (scale feet):");
    JLabel text2 = new JLabel("Distance 2 (scale feet):");
    JLabel text3 = new JLabel("Speed 1 (scale MPH):");
    JLabel text4 = new JLabel("Speed 2 (scale MPH):");

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
        pane4.add(text1);
        pane4.add(distance1);
        getContentPane().add(pane4);

        JPanel pane5 = new JPanel();
        pane5.setLayout(new FlowLayout());
        pane5.add(text2);
        pane5.add(distance2);
        getContentPane().add(pane5);

        JPanel buttons = new JPanel();
        buttons.add(dimButton);
        dimButton.setToolTipText("Use this to choose between English and Metric");
        buttons.add(startButton);
        buttons.add(clearButton);
        getContentPane().add(buttons);

        clearButton.setVisible(false);

        // see if there's a sensor manager, if not disable
        if (null == InstanceManager.sensorManagerInstance()) {
           startButton.setEnabled(false);
           startButton.setToolTipText("Sensors are not supported with this DCC connection");
        }

        JPanel pane6 = new JPanel();
        pane6.setLayout(new FlowLayout());
        pane6.add(text3);
        pane6.add(result1);
        pane6.add(new JLabel("  Time (seconds):"));
        pane6.add(time1);
        getContentPane().add(pane6);

        JPanel pane7 = new JPanel();
        pane7.setLayout(new FlowLayout());
        pane7.add(text4);
        pane7.add(result2);
        pane7.add(new JLabel("  Time (seconds):"));
        pane7.add(time2);
        getContentPane().add(pane7);

        // set the units consistently
        dim();

        // add the actions to the config button
        dimButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dim();
                }
            });

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

    /**
     * "Distance Is Metric": If true, metric distances are being used.
     */
    boolean dim;

    // establish whether English or Metric representation is wanted
    void dim() {
        dimButton.setEnabled(true);
        if (dimButton.getText().equals ("To metric units")) {
          dimButton.setText("To English units");
          dim = true;
          text1.setText("Distance 1 (scale cm):");
          text2.setText("Distance 2 (scale cm):");
          text3.setText("Timer 1 Speed (scale KMH):");
          text4.setText("Timer 2 Speed (scale KMH):");
          }
        else {
          dimButton.setText("To metric units");
          dim = false;
          text1.setText("Distance 1 (scale feet):");
          text2.setText("Distance 2 (scale feet):");
          text3.setText("Timer 1 Speed (scale MPH):");
          text4.setText("Timer 2 Speed (scale MPH):");
          }
       }

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
                    provideSensor(startSensor.getText());
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
        startSensorIcon.setSensor(s.getSystemName(), s.getUserName());

        // set stop sensor1
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(stopSensor1.getText());
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
                            float secs = (stopTime1-startTime)/1000.f;
                            float feet = Float.valueOf(distance1.getText()).floatValue();
                            float speed;
                            if (dim == false) {
                              speed = (feet/5280.f)*(3600.f/secs);
                              }
                            else {
                              speed = (feet/100000.f)*(3600.f/secs);
                              }
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
        stopSensorIcon1.setSensor(s.getSystemName(), s.getUserName());

        // set stop sensor2
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(stopSensor2.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error, but for the second
            // one it's no big deal
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
                            float secs = (stopTime2-startTime)/1000.f;
                            float feet = Float.valueOf(distance2.getText()).floatValue();

                            float speed;
                            if (dim == false) {
                              speed = (feet/5280.f)*(3600.f/secs);
                              }
                            else {
                              speed = (feet/100000.f)*(3600.f/secs);
                              }
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
        stopSensorIcon2.setSensor(s.getSystemName(), s.getUserName());

    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        // dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpeedometerFrame.class.getName());
}


