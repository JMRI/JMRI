// SimpleClockFrame.java

package jmri.jmrit.simpleclock;

import jmri.Timebase;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

import java.awt.*;

import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Frame for user configuration of Simple Timebase
 *
 * @author	Dave Duchamp   Copyright (C) 2004
 * @version	$Revision: 1.5 $
 */
public class SimpleClockFrame extends JmriJFrame
	implements java.beans.PropertyChangeListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.simpleclock.SimpleClockBundle");

    Timebase clock;
    javax.swing.Timer timer = null;
    static int delay = 2*1000;  // update display every two seconds
    protected boolean showTime = false;

    protected javax.swing.JComboBox timeSourceBox = null;

    protected javax.swing.JTextField factorField = new javax.swing.JTextField(5);
    protected javax.swing.JTextField hoursField = new javax.swing.JTextField(2);
    protected javax.swing.JTextField minutesField = new javax.swing.JTextField(2);

    protected javax.swing.JButton setRateButton = new javax.swing.JButton(rb.getString("ButtonSet"));
    protected javax.swing.JButton setTimeButton = new javax.swing.JButton(rb.getString("ButtonSet"));
    protected javax.swing.JButton startButton = new javax.swing.JButton(rb.getString("ButtonStart"));
    protected javax.swing.JButton stopButton = new javax.swing.JButton(rb.getString("ButtonStop"));

    protected javax.swing.JLabel clockStatus = new javax.swing.JLabel();
    protected javax.swing.JLabel timeLabel = new javax.swing.JLabel();

    /**
     * Constructor method
     */
    public SimpleClockFrame() {
    	super();
    }

    /**
     *  Initialize the config window
     */
    public boolean initComponents() {
        setTitle(rb.getString("SimpleClockWindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Determine current state of the clock
        clock = InstanceManager.timebaseInstance();
        if (clock==null) {
            // could not initialize clock
            log.error("Could not obtain a timebase instance.");
            setVisible(false);
            dispose();
            return false;
        }

        // Set up time source choice
        JPanel panel11 = new JPanel();
        panel11.add(new JLabel(rb.getString("TimeSource")+" "));
        timeSourceBox = new JComboBox();
        panel11.add(timeSourceBox);
        timeSourceBox.addItem(rb.getString("ComputerClock"));
// Here add other time source choices in the future
        timeSourceBox.setToolTipText(rb.getString("TipTimeSource"));
        contentPane.add(panel11);

        // Set up speed up factor
        JPanel panel12 = new JPanel();
        panel12.add(new JLabel(rb.getString("SpeedUpFactor")+" "));
        panel12.add(factorField);
        factorField.setText(Double.toString(clock.getRate()));
        factorField.setToolTipText(rb.getString("TipFactorField"));
        panel12.add(new JLabel(":1 "));
        setRateButton.setToolTipText(rb.getString("TipSetRateButton"));
        setRateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setRateButtonActionPerformed();
                }
            });
        panel12.add(setRateButton);
        contentPane.add(panel12);

        // Set up time setup information
        JPanel panel2 = new JPanel();
        panel2.add(new JLabel(rb.getString("NewTime")+" "));
        panel2.add(hoursField);
        hoursField.setText("00");
        hoursField.setToolTipText(rb.getString("TipHoursField"));
        panel2.add(new JLabel(":"));
        panel2.add(minutesField);
        minutesField.setText("00");
        minutesField.setToolTipText(rb.getString("TipMinutesField"));
        setTimeButton.setToolTipText(rb.getString("TipSetTimeButton"));
        setTimeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setTimeButtonActionPerformed();
                }
            });
        panel2.add(setTimeButton);
        contentPane.add(panel2);

        // Set up clock information panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();

        panel31.add(clockStatus);
        panel3.add(panel31);
        JPanel panel32 = new JPanel();
        panel32.add(new JLabel(rb.getString("CurrentTime")+" "));
        setTimeLabel();
        panel32.add(timeLabel);
        panel3.add(panel32);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                                                rb.getString("BoxLabelClockState"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up Start and Stop buttons
        JPanel panel4 = new JPanel();
        startButton.setToolTipText(rb.getString("TipStartButton"));
        startButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startButtonActionPerformed();
                }
            });
        panel4.add(startButton);
        stopButton.setToolTipText(rb.getString("TipStopButton"));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopButtonActionPerformed();
                }
            });
        panel4.add(stopButton);
        contentPane.add(panel4);

        // Listen for closing of this window
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    thisWindowClosing(e);
                }
            });

		// update contents for current status
		updateRunningButton();

        // pack for display
        pack();

        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);

        // request callback to update time
        clock.addMinuteChangeListener( new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        updateTime();
                    }
                });

        return true;
    }

    /**
     * Method to adjust to rate changes
     */
    void updateRate() {
        factorField.setText(Double.toString(clock.getRate()));
	}

    /**
     * Method to adjust to running state changes
     */
    void updateRunningButton() {
    	boolean running = clock.getRun();
        if (running) {
            clockStatus.setText(rb.getString("ClockRunning"));
            startButton.setVisible(false);
            stopButton.setVisible(true);
        }
        else {
            clockStatus.setText(rb.getString("ClockStopped"));
            startButton.setVisible(true);
            stopButton.setVisible(false);
        }
        clockStatus.setVisible(true);
	}

    /**
     * Method to handle Set Rate button
     */
    public void setRateButtonActionPerformed() {
        double rate = 1.0;
        try {
            rate = Double.valueOf(factorField.getText()).doubleValue();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,(rb.getString("ParseRateError")+"\n"+e),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing Rate Field: "+e);
            return;
        }
        try {
            clock.setRate(rate);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,(rb.getString("SetRateError")+"\n"+e),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Exception when setting timebase rate: "+e);
        }
    }

    /**
     * Method to handle Set Time button
     */
    public void setTimeButtonActionPerformed() {
        int hours = 0;
        int minutes = 0;
        // get hours, reporting errors if any
        try {
            hours = Integer.parseInt(hoursField.getText());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,(rb.getString("HoursError")+"\n"+e),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: "+e);
            return;
        }
        if ( (hours<0) || (hours>23) ) {
            JOptionPane.showMessageDialog(this,(rb.getString("HoursRangeError")),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get minutes, reporting errors if any
        try {
            minutes = Integer.parseInt(minutesField.getText());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,(rb.getString("HoursError")+"\n"+e),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: "+e);
            return;
        }
        if ( (minutes<0) || (minutes>59) ) {
            JOptionPane.showMessageDialog(this,(rb.getString("MinutesRangeError")),
                    rb.getString("ErrorTitle"),JOptionPane.ERROR_MESSAGE);
            return;
        }
        // set time of the fast clock
        long mSecPerHour = 3600000;
        long mSecPerMinute = 60000;
        Date tem = clock.getTime();
        int cHours = tem.getHours();
        long cNumMSec = tem.getTime();
        long nNumMSec = ((cNumMSec/mSecPerHour)*mSecPerHour) - (cHours*mSecPerHour) +
                    (hours*mSecPerHour) + (minutes*mSecPerMinute);
        clock.setTime(new Date(nNumMSec));
        showTime = true;
    }

    /**
     * Method to handle Start Clock button
     */
    public void startButtonActionPerformed() {
        clock.setRun(true);
    }

    /**
     * Method to handle Stop Clock button
     */
    public void stopButtonActionPerformed() {
        clock.setRun(false);
    }

    /**
     * Method to update clock state information
     */
    void updateTime() {
        if (clock.getRun() || showTime) {
            showTime = false;
            setTimeLabel();
            timeLabel.setVisible(true);
        }
    }

    /**
     * Method to set the current Timebase time into timeLabel
     */
    void setTimeLabel() {
        // Get time
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();
        // Format and display the time
        timeLabel.setText(" "+(hours/10)+(hours-(hours/10)*10)+":"+
                                (minutes/10)+(minutes-(minutes/10)*10));
    }

    /**
     * Handle a change to clock properties
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		updateRunningButton();
		updateRate();
    }

    /**
     * Method to handle window closing event
     */
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        if (timer!=null) {
            timer.stop();
        }
        dispose();
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleClockFrame.class.getName());
}

/* @(#)SimpleClockFrame.java */
