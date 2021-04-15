package jmri.jmrit.beantable.light;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.implementation.DefaultLightControl;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.ComboBoxToolTipRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to add or edit a single Light Control.
 * Code originally within LightTableAction.
 * 
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class AddEditSingleLightControlFrame extends jmri.util.JmriJFrame {
    
    final LightControl lc;
    private JComboBox<String> typeBox;
    
    private final JLabel status1 = new JLabel();
    
    private final NamedBeanComboBox<Sensor> sensor1Box = new NamedBeanComboBox<>( // Sensor (1 or only)
            InstanceManager.sensorManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Sensor> sensor2Box = new NamedBeanComboBox<>( // Sensor 2
            InstanceManager.sensorManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    
    private final JLabel f1Label = new JLabel(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", ""))); // for 1 sensor
    private final JLabel f1aLabel = new JLabel(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", " 2"))); // for 2nd sensor

    private final SpinnerNumberModel fastHourSpinnerModel1 = new SpinnerNumberModel(0, 0, 23, 1); // 0 - 23 h
    private final JSpinner fastHourSpinner1 = new JSpinner(fastHourSpinnerModel1); // Fast Clock1 hours
    private final SpinnerNumberModel fastMinuteSpinnerModel1 = new SpinnerNumberModel(0, 0, 59, 1); // 0 - 59 min
    private final JSpinner fastMinuteSpinner1 = new JSpinner(fastMinuteSpinnerModel1); // Fast Clock1 minutes
    private final JLabel clockSep1 = new JLabel(" : ");
    private final JLabel clockSep2 = new JLabel(" : ");
    
    private final SpinnerNumberModel fastHourSpinnerModel2 = new SpinnerNumberModel(0, 0, 23, 1); // 0 - 23 h
    private final JSpinner fastHourSpinner2 = new JSpinner(fastHourSpinnerModel2); // Fast Clock2 hours
    private final SpinnerNumberModel fastMinuteSpinnerModel2 = new SpinnerNumberModel(0, 0, 59, 1); // 0 - 59 min
    private final JSpinner fastMinuteSpinner2 = new JSpinner(fastMinuteSpinnerModel2); // Fast Clock2 minutes
    
    private final NamedBeanComboBox<Turnout> turnoutBox = new NamedBeanComboBox<>( // Turnout
            InstanceManager.turnoutManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Sensor> sensorOnBox = new NamedBeanComboBox<>( // Timed ON
            InstanceManager.sensorManagerInstance(), null, NamedBean.DisplayOptions.DISPLAYNAME);
    
    private JComboBox<String> stateBox;
    private ComboBoxToolTipRenderer stateBoxToolTipRenderer;
    
    private final SpinnerNumberModel timedOnSpinnerModel = new SpinnerNumberModel(0, 0, 1000000, 1); // 0 - 1,000,000 msec
    private final JSpinner timedOnSpinner = new JSpinner(timedOnSpinnerModel); // Timed ON
    
    private JPanel sensorTwoPanel;
    
    private final JLabel f2Label = new JLabel(Bundle.getMessage("LightSensorSense"));
    
    private final int sensorActiveIndex = 0;
    private final int sensorInactiveIndex = 1;
    private final int turnoutClosedIndex = 0;
    private final int turnoutThrownIndex = 1;
    
    private JButton createControl;
    private JButton updateControl;
    private JButton cancelControl;
    
    final LightControlPane lcp;
    
    /**
     * Create a new Frame to Add or Edit a Light Control.
     * 
     * @param pane Light Control Pane which instigated the action.
     * @param ctrl If LightControl is null, is a Add Control Window.
     *              If LightControl specified, is an Edit Control window.
     */
    public AddEditSingleLightControlFrame(@Nonnull LightControlPane pane, LightControl ctrl){
        super(Bundle.getMessage("TitleAddLightControl"), false, true);
        lc = ctrl;
        lcp = pane;
        init();
    }
    
    private void init(){
    
        addHelpMenu("package.jmri.jmrit.beantable.LightAddEdit", true);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        
        JPanel controlTypePanel = new JPanel();
        controlTypePanel.setLayout(new FlowLayout());
        controlTypePanel.add(new JLabel(Bundle.getMessage("LightControlType")));
        typeBox = new JComboBox<>(LightControlTableModel.controlTypes);
        ComboBoxToolTipRenderer typeBoxToolTipRenderer = new ComboBoxToolTipRenderer();
        typeBoxToolTipRenderer.setTooltips(LightControlTableModel.getControlTypeTips());
        typeBox.setRenderer(typeBoxToolTipRenderer);
        
        typeBox.addActionListener((ActionEvent e) -> setUpControlType(typeBox.getSelectedIndex()));
        typeBox.setToolTipText(Bundle.getMessage("LightControlTypeHint"));
        
        controlTypePanel.add(typeBox);
        
        JPanel mainOptionsPanel = new JPanel();
        mainOptionsPanel.setLayout(new FlowLayout());
        mainOptionsPanel.add(f1Label);
        mainOptionsPanel.add(sensor1Box);
        
        // set up number formatting
        JSpinner.NumberEditor ne1b = new JSpinner.NumberEditor(fastHourSpinner1, "00"); // 2 digits "01" format
        fastHourSpinner1.setEditor(ne1b);
        mainOptionsPanel.add(fastHourSpinner1);  // hours ON
        mainOptionsPanel.add(clockSep1);
        JSpinner.NumberEditor ne1b1 = new JSpinner.NumberEditor(fastMinuteSpinner1, "00"); // 2 digits "01" format
        fastMinuteSpinner1.setEditor(ne1b1);
        mainOptionsPanel.add(fastMinuteSpinner1); // minutes ON
        mainOptionsPanel.add(turnoutBox);
        mainOptionsPanel.add(sensorOnBox);

        sensor1Box.setAllowNull(true);
        sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));

        sensor2Box.setAllowNull(true);
        sensor2Box.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));

        fastHourSpinner1.setValue(0);  // reset needed
        fastHourSpinner1.setVisible(false);
        fastMinuteSpinner1.setValue(0); // reset needed
        fastMinuteSpinner1.setVisible(false);

        sensorOnBox.setAllowNull(true);
        sensorOnBox.setVisible(false);
        clockSep1.setVisible(false);

        turnoutBox.setAllowNull(true);
        turnoutBox.setVisible(false);

        sensorTwoPanel = new JPanel();
        sensorTwoPanel.setLayout(new FlowLayout());
        sensorTwoPanel.add(f1aLabel);
        sensorTwoPanel.add(sensor2Box);

        JPanel panel33 = new JPanel();
        panel33.setLayout(new FlowLayout());
        
        
        panel33.add(f2Label);
        
        stateBox = new JComboBox<>(new String[]{
            Bundle.getMessage("SensorStateActive"), Bundle.getMessage("SensorStateInactive")});
        stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
        stateBoxToolTipRenderer = new ComboBoxToolTipRenderer();
        stateBox.setRenderer(stateBoxToolTipRenderer);
        panel33.add(stateBox);

        JSpinner.NumberEditor ne2a = new JSpinner.NumberEditor(fastHourSpinner2, "00"); // 2 digits "01" format
        fastHourSpinner2.setEditor(ne2a);
        panel33.add(fastHourSpinner2);  // hours OFF
        panel33.add(clockSep2);
        
        JSpinner.NumberEditor ne2a1 = new JSpinner.NumberEditor(fastMinuteSpinner2, "00"); // 2 digits "01" format
        fastMinuteSpinner2.setEditor(ne2a1);
        panel33.add(fastMinuteSpinner2); // minutes OFF
        panel33.add(timedOnSpinner);

        fastHourSpinner2.setValue(0);  // reset needed
        fastHourSpinner2.setVisible(false);
        fastMinuteSpinner2.setValue(0); // reset needed
        fastMinuteSpinner2.setVisible(false);

        timedOnSpinner.setValue(5000);  // reset needed, default to 5,000 ms
        timedOnSpinner.setVisible(false);
        clockSep2.setVisible(false);

        mainContentPanel.add(controlTypePanel);
        mainContentPanel.add(mainOptionsPanel);
        mainContentPanel.add(sensorTwoPanel);
        mainContentPanel.add(panel33);
        mainContentPanel.setBorder(BorderFactory.createEtchedBorder());
        contentPane.add(mainContentPanel);
        contentPane.add(getButtonPanel());
        
        JPanel statusPanel = new JPanel();
        statusPanel.add(status1);
        contentPane.add(statusPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelControlPressed(null);
            }
        });
        
        typeBox.setSelectedIndex(lcp.getLastSelectedControlIndex()); // force GUI status consistent
        
        if (lc!=null){
            setTitle(Bundle.getMessage("TitleEditLightControl"));
            setFrameToControl(lc);
        }
        
    }
    
    private JPanel getButtonPanel(){
    
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        cancelControl = new JButton(Bundle.getMessage("ButtonCancel"));
        buttonPanel.add(cancelControl);
        cancelControl.addActionListener(this::cancelControlPressed);
        cancelControl.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
        createControl = new JButton(Bundle.getMessage("ButtonCreate"));
        buttonPanel.add(createControl);
        createControl.addActionListener(this::createControlPressed);
        createControl.setToolTipText(Bundle.getMessage("LightCreateControlButtonHint"));
        updateControl = new JButton(Bundle.getMessage("ButtonUpdate"));
        buttonPanel.add(updateControl);
        updateControl.addActionListener(this::updateControlPressed);
        updateControl.setToolTipText(Bundle.getMessage("LightUpdateControlButtonHint"));
        
        cancelControl.setVisible(true);
        updateControl.setVisible(lc!=null);
        createControl.setVisible(lc==null);
    
        return buttonPanel;
    }

    /**
     * Set the Control Information according to control type.
     *
     * @param ctype the control type
     */
    private void setUpControlType(int ctype) {
        // set everything non-visible by default
        clockSep1.setVisible(false);
        clockSep2.setVisible(false);
        fastHourSpinner1.setVisible(false);
        fastHourSpinner2.setVisible(false);
        fastMinuteSpinner1.setVisible(false);
        fastMinuteSpinner2.setVisible(false);
        f1aLabel.setVisible(false);
        sensorOnBox.setVisible(false);
        sensor1Box.setVisible(false);
        sensor2Box.setVisible(false);
        stateBox.setVisible(false);
        timedOnSpinner.setVisible(false);
        turnoutBox.setVisible(false);
        sensorTwoPanel.setVisible(false);
        typeBox.setSelectedIndex(ctype);
        createControl.setEnabled(true);
        updateControl.setEnabled(true);
        
        lcp.setLastSelectedControlIndex(ctype);
        
        List<String> stateTooltips;

        switch (ctype) {
            case Light.SENSOR_CONTROL:
                // set up panel for sensor control
                f1Label.setText(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", ""))); // insert nothing before colon
                sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));
                f2Label.setText(Bundle.getMessage("LightSensorSense"));
                stateBox.removeAllItems();
                stateBox.addItem(Bundle.getMessage("SensorStateActive"));
                stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
                stateTooltips = new ArrayList<>();
                stateTooltips.add(Bundle.getMessage("LightSensorSenseActivTip"));
                stateTooltips.add(Bundle.getMessage("LightSensorSenseInactivTip"));
                stateBoxToolTipRenderer.setTooltips(stateTooltips);
                stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
                f2Label.setVisible(true);
                sensor1Box.setVisible(true);
                stateBox.setVisible(true);
                
                break;
            case Light.FAST_CLOCK_CONTROL:
                // set up panel for fast clock control
                f1Label.setText(Bundle.getMessage("LightScheduleOn"));
                fastHourSpinner1.setToolTipText(Bundle.getMessage("LightScheduleHint"));
                fastMinuteSpinner1.setToolTipText(Bundle.getMessage("LightScheduleHintMinutes"));
                f2Label.setText(Bundle.getMessage("LightScheduleOff"));
                fastHourSpinner2.setToolTipText(Bundle.getMessage("LightScheduleHint"));
                fastMinuteSpinner2.setToolTipText(Bundle.getMessage("LightScheduleHintMinutes"));
                clockSep1.setVisible(true);
                clockSep2.setVisible(true);
                fastHourSpinner1.setVisible(true);
                fastHourSpinner2.setVisible(true);
                fastMinuteSpinner1.setVisible(true);
                fastMinuteSpinner2.setVisible(true);
                f2Label.setVisible(true);
                
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                // set up panel for turnout status control
                f1Label.setText(Bundle.getMessage("LightTurnout"));
                turnoutBox.setToolTipText(Bundle.getMessage("LightTurnoutHint"));
                f2Label.setText(Bundle.getMessage("LightTurnoutSense"));

                stateBox.removeAllItems();
                stateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
                stateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
                stateBox.setToolTipText(Bundle.getMessage("LightTurnoutSenseHint"));

                stateTooltips = new ArrayList<>();
                stateTooltips.add(Bundle.getMessage("LightConToClosedOrThrownTip",
                        InstanceManager.turnoutManagerInstance().getClosedText(),
                        InstanceManager.turnoutManagerInstance().getThrownText()));

                stateTooltips.add(Bundle.getMessage("LightConToClosedOrThrownTip",
                        InstanceManager.turnoutManagerInstance().getThrownText(),
                        InstanceManager.turnoutManagerInstance().getClosedText()));
                stateBoxToolTipRenderer.setTooltips(stateTooltips);


                f2Label.setVisible(true);
                turnoutBox.setVisible(true);
                stateBox.setVisible(true);

                break;
            case Light.TIMED_ON_CONTROL:
                // set up panel for sensor control
                f1Label.setText(Bundle.getMessage("LightTimedSensor"));
                sensorOnBox.setToolTipText(Bundle.getMessage("LightTimedSensorHint"));
                f2Label.setText(Bundle.getMessage("LightTimedDurationOn"));
                timedOnSpinner.setToolTipText(Bundle.getMessage("LightTimedDurationOnHint"));
                f2Label.setVisible(true);
                sensorOnBox.setVisible(true);
                timedOnSpinner.setVisible(true);
                
                break;
            case Light.TWO_SENSOR_CONTROL:
                // set up panel for two sensor control
                sensorTwoPanel.setVisible(true);
                f1Label.setText(Bundle.getMessage("LightSensor", " " + Bundle.getMessage("MakeLabel", "1"))); // for 2-sensor use, insert number "1" before colon
                f1aLabel.setVisible(true);
                sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));
                f2Label.setText(Bundle.getMessage("LightSensorSense"));

                stateBox.removeAllItems();
                stateBox.addItem(Bundle.getMessage("SensorStateActive"));
                stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
                stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));

                stateTooltips = new ArrayList<>();
                stateTooltips.add(Bundle.getMessage("Light2SensorSenseActivTip"));
                stateTooltips.add(Bundle.getMessage("Light2SensorSenseInactivTip"));
                stateBoxToolTipRenderer.setTooltips(stateTooltips);

                f2Label.setVisible(true);
                sensor1Box.setVisible(true);
                sensor2Box.setVisible(true);
                sensor1Box.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));
                stateBox.setVisible(true);

                break;
            case Light.NO_CONTROL:
                // set up panel for no control
                f1Label.setText(Bundle.getMessage("LightNoneSelected"));
                f2Label.setVisible(false);
                createControl.setEnabled(false);
                updateControl.setEnabled(false);
                break;
            default:
                log.error("Unexpected control type in controlTypeChanged: {}", ctype);
                break;
        }
        pack();
        setVisible(true);
    }
    
    protected void cancelControlPressed(ActionEvent e) {
        lcp.closeEditControlWindow();
    }
    
    private void commitEdits(){
        try {
            fastHourSpinner1.commitEdit();
            fastHourSpinner2.commitEdit();
            fastMinuteSpinner1.commitEdit();
            fastMinuteSpinner2.commitEdit();
            timedOnSpinner.commitEdit();
        } catch (java.text.ParseException pe) {
            // unlikely to be thrown as values set to original if incorrect on commitEdit()
        }
    }
    
    protected void updateControlPressed(ActionEvent e) {
        commitEdits();
        LightControl newLc = new DefaultLightControl();
        ArrayList<LightControl> withoutExistingLc = new ArrayList<>(lcp.getControlList());
        withoutExistingLc.remove(lc);
        if (setControlInformation(newLc,withoutExistingLc)) {
            lcp.updateControlPressed(lc,newLc);
            cancelControlPressed(e);
        } else {
            pack();
            setVisible(true);
        }
    }
    
    protected void createControlPressed(ActionEvent e) {
        if (Objects.equals(typeBox.getSelectedItem(), LightControlTableModel.noControl)) {
            return;
        }
        
        commitEdits();
        LightControl newLc = new DefaultLightControl();
        if (setControlInformation(newLc,lcp.getControlList())) {
            lcp.addControlToTable(newLc);
            cancelControlPressed(e);
        } else {
            pack();
            setVisible(true);
        }
    }
    
    private void notifyUser(String message, Color color){
        status1.setText(message);
        status1.setForeground(color);
        jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{
            status1.setText(" ");
        },5000);
    
    }
    
    /**
     * Retrieve control information from pane and update Light Control.
     *
     * @param g LightControl to set to User Settings.
     * @param currentList current Light Control List, used to check that Fast Clock Times are OK.
     * @return 'true' if no errors or warnings
     */
    private boolean setControlInformation(LightControl g, List<LightControl> currentList) {
        // Get control information
        if (LightControlTableModel.sensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SENSOR_CONTROL);
            // Get sensor control information
            Sensor s = null;
            String sensorName = sensor1Box.getSelectedItemDisplayName();
            if (sensorName == null) {
                // no sensor selected
                g.setControlType(Light.NO_CONTROL);
                notifyUser(Bundle.getMessage("LightWarn8"),Color.gray);
            } else {
                // name was selected, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        sensor1Box.setSelectedItem(s);
                    }
                }
            }
            int sState =  ( Bundle.getMessage("SensorStateInactive").equals(stateBox.getSelectedItem()) 
                ? Sensor.INACTIVE : Sensor.ACTIVE);
            g.setControlSensorName(sensorName);
            g.setControlSensorSense(sState);
            if (s == null) {
                notifyUser(Bundle.getMessage("LightWarn1"),Color.red);
                return false;
            }
        } else if (LightControlTableModel.fastClockControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.FAST_CLOCK_CONTROL);
            // read and parse the hours and minutes in the 2 x 2 spinners
            int onHour = (Integer) fastHourSpinner1.getValue();  // hours
            int onMin = (Integer) fastMinuteSpinner1.getValue();  // minutes
            int offHour = (Integer) fastHourSpinner2.getValue(); // hours
            int offMin = (Integer) fastMinuteSpinner2.getValue(); // minutes

            g.setFastClockControlSchedule(onHour, onMin, offHour, offMin);

            if (g.onOffTimesFaulty()) {
                notifyUser(Bundle.getMessage("LightWarn11"),Color.red);
                return false;
            }

            if (g.areFollowerTimesFaulty(currentList)) {
                notifyUser(Bundle.getMessage("LightWarn12"),Color.red);
                return false;
            }

        } else if (LightControlTableModel.turnoutStatusControl.equals(typeBox.getSelectedItem())) {
            boolean error = false;
            Turnout t = null;
            // Set type of control
            g.setControlType(Light.TURNOUT_STATUS_CONTROL);
            // Get turnout control information
            String turnoutName = turnoutBox.getSelectedItemSystemName();
            if (turnoutName == null) {
                // no turnout selected
                g.setControlType(Light.NO_CONTROL);
                notifyUser(Bundle.getMessage("LightWarn10"),Color.gray);
            } else {

                // TODO : Remove Turnouts which are actually lights ( ???? )
                // from the JComboBox list, not after the user has selected one.

                // Ensure that this Turnout is not already a Light
                // String prefix = Objects.requireNonNull(prefixBox.getSelectedItem()).getSystemPrefix();                
                String prefix = InstanceManager.getDefault(LightManager.class).getSystemPrefix();
                if (turnoutName.charAt(prefix.length()) == 'T') {
                    // must be a standard format name (not just a number)
                    String testSN = prefix + "L"
                            + turnoutName.substring(prefix.length() + 1);
                    Light testLight = InstanceManager.getDefault(LightManager.class).
                            getBySystemName(testSN);
                    if (testLight != null) {
                        // Requested turnout bit is already assigned to a Light
                        notifyUser(Bundle.getMessage("LightWarn3") + " " + testSN + ".",Color.red);
                        error = true;
                    }
                }
                if (!error) {
                    // Requested turnout bit is not assigned to a Light
                    t = InstanceManager.turnoutManagerInstance().
                            getByUserName(turnoutName);
                    if (t == null) {
                        // not user name, try system name
                        t = InstanceManager.turnoutManagerInstance().
                                getBySystemName(turnoutName);
                        if (t != null) {
                            // update turnout system name in case it changed
                            turnoutName = t.getSystemName();
                            turnoutBox.setSelectedItem(t);
                        }
                    }
                }
            }
            // Initialize the requested Turnout State
            int tState = Turnout.CLOSED;
            if (Objects.equals(stateBox.getSelectedItem(), InstanceManager.
                    turnoutManagerInstance().getThrownText())) {
                tState = Turnout.THROWN;
            }
            g.setControlTurnout(turnoutName);
            g.setControlTurnoutState(tState);
            if (t == null) {
                notifyUser(Bundle.getMessage("LightWarn2"),Color.red);
                return false;
            }
        } else if (LightControlTableModel.timedOnControl.equals(typeBox.getSelectedItem())) {
            Sensor s = null;
            // Set type of control
            g.setControlType(Light.TIMED_ON_CONTROL);
            // Get trigger sensor control information
            String triggerSensorName = sensorOnBox.getSelectedItemDisplayName();
            if (triggerSensorName == null) {
                // Trigger sensor not selected
                g.setControlType(Light.NO_CONTROL);
                notifyUser(Bundle.getMessage("LightWarn8"),Color.gray);
            } else {
                // sensor was selected, try user name first
                s = InstanceManager.sensorManagerInstance().getByUserName(triggerSensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(triggerSensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        triggerSensorName = s.getSystemName();
                        sensorOnBox.setSelectedItem(s);
                    }
                }
            }
            g.setControlTimedOnSensorName(triggerSensorName);
            int dur = (Integer) timedOnSpinner.getValue();
            g.setTimedOnDuration(dur);
            if (s == null) {
                notifyUser(Bundle.getMessage("LightWarn8"),Color.red);
                return false;
            }
        } else if (LightControlTableModel.twoSensorControl.equals(typeBox.getSelectedItem())) {
            Sensor s = null;
            Sensor s2;
            // Set type of control
            g.setControlType(Light.TWO_SENSOR_CONTROL);
            // Get sensor control information
            String sensorName = sensor1Box.getSelectedItemDisplayName();
            String sensor2Name = sensor2Box.getSelectedItemDisplayName();
            if (sensorName == null || sensor2Name == null) {
                // no sensor(s) selected
                g.setControlType(Light.NO_CONTROL);
                notifyUser(Bundle.getMessage("LightWarn8"),Color.gray);
            } else {
                // name was selected, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        sensor1Box.setSelectedItem(s);
                    }
                }
                s2 = InstanceManager.sensorManagerInstance().
                        getByUserName(sensor2Name);
                if (s2 == null) {
                    // not user name, try system name
                    s2 = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensor2Name);
                    if (s2 != null) {
                        // update sensor system name in case it changed
                        sensor2Name = s2.getSystemName();
                        sensor2Box.setSelectedItem(s2);
                    }
                }
            }
            int sState = Sensor.ACTIVE;
            if (Objects.equals(stateBox.getSelectedItem(), Bundle.getMessage("SensorStateInactive"))) {
                sState = Sensor.INACTIVE;
            }
            g.setControlSensorName(sensorName);
            g.setControlSensor2Name(sensor2Name);
            g.setControlSensorSense(sState);
            if (s == null) {
                notifyUser(Bundle.getMessage("LightWarn1"),Color.red);
                return false;
            }
        } else if (LightControlTableModel.noControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.NO_CONTROL);
        } else {
            log.error("Unexpected control type: {}", typeBox.getSelectedItem());
        }
        return (true);
    }
    
    private void setFrameToControl(LightControl lc){
    
        int ctType = lc.getControlType();
        switch (ctType) {
            case Light.SENSOR_CONTROL:
                setUpControlType(Light.SENSOR_CONTROL);
                sensor1Box.setSelectedItemByName(lc.getControlSensorName());
                stateBox.setSelectedIndex( (lc.getControlSensorSense() == Sensor.ACTIVE)? sensorActiveIndex : sensorInactiveIndex);
                break;
            case Light.FAST_CLOCK_CONTROL:
                setUpControlType(Light.FAST_CLOCK_CONTROL);
                fastHourSpinner1.setValue(lc.getFastClockOnHour());
                fastMinuteSpinner1.setValue(lc.getFastClockOnMin());
                fastHourSpinner2.setValue(lc.getFastClockOffHour());
                fastMinuteSpinner2.setValue(lc.getFastClockOffMin());
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                setUpControlType(Light.TURNOUT_STATUS_CONTROL);
                turnoutBox.setSelectedItemByName(lc.getControlTurnoutName());
                stateBox.setSelectedIndex( (lc.getControlTurnoutState() == Turnout.THROWN)? turnoutThrownIndex : turnoutClosedIndex);
                break;
            case Light.TIMED_ON_CONTROL:
                setUpControlType(Light.TIMED_ON_CONTROL);
                sensorOnBox.setSelectedItemByName(lc.getControlTimedOnSensorName());
                timedOnSpinner.setValue(lc.getTimedOnDuration());
                break;
            case Light.TWO_SENSOR_CONTROL:
                setUpControlType(Light.TWO_SENSOR_CONTROL);
                sensor1Box.setSelectedItemByName(lc.getControlSensorName());
                sensor2Box.setSelectedItemByName(lc.getControlSensor2Name());
                stateBox.setSelectedIndex( (lc.getControlSensorSense() == Sensor.ACTIVE)? sensorActiveIndex : sensorInactiveIndex);
                break;
            case Light.NO_CONTROL:
                setUpControlType(Light.NO_CONTROL);
                break;
            default:
                log.error("Unhandled light control type: {}", ctType);
                break;
        }

    }
    
    private final static Logger log = LoggerFactory.getLogger(AddEditSingleLightControlFrame.class);
    
}
