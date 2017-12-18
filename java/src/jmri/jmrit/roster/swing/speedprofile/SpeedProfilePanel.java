package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ThrottleListener;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.jdom.JDOMUtil;
import jmri.util.swing.BeanSelectCreatePanel;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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

    public static final String XML_ROOT = "speedprofiler-config";
    public static final String XML_NAMESPACE = "http://jmri.org/xml/schema/speedometer-3-9-3.xsd";
    JButton profileButton = new JButton(Bundle.getMessage("ButtonStart"));
    JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton testButton = new JButton(Bundle.getMessage("ButtonTest"));
    JButton testCancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton clearNewDataButton = new JButton(Bundle.getMessage("ButtonClearNewData"));
    JButton viewNewButton = new JButton(Bundle.getMessage("ButtonViewNew"));
    JButton viewMergedButton = new JButton(Bundle.getMessage("ButtonViewMerged"));
    JButton viewButton = new JButton(Bundle.getMessage("ButtonViewCurrent"));

    JButton updateProfileButton = new JButton(Bundle.getMessage("ButtonUpdateProfile"));
    JButton replaceProfileButton = new JButton(Bundle.getMessage("ButtonSaveProfile"));
    JButton deleteProfileButton = new JButton(Bundle.getMessage("ButtonDeleteProfile"));
    JButton saveDefaultsButton = new JButton(Bundle.getMessage("ButtonSaveDefaults"));
    JTextField lengthField = new JTextField(10);
    JTextField sensorDelay = new JTextField(5);
    JTextField speedStepTest = new JTextField(5);
    JTextField speedStepTestFwd = new JTextField(10);
    JTextField speedStepTestRev = new JTextField(10);
    JTextField speedStepFrom = new JTextField(5);
    JTextField speedStepTo = new JTextField(5);
    JTextField speedStepIncr = new JTextField(5);
    JLabel warrentScaleLabel = new JLabel();

    // Start or finish sensor
    BeanSelectCreatePanel<Sensor> sensorAPanel = new BeanSelectCreatePanel<>(InstanceManager.sensorManagerInstance(), null);

    // Finish or start sensor
    BeanSelectCreatePanel<Sensor> sensorBPanel = new BeanSelectCreatePanel<>(InstanceManager.sensorManagerInstance(), null);

    // Block sensor
    BeanSelectCreatePanel<Block> blockCPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(jmri.BlockManager.class), null);
    BeanSelectCreatePanel<Sensor> sensorCPanel = new BeanSelectCreatePanel<>(InstanceManager.sensorManagerInstance(), null);

    RosterEntryComboBox reBox = new RosterEntryComboBox();
    SpeedProfileTable table = null;
    boolean profile = false;
    boolean test = false;
    float testSpeedFwd = 0.0f;
    float testSpeedRev = 0.0f;
    boolean save = false;
    boolean unmergedNewData = false;             // true is new data has been gathered but not merged to profile
    boolean unsavedUpdatedProfile = false;       // true if the roster profile has been updated but not saved.

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
        label = new JLabel(Bundle.getMessage("LabelSensorDelay"));
        addRow(main, gb, c, 1, label, sensorDelay);
        label = new JLabel(Bundle.getMessage("LabelStartSensor"));
        addRow(main, gb, c, 2, label, sensorAPanel);
        label = new JLabel(Bundle.getMessage("LabelBlockSensor"));
        addRow(main, gb, c, 3, label, sensorCPanel);
        label = new JLabel(Bundle.getMessage("LabelFinishSensor"));
        addRow(main, gb, c, 4, label, sensorBPanel);
        label = new JLabel(Bundle.getMessage("LabelSelectRoster"));
        JPanel left = makePadPanel(label);
        JPanel right = makePadPanel(reBox);
        addRow(main, gb, c, 5, left, right);
        JPanel panelViews = new JPanel();
        panelViews.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleView")));
        panelViews.setLayout(new BoxLayout(panelViews, BoxLayout.LINE_AXIS));
        panelViews.add(clearNewDataButton);
        panelViews.add(viewNewButton);
        panelViews.add(viewMergedButton);
        panelViews.add(viewButton);
        left = makePadPanel(panelViews);
        JPanel panelProfileControl = new JPanel();
        panelProfileControl.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ButtonProfile")));
        panelProfileControl.setLayout(new BoxLayout(panelProfileControl, BoxLayout.LINE_AXIS));
        panelProfileControl.add(profileButton);
        panelProfileControl.add(cancelButton);
        right = makePadPanel(panelProfileControl);
        addRow(main, gb, c, 6, left, right);

        left = new JPanel();
        left.add(Box.createRigidArea(new java.awt.Dimension(20, 10)));
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(makeLabelPanel("LabelStartStep", speedStepFrom));
        left.add(makeLabelPanel("LabelFinishStep", speedStepTo));
        left.add(makeLabelPanel("LabelStepIncr", speedStepIncr));
        right = new JPanel();
        addRow(main, gb, c, 7, left, right);

        JPanel testDataPanel = new JPanel();
        testDataPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TestProfileData")));
        testDataPanel.setLayout(new BoxLayout(testDataPanel, BoxLayout.LINE_AXIS));
        testDataPanel.add(makeLabelPanel("LabelTestStep", speedStepTest));
        testDataPanel.add(makeLabelPanel("LabelTestStepFwd", speedStepTestFwd));
        testDataPanel.add(makeLabelPanel("LabelTestStepRev", speedStepTestRev));
        left = makePadPanel(testDataPanel);

        JPanel testProfileControl = new JPanel();
        testProfileControl.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleTestProfile")));
        testProfileControl.setLayout(new BoxLayout(testProfileControl, BoxLayout.LINE_AXIS));
        testProfileControl.add(testButton);
        testProfileControl.add(testCancelButton);
        right = makePadPanel(testProfileControl);

        addRow(main, gb, c, 8, left, right);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 2;
        sourceLabel = new JLabel("   ");
        sourceLabel.setBackground(Color.white);
        left = makePadPanel(sourceLabel);
        gb.setConstraints(left, c);
        main.add(left);

        WarrantPreferences preferences = WarrantPreferences.getDefault();
        warrentScaleLabel.setText("Layout Scale: " + Float.toString(preferences.getLayoutScale()));
        warrentScaleLabel.setBackground(Color.white);
        left = makePadPanel(warrentScaleLabel);
        c.gridy = 11;
        gb.setConstraints(left, c);
        main.add(left);

        c.gridy = 12;
        JPanel southBtnPanel = new JPanel();
        southBtnPanel.add(clearNewDataButton);
        southBtnPanel.add(updateProfileButton);
        southBtnPanel.add(replaceProfileButton);
        southBtnPanel.add(deleteProfileButton);
        southBtnPanel.add(saveDefaultsButton);
        main.add(southBtnPanel, c);

        add(main, BorderLayout.CENTER);

        profileButton.addActionListener((ActionEvent e) -> {
            profile = true;
            setupProfile();
        });
        cancelButton.addActionListener((ActionEvent e) -> {
            cancelButton();
        });
        testButton.addActionListener((ActionEvent e) -> {
            test = true;
            testButton();
        });
        testCancelButton.addActionListener((ActionEvent e) -> {
            cancelButton();
        });
        viewButton.addActionListener((ActionEvent e) -> {
            viewRosterProfileData();
        });

        viewNewButton.addActionListener((ActionEvent e) -> {
            viewNewProfileData();
        });

        saveDefaultsButton.addActionListener((ActionEvent e) -> {
            doSaveSettings();
        });
        clearNewDataButton.addActionListener((ActionEvent e) -> {
            clearNewData();
        });
        viewMergedButton.addActionListener((ActionEvent e) -> {
            viewMergedData();
        });
        updateProfileButton.addActionListener((ActionEvent e) -> {
            updateSpeedProfileWithResults();
        });
        replaceProfileButton.addActionListener((ActionEvent e) -> {
            removeSpeedProfile();
            updateSpeedProfileWithResults();
        });
        deleteProfileButton.addActionListener((ActionEvent e) -> {
            removeSpeedProfile();
        });

        setButtonStates(true);
        //Attempt to reload last values */
        doLoad();

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
        panel.add(Box.createRigidArea(new java.awt.Dimension(20, 20)));
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
    protected int profileSpeedStepMode;
    protected float profileSensorDelay;
    protected float profileBlockLength;
    RosterSpeedProfile rosterSpeedProfile;

    void setupProfile() {
        String text;
        finishSpeedStep = 0;
        stepIncr = 1;
        profileStep = 1;
        profileSensorDelay = 0.0f;
        try {
            profileBlockLength = Float.parseFloat(lengthField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorLengthInvalid"));
            return;
        }
        text = sensorDelay.getText();
        if (text != null && text.trim().length() > 0) {
            try {
                profileSensorDelay = Float.parseFloat(sensorDelay.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorDelayInvalid"));
                return;
            }
        }
        setButtonStates(false);
        if (sensorA == null) {
            try {
                sensorA = new SensorDetails((Sensor) sensorAPanel.getNamedBean());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelStartSensor")));
                setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorAPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelStartSensor")));
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
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelFinishSensor")));
                setButtonStates(true);
                return;
            }

        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorBPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelFinishSensor")));
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
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelBlockSensor")));
                setButtonStates(true);
                return;
            }
        } else {
            Sensor tmpSen = null;
            try {
                tmpSen = (Sensor) sensorCPanel.getNamedBean();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSensorNotFound", Bundle.getMessage("LabelBlockSensor")));
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
        text = speedStepFrom.getText();
        if (text != null && text.trim().length() > 0) {
            try {
                profileStep = Integer.parseInt(text);
                if (!speedStepNumOK(profileStep, "LabelStartStep")) {
                    setButtonStates(true);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelStartStep")));
                setButtonStates(true);
                return;
            }
        }
        text = speedStepTo.getText();
        if (text != null && text.trim().length() > 0) {
            try {
                finishSpeedStep = Integer.parseInt(text);
                if (!speedStepNumOK(finishSpeedStep, "LabelFinishStep")) {
                    setButtonStates(true);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage("LabelFinishStep")));
                setButtonStates(true);
                return;
            }
        }
        text = speedStepIncr.getText();
        if (text != null && text.trim().length() > 0) {
            try {
                stepIncr = Integer.parseInt(text);
                if (!speedStepNumOK(stepIncr, "LabelStepIncr")) {
                    setButtonStates(true);
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
        if (num < 1 || num > 126) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSpeedStep", Bundle.getMessage(step)));
            setButtonStates(true);
            return false;
        }
        return true;
    }

    javax.swing.Timer overRunTimer = null;

    @Override
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
//        int speedStep;
        if (speedStepMode == DccThrottle.SpeedStepMode14) {
            profileSpeedStepMode = 14;
        } else if (speedStepMode == DccThrottle.SpeedStepMode27) {
            profileSpeedStepMode = 27;
        } else if (speedStepMode == DccThrottle.SpeedStepMode28) {
            profileSpeedStepMode = 28;
        } else {// default to 128 speed step mode
            profileSpeedStepMode = 126;
        }
        if (finishSpeedStep <= 0) {
            finishSpeedStep = profileSpeedStepMode;
        }

        log.debug("Speed step mode " + profileSpeedStepMode);
        profileSpeed = profileIncrement * profileStep;

        if (profile) {
            startSensor = middleBlockSensor.getSensor();
            finishSensor = sensorB.getSensor();
            startListener = new PropertyChangeListener() {
                @Override
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
                @Override
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
            // Speed test.
            // Once back and forth
            stepIncr = 1;
            profileStep = Integer.parseInt(speedStepTest.getText());
            finishSpeedStep = profileStep;
            profileSpeed = profileIncrement * profileStep;
            startSensor = middleBlockSensor.getSensor();
            finishSensor = sensorB.getSensor();
            startListener = new PropertyChangeListener() {
                @Override
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
                @Override
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
        }
    }

    void setButtonStates(boolean state) {
        cancelButton.setEnabled(!state);
        profileButton.setEnabled(state);
        testButton.setEnabled(state);
        testCancelButton.setEnabled(!state);
        viewButton.setEnabled(state);
        deleteProfileButton.setEnabled(state);
        if (state && speeds.size() > 0) {
            viewNewButton.setEnabled(true);
            viewMergedButton.setEnabled(true);
            replaceProfileButton.setEnabled(true);
            updateProfileButton.setEnabled(true);
            clearNewDataButton.setEnabled(true);
        } else {
            viewNewButton.setEnabled(false);
            viewMergedButton.setEnabled(false);
            replaceProfileButton.setEnabled(false);
            updateProfileButton.setEnabled(false);
            clearNewDataButton.setEnabled(false);
        }
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

    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorFailThrottleRequest"));
        log.error("Throttle request failed for " + address + " because " + reason);
        setButtonStates(true);
    }

    @Override
    public void notifyStealThrottleRequired(jmri.LocoAddress address){
        // this is an automatically stealing impelementation.
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
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
        t.setIsForward(!isForward);
        // this switching back a forward helps if the throttle was stolen.
        // the sleeps are needed as some systems dont like a speed setting right after a direction setting.
        //If we had guarenteed access to the dispatcher frame we could use
        //         Thread.sleep(InstanceManager.getDefault(DispatcherFrame.class).getMinThrottleInterval() * 2)
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            // Nothing I can do.
        }

        t.setIsForward(isForward);
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            // Nothing I can do.
        }

        log.debug("Set speed to [" + profileSpeed + "] isForward [" + isForward + "] Increment [" + profileIncrement + "] Step [" + profileStep + "] SpeedStepMode [" + profileSpeedStepMode + "]");
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
            profileSpeed = profileIncrement * stepIncr + profileSpeed;
            profileStep += stepIncr;
        }

        if (profileStep > finishSpeedStep) {
            t.setSpeedSetting(0.0f);
            if (!profile) {
                // there are only the 2 fields on screen to be updated
                speedStepTestFwd.setText(re.getSpeedProfile().convertMMSToScaleSpeedWithUnits(testSpeedFwd));
                speedStepTestRev.setText(re.getSpeedProfile().convertMMSToScaleSpeedWithUnits(testSpeedRev));
            }
            //updateSpeedProfileWithResults();
            setButtonStates(true);
            return;
        }
        // Loco may have been brought to half-speed in stopCurrentSpeedStep, so wait for that to take effect then stop & restart
        javax.swing.Timer stopTimer = new javax.swing.Timer(2500, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                // finally command the stop
                t.setSpeedSetting(0.0f);
                // and a second later, restart going the other way
                javax.swing.Timer restartTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
                    @Override
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
        duration = duration - (profileSensorDelay / 1000); // allow for time differences between sensor delays
        float speed = profileBlockLength / duration;
        if (log.isDebugEnabled()) {
            log.debug("Step:" + profileStep + " duration:" + duration + " length:" + profileBlockLength + " speed:" + speed);
        }


        if (profile) {
            // save results to table
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
        } else {
            // testing, save results to the 2 fields.
            if (isForward) {
                testSpeedFwd = speed;
            } else {
                testSpeedRev = speed;
            }
        }
    }

    /**
     * merge the new data into the existing speedprofile, or create if not
     * current, and save. clear new data.
     */
    void updateSpeedProfileWithResults() {
        cancelButton();
        RosterSpeedProfile rosterSpeedProfile = re.getSpeedProfile();
        if (rosterSpeedProfile == null) {
            rosterSpeedProfile = new RosterSpeedProfile(re);
            re.setSpeedProfile(rosterSpeedProfile);
        }
        for (Integer i : speeds.keySet()) {
            rosterSpeedProfile.setSpeed(i, speeds.get(i).getForwardSpeed(), speeds.get(i).getReverseSpeed());
        }
        re.updateFile();
        Roster.getDefault().writeRoster();
        clearNewData();
        setButtonStates(true);
        save = false;
    }

    /**
     * Merge the current profile with the new data in a temp area and show.
     */
    void viewMergedData() {
        // create a new temporay rosterspeedentry
        RosterEntry tmpRe = new RosterEntry();
        RosterSpeedProfile tmpRsp = new RosterSpeedProfile(tmpRe);
        // reference the current one.
        RosterSpeedProfile rosterSpeedProfile = re.getSpeedProfile();
        //copy across the profile data
        for (Integer i : rosterSpeedProfile.getProfileSpeeds().keySet()) {
            tmpRsp.setSpeed(i, rosterSpeedProfile.getProfileSpeeds().get(i).getForwardSpeed(), rosterSpeedProfile.getProfileSpeeds().get(i).getReverseSpeed());
        }
        //copy, merge the newdata speed points
        for (Integer i : speeds.keySet()) {
            tmpRsp.setSpeed(i, speeds.get(i).getForwardSpeed(), speeds.get(i).getReverseSpeed());
        }
        // show, its a bit convoluted, to get the speed table
        // we have to set the new profile in the tmp rosterentry
        // and ask for it back as a speedtable.
        tmpRe.setSpeedProfile(tmpRsp);
        RosterSpeedProfile tmpSp = tmpRe.getSpeedProfile();
        if (tmpSp != null) {
            if (table != null) {
                table.dispose();
            }
            table = new SpeedProfileTable(tmpSp, tmpRe.getId());
            table.setVisible(true);
            return;
        }
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSpeedProfile"));
        setButtonStates(true);
    }

    void clearNewData() {
        speeds.clear();
    }

    void removeSpeedProfile() {
        cancelButton();
        RosterSpeedProfile rosterSpeedProfile = re.getSpeedProfile();
        if (rosterSpeedProfile != null) {
            rosterSpeedProfile.clearCurrentProfile();
        }
        re.updateFile();
        Roster.getDefault().writeRoster();
        save = false;
    }

    /**
     * View the new data collected we create a dummy entry and file with
     * collected data
     */
    void viewNewProfileData() {
        RosterEntry tmpRe = new RosterEntry();
        RosterSpeedProfile rosterSpeedProfile = tmpRe.getSpeedProfile();
        if (rosterSpeedProfile == null) {
            rosterSpeedProfile = new RosterSpeedProfile(tmpRe);
            tmpRe.setSpeedProfile(rosterSpeedProfile);
        }
        for (Integer i : speeds.keySet()) {
            rosterSpeedProfile.setSpeed(i, speeds.get(i).getForwardSpeed(), speeds.get(i).getReverseSpeed());
        }
        if (tmpRe != null) {
            RosterSpeedProfile speedProfile = tmpRe.getSpeedProfile();
            if (speedProfile != null) {
                if (table != null) {
                    table.dispose();
                }
                table = new SpeedProfileTable(speedProfile, tmpRe.getId());
                table.setVisible(true);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSpeedProfile"));
        setButtonStates(true);
    }

    /**
     * View the current speedprofile table entrys
     */
    void viewRosterProfileData() {
        if (reBox.getSelectedRosterEntries().length == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoRosterSelected"));
            setButtonStates(true);
            return;
        }
        re = reBox.getSelectedRosterEntries()[0];
        if (re != null) {
            RosterSpeedProfile speedProfile = re.getSpeedProfile();
            if (speedProfile != null) {
                if (table != null) {
                    table.dispose();
                }
                table = new SpeedProfileTable(re.getSpeedProfile(), re.getId());
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
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // Nothing I can do.
            }

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
        } catch (NumberFormatException e) {
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
            usingGlobal = sen.getUseDefaultTimerSettings();
            activeDelay = sen.getSensorDebounceGoingActiveTimer();
            inactiveDelay = sen.getSensorDebounceGoingInActiveTimer();
        }

        void setupSensor() {
            sensor.setUseDefaultTimerSettings(false);
            sensor.setSensorDebounceGoingActiveTimer(0);
            sensor.setSensorDebounceGoingInActiveTimer(0);
        }

        void resetDetails() {
            sensor.setUseDefaultTimerSettings(usingGlobal);
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

    /*
     *  Here starts the code for saving and restoring the settings
     */
    /**
     * Save current sensor and block information to file
     */
    private void doSaveSettings() {
        log.debug("Start storing SpeedProfiler settings...");

        // Create root element
        Element root = new Element(XML_ROOT, XML_NAMESPACE);

        Element values;

        // Store configuration
        root.addContent(values = new Element("configuration"));
        if (lengthField.getText().length() > 0) {
            values.addContent(new Element("length").addContent(lengthField.getText()));
        }
        if (sensorDelay.getText().length() > 0) {
            values.addContent(new Element("sensordelay").addContent(sensorDelay.getText()));
        }
        // Store values
        //if (sensorAPanel.getNamedBean(). > 0) {
        // Create sensors element
        root.addContent(values = new Element("sensors"));

        // Store start sensor
        Element e = new Element("sensor");
        e.addContent(new Element("sensorname").addContent("sensorAPanel"));
        e.addContent(new Element("sensorvalue").addContent(sensorAPanel.getDisplayName()));
        values.addContent(e);
        e = new Element("sensor");
        e.addContent(new Element("sensorname").addContent("sensorBPanel"));
        e.addContent(new Element("sensorvalue").addContent(sensorBPanel.getDisplayName()));
        values.addContent(e);
        e = new Element("sensor");
        e.addContent(new Element("sensorname").addContent("sensorCPanel"));
        e.addContent(new Element("sensorvalue").addContent(sensorCPanel.getDisplayName()));
        values.addContent(e);
        root.addContent(values = new Element("steps"));
        if (speedStepFrom.getText().length() > 0) {
            values.addContent(new Element("speedStepFrom").addContent(speedStepFrom.getText()));
        }
        if (speedStepTo.getText().length() > 0) {
            values.addContent(new Element("speedStepTo").addContent(speedStepTo.getText()));
        }
        if (speedStepIncr.getText().length() > 0) {
            values.addContent(new Element("speedStepIncr").addContent(speedStepIncr.getText()));
        }

        try {
            ProfileUtils.getAuxiliaryConfiguration(ProfileManager.getDefault().getActiveProfile())
                    .putConfigurationFragment(JDOMUtil.toW3CElement(root), true);
        } catch (JDOMException ex) {
            log.error("Unable to create create XML", ex);
        }

        log.debug("...done");
    }

    /*
     * Loads the Block and sensor information previously saved
     */
    private void doLoad() {
        Element root;

        log.debug("Check if there's anything to load");
        try {
            root = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(ProfileManager.getDefault().getActiveProfile())
                    .getConfigurationFragment(XML_ROOT, XML_NAMESPACE, true));
        } catch (NullPointerException ex) {
            // expected if never saved before
            log.debug("Nothing to load");
            return;
        }

        log.debug("Start loading SpeedProfiler settings...");

        // First read configuration
        if (root.getChild("configuration") != null) {
            @SuppressWarnings("unchecked")
            List<Element> l = root.getChild("configuration").getChildren();
            if (log.isDebugEnabled()) {
                log.debug("readFile sees " + l.size() + " configurations");
            }
            for (int i = 0; i < l.size(); i++) {
                Element e = l.get(i);
                switch (e.getName()) {
                    case "length":
                        lengthField.setText(e.getValue());
                        break;
                    case "sensordelay":
                        sensorDelay.setText(e.getValue());
                        break;
                    default:
                        log.warn("Invalid field in PanelProSpeedProfiler.xml");
                }
            }
        }
        // Now read sensor information
        if (root.getChild("sensors") != null) {
            @SuppressWarnings("unchecked")
            List<Element> l = root.getChild("sensors").getChildren("sensor");
            if (log.isDebugEnabled()) {
                log.debug("readFile sees " + l.size() + " sensors");
            }
            SensorManager manager = InstanceManager.getDefault(SensorManager.class);
            for (int i = 0; i < l.size(); i++) {
                Element e = l.get(i);
                String sensorType = e.getChild("sensorname").getText();
                switch (sensorType) {
                    case "sensorAPanel":
                        sensorAPanel.setDefaultNamedBean(manager.getByUserName(e.getChild("sensorvalue").getText()));
                        break;
                    case "sensorBPanel":
                        sensorBPanel.setDefaultNamedBean(manager.getByUserName(e.getChild("sensorvalue").getText()));
                        break;
                    case "sensorCPanel":
                        sensorCPanel.setDefaultNamedBean(manager.getByUserName(e.getChild("sensorvalue").getText()));
                        break;
                    default:
                        log.warn("Invalid Sensor found in DecoderProSpeedProfile.xml");
                }
            }
        }
        if (root.getChild("steps") != null) {
            @SuppressWarnings("unchecked")
            List<Element> l = root.getChild("steps").getChildren();
            for (int i = 0; i < l.size(); i++) {
                Element e = l.get(i);
                switch (e.getName()) {
                    case "speedStepFrom":
                        speedStepFrom.setText(e.getValue());
                        break;
                    case "speedStepTo":
                        speedStepTo.setText(e.getValue());
                        break;
                    case "speedStepIncr":
                        speedStepIncr.setText(e.getValue());
                        break;
                    default:
                        log.warn("Invalid field in steps of PanelProSpeedProfiler.xml");
                }
            }
        }

        log.debug("...done");
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedProfilePanel.class);
}
