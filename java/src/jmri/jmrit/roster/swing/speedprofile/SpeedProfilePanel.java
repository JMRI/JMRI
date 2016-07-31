package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set up and run automated speed table calibration.
 * <p>
 * Uses three sensors in a row:
 * <ul>
 * <li>Start sensor: Track where locomotive starts
 * <li>Block sensor: Middle track. This time through this is used to measure the
 * speed.
 * <li>Finish sensor: Track where locomotive stops before repeating.
 * </ul>
 * The expected sequence is:
 * <ul>
 * <li>Start moving with start sensor on, others off.
 * <li>Block (middle) sensor goes active: startListener calls startTiming
 * <li>Finish sensor goes active: finishListener calls stopCurrentSpeedStep
 * <li>Block (middle) sensor goes inactive: startListener calls stopLoco, which
 * stops loco after 2.5 seconds
 * </ul>
 * After a forward run, the start and finish sensors are swapped for a run in
 * reverse.
 */
class SpeedProfilePanel extends jmri.util.swing.JmriPanel implements ThrottleListener {

    JButton profileButton = new JButton(Bundle.getMessage("ButtonProfile"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton testButton = new JButton(Bundle.getMessage("ButtonTest"));
    JButton viewButton = new JButton(Bundle.getMessage("ButtonView"));
    JTextField lengthField = new JTextField(10);
    JTextField speedStepTest = new JTextField(5);
    JTextField speedStepFrom = new JTextField(5);
    JTextField speedStepTo = new JTextField(5);
    JTextField speedStepIncr = new JTextField(5);
    JRadioButton clearPofile = new JRadioButton();
    JRadioButton updatePofile = new JRadioButton();

    // Start or finish sensor
    BeanSelectCreatePanel sensorAPanel = new BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);

    // Finish or start sensor
    BeanSelectCreatePanel sensorBPanel = new BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);

    // Block sensor
    BeanSelectCreatePanel blockCPanel = new BeanSelectCreatePanel(InstanceManager.getDefault(jmri.BlockManager.class), null);
    BeanSelectCreatePanel sensorCPanel = new BeanSelectCreatePanel(InstanceManager.sensorManagerInstance(), null);

    RosterEntryComboBox reBox = new RosterEntryComboBox();
    SpeedProfileTable table = null;
    boolean profile = false;
    boolean test = false;
    boolean save = false;

    JLabel sourceLabel = new JLabel();
//    JTextField sourceLabel;

    public SpeedProfilePanel() {
        JPanel main = new JPanel();

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        main.setLayout(gb);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        JLabel label = new JLabel(Bundle.getMessage("LabelLengthOfBlock"));
        addRow(main, gb, c, 0, label, lengthField);
        label = new JLabel(Bundle.getMessage("LabelStartSensor"));
        addRow(main, gb, c, 1, label, sensorAPanel);
        label = new JLabel(Bundle.getMessage("LabelBlockSensor"));
        addRow(main, gb, c, 2, label, sensorCPanel);
        label = new JLabel(Bundle.getMessage("LabelFinishSensor"));
        addRow(main, gb, c, 3, label, sensorBPanel);
        label = new JLabel(Bundle.getMessage("LabelSelectRoster"));
        JPanel left = makePadPanel(label);
        JPanel right = makePadPanel(reBox);
        addRow(main, gb, c, 4, left, right);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.add(viewButton);
        p.add(Box.createRigidArea(new java.awt.Dimension(6,1)));
        p.add(cancelButton);
        left = makePadPanel(p);
        right = makePadPanel(profileButton);
        addRow(main, gb, c, 5, left, right);

        left = new JPanel();
        left.add(Box.createRigidArea(new java.awt.Dimension(20,10)));
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(makeLabelPanel("LabelStartStep", speedStepFrom));
        left.add(makeLabelPanel("LabelFinishStep", speedStepTo));
        left.add(makeLabelPanel("LabelStepIncr", speedStepIncr));       
        right = new JPanel();
//        right.add(Box.createRigidArea(new java.awt.Dimension(20,10)));
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
        right.add(makeLabelPanel("ButtonClear", clearPofile));
        right.add(makeLabelPanel("ButtonUpdate", updatePofile));
        addRow(main, gb, c, 6, left, right);
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(clearPofile);
        bg.add(updatePofile);

        JPanel testStep = makeLabelPanel("LabelTestStep", speedStepTest);
        left = makePadPanel(testStep);
        right = makePadPanel(testButton);
        addRow(main, gb, c, 7, left, right);
        
       c.fill = GridBagConstraints.HORIZONTAL;
       c.gridx = 0;
       c.gridy = 8;
       c.gridwidth = 2;
//       sourceLabel = new JTextField(10);
       sourceLabel = new JLabel("   ");
//       sourceLabel.setEditable(false);
       sourceLabel.setBackground(Color.white);
       left = makePadPanel(sourceLabel);
       gb.setConstraints(left, c);
       main.add(left);
        
        add(main, BorderLayout.CENTER);

        profileButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        profile = true;
                        setupProfile();
                    }
                });
        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButton();
                    }
                });
        testButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        test = true;
                        testButton();
                    }
                });
        viewButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        viewProfile();
                    }
                });
        setButtonStates(true);
    }
    
    static void addRow(JPanel main, GridBagLayout gb, GridBagConstraints c, int row, Component left, Component right) {
        c.gridx = 0;
        c.gridy = row;
        gb.setConstraints(left, c);
        main.add(left);
        c.gridx = 1;
        gb.setConstraints(right, c);
        main.add(right);
    }

    static JPanel makePadPanel(Component comp) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createRigidArea(new java.awt.Dimension(20,20)));
        panel.add(comp);
        return panel;
   }

    static JPanel makeLabelPanel(String text, Component comp) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(new JLabel(Bundle.getMessage(text)));
        panel.add(comp);
        return panel;
    }
    
    SensorDetails sensorA;
    SensorDetails sensorB;
    RosterEntry re;
    DccThrottle t;
    int finishSpeedStep;
    protected int stepIncr;
    protected int profileStep;
    protected float profileSpeed;
    protected float profileIncrement;
    RosterSpeedProfile rosterSpeedProfile;

    void setupProfile() {
        finishSpeedStep = 0;
        stepIncr = 1;
        profileStep = 1;
        try {
            Integer.parseInt(lengthField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorLengthInvalid"));
            return;
        }
        setButtonStates(false);
        if (sensorA == null) {
            try {
                sensorA = new SensorDetails((Sensor) sensorAPanel.getNamedBean());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Start"));
                setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorAPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Start"));
                setButtonStates(true);
                return;
            }
            if (tmpSen != sensorA.getSensor()) {
                sensorA.resetDetails();
                sensorA = new SensorDetails(tmpSen);
            }
        }
        if (sensorB == null) {
            try {
                sensorB = new SensorDetails((Sensor) sensorBPanel.getNamedBean());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Finish"));
                setButtonStates(true);
                return;
            }

        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorBPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Finish"));
                setButtonStates(true);
                return;
            }
            if (tmpSen != sensorB.getSensor()) {
                sensorB.resetDetails();
                sensorB = new SensorDetails(tmpSen);
            }
        }
        if (middleBlockSensor == null) {
            try {
                middleBlockSensor = new SensorDetails((Sensor) sensorCPanel.getNamedBean());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Block"));
                setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorCPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", "Block"));
                setButtonStates(true);
                return;
            }
            if (tmpSen != middleBlockSensor.getSensor()) {
                middleBlockSensor.resetDetails();
                middleBlockSensor = new SensorDetails(tmpSen);
            }
        }
        if (reBox.getSelectedRosterEntries().length == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoRosterSelected"));
            log.warn("No roster Entry selected.");
            setButtonStates(true);
            return;
        }
        String text = speedStepFrom.getText();
        if (text!=null && text.trim().length()>0) {
            try {
                profileStep = Integer.parseInt(text);
                if (!speedStepNumOK(profileStep, "LabelStartStep")) {
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelStartStep")));
                setButtonStates(true);
                return;
            }            
        }
        text = speedStepTo.getText();
        if (text!=null && text.trim().length()>0) {
            try {
                finishSpeedStep = Integer.parseInt(text);
                if (!speedStepNumOK(finishSpeedStep, "LabelFinishStep")) {
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelFinishStep")));
                setButtonStates(true);
                return;
            }            
        }
        text = speedStepIncr.getText();
        if (text!=null && text.trim().length()>0) {
            try {
                stepIncr = Integer.parseInt(text);
                if (!speedStepNumOK(stepIncr, "LabelStepIncr")) {
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelStepIncr")));
                setButtonStates(true);
                return;
            }            
        }
        
        re = reBox.getSelectedRosterEntries()[0];
        boolean ok = InstanceManager.throttleManagerInstance().requestThrottle(re, this);
        if (!ok) {
            log.warn("Throttle for locomotive " + re.getId() + " could not be setup.");
            setButtonStates(true);
            return;
        }
    }
    
    boolean speedStepNumOK(int num, String step) {
        if (num <1 || num>126) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage(step)));
            setButtonStates(true);
            return false;                   
        }
        return true;
    }

    javax.swing.Timer overRunTimer = null;

    public void notifyThrottleFound(DccThrottle _throttle) {
        t = _throttle;
        if (t == null) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorThrottleNotFound"));
            log.warn("null throttle returned for train  " + re.getId() + " during automatic initialization.");
            setButtonStates(true);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("throttle address= " + t.getLocoAddress().toString());
        }

        int speedStepMode = t.getSpeedStepMode();
        profileIncrement = t.getSpeedIncrement();
        int speedStep;
        if (speedStepMode == DccThrottle.SpeedStepMode14) {
            speedStep = 14;
        } else if (speedStepMode == DccThrottle.SpeedStepMode27) {
            speedStep = 27;
        } else if (speedStepMode == DccThrottle.SpeedStepMode28) {
            speedStep = 28;
        } else {// default to 128 speed step mode
            speedStep = 126;
        }
        if (finishSpeedStep<=0) {
            finishSpeedStep = speedStep;
        }

        log.debug("Speed step mode " + speedStepMode);
        profileSpeed = profileIncrement*profileStep;

        if (profile) {
            startSensor = middleBlockSensor.getSensor();
            finishSensor = sensorB.getSensor();
            startListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()) == Sensor.ACTIVE) {
                            startTiming();
                        }
                        if (((Integer) e.getNewValue()) == Sensor.INACTIVE) {
                            stopLoco();
                        }
                    }
                }
            };
            finishListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()) == Sensor.ACTIVE) {
                            stopCurrentSpeedStep();
                        }
                    }
                }
            };

            isForward = true;
            startProfile();
        } else {
            if (re.getSpeedProfile() == null) {
                log.error("Loco has no speed profile");
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSpeedProfile"));
                setButtonStates(true);
                return;
            }
            startSensor = middleBlockSensor.getSensor();
            startListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()) == Sensor.ACTIVE) {
                            stopTrainTest();
                        }
                    }
                }
            };
            startSensor.addPropertyChangeListener(startListener);
            int startstep = Integer.parseInt(speedStepTest.getText());
            isForward = true;
            t.setIsForward(isForward);
            profileSpeed = profileIncrement * startstep;
            t.setSpeedSetting(profileSpeed);
        }
    }

    void setButtonStates(boolean state) {
        cancelButton.setEnabled(!state);
        profileButton.setEnabled(state);
        testButton.setEnabled(state);
        viewButton.setEnabled(state);
        if (state) {
            sourceLabel.setText("   ");
            profile = false;
            test = false;
        }
        if (sensorA != null) {
            sensorA.resetDetails();
        }
        if (sensorB != null) {
            sensorB.resetDetails();
        }
        if (middleBlockSensor != null) {
            middleBlockSensor.resetDetails();
        }
    }

    public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason) {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorFailThrottleRequest"));
        log.error("Throttle request failed for " + address + " because " + reason);
        setButtonStates(true);
    }

    PropertyChangeListener startListener = null;
    PropertyChangeListener finishListener = null;
    PropertyChangeListener middleListener = null;

    Sensor startSensor;
    Sensor finishSensor;
    SensorDetails middleBlockSensor;

    void startProfile() {
        stepCalculated = false;
        sourceLabel.setText(Bundle.getMessage("StatusLabelNextRun"));
        if (isForward) {
            finishSensor = sensorB.getSensor();
        } else {
            finishSensor = sensorA.getSensor();
        }
        startSensor = middleBlockSensor.getSensor();
        startSensor.addPropertyChangeListener(startListener);
        finishSensor.addPropertyChangeListener(finishListener);
        t.setIsForward(isForward);
        log.debug("Set speed to " + profileSpeed + " isForward " + isForward);
        t.setSpeedSetting(profileSpeed);
        sourceLabel.setText(Bundle.getMessage("StatusLabelBlockToGoActive"));
    }

    boolean isForward = true;

    void startTiming() {
        startTime = System.nanoTime();
        sourceLabel.setText(Bundle.getMessage("StatusLabelCurrentRun", (isForward ? "(forward) " : "(reverse) "), profileStep, finishSpeedStep));
    }

    boolean stepCalculated = false;

    void stopCurrentSpeedStep() {
        finishTime = System.nanoTime();
        stepCalculated = true;
        finishSensor.removePropertyChangeListener(finishListener);
        sourceLabel.setText(Bundle.getMessage("StatusLabelCalculating"));
        if (profileStep >= 4) {
            t.setSpeedSetting(profileSpeed / 2);
        }
        calculateSpeed();
        sourceLabel.setText(Bundle.getMessage("StatusLabelWaitingToClear"));
    }

    void stopLoco() {

        if (!stepCalculated) {
            return;
        }

        startSensor.removePropertyChangeListener(startListener);
        finishSensor.removePropertyChangeListener(finishListener);

        isForward = !isForward;
        if (isForward) {
            profileSpeed = profileIncrement*stepIncr + profileSpeed;
            profileStep += stepIncr;
        }

        if (profileStep > finishSpeedStep) {
            t.setSpeedSetting(0.0f);
            updateSpeedProfileWithResults();
            setButtonStates(true);
            return;
        }
        // Loco may have been brought to half-speed in stopCurrentSpeedStep, so wait for that to take effect then stop & restart
        javax.swing.Timer stopTimer = new javax.swing.Timer(2500, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {

                // finally command the stop
                t.setSpeedSetting(0.0f);

                // and a second later, restart going the other way
                javax.swing.Timer restartTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        startProfile();
                    }
                });
                restartTimer.setRepeats(false);
                restartTimer.start();
            }
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
    }

    void calculateSpeed() {
        float duration = (((float) (finishTime - startTime)) / 1000000000); //Now in seconds
        duration = duration - 2f;  // Abratory time to add in feedback delay
        float length = (Integer.parseInt(lengthField.getText())); //left as mm
        float speed = length / duration;
        if (log.isDebugEnabled()) {
            log.debug("Step:" + profileStep + " duration:" + duration + " length:" + length + " speed:" + speed);
        }

        int iSpeedStep = Math.round(profileSpeed * 1000);
        if (!speeds.containsKey(iSpeedStep)) {
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);

        if (isForward) {
            ss.setForwardSpeed(speed);
        } else {
            ss.setReverseSpeed(speed);
        }
        save = true;
    }

    void updateSpeedProfileWithResults() {
        cancelButton();
        RosterSpeedProfile rosterSpeedProfile = re.getSpeedProfile();
        if (rosterSpeedProfile == null) {
            rosterSpeedProfile = new RosterSpeedProfile(re);
            re.setSpeedProfile(rosterSpeedProfile);
        } else if (clearPofile.isSelected()) {
            rosterSpeedProfile.clearCurrentProfile();
        }
        for (Integer i : speeds.keySet()) {
            rosterSpeedProfile.setSpeed(i, speeds.get(i).getForwardSpeed(), speeds.get(i).getReverseSpeed());
        }
        re.updateFile();
        Roster.writeRosterFile();
        save = false;
    }
    
    void viewProfile() {
        if (reBox.getSelectedRosterEntries().length == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoRosterSelected"));
            setButtonStates(true);
            return;
        }
        re = reBox.getSelectedRosterEntries()[0];
        if (re!=null) {
            RosterSpeedProfile speedProfile = re.getSpeedProfile();
            if (speedProfile != null) {
                if (table !=null) {
                    table.dispose();
                }
                table = new SpeedProfileTable(speedProfile, re.getId());
                table.setVisible(true);
                return;
            }            
        }
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSpeedProfile"));
        setButtonStates(true);
    }

    void cancelButton() {
        if (t != null) {
            t.setSpeedSetting(0.0f);
            InstanceManager.throttleManagerInstance().releaseThrottle(t, this);
            t = null;
        }
        if (startSensor != null) {
            startSensor.removePropertyChangeListener(startListener);
        }
        if (finishSensor != null) {
            finishSensor.removePropertyChangeListener(finishListener);
        }
        if (middleListener != null) {
            middleBlockSensor.getSensor().removePropertyChangeListener(middleListener);
        }
        setButtonStates(true);
    }

    void testButton() {
        //Should also test that the step is no greater than those avaialble on the throttle.
        try {
            Integer.parseInt(speedStepTest.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelTestStep")));
            return;
        }
        setupProfile();

    }

    void stopTrainTest() {
        int sectionlength = Integer.parseInt(lengthField.getText());
        re.getSpeedProfile().changeLocoSpeed(t, sectionlength, 0.0f);
        setButtonStates(true);
        startSensor.removePropertyChangeListener(startListener);
    }

    long startTime;
    long finishTime;

    ArrayList<Double> forwardOverRuns = new ArrayList<Double>();
    ArrayList<Double> reverseOverRuns = new ArrayList<Double>();

    JPanel update;

    static class SensorDetails {

        Sensor sensor = null;
        long inactiveDelay = 0;
        long activeDelay = 0;
        boolean usingGlobal = false;

        SensorDetails(Sensor sen) {
            sensor = sen;
            usingGlobal = sen.useDefaultTimerSettings();
            activeDelay = sen.getSensorDebounceGoingActiveTimer();
            inactiveDelay = sen.getSensorDebounceGoingInActiveTimer();
        }

        void setupSensor() {
            sensor.useDefaultTimerSettings(false);
            sensor.setSensorDebounceGoingActiveTimer(0);
            sensor.setSensorDebounceGoingInActiveTimer(0);
        }

        void resetDetails() {
            sensor.useDefaultTimerSettings(usingGlobal);
            sensor.setSensorDebounceGoingActiveTimer(activeDelay);
            sensor.setSensorDebounceGoingInActiveTimer(inactiveDelay);
        }

        Sensor getSensor() {
            return sensor;
        }

    }

    TreeMap<Integer, SpeedStep> speeds = new TreeMap<Integer, SpeedStep>();

    static class SpeedStep {

        float forward = 0.0f;
        float reverse = 0.0f;

        SpeedStep() {
        }

        void setForwardSpeed(float speed) {
            forward = speed;
        }

        void setReverseSpeed(float speed) {
            reverse = speed;
        }

        float getForwardSpeed() {
            return forward;
        }

        float getReverseSpeed() {
            return reverse;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedProfilePanel.class);
}
