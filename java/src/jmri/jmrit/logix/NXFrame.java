package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import jmri.implementation.SignalSpeedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for defining and launching an entry/exit warrant. An NX warrant is a
 * warrant that can be defined on the run without a pre-recorded learn mode
 * session using a set script for ramping startup and stop throttle settings.
 * <P>
 * The route can be defined in a form or by mouse clicking on the OBlock
 * IndicatorTrack icons.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author  Pete Cressman  Copyright (C) 2009, 2010, 2015
 */
public class NXFrame extends WarrantRoute {

    private static final long serialVersionUID = -8971792418011219112L;
    WarrantTableFrame   _parent;
    private float _scale = 87.1f;
    private float _factor = 0.81f;  // ratio of throttle setting to scale speed

    JTextField _trainNameBox = new JTextField(6);
    JTextField _maxSpeedBox = new JTextField(6);
    JTextField _minSpeedBox = new JTextField(6);
    JRadioButton _forward = new JRadioButton();
    JRadioButton _reverse = new JRadioButton();
    JCheckBox _stageEStop = new JCheckBox();
    JCheckBox _haltStartBox = new JCheckBox();
    JCheckBox _calibrateBox = new JCheckBox();
//    JCheckBox _addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField(6);
    JTextField _factorbox = new JTextField(6);
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel      _controlPanel;
    JPanel      _autoRunPanel;
    JPanel      _manualPanel;
//  static boolean _addTracker = false;
    private boolean _haltStart = false;
    private float _maxSpeed = 0.5f;
    private float _minSpeed = 0.05f;
    private float _intervalTime = 0.0f;     // milliseconds
    private float _throttleIncr = 0.0f;
    
    private static NXFrame _instance;

    static public NXFrame getInstance() {
        if (_instance == null) {
            _instance = new NXFrame();
        }
        _instance.setAddress(null);
        _instance.setTrainName(null);
        _instance._trainNameBox.setText(null);
        _instance.clearRoute();
        return _instance;
    }

    private NXFrame() {
        super();
        _parent = WarrantTableFrame.getInstance();
    }

    public void init() {
        makeMenus();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        _controlPanel = new JPanel();
        _controlPanel.setLayout(new BoxLayout(_controlPanel, BoxLayout.Y_AXIS));
        _controlPanel.add(Box.createVerticalGlue());
        _controlPanel.add(makeBlockPanels());
        _controlPanel.add(searchDepthPanel(false));

        _autoRunPanel = makeAutoRunPanel(SignalSpeedMap.getMap().getInterpretation());
        _factorbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSpeeds();
            }
        });
        _maxSpeedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getBoxData();
            }
        });
        _manualPanel = new JPanel();
        _manualPanel.setLayout(new BoxLayout(_manualPanel, BoxLayout.X_AXIS));
        _manualPanel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        _manualPanel.add(makeTextBoxPanel(false, _trainNameBox, "TrainName", "noTrainName"));
        _manualPanel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));

        _controlPanel.add(_autoRunPanel);
        _controlPanel.add(_manualPanel);
        _manualPanel.setVisible(false);
        _controlPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _forward.setSelected(true);
        _stageEStop.setSelected(false);
        _haltStartBox.setSelected(_haltStart);
        _calibrateBox.setSelected(false);
        _rampInterval.setText(Float.toString(_intervalTime / 1000));
        _factorbox.setText(Float.toString(_factor));
        _factorbox.setToolTipText(Bundle.getMessage("ToolTipThrottleScale"));
        JPanel p = new JPanel();
        p.add(Box.createGlue());
        JButton button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeAndRunWarrant();
            }
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeFrame();
            }
        });
        p.add(button);
        p.add(Box.createGlue());
        _controlPanel.add(p);
        
        _controlPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        _controlPanel.add(makeSwitchPanel());
        
        mainPanel.add(_controlPanel);
        getContentPane().add(mainPanel);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeFrame();
            }
        });
        setLocation(_parent.getLocation().x+200, _parent.getLocation().y+200);
        setAlwaysOnTop(true);
        pack();
    }
    
    public void updatePanel(int interp) {
        if (_controlPanel==null) {
            return;
        }
        closeFrame();
        clearTempWarrant();
        java.awt.Component[] list = _controlPanel.getComponents();
        int i = 0;
        while (i<list.length && !list[i].equals(_autoRunPanel)) {
            i++;
        }
        _controlPanel.remove(_autoRunPanel);
        _autoRunPanel = makeAutoRunPanel(interp);
        _controlPanel.add(_autoRunPanel, i);
    }
    
    private void updateSpeeds() {
        String msg = null;
        String text = _factorbox.getText();
        try {
            _factor = Float.parseFloat(text);
            if (_factor>=8 || _factor<=0.1) {
                msg = Bundle.getMessage("InvalidFactor", text);                                    
            }
        } catch (NumberFormatException nfe) {
            msg = Bundle.getMessage("MustBeFloat", text);                                    
        }
        if (msg==null) {
            switch ( SignalSpeedMap.getMap().getInterpretation()) {
                case SignalSpeedMap.SPEED_MPH:
                    _maxSpeedBox.setText(round(_maxSpeed*3600*1000/(_factor*12*5280), 2));
                    _minSpeedBox.setText(round(_throttleIncr*3600*1000/(_factor*12*5280), 2));
                    break;
                case SignalSpeedMap.SPEED_KMPH:
                    _maxSpeedBox.setText(round(_maxSpeed*3600*25.4f/(_factor*1000), 2));
                    _minSpeedBox.setText(round(_throttleIncr*3600*25.4f/(_factor*1000), 2));
            }
        } else {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);            
        }
    }
    static private  String round(float f, int place) {
        double e = Math.pow(10,place); 
        return Double.toString(Math.round(f*e)/e);
    }
    
    private JPanel makeSwitchPanel() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(_runAuto);
        bg.add(_runManual);
        _runAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(true);
            }
        });
        _runManual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                enableAuto(false);
            }
        });
        _runAuto.setSelected(true);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runAuto);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runManual);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        return pp;
    }
    
    private JPanel makeAutoRunPanel(int interpretation) {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        float maxSpeed;
        float throttleIncr;
        String maxSpeedLabel;
        String throttleIncrLabel;
        switch ( interpretation) {
            case SignalSpeedMap.PERCENT_NORMAL:
            case SignalSpeedMap.PERCENT_THROTTLE:
                maxSpeed = _maxSpeed;
                maxSpeedLabel = "MaxSpeed";
                throttleIncr = _throttleIncr;
                throttleIncrLabel = "RampIncrement";
                break;
            case SignalSpeedMap.SPEED_MPH:
                maxSpeed = Math.round(_maxSpeed*3600*1000/(_factor*12*5280)*100)/100;
                maxSpeedLabel = "MaxMph";
                throttleIncr = Math.round(_throttleIncr*3600*1000/(_factor*12*5280)*100)/100;
                throttleIncrLabel = "MinMph";
                break;
            case SignalSpeedMap.SPEED_KMPH:
                maxSpeed = Math.round(_maxSpeed*3600*25.4f/(_factor*1000)*100)/100;
                maxSpeedLabel = "MaxKMph";
                throttleIncr = Math.round(_throttleIncr*3600*25.4f/(_factor*1000)*100)/100;
                throttleIncrLabel = "MinKMph";
                break;
            default:
                maxSpeed = _maxSpeed;                    
                maxSpeedLabel = "MaxSpeed";
                throttleIncr = _throttleIncr;
                throttleIncrLabel = "RampIncrement";
        }
//        p1.add(WarrantFrame.makeTextBoxPanel(false, _maxSpeedBox, maxSpeedLabel, ""));      //???
        p1.add(makeTextBoxPanel(false, _rampInterval, "rampInterval", null));
        p1.add(makeTextBoxPanel(false, _minSpeedBox, throttleIncrLabel, "ToolTipRampIncrement"));
        p1.add(makeTextBoxPanel(false, _factorbox, "ThrottleFactor", "throttleFactor"));
       _maxSpeedBox.setText(Float.toString(maxSpeed));
        _minSpeedBox.setText(Float.toString(throttleIncr));
        
        JPanel p2 = makeTrainPanel();

        JPanel autoRunPanel = new JPanel();
        autoRunPanel.setLayout(new BoxLayout(autoRunPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p2);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(pp);
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(Box.createHorizontalStrut(STRUT_SIZE));
        p1.add(makeTextBoxPanel(false, _forward, "forward", null));
//        p1.add(Box.createHorizontalStrut(STRUT_SIZE));
        p1.add(makeTextBoxPanel(false, _reverse, "reverse", null));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(p1);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(makeTextBoxPanel(false, _maxSpeedBox, maxSpeedLabel, null));
        pp.add(Box.createHorizontalStrut(2*STRUT_SIZE));
        autoRunPanel.add(pp);

//        autoRunPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _stageEStop, "StageEStop", null));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _haltStartBox, "HaltAtStart", null));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(makeTextBoxPanel(false, _calibrateBox, "Calibrate", "calibBlockMessage"));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(ppp);
//        autoRunPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        return autoRunPanel;
    }

    private void makeMenus() {
        setTitle(Bundle.getMessage("AutoWarrant"));
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.NXWarrant", true);
    }

    private void enableAuto(boolean enable) {
        if (enable) {
            _manualPanel.setVisible(false);
            _autoRunPanel.setVisible(true);
        } else {
            _manualPanel.setVisible(true);
            _autoRunPanel.setVisible(false);
        }
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
     * Callback from RouteFinder.findRoute()
     */
    @Override
    public void selectedRoute(ArrayList<BlockOrder> orders) {
        if (log.isDebugEnabled()) log.debug("NXFrame selectedRoute()");
        String msg =null;
        Warrant warrant = null;
        if (_runManual.isSelected()) {
            runManual();
            return;
        }
        msg = checkLocoAddress();
        if (msg==null) {
            String name = getTrainName();
            if (name==null || name.trim().length()==0) {
                name = getAddress();
            }
            String s = (""+Math.random()).substring(2);
            warrant = new Warrant("IW"+s, "NX("+getAddress()+")");
            warrant.setDccAddress(getLocoAddress());
            warrant.setTrainName(name);

            msg = getBoxData();
            if (msg==null) {
                msg = makeCommands(warrant);                            
            }
            if (msg==null) {
                warrant.setBlockOrders(getOrders());
                warrant.setOrders(getOrders());
                warrant.setThrottleFactor(_factor);
            }
        }
        if (msg==null && warrant!=null) {
            Calibrater calib = null;
            if (_calibrateBox.isSelected()) {
                warrant.setViaOrder(getViaBlockOrder());
                calib = new Calibrater(warrant, getLocation());
                msg = calib.verifyCalibrate();
                if (msg!=null) {
                    calib = null;
                }                
            }
            warrant.setCalibrater(calib);
            if (msg==null) {
                _parent.getModel().addNXWarrant(warrant);   //need to catch propertyChange at start
                if (log.isDebugEnabled()) log.debug("NXWarrant added to table");
                msg = _parent.runTrain(warrant);                
            }
            if (msg!=null) {
                if (log.isDebugEnabled()) log.debug("WarrantTableFrame run warrant. msg= "+msg+" Remove warrant "+warrant.getDisplayName());
                _parent.getModel().removeNXWarrant(warrant);
            }
        }
        if (msg==null) {
//            if (log.isDebugEnabled()) log.debug("Warrant "+warrant.getDisplayName()+" running.");
            if (_haltStartBox.isSelected()) {
                _haltStart = true;
                class Halter implements Runnable {
                    Warrant war;
                    Halter (Warrant w) {
                        war = w;
                    }
                    public void run() {
                        int limit = 0;  
                        try {
                            while (!war.controlRunTrain(Warrant.HALT) && limit<3000) {
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
            } else {
                _haltStart = false;                 
            }
        }
        if (msg!=null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            warrant = null;
        } else {
            _parent.scrollTable();
            closeFrame();           
            if (log.isDebugEnabled()) log.debug("Close Frame.");
        }
    }

    private void runManual() {
        String name = _trainNameBox.getText();
        if (name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noTrainName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String s = ("" + Math.random()).substring(2);
        Warrant warrant = new Warrant("IW" + s, "NX(" + name + ")");
        warrant.setTrainName(name);
        warrant.setRoute(0, getOrders());
        _parent.getModel().addNXWarrant(warrant);
        warrant.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
        _parent.scrollTable();
        closeFrame();
    }

    private void closeFrame() {
        dispose();
        _parent.closeNXFrame();
    }


    public void setMaxSpeed(float s) {
        _maxSpeed = s;
    }

    public float getMaxSpeed() {
        return _maxSpeed;
    }

    public void setTimeInterval(float s) {
        _intervalTime = s;
        _rampInterval.setText(Float.toString(_intervalTime/1000));
    }
    public void setRampIncrement(float throttleIncr) {
        _throttleIncr = throttleIncr;        
    }

    public void setStartHalt(boolean s) {
        _haltStart = s;
    }

    public boolean getStartHalt() {
        return _haltStart;
    }

    public void setScale(float s) {
        _scale = s;
    }
    public float getScale() {
        return _scale;
    }

    public void setThrottleScale(float s) {
        _factor = s;
        _factorbox.setText(Float.toString(_factor));
    }
    public float getThrottleFactor() {
        return _factor;
    }

    private String getBoxData() {
        String text = null;
        try {
            text = _factorbox.getText();
            _factor = Float.parseFloat(text);
            if (_factor>10 || _factor<=0.1) {
                return Bundle.getMessage("InvalidFactor", text);                                    
            }
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("MustBeFloat", text);                                    
        }
        float maxSpeed = _maxSpeed;
        float minSpeed = _minSpeed;
        try {
            text = _maxSpeedBox.getText();
            maxSpeed = Float.parseFloat(text);
            text = _minSpeedBox.getText();
            minSpeed = Float.parseFloat(text);
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("MustBeFloat", text);             
        }
        String speedErr;
        switch ( SignalSpeedMap.getMap().getInterpretation()) {
            case SignalSpeedMap.SPEED_MPH:
                _maxSpeed = maxSpeed*_factor*12*5280/(3600*1000);
                _minSpeed = minSpeed*_factor*12*5280/(3600*1000);
                speedErr = Bundle.getMessage("speedMph");
                break;
            case SignalSpeedMap.SPEED_KMPH:
                _maxSpeed = maxSpeed*_factor*1000/(3600*25.4f);
                _minSpeed = minSpeed*_factor*1000/(3600*25.4f);
                speedErr = Bundle.getMessage("speedKmph");
                break;
            default:
                _maxSpeed = maxSpeed;
                _minSpeed = minSpeed;
                speedErr = "";      // Bundle.getMessage("throttlesetting");
        }
        if (_maxSpeed>1.0 || _maxSpeed<0.008) {
            return Bundle.getMessage("badSpeed", maxSpeed, speedErr);                                 
        }
        if (_minSpeed>0.8 || _minSpeed<0.002|| _minSpeed>=_maxSpeed) {
            return Bundle.getMessage("badSpeed", minSpeed, speedErr);                                 
        }
        try {
            text = _rampInterval.getText();
            _intervalTime = Float.parseFloat(text)*1000;
            if (_intervalTime>30000 || _intervalTime<300) {
                return Bundle.getMessage("InvalidTime", text);                                  
            }
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("InvalidTime", text);                                  
        }
        return null;
    }

    /*
     * Return length of warrant route in inches.  Assume start and end is in the middle of first
     * and last blocks.  Use a default length for blocks with unspecified length.
     */
    private float getTotalLength(float defaultBlockLen) {
        
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(0); 
        float len = bo.getPath().getLengthIn();
        if (len<=0) {
            len = defaultBlockLen;
        }
        float totalLen = len/2;      // estimated distance of the route
        for (int i=1; i<orders.size()-1; i++) {
            len =  orders.get(i).getPath().getLengthIn();
            if (len<=0) {
                // intermediate blocks should not be zero
                log.warn("Route through block \""+orders.get(i).getBlock().getDisplayName()+"\" has length zero. Using "+
                        defaultBlockLen+ " for actual length.");
                len = defaultBlockLen;
            }
            totalLen += len;
        }
        bo = orders.get(orders.size()-1);
        len = bo.getPath().getLengthIn();
        if (len<=0) {
            len = defaultBlockLen;
        }
        totalLen += len/2;
        return totalLen;
    }
    private float getRampLength(float totalLen) {
        float speed = 0.0f;
        float rampLength = 0.0f;
        int numSteps = 0;
        while (speed<_maxSpeed) {
            float dist = (speed + _minSpeed/2)*_intervalTime/(_factor*_scale);
            if (rampLength + dist <= totalLen/2) {
                rampLength += dist;
                speed += _minSpeed;
                numSteps++;
            } else {
                // cannot get to _maxSpeed and have length to decelerate
                _maxSpeed = speed;      // modify
                break;
            }
        }
        // add the smidge of distance needed to reach _maxSpeed
//        rampLength += (_maxSpeed - speed)*_intervalTime/(_factor*_scale);
        if (log.isDebugEnabled()) log.debug("Route length= "+totalLen+" uses "+numSteps+" speed steps of delta= "+
                _minSpeed+" and Factor= "+_factor+" for rampLength = "+rampLength);
        return rampLength;
    }
    
    private String makeCommands(Warrant w) {
        
        int nextIdx = 0;        // block index - increment after getting a block order
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(nextIdx++); 
        String blockName = bo.getBlock().getDisplayName();
        
        w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
        if (_forward.isSelected()) {
            w.addThrottleCommand(new ThrottleSetting(1000, "Forward", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));            
        } else {
            w.addThrottleCommand(new ThrottleSetting(1000, "Forward", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(2000, "F3", "true", blockName));            
            w.addThrottleCommand(new ThrottleSetting(500, "F3", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(500, "F3", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(500, "F1", "true", blockName));
        }
        
        // estimate for blocks of zero length - an estimate of ramp length 
        float defaultBlockLen = 6*_maxSpeed*_intervalTime/(_factor*_scale);
        float totalLen = getTotalLength(defaultBlockLen);
        float rampLength = getRampLength(totalLen);

        float blockLen = bo.getPath().getLengthIn();    // length of path in current block
        if (blockLen<=0) {
            blockLen = defaultBlockLen;
        }
        blockLen /=2;
        
        // start train
        float speedTime = 500;      // ms time to complete speed step from last block
        float noopTime = 0;         // ms time for entry into next block
        float curSpeed = _minSpeed;
        // each speed step will last for _intervalTime ms
        float curDistance = curSpeed*_intervalTime/(_factor*_scale);    // distance traveled in current block
        float remRamp = rampLength - curDistance;
        w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
        if (log.isDebugEnabled()) log.debug("Start Ramp Up in block \""+blockName+ "\" to speed "+curSpeed+" in "
                +(int)speedTime+"ms to reach curDistance= "+curDistance+", remRamp= "+remRamp+", blockLen= "+blockLen);
         
        while (remRamp > 0.0f) {       // ramp up loop
            
            while (curDistance < blockLen && remRamp > 0.0f) {             
                float dist = (curSpeed + _minSpeed/2)*_intervalTime/(_factor*_scale);
                if (curDistance + dist <= blockLen && remRamp > 0.0f) {
                    curDistance += dist;
                    float remSpeed = _maxSpeed - curSpeed;
                    if (0<remSpeed && remSpeed < _minSpeed) {                        
                        curSpeed += remSpeed;
                        remRamp = -1;   // insure we break out of loop
                    } else {
                        curSpeed += _minSpeed;                        
                    }
                    remRamp -= dist;
                    w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
                    if (log.isDebugEnabled()) log.debug("Ramp Up in block \""+blockName+ "\" to speed "+curSpeed+" in "
                           +(int)speedTime+"ms to reach curDistance= "+curDistance+", remRamp= "+remRamp);
                    speedTime = _intervalTime;
                } else {
                    break;
                }
            }
            
            // Possible case where curDistance can exceed the length of a very short block that was just entered.
            // Move to next block and adjust the distance times into that block
            if (curDistance>=blockLen) {
                noopTime = (blockLen)*(_factor*_scale)/curSpeed;  // time to next block
                speedTime = (curDistance-blockLen)*(_factor*_scale)/curSpeed;                
            } else {
                noopTime = (blockLen-curDistance)*(_factor*_scale)/curSpeed;  // time to next block
                speedTime = _intervalTime - noopTime;   // time to next speed change                
            }
            
            // break out here if deceleration is to be started in this block
            if (totalLen - blockLen <= rampLength) {
                break;
            }
            if (nextIdx < orders.size()) {    // not the last block
                curDistance = curSpeed*speedTime/(_factor*_scale);
                totalLen -= blockLen;
                if (log.isDebugEnabled()) log.debug("Leave RampUp block \""+blockName+"\" noopTime= "+noopTime+
                        ", in distance="+curSpeed*noopTime/(_factor*_scale)+", blockLen= "+blockLen+
                        ", remRamp= "+remRamp);
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthIn();
                if (blockLen<=0)  {
                    blockLen = defaultBlockLen;
                }                
                w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+
                        noopTime+", blockLen= "+blockLen);
            }
        }
        if (log.isDebugEnabled()) log.debug("Ramp Up done at block \""+blockName+"\" curSpeed=" +
                ""+curSpeed+", blockLen= "+blockLen+" totalLen= "+totalLen+", rampLength= "+
                rampLength+", remRamp= "+remRamp);
            
        // run through mid route at max speed
        while (nextIdx < orders.size()) {
            if (totalLen-blockLen <= rampLength) {
                // Start ramp down in current block
                break;
            }
            totalLen -= blockLen;
            // constant speed
            noopTime = (blockLen-curDistance)*(_factor*_scale)/curSpeed;                
            if (log.isDebugEnabled()) log.debug("Leave MidRoute block \""+blockName+"\" noopTime= "+noopTime+
                    ", curDistance="+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen);
            bo = orders.get(nextIdx++);
            blockName = bo.getBlock().getDisplayName();
            blockLen = bo.getPath().getLengthIn();
            if (nextIdx == orders.size()) {
                blockLen /= 2;
            } else if (blockLen<=0) {
                blockLen = defaultBlockLen;
            }
            w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
            if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
            curDistance = 0;
        }
        
        // Ramp down.  use negative delta
        remRamp = rampLength;
        speedTime = (totalLen-rampLength)*(_factor*_scale)/curSpeed;
        curDistance = totalLen - rampLength;
        if (log.isDebugEnabled()) log.debug("Begin Ramp Down at block \""+blockName+"\" curDistance= "
                +curDistance+" SpeedTime= "+(int)speedTime+"ms, blockLen= "+blockLen+", totalLen= "+totalLen+
                ", rampLength= "+rampLength+" curSpeed= "+curSpeed);
        
        while (curSpeed >= _minSpeed) {
            if (nextIdx == orders.size()) { // at last block
                if (_stageEStop.isSelected()) {
                    w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
                    _intervalTime = 0;
                    break;
                }
            }
            
            while (curDistance < blockLen && curSpeed >= _minSpeed) {             
                float dist = (curSpeed-_minSpeed/2)*_intervalTime/(_factor*_scale);
                if (curDistance + dist <= blockLen) {
                    curDistance += dist;
                    curSpeed -= _minSpeed;
                    remRamp -= dist;
                    w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
                    if (log.isDebugEnabled()) log.debug("Ramp Down in block \""+blockName+"\" to speed "+
                            curSpeed+" in "+(int)speedTime+"ms to reach curDistance= "+curDistance+" where blockLen= "+blockLen+", remRamp= "+remRamp);                        
                    speedTime = _intervalTime;
                } else {
                    break;
                }
            }
            
            if (nextIdx < orders.size()) {
                noopTime = (blockLen-curDistance)*(_factor*_scale)/curSpeed;
                if (noopTime<0 || _intervalTime<noopTime) {
                    log.error("Ramp Down NoOp time invalid! noopTime= "+noopTime);
                    noopTime = _intervalTime/2;
                }
                speedTime = _intervalTime - noopTime;
                totalLen -= blockLen;
                if (log.isDebugEnabled()) log.debug("Leave RampDown block \""+blockName+"\" noopTime= "+noopTime+
                        ", in distance="+curSpeed*noopTime/(_factor*_scale)+", blockLen= "+blockLen+
                        ", totalLen= "+totalLen+", remRamp= "+remRamp);
                bo = orders.get(nextIdx++);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthIn();
                if (blockLen<=0)  {
                    blockLen = defaultBlockLen;
                }
                w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
                curDistance = curSpeed*speedTime/(_factor*_scale);
            } else {
                if (blockLen==0) {
                    speedTime = 0;
                }
                break;                
            }
        }
        if (log.isDebugEnabled()) {
            curDistance += curSpeed*speedTime/(_factor*_scale);
            remRamp -=  curSpeed*speedTime/(_factor*_scale);                
            log.debug("Ramp down last speed change in block \""+blockName+"\" to speed "+curSpeed+
                    " after "+(int)_intervalTime+"ms. at curDistance= "+curDistance+", remRamp= "+remRamp);         
        }
        w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", "0.0", blockName));               
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

    private boolean makeAndRunWarrant() {
        String msg = checkLocoAddress();
        if (msg == null) {
            if (log.isDebugEnabled()) log.debug("NXWarrant makeAndRunWarrant calls findRoute()");
            msg = findRoute();
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    static Logger log = LoggerFactory.getLogger(NXFrame.class.getName());
}
