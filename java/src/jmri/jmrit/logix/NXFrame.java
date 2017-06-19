package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for defining and launching an entry/exit warrant. An NX warrant is a
 * warrant that can be defined on the run without a pre-recorded learn mode
 * session using a set script for ramping startup and stop throttle settings.
 * <P>
 * The route can be defined in a form or by mouse clicking on the OBlock
 * IndicatorTrack icons.
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2015
 */
public class NXFrame extends WarrantRoute {

    private float _scale = 87.1f;
    private float _intervalTime = 0.0f;     // milliseconds
    private float _throttleIncr = 0.0f;
    private float _maxThrottle = 0.75f;
    private float _startDist;   // mm start distance to portal
    private float _stopDist;    // mm stop distance from portal
    private float _totalLen;    // route length of warrant

    private JTextField _maxSpeedBox = new JTextField(6);
    private JTextField _originDist = new JTextField(6);
    private JButton _originUnits = new JButton();
    private JTextField _destDist = new JTextField(6);
    private JButton _destUnits = new JButton();
    private JRadioButton _forward = new JRadioButton();
    private JRadioButton _reverse = new JRadioButton();
    private JCheckBox _stageEStop = new JCheckBox();
    private JCheckBox _shareRouteBox = new JCheckBox();
    private JCheckBox _haltStartBox = new JCheckBox();
    private JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    private JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));

    static private JPanel _routePanel;
    private JPanel _autoRunPanel;
    private JPanel _switchPanel;
    private JPanel _trainPanel;

    /**
     * Get the default instance of an NXFrame.
     *
     * @return the default instance, creating it if necessary
     * @deprecated since 4.7.4; use {@link #getDefault() } instead
     *
    @Deprecated
    static public NXFrame getInstance() {
        return new NXFrame();
    }*/

    /**
     * Get the default instance of an NXFrame.
     *
     * @return the default instance, creating it if necessary
     */
    static public NXFrame getDefault() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        NXFrame instance = InstanceManager.getOptionalDefault(NXFrame.class).orElseGet(() -> {
            return InstanceManager.setDefault(NXFrame.class, new NXFrame());
        });
        if (!instance.isVisible()) {
            instance.updatePreferences();
            instance.setTrainInfo(null);
            instance.clearRoute();
            JPanel con = (JPanel)instance.getContentPane().getComponent(0);
            con.removeAll();
            con.add(_routePanel);
            instance.pack();
        }
        return instance;
    }

    private NXFrame() {
        super();
        init();
    }
    
    private void updatePreferences() {
        WarrantPreferences preferences = WarrantPreferences.getDefault();
        setScale(preferences.getLayoutScale());
        setDepth(preferences.getSearchDepth());
        setTimeInterval(preferences.getTimeIncrement());
        setThrottleIncrement(preferences.getThrottleIncrement());
    }

    private void init() {
        updatePreferences();
        makeMenus();

        _routePanel = new JPanel();
        _routePanel.setLayout(new BoxLayout(_routePanel, BoxLayout.PAGE_AXIS));
        _routePanel.add(Box.createVerticalGlue());
        _routePanel.add(makeBlockPanels(true));
 
        _forward.setSelected(true);
        _stageEStop.setSelected(false);
        _haltStartBox.setSelected(false);
        _runAuto.setSelected(true);

        _autoRunPanel = makeAutoRunPanel();
        _switchPanel = makeSwitchPanel();
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(_routePanel);
        getContentPane().add(mainPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeFrame();
            }
        });
        setVisible(true);
        pack();
    }

    protected boolean isRouteSeaching() {
        return _routePanel.isVisible();
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
            setPanel(_autoRunPanel);
        });
        _runManual.addActionListener((ActionEvent event) -> {
            setPanel(_trainPanel);
            _stageEStop.setSelected(false);
            _shareRouteBox.setSelected(false);
            _haltStartBox.setSelected(false);
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
            JPanel con = (JPanel)getContentPane().getComponent(0);
            con.removeAll();
            con.add(_routePanel);
            pack();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener((ActionEvent e) -> {
            makeAndRunWarrant();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener((ActionEvent e) -> {
            closeFrame();
        });
        p.add(button);
        p.add(Box.createGlue());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(pp);
        panel.add(p);
        return panel;
    }

    private JPanel makeAutoRunPanel() {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        p1.add(makeTextBoxPanel(false, _maxSpeedBox, "MaxSpeed", null));

        _originUnits.setText("In");
        _originUnits.setSelected(true);
        _destUnits.setText("In");
        _destUnits.setSelected(true);
        int bWidth = new JTextField(2).getPreferredSize().width;
        int bHeight = new JTextField(2).getPreferredSize().height;
        _originUnits.setMaximumSize(new Dimension(bWidth, bHeight));
        _destUnits.setMaximumSize(new Dimension(bWidth, bHeight));
        
        _originUnits.addActionListener((ActionEvent evt)-> {
            float num = 0;
            try {
                num =  Float.parseFloat(_originDist.getText());
            } catch (NumberFormatException nfe) {
                // errors reported later
            }
            if (_originUnits.getText().equals("In")) {
                _originUnits.setText("Cm");
                num = Math.round(num * 254f);
                _originDist.setText(Float.toString(num / 100));
            } else {
                num = Math.round(num * 100f / 2.54f);
                _originUnits.setText("In");
                _originDist.setText(Float.toString(num / 100));
            }
        });
        _destUnits.setActionCommand("In");
        _destUnits.addActionListener((ActionEvent evt)-> {
            float num = 0;
            try {
                num =  Float.parseFloat(_destDist.getText());
            } catch (NumberFormatException nfe) {
                // errors reported later
            }
            if (_destUnits.getText().equals("In")) {
                _destUnits.setText("Cm");
                _destDist.setText(Float.toString(num * 2.54f));
            } else {
                _destUnits.setText("In");
                _destDist.setText(Float.toString(num / 2.54f));
            }
        });

        JPanel p = new JPanel();
        p.add(_originDist);
        p.add(_originUnits);
        _originDist.setToolTipText("startDistance");
        _originUnits.setToolTipText("startDistance");
        p1.add(makeTextBoxPanel(false, p, "startDistance", "ToolTipStartDistance"));
        p = new JPanel();
        p.add(_destDist);
        p.add(_destUnits);
        _destDist.setToolTipText("stopDistance");
        _destUnits.setToolTipText("stopDistance");
        p1.add(makeTextBoxPanel(false, p, "stopDistance", "ToolTipStopDistance"));

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.PAGE_AXIS));
        _trainPanel = makeTrainIdPanel(null);
        p2.add(_trainPanel);

        JPanel autoRunPanel = new JPanel();
        autoRunPanel.setLayout(new BoxLayout(autoRunPanel, BoxLayout.PAGE_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalGlue());
        pp.add(p2);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(pp);

        ButtonGroup bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
        p1.add(Box.createHorizontalGlue());
        p1.add(makeTextBoxPanel(false, _forward, "forward", null));
        p1.add(makeTextBoxPanel(false, _reverse, "reverse", null));
        p1.add(Box.createHorizontalGlue());
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalGlue());
//        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalGlue());
//        pp.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        autoRunPanel.add(pp);

        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.LINE_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _stageEStop, "StageEStop", null));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _haltStartBox, "HaltAtStart", null));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _shareRouteBox, "ShareRoute", "ToolTipShareRoute"));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(ppp);
        return autoRunPanel;
    }
    
    private void updateAutoRunPanel() {
        _startDist = _orders.get(0).getBlock().getLengthMm() / 2;
        _stopDist = _orders.get(_orders.size()-1).getBlock().getLengthMm() / 2;
        if (_originUnits.getText().equals("In")) {
            float num = Math.round(_startDist * 100 / 25.4f);
            _originDist.setText(Float.toString(num / 100f));
        } else {
            float num = Math.round(_startDist * 100);
            _originDist.setText(Float.toString(_startDist / 1000f));
        }
        if (_destUnits.getText().equals("In")) {
            float num = Math.round(_stopDist * 100 / 25.4f);
            _destDist.setText(Float.toString(num / 100f));
        } else {
            float num = Math.round(_stopDist * 100);
            _destDist.setText(Float.toString(num / 1000f));
        }
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
//        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                            " source= "+e.getSource().getClass().getName());
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
    public void selectedRoute(ArrayList<BlockOrder> orders) {
        JPanel con = (JPanel)getContentPane().getComponent(0);
        con.removeAll();
        con.add(_autoRunPanel);
        con.add(_switchPanel);
        updateAutoRunPanel();
        pack();
    }
    
    private void makeAndRunWarrant() {
        if (log.isDebugEnabled()) {
            log.debug("NXFrame selectedRoute()");
        }
        String msg = getBoxData();
        if (msg == null) {
            msg = checkLocoAddress();
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String s = ("" + Math.random()).substring(2);
        Warrant warrant = new Warrant("IW" + s, "NX(" + getAddress() + ")");
        warrant.setSpeedUtil(_speedUtil);
        _speedUtil.setWarrant(warrant);
        warrant.setTrainName(getTrainName());
        warrant.setBlockOrders(_orders);
        _speedUtil.makeSpeedTree();
        int mode;
        if (!_runManual.isSelected()) {
            mode = Warrant.MODE_RUN;
            warrant.setShareRoute(_shareRouteBox.isSelected());
            msg = makeCommands(warrant);
        } else {
            mode = Warrant.MODE_MANUAL;
        }
        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        if (msg == null) {
            tableFrame.getModel().addNXWarrant(warrant);   //need to catch propertyChange at start
            if (log.isDebugEnabled()) {
                log.debug("NXWarrant added to table");
            }
            msg = tableFrame.runTrain(warrant, mode);
            tableFrame.scrollTable();
        }
        if (msg != null) {
            if (log.isDebugEnabled()) {
                log.debug("WarrantTableFrame run warrant. msg= " + msg + " Remove warrant " + warrant.getDisplayName());
            }
            tableFrame.getModel().removeWarrant(warrant);
        }

        if (msg == null && mode == Warrant.MODE_RUN) {
//            if (log.isDebugEnabled()) log.debug("Warrant "+warrant.getDisplayName()+" running.");
            if (_haltStartBox.isSelected()) {
                class Halter implements Runnable {

                    Warrant war;

                    Halter(Warrant w) {
                        war = w;
                    }

                    @Override
                    public void run() {
                        int limit = 0;
                        try {
                            // wait until _engineer is assigned so HALT can take effect
                            while (!war.controlRunTrain(Warrant.HALT) && limit < 3000) {
                                Thread.sleep(200);
                                limit += 200;
                            }
                        } catch (InterruptedException e) {
                            war.controlRunTrain(Warrant.HALT);
                        }
                    }
                }
                Halter h = new Halter(warrant);
                new Thread(h).start();
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        } else {
            closeFrame();
            if (log.isDebugEnabled()) {
                log.debug("Close Frame.");
            }
        }
    }

    protected void closeFrame() {
        clearTempWarrant();
        dispose();
    }

    // for the convenience of testing
    protected void setTimeInterval(float s) {
        _intervalTime = s;
    }

    /**
     * for the convenience of testing
     * @param increment the throttle increment
     */
    protected void setThrottleIncrement(float increment) {
        this._throttleIncr = increment;
    }

    // for the convenience of testing
    protected void setScale(float s) {
        _scale = s;
    }

    // for testing
    protected void setMaxSpeed(float s) {
        _maxThrottle = s;
        _maxSpeedBox.setText(Float.toString(s));
    }
    
    private String getBoxData() {
        String text = null;
        float maxSpeed;
        float oDist;
        float dDist;
        try {
            text = _maxSpeedBox.getText();
            maxSpeed = Float.parseFloat(text);
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("badSpeed", text);
        }
        try {
            text = _originDist.getText();
            oDist = Float.parseFloat(text);
            text = _destDist.getText();
            dDist = Float.parseFloat(text);
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("MustBeFloat", text);
        }
        BlockOrder bo = _orders.get(0);
        if (_originUnits.getText().equals("In")){
            float len = bo.getPath().getLengthIn();
            if (oDist > 0 && oDist < len) {
                _startDist = oDist * 25.4f;
            } else {
                return lengthError(bo.getPathName(), bo.getBlock().getDisplayName(), len, oDist, true);            
            }
        } else {
            float len = bo.getPath().getLengthCm();
            if (oDist > 0 && oDist < len) {
                _startDist = oDist * 10f;
            } else {
                return lengthError(bo.getPathName(), bo.getBlock().getDisplayName(), len, oDist, false);            
            }
        }
        bo = _orders.get(_orders.size()-1);
        if (_destUnits.getText().equals("In")) {
            float len = bo.getPath().getLengthIn();
            if (dDist > 0 && dDist < len) {
                _stopDist = dDist * 25.4f;
            } else {
                return lengthError(bo.getPathName(), bo.getBlock().getDisplayName(), len, dDist, true);            
            }
        } else {
            float len = bo.getPath().getLengthCm();
            if (dDist > 0 && dDist < len) {
                _stopDist = dDist * 10f;
            } else {
                return lengthError(bo.getPathName(), bo.getBlock().getDisplayName(), len, dDist, false);            
            }
        }
        
        if (maxSpeed >= 1.0 || maxSpeed < 0.008) {
            return Bundle.getMessage("badSpeed", maxSpeed);
        }
        _maxThrottle = maxSpeed;
        setAddress();
        return null;
    }
    private String lengthError(String pathName, String blockName, float pathLen, float dist, boolean isInches) {
        if (pathLen <= 0) {
            return Bundle.getMessage("zeroPathLength", pathName, blockName);
        } else if (dist <= 0) {
            return Bundle.getMessage("MustBeFloat", dist);  // positive message                                    
        } else {
            if (isInches) {
                return Bundle.getMessage("BadLengthIn", pathName, blockName, pathLen, dist);                                        
            } else {
                return Bundle.getMessage("BadLengthCm", pathName, blockName, pathLen, dist);                                        
            }
        }
     }

    /*
     * Return length of warrant route in mm.  Assume start and end is in the middle of first
     * and last blocks.  Use a default length for blocks with unspecified length.
     */
    private String getTotalLength() {
        _totalLen = 0.0f;
        List<BlockOrder> orders = getOrders();
        _totalLen = _startDist;
        for (int i = 1; i < orders.size() - 1; i++) {
            BlockOrder bo = orders.get(i);
            float len = bo.getPath().getLengthMm();
            if (len <= 0) {
                return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
            }
            _totalLen += len;
        }
        _totalLen += _stopDist;
        return null;
    }

    private float getRampLength() {
        float speed = 0.0f;     // throttle setting
        float rampLength = 0.0f;
        int numSteps = 0;
        while (speed < _maxThrottle) {
           float dist = _speedUtil.getDistanceTraveled((speed + _throttleIncr / 2), Warrant.Normal, _intervalTime, _forward.isSelected());
            if (rampLength + dist <= _totalLen / 2) {
                if ((speed + _throttleIncr) > _maxThrottle) {
                    dist = dist * (_maxThrottle - speed) / _throttleIncr;
                    speed = _maxThrottle;
                } else {
                    speed += _throttleIncr;
                }
                rampLength += dist;
                numSteps++;
                if (log.isDebugEnabled()) {
                    log.debug("step " + numSteps + " dist= " + dist + " speed= " + speed
                            + " rampLength = " + rampLength);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("cannot reach max Speed and have enough length to decelerate. _maxThrottle set to {}",
                            _maxThrottle);
                    _maxThrottle = speed;      // modify
                }
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(numSteps + " speed steps of delta= "
                    + _throttleIncr + " for rampLength = " + rampLength + " to reach speed " + _maxThrottle);
        }
        return rampLength;
    }

    private String makeCommands(Warrant w) {

        int nextIdx = 0;        // block index - increment after getting a block order
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(nextIdx++);
        String blockName = bo.getBlock().getDisplayName();
        boolean isForward = _forward.isSelected();

        w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
        if (isForward) {
            w.addThrottleCommand(new ThrottleSetting(100, "Forward", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
        } else {
            w.addThrottleCommand(new ThrottleSetting(100, "Forward", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F3", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(500, "F3", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(500, "F3", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(500, "F1", "true", blockName));
        }

        String msg = getTotalLength();
        if (msg != null) {
            return msg;
        }
        float rampLength = getRampLength();

        if (log.isDebugEnabled()) {
            log.debug("_maxThrottle= {} ({} meters per sec), scale= {}", 
                    _maxThrottle, _speedUtil.getTrackSpeed(_maxThrottle, isForward), _scale);
            log.debug("Route length= {}, rampLength= {}", _totalLen, rampLength);
        }

        float blockLen = _startDist;    // length of path in current block

        // start train
        float speedTime = 0;    // ms time to complete speed step from last block
        float noopTime = 0;     // ms time for entry into next block
        float curSpeed = 0;
        // each speed step will last for _intervalTime ms
        float curDistance = 0;  // distance traveled in current block
        float remRamp = rampLength;
        if (log.isDebugEnabled()) {
            log.debug("Start Ramp Up in block \"" + blockName + "\" in "
                    + (int) speedTime + "ms, remRamp= " + remRamp + ", blockLen= " + blockLen);
        }

        while (remRamp > 0.0f) {       // ramp up loop

            curDistance = _speedUtil.getDistanceTraveled(curSpeed, Warrant.Normal, speedTime, isForward);
            while (curDistance < blockLen && curSpeed < _maxThrottle) {
                float dist = _speedUtil.getDistanceTraveled((curSpeed + _throttleIncr / 2), Warrant.Normal, _intervalTime, isForward);
                float prevSpeed = curSpeed;
                if ((curSpeed + _throttleIncr) > _maxThrottle) {
                    dist = dist * (_maxThrottle - curSpeed) / _throttleIncr;
                    curSpeed = _maxThrottle;
                } else {
                    curSpeed += _throttleIncr;
                }
                if (curDistance + dist <= blockLen && remRamp > 0.0f) {
                    curDistance += dist;
                    remRamp -= dist;
                    w.addThrottleCommand(new ThrottleSetting((int) speedTime, "Speed", Float.toString(curSpeed), blockName));
                    if (log.isDebugEnabled()) {
                        log.debug(" dist= " + dist);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Ramp Up in block \"" + blockName + "\" to speed " + curSpeed + " in "
                                + (int) speedTime + "ms to reach curDistance= " + curDistance + " with remRamp= " + remRamp);
                    }
                    speedTime = _intervalTime;
                } else {
                    curSpeed = prevSpeed;
                    break;
                }
            }

            // Possible case where curDistance can exceed the length of a block that was just entered.
            // Move to next block and adjust the distance times into that block
            if (curDistance >= blockLen) {
                noopTime = _speedUtil.getTimeForDistance(curSpeed, blockLen,isForward);   // time overrunning block
                speedTime = _speedUtil.getTimeForDistance(curSpeed, curDistance - blockLen, isForward); // time to next block
            } else {
                noopTime = _speedUtil.getTimeForDistance(curSpeed, blockLen - curDistance, isForward);   // time to next block
                speedTime = _intervalTime - noopTime;   // time to next speed change
            }

            // break out here if deceleration is to be started in this block
            if (_totalLen - blockLen <= rampLength || curSpeed >= _maxThrottle) {
                break;
            }
            if (remRamp > 0.0f && nextIdx < orders.size()) {
                _totalLen -= blockLen;
                if (log.isDebugEnabled()) {
                    log.debug("Leave RampUp block \"{}\"  curDistance= {}, blockLen= {} curSpeed = {} remRamp= {}",
                            blockName, curDistance, blockLen, curSpeed, remRamp);
                }
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthMm();
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) log.debug("Enter block \"{}\" noopTime= {}, speedTime= {} blockLen= {}",
                            blockName, noopTime, speedTime, blockLen);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Ramp Up done at block \"{}\" curSpeed={} totalLen={} rampLength={} remRamp={}", 
                    blockName, curSpeed, _totalLen, rampLength, remRamp);
        }

        if (_totalLen - blockLen > rampLength) {
            _totalLen -= blockLen;
            if (nextIdx < orders.size()) {    // not the last block
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthMm();
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) {
                    log.debug("Enter block \"" + blockName + "\" noopTime= "
                            + noopTime + ", blockLen= " + blockLen);
                }
                curDistance = 0;
            }

            // run through mid route at max speed
            while (nextIdx < orders.size() && _totalLen - blockLen > rampLength) {
                _totalLen -= blockLen;
                // constant speed, get time to next block
                noopTime = _speedUtil.getTimeForDistance(curSpeed, blockLen - curDistance, isForward);   // time to next block
                speedTime = _intervalTime - noopTime;   // time to next speed change
                if (log.isDebugEnabled()) {
                    log.debug("Leave MidRoute block \"" + blockName + "\" noopTime= " + noopTime
                            + ", curDistance=" + curDistance + ", blockLen= " + blockLen + ", totalLen= " + _totalLen);
                }
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthMm();
                if (nextIdx == orders.size()) {
                    blockLen /= 2;
                }
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) {
                    log.debug("Enter block \"" + blockName + "\" noopTime= " + noopTime + ", blockLen= " + blockLen);
                }
                curDistance = 0;
            }
        } // else Start ramp down in current block

        // Ramp down.  use negative delta
        remRamp = rampLength;
        speedTime = _speedUtil.getTimeForDistance(curSpeed, _totalLen - rampLength, isForward); // time to next block
        curDistance = _totalLen - rampLength;
        if (log.isDebugEnabled()) {
            log.debug("Begin Ramp Down at block \"" + blockName + "\" curDistance= "
                    + curDistance + " SpeedTime= " + (int) speedTime + "ms, blockLen= " + blockLen + ", totalLen= " + _totalLen
                    + ", rampLength= " + rampLength + " curSpeed= " + curSpeed);
        }

        while (curSpeed > 0) {
            if (nextIdx == orders.size()) { // at last block
                if (_stageEStop.isSelected()) {
                    w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
                    _intervalTime = 0;
                    break;
                }
            }

            do {
                float dist = _speedUtil.getDistanceTraveled((curSpeed - _throttleIncr / 2), Warrant.Normal, _intervalTime, isForward);
                curDistance += dist;
                curSpeed -= _throttleIncr;
                if (curSpeed <.002) {   // fraction less than 1/4 step
                    curSpeed = 0.0f;
                }
                remRamp -= dist;
                w.addThrottleCommand(new ThrottleSetting((int) speedTime, "Speed", Float.toString(curSpeed), blockName));
                if (log.isDebugEnabled()) {
                    log.debug("Ramp Down in block \"" + blockName + "\" to speed "
                            + curSpeed + " in " + (int) speedTime + "ms to reach curDistance= " + curDistance + " with remRamp= " + remRamp);
                }
                if (curDistance >= blockLen) {
                    speedTime = _speedUtil.getTimeForDistance(curSpeed, curDistance - blockLen, isForward); // time to next block
                } else {
                    speedTime = _intervalTime;
                }
                noopTime = _intervalTime - speedTime;
            } while (curDistance < blockLen && curSpeed > 0);

            if (nextIdx < orders.size()) {
                _totalLen -= blockLen;
                if (log.isDebugEnabled()) {
                    log.debug("Leave RampDown block \"{}\"  curDistance= {}, blockLen= {} curSpeed = {} remRamp= {}",
                            blockName, curDistance, blockLen, curSpeed, remRamp);
                }
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                if (nextIdx == orders.size()) {
                    blockLen = _stopDist;
                } else {
                    blockLen = bo.getPath().getLengthMm();                    
                }
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) {
                    log.debug("Enter block \"" + blockName + "\" noopTime= " + noopTime + ", blockLen= " + blockLen);
                }
                curDistance = _speedUtil.getDistanceTraveled(curSpeed, Warrant.Normal, speedTime, isForward);
            } else {
                if (blockLen == 0) {
                    speedTime = 0;
                }
                break;
            }
        }
        // Ramp down finished
        if (curSpeed > 0) {   // cleanup fractional speeds. insure speed 0 - should never happen.
            log.warn("Ramp down LAST speed change in block \"" + blockName + "\" to speed 0  after "
                    + (int) speedTime + "ms. curSpeed= " + curSpeed + ", curDistance= " + curDistance + ", remRamp= " + remRamp);
            w.addThrottleCommand(new ThrottleSetting((int) speedTime, "Speed", "0.0", blockName));
        }
        w.addThrottleCommand(new ThrottleSetting(500, "F1", "false", blockName));
        w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
        w.addThrottleCommand(new ThrottleSetting(3000, "F2", "false", blockName));
        w.addThrottleCommand(new ThrottleSetting(1000, "F0", "false", blockName));
        /*      if (_addTracker.isSelected()) {
            WarrantTableFrame._defaultAddTracker = true;
            w.addThrottleCommand(new ThrottleSetting(10, "START TRACKER", "", blockName));
        } else {
            WarrantTableFrame._defaultAddTracker = false;
        }*/
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(NXFrame.class.getName());
}
