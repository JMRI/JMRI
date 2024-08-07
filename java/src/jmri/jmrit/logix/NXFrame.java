package jmri.jmrit.logix;

import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.JmriException;
import jmri.SpeedStepMode;
import jmri.jmrit.logix.ThrottleSetting.Command;
import jmri.jmrit.logix.ThrottleSetting.ValueType;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for defining and launching an entry/exit warrant. An NX warrant is a
 * warrant that can be defined on the run without a pre-recorded learn mode
 * session using a set script for ramping startup and stop throttle settings.
 * <p>
 * The route can be defined in a form or by mouse clicking on the OBlock
 * IndicatorTrack icons.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2015
 */
public class NXFrame extends WarrantRoute {

    private float _maxThrottle = 0.0f;
    private float _startDist;   // mm start distance to portal
    private float _stopDist;    // mm stop distance from portal

    private final JTextField _maxThrottleBox = new JTextField(6);
    private final JTextField _maxSpeedBox = new JTextField(6);
    private final JLabel _maxSpeedBoxLabel = new JLabel(Bundle.getMessage("scaleSpeed"));

    private final JTextField _originDist = new JTextField(6);
    private DisplayButton _originUnits;
    private final JTextField _destDist = new JTextField(6);
    private DisplayButton _destUnits;
    private final JSpinner _timeIncre = new JSpinner(new SpinnerNumberModel(750, 200, 9000, 1));
    private final JTextField _rampIncre = new JTextField(6);
    private final JRadioButton _forward = new JRadioButton();
    private final JRadioButton _reverse = new JRadioButton();
    private final JCheckBox _noRamp = new JCheckBox();
    private final JCheckBox _noSound = new JCheckBox();
    private final JCheckBox _stageEStop = new JCheckBox();
    private final JCheckBox _shareRouteBox = new JCheckBox();
    private final JCheckBox _haltStartBox = new JCheckBox();
    private final JCheckBox _addTracker = new JCheckBox();
    private final JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    private final JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));

    private JPanel _routePanel = new JPanel();
    private JPanel _autoRunPanel;
    private final JPanel __trainHolder = new JPanel();
    private JPanel _switchPanel;
    private JPanel _trainPanel;

    protected NXFrame() {
        super();
        init();
    }

    private void init() {
        log.debug("newInstance");
        makeMenus();

        _routePanel = new JPanel();
        _routePanel.setLayout(new BoxLayout(_routePanel, BoxLayout.PAGE_AXIS));
        _routePanel.add(Box.createVerticalGlue());
        _routePanel.add(makeBlockPanels(true));

        _forward.setSelected(true);
        _speedUtil.setIsForward(true);
        _stageEStop.setSelected(false);
        _haltStartBox.setSelected(false);
        _runAuto.setSelected(true);

        _autoRunPanel = makeAutoRunPanel();
        _switchPanel = makeSwitchPanel();
        _maxSpeedBox.setEnabled(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(_routePanel);
        getContentPane().add(mainPanel);

        if (_maxThrottle <= 0.1f) {
            _maxThrottle = WarrantPreferences.getDefault().getThrottleScale()*100;
        }
        _maxThrottleBox.setText(NumberFormat.getNumberInstance().format(_maxThrottle));
        maxThrottleEventAction();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                WarrantTableAction.getDefault().closeNXFrame();
            }
        });
        setAlwaysOnTop(true);
        setVisible(true);
        pack();
    }

    protected boolean isRouteSeaching() {
        return _routePanel.isVisible();
    }

    private void setPanel() {
        __trainHolder.add(_trainPanel);
    }
    private void setPanel(JPanel p) {
        JPanel con = (JPanel)getContentPane().getComponent(0);
        con.removeAll();
        con.add(p);
        con.add(_switchPanel);
        pack();
    }

    private JPanel makeSwitchPanel() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(_runAuto);
        bg.add(_runManual);
        _runAuto.addActionListener((ActionEvent event) -> {
            setPanel();
            setPanel(_autoRunPanel);
        });
        _runManual.addActionListener((ActionEvent event) -> {
            setPanel(_trainPanel);
            _stageEStop.setSelected(false);
            _shareRouteBox.setSelected(false);
            _haltStartBox.setSelected(false);
            _addTracker.setSelected(false);
        });
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalGlue());
        pp.add(_runAuto);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runManual);
        pp.add(Box.createHorizontalGlue());

        JPanel p = new JPanel();
        p.add(Box.createGlue());
        JButton button = new JButton(Bundle.getMessage("ButtonRoute"));
        button.addActionListener((ActionEvent e) -> {
            clearTempWarrant();
            JPanel con = (JPanel)getContentPane().getComponent(0);
            con.removeAll();
            con.add(_routePanel);
            pack();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener((ActionEvent e) -> {
            clearTempWarrant();
            makeAndRunWarrant();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener((ActionEvent e) -> WarrantTableAction.getDefault().closeNXFrame());
        p.add(button);
        p.add(Box.createGlue());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(pp);
        panel.add(p);
        return panel;
    }

    @Override
    protected void maxThrottleEventAction() {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        float num;
        try {
            num =  formatter.parse(_maxThrottleBox.getText()).floatValue();
            num = Math.min(100.0f, Math.max(num,  0.f));
            _maxThrottleBox.setText(formatter.format(num));
        } catch (java.text.ParseException pe) {
            _maxThrottleBox.setText(null);
            _maxSpeedBox.setText(null);
            return;
        }
        float speed = _speedUtil.getTrackSpeed(num/100);    // returns mm/ms (meters/sec)
        switch(_displayPref) {
            case MPH:
                // Convert meters/sec to scale miles/hr
                _maxSpeedBox.setText(formatter.format(speed * _scale * 2.2369363f));
                break;
            case KPH:
                // Convert meters/sec to scale kilometers/hr
                _maxSpeedBox.setText(formatter.format(speed * _scale * 3.6f));
                break;
            case MMPS:
                // Convert meters/sec to millimeters/sec
                _maxSpeedBox.setText(formatter.format(speed * 1000));  // mm/sec
                break;
            case INPS:
            default:
                // Convert meters/sec to inchec/sec
                _maxSpeedBox.setText(formatter.format(speed * 39.37f));  // in/sec
        }
    }

    private void unitsEventAction(@Nonnull JButton button, JTextField field) {
        try {
            getDistance(_originDist, _orders.get(0));
            getDistance(_destDist, _orders.get(_orders.size()-1));
        } catch (JmriException je) {
            JmriJOptionPane.showMessageDialog(this, je.getMessage(),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            return;
        }
        if (button.getText().equals(Display.IN.toString())) {
            _units = Display.CM;
        } else {
            _units = Display.IN;
        }
        setFieldText(_originUnits, _originDist);
        setFieldText(_destUnits, _destDist);
    }
    // convert to units change
    private void setFieldText(@Nonnull JButton button, @Nonnull JTextField field) {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(2);
        float num = 0;
        try {
            num =  formatter.parse(field.getText()).floatValue();
        } catch (java.text.ParseException pe) {
            // errors reported later
        }
        if (_units.equals(Display.IN)) {
            num *= 0.393701f;
        } else {
            num *= 2.54f;
        }
        button.setText(_units.toString());
        field.setText(formatter.format(num));
    }

    private JPanel makeAutoRunPanel() {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));

        DisplayButton speedUnitsButton = new DisplayButton(_displayPref);
        _originUnits = new DisplayButton(_units);
        _destUnits = new DisplayButton(_units);

        _maxThrottleBox.addActionListener((ActionEvent evt)-> maxThrottleEventAction());

        _maxSpeedBox.addActionListener((ActionEvent evt)-> {
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMaximumFractionDigits(2);
            float num;
            try {
                num =  formatter.parse(_maxSpeedBox.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                _maxSpeedBox.setText("");
                return;
            }
            if (num < 0) {
                _maxSpeedBox.setText(formatter.format(0));
                _maxThrottleBox.setText(formatter.format(0));
                return;
            }
            // maxSpeed is speed at full throttle in mm/sec
            float maxSpeed = _speedUtil.getTrackSpeed(1);   // mm/ms, i.e. m/s
            // maximum number is maxSpeed when converted to selected units
            float maxNum;
            // convert to display units. Note real world speed is converted to scaled world speed
            // display label changes "Scale speed" to "Track Speed" accordingly
            switch (_displayPref) {
                case MPH:
                    maxNum = maxSpeed * 2.2369363f *_scale; // convert meters/sec to miles/hr
                    break;
                case KPH:
                    maxNum = maxSpeed * 3.6f * _scale;  // convert meters/sec to to kilometers/hr
                    break;
                case MMPS:
                    maxNum = maxSpeed * 1000;   // convert meters/sec to milimeters/sec
                    break;
                default:
                    maxNum = maxSpeed * 39.37f; // convert meters/sec to inches/sec
                    break;
            }
            if (num > maxNum) {
                String name = _speedUtil.getRosterId();
                if (name == null || name.charAt(0) == '$') {
                    name = getTrainName();
                    if (name == null || name.isEmpty()) {
                        name = Bundle.getMessage("Unknown");
                    }
                }
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("maxSpeedLimit",
                        name, formatter.format(maxNum), speedUnitsButton.getText()),
                        Bundle.getMessage("MessageTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                _maxSpeedBox.setText(formatter.format(maxNum));
                _maxThrottleBox.setText(formatter.format(100));
                return;
            }
            // convert to display num in selected units to track speed in meters/sec (mm/ms)
            // reciprocal of above
            switch (_displayPref) {
                case MPH:
                    num = num * 0.44704f / _scale;  // convert scale miles/hr to mm/msec
                    break;
                case KPH:
                    num = num * 0.277778f / _scale;  // convert scale kilometers/hr to mm/msec
                    break;
                case MMPS:
                    num /= 1000;  // convert mm/sec to mm/msec
                    break;
                default:
                    num /= 39.37f;  // convert inches/sec to mm/msec
                    break;
            }
            // get throttla setting and display as percent full throttle.
            float throttle = _speedUtil.getThrottleSettingForSpeed(num)*100;
            _maxThrottleBox.setText(formatter.format(throttle));
        });

        // User makes a choice for their desired units (_displayPref) to show max speed
        speedUnitsButton.addActionListener((ActionEvent evt)-> {
            NumberFormat formatter = NumberFormat.getNumberInstance();
            float num;
            try {
                num =  formatter.parse(_maxSpeedBox.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                _maxSpeedBox.setText(null);
                return;
            }
            // display preference for units cycles through 4 choices
            // convert old choice to new
            switch (_displayPref) {
                case MPH:
                    _displayPref = Display.KPH;
                    _maxSpeedBox.setText(formatter.format(num * 1.60934f)); // miles/hr to km/hr
                    break;
                case KPH:
                    _displayPref = Display.MMPS;
                    _maxSpeedBox.setText(formatter.format(num * 0277.778f / _scale));   // scale km/hr to mm/sec
                    _maxSpeedBoxLabel.setText(Bundle.getMessage("trackSpeed"));
                    break;
                case MMPS:
                    _displayPref = Display.INPS;
                    _maxSpeedBox.setText(formatter.format(num * 0.03937f)); // mm/sec to in/sec
                    break;
                default:
                    _displayPref = Display.MPH;
                    _maxSpeedBox.setText(formatter.format(num * 0.056818f * _scale)); // inches/sec to scale miles/hr
                    _maxSpeedBoxLabel.setText(Bundle.getMessage("scaleSpeed"));
                    break;
                }
                // display label changes "Scale speed" to "Track Speed" accordingly
                speedUnitsButton.setDisplayPref(_displayPref);
            });

        p1.add(makeTextAndButtonPanel(_maxThrottleBox, new JLabel(Bundle.getMessage("percent")),
                new JLabel(Bundle.getMessage("MaxSpeed")), "ToolTipPercentThrottle"));
        p1.add(makeTextAndButtonPanel(_maxSpeedBox, speedUnitsButton,
                _maxSpeedBoxLabel, "ToolTipScaleSpeed"));

        _originUnits.addActionListener((ActionEvent evt)->
            unitsEventAction(_originUnits, _originDist));
        _destUnits.addActionListener((ActionEvent evt)->
            unitsEventAction(_destUnits, _destDist));

        p1.add(makeTextAndButtonPanel(_originDist, _originUnits,
                new JLabel(Bundle.getMessage("startDistance")), "ToolTipStartDistance"));
        p1.add(makeTextAndButtonPanel(_destDist, _destUnits,
                new JLabel(Bundle.getMessage("stopDistance")), "ToolTipStopDistance"));
        p1.add(WarrantPreferencesPanel.timeIncrementPanel(false, _timeIncre));
        p1.add(WarrantPreferencesPanel.throttleIncrementPanel(false, _rampIncre));
        _rampIncre.addActionListener((ActionEvent e)->{
                String text = _rampIncre.getText();
                boolean showdialog;
                try {
                    float incr = NumberFormat.getNumberInstance().parse(text).floatValue();
                    showdialog = (incr < 0.5f || incr > 25f);
                } catch (java.text.ParseException pe) {
                    showdialog = true;
                }
                if (showdialog) {
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("rampIncrWarning", text),
                            Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                }
            });
        ButtonGroup bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalGlue());
        pp.add(makeTextBoxPanel(false, _forward, "forward", null));
        pp.add(makeTextBoxPanel(false, _reverse, "reverse", null));
        pp.add(Box.createHorizontalGlue());
        p1.add(pp);

        __trainHolder.setLayout(new BoxLayout(__trainHolder, BoxLayout.PAGE_AXIS));
        _trainPanel = makeTrainIdPanel(null);
        __trainHolder.add(_trainPanel);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.PAGE_AXIS));
        p2.add(__trainHolder);
        p2.add(makeTextBoxPanel(_noRamp, "NoRamping", "ToolTipNoRamping"));
        p2.add(makeTextBoxPanel(_noSound, "NoSound", "ToolTipNoSound"));
        p2.add(makeTextBoxPanel(_stageEStop, "StageEStop", null));
        p2.add(makeTextBoxPanel(_haltStartBox, "HaltAtStart", null));
        p2.add(makeTextBoxPanel(_shareRouteBox, "ShareRoute", "ToolTipShareRoute"));
        p2.add(makeTextBoxPanel(_addTracker, "AddTracker", "ToolTipAddTracker"));


        JPanel autoRunPanel = new JPanel();
        autoRunPanel.setLayout(new BoxLayout(autoRunPanel, BoxLayout.PAGE_AXIS));
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.LINE_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(p1);
        ppp.add(Box.createHorizontalGlue());
        ppp.add(p2);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(ppp);

        _forward.addActionListener((ActionEvent evt)-> maxThrottleEventAction());
        _reverse.addActionListener((ActionEvent evt)-> maxThrottleEventAction());

        return autoRunPanel;
    }

    private void updateAutoRunPanel() {
        _startDist = getPathLength(_orders.get(0)) * 0.4f;
        _stopDist = getPathLength(_orders.get(_orders.size()-1)) * 0.6f;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        if (_units.equals(Display.IN)) {
            // convert millimeters to inches
            _originDist.setText(formatter.format(_startDist * 0.0393701));
            _destDist.setText(formatter.format(_stopDist * 0.0393701));
        } else {
         // convert millimeters to centimeters
            _originDist.setText(formatter.format(_startDist / 10));
            _destDist.setText(formatter.format(_stopDist / 10));
        }
         _autoRunPanel.repaint();
    }

    private void makeMenus() {
        setTitle(Bundle.getMessage("AutoWarrant"));
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.NXWarrant", true);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
        log.trace("propertyChange \"{}\" old= {} new= {} source= {}",property,
            e.getOldValue(), e.getNewValue(), e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
            doAction(e.getSource());
        }
    }

    /**
     * Called by {@link jmri.jmrit.logix.RouteFinder#run()}. If all goes well,
     * WarrantTableFrame.runTrain(warrant) will run the warrant
     *
     * @param orders list of block orders
     */
    @Override
    protected void selectedRoute(ArrayList<BlockOrder> orders) {
        JPanel con = (JPanel)getContentPane().getComponent(0);
        con.removeAll();
        if (_runAuto.isSelected()) {
            con.add(_autoRunPanel);
        } else {
            con.add(_trainPanel);
        }
        con.add(_switchPanel);
        updateAutoRunPanel();
        pack();
    }

    private void makeAndRunWarrant() {
        String msg = getBoxData();
        if (msg == null) {
            msg = checkLocoAddress();
        }
        if (msg != null) {
            JmriJOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            return;
        }
        // There is a dccAddress so a throttle can be acquired
        String s = ("" + Math.random()).substring(2);
        Warrant warrant = new Warrant("IW" + s, "NX(" + getAddress() + ")");
        warrant.setBlockOrders(_orders);
        warrant.setTrainName(getTrainName());
        warrant.setNoRamp(_noRamp.isSelected());
        _speedUtil.setIsForward(_forward.isSelected());
        warrant.setSpeedUtil(_speedUtil);   // transfer SpeedUtil to warrant
        log.debug("Warrant {}. Route and loco set.", warrant.getDisplayName());
        int mode;
        if (!_runManual.isSelected()) {
            mode = Warrant.MODE_RUN;
            warrant.setShareRoute(_shareRouteBox.isSelected());
            warrant.setAddTracker(_addTracker.isSelected());
            warrant.setHaltStart(_haltStartBox.isSelected());
            msg = makeCommands(warrant);
        } else {
            mode = Warrant.MODE_MANUAL;
        }
        if (msg == null) {
            WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
            tableFrame.setVisible(true);
            warrant.setNXWarrant(true);
            tableFrame.getModel().addNXWarrant(warrant);   //need to catch propertyChange at start
            log.debug("NXWarrant added to table");
            msg = tableFrame.runTrain(warrant, mode);
            if (msg != null) {
                log.debug("WarrantTableFrame run warrant. msg= {} Remove warrant {}",msg,warrant.getDisplayName());
                tableFrame.getModel().removeWarrant(warrant, false);
            }
        }
        if (msg != null) {
            JmriJOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
        } else {
            WarrantTableAction.getDefault().closeNXFrame();
        }
    }

    // for testing
    protected void setMaxSpeed(float s) {
        _maxThrottle = s;
        _maxThrottleBox.setText(NumberFormat.getNumberInstance().format(s));
    }

    private String getBoxData() {
        String text = null;
        float maxSpeed;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        try {
            text = _maxThrottleBox.getText();
            maxSpeed = formatter.parse(text).floatValue();
        } catch (java.text.ParseException pe) {
            if (text==null) {
                text = "\"\"";
            }
            return Bundle.getMessage("badSpeed100", text);
        }

        try {
            _startDist = getDistance(_originDist, _orders.get(0));
            if (_startDist < 2) {
                _startDist = 2; // leave block by at least 2 millimeters - cannot be 0
            }
        } catch (JmriException je) {
            return je.getMessage();
        }

        try {
            _stopDist = getDistance(_destDist, _orders.get(_orders.size()-1));
            if (_stopDist < 2) {
                _stopDist = 2; // enter block by at least 2 millimeters - cannot be 0
            }
        } catch (JmriException je) {
            return je.getMessage();
        }

        if (maxSpeed > 100f || maxSpeed < 0.001f) {
            return Bundle.getMessage("badSpeed100", maxSpeed);
        }
        _maxThrottle = maxSpeed / 100;

        String msg = setAddress();
        if (msg != null) {
            return msg;
        }

        int time = (Integer)_timeIncre.getValue();
        _speedUtil.setRampTimeIncrement(time);

        try {
            text = _rampIncre.getText();
            float incre = NumberFormat.getNumberInstance().parse(text).floatValue();
            if (incre < 0.5f || incre > 25f) {
                return Bundle.getMessage("rampIncrWarning", text);
            } else {
                _speedUtil.setRampThrottleIncrement(incre/100);
            }
        } catch (java.text.ParseException pe) {
            return Bundle.getMessage("MustBeFloat", text);
        }
        return null;
    }

    private float getDistance(@Nonnull JTextField field, @Nonnull BlockOrder bo) throws JmriException {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        float distance;
        String text = field.getText();
        try {
            distance = formatter.parse(text).floatValue();
        } catch (java.text.ParseException pe) {
            throw new JmriException(Bundle.getMessage("MustBeFloat", text));
        }
        float pathLen = getPathLength(bo);
        if (pathLen <= 0) {
            throw new JmriException(Bundle.getMessage("zeroPathLength", 
                bo.getPathName(), bo.getBlock().getDisplayName()));
        }
        if (_units.equals(Display.IN)){
            distance *= 25.4f;  // convert inches to millimeters
            if (distance > pathLen) {
                field.setText(formatter.format(pathLen*0.03937008f));
                throw new JmriException(Bundle.getMessage(
                        "BadLengthIn", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen*0.03937008f, text));
            } else if (distance < 0) {
                field.setText("0");
                throw new JmriException(Bundle.getMessage(
                        "BadLengthIn", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen*0.03937008f, text));
            }
        } else {
            distance *= 10f;  // convert centimeters to millimeters
            if (distance > pathLen) {
                field.setText(formatter.format(pathLen*0.1f));
                throw new JmriException(Bundle.getMessage(
                        "BadLengthCm", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen*0.1f, text));
            } else if (distance < 0) {
                field.setText("0");
                throw new JmriException(Bundle.getMessage(
                        "BadLengthCm", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen*0.1f, text));
            }
        }
        return distance;
    }

    private float getPathLength(@Nonnull BlockOrder bo) {
        float len = bo.getPathLength();
        if (len <= 0) {
            len = bo.getPathLength();
            if ( len <= 0) {
                String sLen = JmriJOptionPane.showInputDialog(this,
                        Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName())
                        + Bundle.getMessage("getPathLength", bo.getPathName(), bo.getBlock().getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                try {
                    len = NumberFormat.getNumberInstance().parse(sLen).floatValue();
                } catch (java.text.ParseException | java.lang.NullPointerException pe) {
                    len = 0.0f;
                }
                bo.setPathLength(len);
            }
        }
       return len;
    }

    /**
     * Return length of warrant route in mm.
     * @throws JmriException when a Path Length is &lt; or equals 0
     */
    private float getTotalLength() throws JmriException {
        List<BlockOrder> orders = getOrders();
        float totalLen = _startDist;
        for (int i = 1; i < orders.size() - 1; i++) {
            BlockOrder bo = orders.get(i);
            float pathLen = getPathLength(bo);
            if (pathLen <= 0) {
                throw new JmriException(Bundle.getMessage("zeroPathLength",
                    bo.getPathName(), bo.getBlock().getDisplayName()));
            }
            totalLen += pathLen;
        }
        totalLen += _stopDist;
        return totalLen;
    }

    @CheckForNull
    private String makeCommands(@Nonnull Warrant w) {

        int nextIdx = 0;        // block index - increment after getting a block order
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(nextIdx++);
        String blockName = bo.getBlock().getDisplayName();

        int cmdNum;
        w.addThrottleCommand(new ThrottleSetting(0, Command.FKEY, 0,
            ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "", blockName));
        if (_forward.isSelected()) {
            w.addThrottleCommand(new ThrottleSetting(100, Command.FORWARD, -1,
                ValueType.VAL_TRUE, SpeedStepMode.UNKNOWN, 0, "",  blockName));
            if (!_noSound.isSelected()) {
                w.addThrottleCommand(new ThrottleSetting(1000, Command.FKEY, 2,
                    ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "", blockName));
                w.addThrottleCommand(new ThrottleSetting(2500, Command.FKEY, 2,
                    ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
                w.addThrottleCommand(new ThrottleSetting(1000, Command.FKEY, 2,
                    ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "", blockName));
                w.addThrottleCommand(new ThrottleSetting(2500, Command.FKEY, 2,
                    ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
                cmdNum = 7;
            } else {
                cmdNum = 3;
            }
        } else {
            w.addThrottleCommand(new ThrottleSetting(100, Command.FORWARD, -1,
                ValueType.VAL_FALSE, SpeedStepMode.UNKNOWN, 0, "", blockName));
            if (!_noSound.isSelected()) {
                w.addThrottleCommand(new ThrottleSetting(1000, Command.FKEY, 3,
                    ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "",  blockName));
                w.addThrottleCommand(new ThrottleSetting(500, Command.FKEY, 3,
                    ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
                w.addThrottleCommand(new ThrottleSetting(500, Command.FKEY, 3,
                    ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "", blockName));
                w.addThrottleCommand(new ThrottleSetting(500, Command.FKEY, 3,
                    ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
                cmdNum = 6;
            } else {
                cmdNum = 2;
            }
        }

        float totalLen;
        try {
            totalLen = getTotalLength();
        } catch (JmriException je) {
            return je.getMessage();
        }

        RampData upRamp = _speedUtil.getRampForSpeedChange(0f, _maxThrottle);
        RampData downRamp = _speedUtil.getRampForSpeedChange(_maxThrottle, 0f);
        float upRampLength = upRamp.getRampLength();
        float dnRampLength = downRamp.getRampLength();
        int timeInterval = downRamp.getRampTimeIncrement();
        float intervalDist = totalLen - (upRampLength + dnRampLength);
        while (intervalDist < 0) {
            log.debug("Route length= {}, upRampLength= {}, dnRampLength= {}, intervalDist= {}, _maxThrottle= {}",
                    totalLen, upRampLength, dnRampLength, intervalDist, _maxThrottle);
            ListIterator<Float> downIter = downRamp.speedIterator(false);
            float prevSetting = downIter.previous();   // top value is _maxThrottle
            if (downIter.hasPrevious()) { // if none, empty ramp
                prevSetting = downIter.previous();
                _maxThrottle = prevSetting;    // last throttle increment
            } else {
                _maxThrottle = _speedUtil.getThrottleSettingForSpeed(totalLen/(timeInterval*2));
            }
            upRamp = _speedUtil.getRampForSpeedChange(0f, _maxThrottle);
            downRamp = _speedUtil.getRampForSpeedChange(_maxThrottle, 0f);
            upRampLength = upRamp.getRampLength();
            dnRampLength = downRamp.getRampLength();
            intervalDist = totalLen - (upRampLength + dnRampLength);
        }
        if (upRampLength < 1) {
            upRamp = _speedUtil.getRampForSpeedChange(0f, _speedUtil.getRampThrottleIncrement());
        }
        if (dnRampLength < 1) {
            downRamp = _speedUtil.getRampForSpeedChange(0f, _speedUtil.getRampThrottleIncrement());
        }
        log.debug("Route length= {}, upRampLength= {}, dnRampLength= {}, intervalDist= {}, _maxThrottle= {}",
                totalLen, upRampLength, dnRampLength, intervalDist, _maxThrottle);

        float blockLen = _startDist;    // length of path in current block
        float sumBlkLen = 0;    // sum of path lengths at NOOP

        // start train
        int speedTime = 0;      // ms time to complete speed step from last block
        int noopTime = 0;       // ms time for entry into next block
        ListIterator<Float> iter = upRamp.speedIterator(true);
        float curThrottle = 0;  // throttle setting
        float prevThrottle = 0;
        float curDistance = 0;  // current distance traveled up to issuing next command
        float blkDistance = 0;  // distance traveled in current block up to issuing next command
        float dist = 0f;    // increment to accumulate curDistance and blkDistance

        log.debug("Start in block \"{}\" startDist= {} stopDist= {}", blockName, _startDist, _stopDist);

        while (iter.hasNext()) {       // ramp up loop

            while (iter.hasNext()) {
                // interval distance up to speed change
                dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, speedTime);
                if (blkDistance + dist >= blockLen) {
                    break;  // cannot finish upRamp within this block
                }
                blkDistance += dist;
                curDistance += dist;
                float nextThrottle = iter.next();
                w.addThrottleCommand(new ThrottleSetting(speedTime, Command.SPEED, -1, ValueType.VAL_FLOAT,
                        SpeedStepMode.UNKNOWN, nextThrottle, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                if (log.isDebugEnabled()) {
                    log.debug("cmd#{}. UpRamp block \"{}\" set speed {} after {}ms " +
                        "dist= {} from {} to {}, blkDist= {} curDist= {}",
                        cmdNum++, blockName, nextThrottle, speedTime,
                        dist, prevThrottle, curThrottle, blkDistance, curDistance);
                }
                prevThrottle = curThrottle;
                curThrottle = nextThrottle;
                speedTime = timeInterval;
            }   // end of upRamp within a block

            if (blkDistance >= blockLen) {
                // Possible case where initial blkDistance can exceed the length of a block that was just entered.
                // Skip over block and move to next block and adjust the distance times into that block
                noopTime = _speedUtil.getTimeForDistance(curThrottle, blockLen);   // noop distance to run through block
                speedTime = _speedUtil.getTimeForDistance(curThrottle, blkDistance - blockLen);
            } else {
                // typical case where next speed change broke out of above loop. (blkDistance + dist >= blockLen)
                noopTime = _speedUtil.getTimeForDistance(curThrottle, (blockLen - blkDistance));   // time to next block
                speedTime = timeInterval - noopTime; // time to next speed change
            }

            if (log.isDebugEnabled()) {
                log.debug("Leave block \"{}\"  curThrottle= {}, blockLen= {} blkDist= {}, " +
                    "noopTime= {} 'speedTime'= {}, curDist= {}",
                    blockName, curThrottle, blockLen, blkDistance, noopTime, speedTime, curDistance);
            }
            if (!iter.hasNext()) {
                break;
            }

            if (nextIdx < orders.size()) {
                if (noopTime > timeInterval) {
                    speedTime = 0;
                } else {
                    speedTime = timeInterval - noopTime;
                }
                dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, noopTime);
                blkDistance += dist;
                curDistance += dist;
                sumBlkLen += blockLen;
                bo = orders.get(nextIdx++);
                blockLen = getPathLength(bo);
                if (blockLen <= 0) {
                    return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                 }
                blockName = bo.getBlock().getDisplayName();
                w.addThrottleCommand(new ThrottleSetting(noopTime, Command.NOOP, -1, ValueType.VAL_NOOP,
                        SpeedStepMode.UNKNOWN, 0, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                if (log.isDebugEnabled()) {
                    log.debug("cmd#{}. Enter RampUp block \"{}\" noopTime= {}"
                        + ", dist= {} blockLen= {}, blkDist= {}, sumBlkLen= {}, curDist= {}",
                        cmdNum++, blockName, noopTime, dist, blockLen, blkDistance, sumBlkLen, curDistance);
                }
                blkDistance = 0;
                curDistance = sumBlkLen;
           }
        }   // end of upRamp loop

        if (blkDistance < 0.01) {   // no increase of speed in this block
            dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, speedTime);
            log.debug("No speed increase in block \"{}\" speedTime= {} dist= {}"
                + ", blkDist= {}, curDist= {} upRampLength={}",
                blockName, speedTime, dist, blkDistance, curDistance, upRampLength);
            blkDistance += dist;
            curDistance += dist;    // curDistance ought to equal upRampLength
        }
        log.debug("Ramp Up done in block \"{}\" speedTime= {} dist= {}"
            + ", blkDist= {}, curDist= {} upRampLength= {} diff= {}",
            blockName, speedTime, dist, blkDistance, curDistance, upRampLength, upRampLength - curDistance);
        blkDistance += (upRampLength - curDistance);  // adjustment for getDistanceOfSpeedChange calculation variances
        curDistance = upRampLength;
        if (log.isDebugEnabled()) {
            log.debug("Ramp Up done in block \"{}\" timeInterval= {} dist= {}"
                + ", blkDist= {}, curDist= {} upRampLength= {}",
                blockName, noopTime, dist, blkDistance, curDistance, upRampLength);
        }
        prevThrottle = curThrottle; // travel at curThrottle (max speed) for a period of time

        if ( log.isDebugEnabled() && Math.abs(curThrottle - _maxThrottle) > 0.001) {
            log.error("curThrottle = {} _maxThrottle = {} prevThrottle= {}", curThrottle, _maxThrottle, prevThrottle);
        }

        // (sumBlkLen + blockLen) is total distance traveled to end of current block
        if (totalLen - sumBlkLen - blockLen > dnRampLength) {
            if (!iter.hasNext()) {  // upRamp done. At maxThrottle for remainder of block
                if (nextIdx < orders.size()) {    // not the last block
                    dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, noopTime);
                    blkDistance += dist;
                    curDistance += dist;
                    sumBlkLen += blockLen;
                    bo = orders.get(nextIdx++);
                    blockLen = getPathLength(bo);
                    if (blockLen <= 0) {
                        return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                     }
                    blockName = bo.getBlock().getDisplayName();
                    w.addThrottleCommand(new ThrottleSetting(noopTime, Command.NOOP, -1, ValueType.VAL_NOOP,
                            SpeedStepMode.UNKNOWN, 0, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                    if (log.isDebugEnabled()) {
                        log.debug("cmd#{}. Enter RampUp block \"{}\" noopTime= {}"
                            + ", dist= {} blockLen= {}, blkDist= {}, sumBlkLen= {}, curDist= {}",
                            cmdNum++, blockName, noopTime,
                        dist, blockLen, blkDistance, sumBlkLen, curDistance);
                    }
                    curDistance = sumBlkLen;
                    blkDistance = 0;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Ramp Up done at last block \"{}\" curThrottle={}, blkDist={}, curDist={}",
                                blockName, curThrottle, blkDistance, curDistance);
                    }
                } // left block where up ramp finished

                // run through mid route at max speed
                while (nextIdx < orders.size() && totalLen - sumBlkLen - blockLen > dnRampLength) {
                    // constant speed, get time to next block
                    noopTime = _speedUtil.getTimeForDistance(curThrottle, blockLen);   // time to next block
                    sumBlkLen += blockLen;
                    curDistance += blockLen;
                    if (log.isDebugEnabled()) {
                        log.debug("Leave MidRoute block \"{}\" noopTime= {} blockLen= {}, sumBlkLen= {}, curDist={}",
                                blockName, noopTime, blockLen, sumBlkLen, curDistance);
                    }
                    bo = orders.get(nextIdx++);
                    blockLen = getPathLength(bo);
                    if (blockLen <= 0) {
                        return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                     }
                    blockName = bo.getBlock().getDisplayName();
                    if (nextIdx == orders.size()) {
                        blockLen = _stopDist;
                    }
                    w.addThrottleCommand(new ThrottleSetting(noopTime, Command.NOOP, -1, ValueType.VAL_NOOP,
                            SpeedStepMode.UNKNOWN, 0, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                    if (log.isDebugEnabled()) {
                        log.debug("cmd#{}. Enter MidRoute block \"{}\" noopTime= {},"
                            + "blockLen= {}, sumBlkLen= {}, curDist={}",
                            cmdNum++, blockName, noopTime, blockLen, sumBlkLen, curDistance);
                    }
                }
            }
            blkDistance = 0;
            dist = totalLen - sumBlkLen - dnRampLength;
        } else {    // up ramp and down ramp in the same block
            dist = totalLen - dnRampLength - upRampLength;
        }

        speedTime =_speedUtil.getTimeForDistance(curThrottle, dist);
        blkDistance += dist;
        curDistance += dist;

        float diff = totalLen - dnRampLength - curDistance;
        if (log.isDebugEnabled()) {
            log.debug("Begin Ramp Down in block \"{}\" speedTime= {} dist= {}"
                + ", blkDist= {}, curDist= {} dnRampLength= {} diff= {}",
                blockName, speedTime, dist, blkDistance, curDistance, dnRampLength, diff);
        }
        blkDistance += diff;  // adjustment for getDistanceOfSpeedChange calculation variances
        curDistance = totalLen - dnRampLength;

        // Ramp down.
        if (log.isDebugEnabled()) {
            log.debug("Begin Ramp Down at block \"{}\" blockLen={},"
                + " at blkDistance= {} curDist= {} sumBlkLen= {} curThrottle= {}",
                blockName, blockLen, blkDistance, curDistance, sumBlkLen, curThrottle);
        }

        iter = downRamp.speedIterator(false);
        iter.previous();   // discard, equals curThrottle

        float nextThrottle = iter.previous();
        w.addThrottleCommand(new ThrottleSetting(speedTime, Command.SPEED, -1, ValueType.VAL_FLOAT,
                SpeedStepMode.UNKNOWN, nextThrottle, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
        if (log.isDebugEnabled()) {
            log.debug("cmd#{}. DownRamp block \"{}\" set speed {} after {}ms "
                + "dist= {} from {} to {}, blkDist= {} curDist={}",
                cmdNum++, blockName, nextThrottle, speedTime,
                dist, prevThrottle, curThrottle, blkDistance, curDistance);
        }
        prevThrottle = curThrottle;
        curThrottle = nextThrottle;
        speedTime = timeInterval;

        while (iter.hasPrevious()) {
            if ( nextIdx == orders.size() /* at last block */ && _stageEStop.isSelected() ) {
                w.addThrottleCommand(new ThrottleSetting(50, Command.SPEED, -1, ValueType.VAL_FLOAT,
                        SpeedStepMode.UNKNOWN, -0.5f, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                log.debug("cmd#{}. At block \"{}\" EStop set speed= {}", cmdNum++, blockName, -0.5);
                break;
            }

            nextThrottle = curThrottle;
            while (iter.hasPrevious()) {
                dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, speedTime);
                if (blkDistance + dist >= blockLen) {
                    break;
                }
                nextThrottle = iter.previous();

                if (!iter.hasPrevious() && nextIdx != orders.size()) {
                    // nextThrottle is last speed setting. Make sure speed 0 is set in last block
                    log.debug("BEFORE last block! Set speed {} in block \"{}\" after {}ms!"
                        + "dist= {}, blkDist= {} curDist={}, blockLen= {}",
                        nextThrottle, blockName, speedTime, dist, blkDistance, curDistance, blockLen);
                    iter.next();    // Back up.
                    noopTime = speedTime;
                    speedTime = -1;
                    break;
                }
                // interval distance up to speed change
                blkDistance += dist;
                curDistance += dist;
                w.addThrottleCommand(new ThrottleSetting(speedTime, Command.SPEED, -1, ValueType.VAL_FLOAT,
                        SpeedStepMode.UNKNOWN, nextThrottle, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                if (log.isDebugEnabled()) {
                    log.debug("cmd#{}. DownRamp block \"{}\" set speed {} after {}ms"
                        + " dist= {} from {} to {}, blkDist= {} curDist={}",
                        cmdNum++, blockName, nextThrottle, speedTime,
                        dist, prevThrottle, curThrottle, blkDistance, curDistance);
                }
                prevThrottle = curThrottle;
                curThrottle = nextThrottle;
                speedTime = timeInterval;
            }

            if (!iter.hasPrevious()) {
                break;
            }

            if (speedTime < 0) {
                speedTime = 0;
            } else {
                if (blkDistance >= blockLen) {
                    // Possible case where blkDistance can exceed the length of a block that was just entered.
                    // Skip over block and move to next block and adjust the distance times into that block
                    noopTime = _speedUtil.getTimeForDistance(curThrottle, blockLen);   // noop distance to run through block
                    speedTime = _speedUtil.getTimeForDistance(curThrottle, blkDistance - blockLen);
                } else {
                    // typical case where next speed change broke out of above loop. (blkDistance + dist >= blockLen)
                    noopTime = _speedUtil.getTimeForDistance(curThrottle, (blockLen - blkDistance));   // time to next block
                    speedTime = timeInterval - noopTime;   // time to next speed change
                }
           }

            if (log.isDebugEnabled()) {
                log.debug("Leave block \"{}\" curThrottle= {}, blockLen= {}"
                    + " BlkDist= {}, noopTime= {} 'speedTime'= {}, curDist= {}",
                    blockName, curThrottle, blockLen, blkDistance, noopTime, speedTime, curDistance);
            }

            if (nextIdx < orders.size()) {
                if (noopTime > timeInterval) {
                    speedTime = 0;
                } else {
                    speedTime = timeInterval - noopTime;
                }
                dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, noopTime);
                blkDistance += dist;
                curDistance += dist;
                sumBlkLen += blockLen;
                bo = orders.get(nextIdx++);
                if (nextIdx == orders.size()) {
                    blockLen = _stopDist;
                } else {
                    blockLen = getPathLength(bo);
                }
                if (blockLen <= 0) {
                    return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                }
                blockName = bo.getBlock().getDisplayName();
                w.addThrottleCommand(new ThrottleSetting(noopTime, Command.NOOP, -1, ValueType.VAL_NOOP,
                        SpeedStepMode.UNKNOWN, 0, "", blockName, _speedUtil.getTrackSpeed(curThrottle)));
                if (log.isDebugEnabled()) {
                    log.debug("cmd#{}. Enter RampDown block \"{}\" noopTime= {},"
                        + " dist= {} blockLen= {}, blkDist= {}, sumBlkLen= {}, curDist= {}",
                        cmdNum++, blockName, noopTime, dist, blockLen, blkDistance, sumBlkLen, curDistance);
                }
                blkDistance = 0;
                curDistance = sumBlkLen;
                if (nextIdx == orders.size()) {
                    dist = _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, speedTime);
                    if (dist > blockLen) {
                        speedTime = 0;
                    }
                }
            } else if (iter.hasPrevious()) { // Should not happen. Error in distance calculation
                _stageEStop.setSelected(true);
                log.error("cmd#{}. ERROR speed in block \"{}\" set speed {} "
                    + "after {}ms dist= {} from {} to {}, blkDist= {} curDist={}",
                    cmdNum++, blockName, nextThrottle,
                    speedTime, dist, prevThrottle, curThrottle, blkDistance, curDistance);
            }
        }

        // Ramp down finished
        if (log.isDebugEnabled()) {
            sumBlkLen += _stopDist;
            curDistance += _speedUtil.getDistanceOfSpeedChange(prevThrottle, curThrottle, speedTime);
            log.debug("Ramp down done at block \"{}\", blockLen= {}, "
                + "BlkDist= {}, curDist= {}, sumBlkLen= {}, totalLen= {},",
                blockName, blockLen, blkDistance, curDistance, sumBlkLen, totalLen);
        }
        if (!_noSound.isSelected()) {
            w.addThrottleCommand(new ThrottleSetting(500, Command.FKEY, 1,
                ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, Command.FKEY, 2,
                ValueType.VAL_ON, SpeedStepMode.UNKNOWN, 0, "", blockName));
            w.addThrottleCommand(new ThrottleSetting(2000, Command.FKEY, 2,
                ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
        }
        w.addThrottleCommand(new ThrottleSetting(500, Command.FKEY, 0,
            ValueType.VAL_OFF, SpeedStepMode.UNKNOWN, 0, "", blockName));
        return null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NXFrame.class);

}
