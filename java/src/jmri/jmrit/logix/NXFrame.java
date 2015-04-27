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
import jmri.DccLocoAddress;
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

    JTextField _dccNumBox = new JTextField(6);
    JTextField _trainNameBox = new JTextField(6);
    JTextField _nameBox = new JTextField(6);
    JTextField _maxSpeedBox = new JTextField(6);
    JTextField _minSpeedBox = new JTextField(6);
    JRadioButton _forward = new JRadioButton(Bundle.getMessage("forward"));
    JRadioButton _reverse = new JRadioButton(Bundle.getMessage("reverse"));
    JCheckBox _stageEStop = new JCheckBox();
    JCheckBox _haltStartBox = new JCheckBox();
//    JCheckBox _addTracker = new JCheckBox();
    JTextField _rampInterval = new JTextField(6);
    JTextField _factorbox = new JTextField(6);
    JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));
    JPanel      _controlPanel;
    JPanel      _autoRunPanel;
    JPanel      _manualPanel;
//  static boolean _addTracker = false;
    private boolean _eStop = false;
    private boolean _haltStart = false;
    private float _maxSpeed = 0.5f;
    private float _minSpeed = 0.05f;
    private float _intervalTime = 0.0f;
    private float _throttleIncr = 0.0f;
    
    private String _addr;
    private int _dccNum;
    private boolean  _isLong;
    private int _numSteps;

    private boolean _calibrate;
    
    private static NXFrame _instance;

    static public NXFrame getInstance() {
        if (_instance == null) {
            _instance = new NXFrame();
        }
        _instance._dccNumBox.setText(null);
        _instance._trainNameBox.setText(null);
        _instance._nameBox.setText(null);
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
        _controlPanel.add(pp);
        _controlPanel.add(Box.createVerticalStrut(STRUT_SIZE));

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
        _manualPanel.add(WarrantFrame.makeTextBoxPanel(false, _nameBox, "TrainName", true));
        _manualPanel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));

        _controlPanel.add(_autoRunPanel);
        _controlPanel.add(_manualPanel);
        _manualPanel.setVisible(false);
        _controlPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _forward.setSelected(true);
        _stageEStop.setSelected(_eStop);
        _haltStartBox.setSelected(_haltStart);
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
        button = new JButton(Bundle.getMessage("ButtonCalibrate"));
        button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        calibrationDialog();
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
            if (_factor>8 || _factor<=0.1) {
                msg = Bundle.getMessage("InvalidNumber", text);                                    
            }
        } catch (NumberFormatException nfe) {
            msg = Bundle.getMessage("MustBeFloat", text);                                    
        }
        if (msg==null) {
            switch ( SignalSpeedMap.getMap().getInterpretation()) {
                case SignalSpeedMap.SPEED_MPH:
                    _maxSpeedBox.setText(Float.toString(_maxSpeed*3600*1000/(_factor*12*5280)));
                    _minSpeedBox.setText(Float.toString(_throttleIncr*3600*1000/(_factor*24*5280)));
                    break;
                case SignalSpeedMap.SPEED_KMPH:
                    _maxSpeedBox.setText(Float.toString(_maxSpeed*3600*25.4f/(_factor*1000)));
                    _minSpeedBox.setText(Float.toString(_throttleIncr*3600*25.4f/(_factor*2000)));
            }
        } else {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);            
        }
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
                throttleIncrLabel = "MinSpeed";
                break;
            case SignalSpeedMap.SPEED_MPH:
                maxSpeed = _maxSpeed*3600*1000/(_factor*12*5280);
                maxSpeedLabel = "MaxMph";
                throttleIncr = _throttleIncr*3600*1000/(_factor*12*5280);
                throttleIncrLabel = "MinMph";
                break;
            case SignalSpeedMap.SPEED_KMPH:
                maxSpeed = _maxSpeed*3600*25.4f/(_factor*1000);
                maxSpeedLabel = "MaxKMph";
                throttleIncr = _throttleIncr*3600*25.4f/(_factor*1000);
                throttleIncrLabel = "MinKMph";
                break;
            default:
                maxSpeed = _maxSpeed;                    
                maxSpeedLabel = "MaxSpeed";
                throttleIncr = _throttleIncr;
                throttleIncrLabel = "MinSpeed";
        }
        p1.add(WarrantFrame.makeTextBoxPanel(false, _maxSpeedBox, maxSpeedLabel, true));
        p1.add(WarrantFrame.makeTextBoxPanel(false, _minSpeedBox, throttleIncrLabel, true));
        _maxSpeedBox.setText(Float.toString(maxSpeed));
        _minSpeedBox.setText(Float.toString(throttleIncr/2));
        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(WarrantFrame.makeTextBoxPanel(false, _dccNumBox, "DccAddress", true));
        p2.add(WarrantFrame.makeTextBoxPanel(false, _trainNameBox, "TrainName", true));

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
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(_forward);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(_reverse);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(ppp);
        autoRunPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeBoxPanel(false, _stageEStop, "StageEStop"));
        ppp.add(WarrantFrame.makeBoxPanel(false, _haltStartBox, "HaltAtStart"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _rampInterval, "rampInterval", true));
        ppp.add(WarrantFrame.makeTextBoxPanel(false, _factorbox, "throttleFactor", true));
//        ppp.add(WarrantFrame.makeBoxPanel(false, _addTracker, "AddTracker"));
        pp.add(ppp);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(pp);
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
        } else if (_dccNumBox.getText()==null || _dccNumBox.getText().length()==0){
            msg = Bundle.getMessage("NoLoco");
        }
        if (msg==null) {
            String name =_trainNameBox.getText();
            if (name==null || name.trim().length()==0) {
                name = _addr;
            }
            String s = (""+Math.random()).substring(2);
            warrant = new Warrant("IW"+s, "NX("+_addr+")");
            warrant.setDccAddress( new DccLocoAddress(_dccNum, _isLong));
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
            if (_calibrate) {
                warrant.setViaOrder(getViaBlockOrder());
                calib = new Calibrater(warrant);
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
                _calibrate = false;
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

    private void calibrationDialog() {
        _calibrate = (JOptionPane.showConfirmDialog(this, Bundle.getMessage("calibBlockMessage",
                _dccNumBox.getText()), Bundle.getMessage("calibBlockTitle"), 
                JOptionPane. YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }
    
    private void runManual() {
        String name = _nameBox.getText();
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
//        _minSpeedBox.setText(Float.toString(_throttleIncr/2));
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
                return Bundle.getMessage("InvalidNumber", text);                                    
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
            case SignalSpeedMap.PERCENT_NORMAL:
            case SignalSpeedMap.PERCENT_THROTTLE:
                _maxSpeed = maxSpeed;
                _minSpeed = minSpeed;
                speedErr = Bundle.getMessage("throttlesetting");
                break;
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
                speedErr = Bundle.getMessage("throttlesetting");
        }
        if (_maxSpeed>1.0 || _maxSpeed<0.008) {
            return Bundle.getMessage("badSpeed", maxSpeed, speedErr);                                 
        }
        if (_minSpeed>=1.0 || _minSpeed<0.01575 || _minSpeed>=_maxSpeed) {
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
    
    private String makeCommands(Warrant w) {
        
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(0); 
//        OPath path = bo.getPath();
        String blockName = bo.getBlock().getDisplayName();
        w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
        w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
        w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
        w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
        w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
        w.addThrottleCommand(new ThrottleSetting(1000, "Forward",
                (_forward.isSelected() ? "true" : "false"), blockName));
        
        float delta = _minSpeed*2;          // _throttleIncr;
        
        float defaultBlockLen = 5*_maxSpeed*_intervalTime/(_factor*_scale);
        float blockLen = bo.getPath().getLengthIn()/2;  // save for first time through ramp up loop
        float totalLen = blockLen;     // estimated distance of the route
        int orderSize = orders.size();
        for (int i=1; i<orderSize-1; i++) {
            float len = orders.get(i).getPath().getLengthIn();
            if (len<=0) {
                // intermediate blocks should not be zero
                log.warn(w.getDisplayName()+" route through block \""+orders.get(i).getBlock().getDisplayName()+"\" has length zero. Using "+
                        defaultBlockLen+ " for actual length.");
                len = defaultBlockLen;
            }
            totalLen += len;
        }
        bo = orders.get(orderSize-1);
        OBlock block = bo.getBlock();
        if ((block.getState() & OBlock.DARK) == 0) {        // reduce length for dark blocks
            totalLen += bo.getPath().getLengthIn()/2;       // OK if user has set to 0            
        } else {
            totalLen += Math.max(bo.getPath().getLengthIn()/2-12, 0);
        }
        float curSpeed = delta;
        float rampLength = (_minSpeed*_intervalTime)/(_factor*_scale);      // actual ramp distance to use.
        rampLength += (delta*_intervalTime)/(_factor*_scale);
        _numSteps = 1;
        while (curSpeed<=_maxSpeed) {
            float dist = (curSpeed + delta)*_intervalTime/(_factor*_scale);
            if (rampLength + dist <= totalLen/2) {
                rampLength += dist;
                curSpeed += delta;
                _numSteps++;                             
            } else {
                break;
            }
        }

        if (log.isDebugEnabled()) log.debug("Route length= "+totalLen+" uses "+_numSteps+" speed steps of delta= "+
                delta+" and Factor= "+_factor+" for rampLength = "+rampLength);
/*        if (log.isDebugEnabled()) {
            float rampDownLen = 0;
            log.debug("curSpeed= "+curSpeed);
            int downStep = 0;
            for (int step=_numSteps; step>0; step--) {
                rampDownLen += (curSpeed - delta/2)*_intervalTime/(_factor*_scale);
                curSpeed -= delta;
                downStep++;
            }
            rampDownLen += (curSpeed/2)*_intervalTime/(_factor*_scale);
            log.debug("rampDownLen= "+rampDownLen+" uses "+downStep+" speed steps of delta= "+
                    delta+" for rampLength = "+rampLength+" to last curSpeed= "+curSpeed);
        }*/
                
        int idx = 0;        // block index

        float noopTime = 0;         // ms time for entry into next block
        float curDistance = 0;      // distance traveled in current block
        float remRamp = rampLength;
        // start train
        float speedTime = _intervalTime;        // ms time to complete speed step in next block
        curSpeed = delta;
        w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(_minSpeed), blockName));
        w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
        int curSteps = 1;
        
        boolean start = true;
        while (curSteps < _numSteps) {       // ramp up loop
            if (start) {
                curDistance = (_minSpeed*_intervalTime)/(_factor*_scale);
                remRamp -= curDistance;
                start = false;
            }
            int steps = 0;
            float speed = curSpeed;
            // Assume linear speed change
            while (curSteps+steps < _numSteps) {             
                float dist = (speed + delta)*_intervalTime/(_factor*_scale);
                if (curDistance + dist <= blockLen) {
                    curDistance += dist;
                    speed += delta;
                    steps++;
                    remRamp -= dist;
                } else {
                    break;
                }
            }
            if (steps>0) {
                curSpeed += delta;
                w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", Float.toString(curSpeed), blockName));
                if (steps>1) {
                    curSpeed = rampSpeed(w, (int)_intervalTime, curSpeed, delta, blockName, steps-1);   //steps<=0 OK, no speed change
                }
                if (log.isDebugEnabled()) log.debug("Continue Ramp Up at "+(int)speedTime+"ms in block \""+blockName+
                        "\" to speed "+curSpeed+" after "+steps+" steps to reach curDistance= "+curDistance+", remRamp= "+remRamp);
                curSteps += steps;
            }

            totalLen -= blockLen;
            if (curSteps==_numSteps) {               
                noopTime = (blockLen-curDistance)*(_factor*_scale)/(curSpeed);  // constant
                speedTime = 0;
                curDistance = 0;
            } else {
                noopTime = (blockLen-curDistance)*(_factor*_scale)/(curSpeed-delta); // accelerating
                speedTime = _intervalTime - noopTime;
                curDistance = (curSpeed-delta)*speedTime/(_factor*_scale);
            }
            if (log.isDebugEnabled()) log.debug("Leave RampUp block \""+blockName+"\" noopTime= "+noopTime+
                    ", in distance="+curSpeed*noopTime/(_factor*_scale)+", blockLen= "+blockLen+
                    ", remRamp= "+remRamp);
            bo = orders.get(++idx);
            blockName = bo.getBlock().getDisplayName();
            blockLen = bo.getPath().getLengthIn();
            if (blockLen<=0 && idx<orderSize-1)  {
                blockLen = rampLength;
            }
            w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
            if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
        }
        if (log.isDebugEnabled()) log.debug("Ramp Up done at block \""+blockName+"\" curSteps= "+curSteps+
                ", curSpeed="+curSpeed+", blockLen= "+blockLen+" totalLen= "+totalLen+", rampLength= "+
                rampLength+", remRamp= "+remRamp);
            
        // run through mid route at max speed
        while (idx<orderSize-1) {
            if (totalLen-blockLen <= rampLength) {
                // Start ramp down in this block
                break;
            }
            totalLen -= blockLen;
            // constant speed
            noopTime = (blockLen-curDistance)*(_factor*_scale)/curSpeed;                
            if (log.isDebugEnabled()) log.debug("Leave MidRoute block \""+blockName+"\" noopTime= "+noopTime+
                    ", curDistance="+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen);
            bo = orders.get(++idx);
            blockName = bo.getBlock().getDisplayName();
            blockLen = bo.getPath().getLengthIn();
            if (idx==orderSize-1) {
                blockLen /= 2;
            } else if (blockLen<=0) {
                blockLen = rampLength;
            }
            w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
            if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
            curDistance = 0;
        }
        if (log.isDebugEnabled()) log.debug("Begin Ramp Down at block \""+blockName+"\", curSteps= "+curSteps+
                ", curDistance= "+curDistance+", blockLen= "+blockLen+", totalLen= "+totalLen+
                ", rampLength= "+rampLength+" curSpeed= "+curSpeed);
        
        // Ramp down.  use negative delta
        remRamp = rampLength;
        start = true;
        while (curSteps>1) {
            if (idx==orderSize-1) {
                // at last block
                if (_stageEStop.isSelected()) {
                    _eStop = true;
                    w.addThrottleCommand(new ThrottleSetting(0, "Speed", "-0.5", blockName));
                    _intervalTime = 0;
                    break;
                } else {
                    _eStop = false;                 
                }
            }
            if (start) {
                // constant speed
                speedTime = (totalLen-curDistance-rampLength)*(_factor*_scale)/curSpeed;
                curDistance =totalLen-rampLength;
                start = false;
            }
            int steps = 1;      // at least one speed change.  Maybe more.
            float speed = curSpeed;
            while (curSteps-steps > 1) {                
                float dist = (speed - delta)*_intervalTime/(_factor*_scale);
                if (curDistance + dist <= blockLen) {
                    curDistance += dist;
                    speed -= delta;
                    steps++;
                    remRamp -= dist;
                } else {
                    break;
                }
            }
            curSpeed = rampSpeed(w, (int)speedTime, curSpeed, -delta, blockName, steps);    //steps==0 OK, no speed change                  
            if (log.isDebugEnabled()) log.debug("Ramp Down after "+(int)speedTime+"ms in block \""+blockName+
                    "\" to speed "+curSpeed+" and do "+steps+" steps to reach curDistance= "+curDistance+", remRamp= "+remRamp);
            curSteps -= steps;
            totalLen -= blockLen;
            
            if (idx < orderSize-1) {
                noopTime = (blockLen-curDistance)*(_factor*_scale)/(curSpeed+delta);
                speedTime = _intervalTime - noopTime;
                if (log.isDebugEnabled()) log.debug("Leave RampDown block \""+blockName+"\" noopTime= "+noopTime+
                        ", in distance="+curSpeed*noopTime/(_factor*_scale)+", blockLen= "+blockLen+
                        ", totalLen= "+totalLen+", remRamp= "+remRamp);
                bo = orders.get(++idx);
                blockName = bo.getBlock().getDisplayName();
                blockLen = bo.getPath().getLengthIn();
                if (blockLen<=0 && idx<orderSize-1)  {
                    blockLen = rampLength;
                }
                w.addThrottleCommand(new ThrottleSetting((int)noopTime, "NoOp", "Enter Block", blockName));
                if (log.isDebugEnabled()) log.debug("Enter block \""+blockName+"\" noopTime= "+noopTime);
                curDistance = (curSpeed+delta)*speedTime/(_factor*_scale);
            } else {
                if (blockLen==0) {
                    speedTime = 0;
                } else {
                    speedTime = _intervalTime;
                }
                break;                
            }
            if (log.isDebugEnabled()) log.debug("In block \""+blockName+"\" curSteps= "+curSteps+", curDistance= "+curDistance);
        }
        if (log.isDebugEnabled()) {
            curDistance += curSpeed*speedTime/(_factor*_scale);
            remRamp -=  curSpeed*speedTime/(_factor*_scale);                
            log.debug("Ramp down last speed change in block \""+blockName+"\" to speed "+curSpeed+
                    " after "+(int)_intervalTime+"ms. at curDistance= "+curDistance+", remRamp= "+remRamp);         
        }
        w.addThrottleCommand(new ThrottleSetting((int)speedTime, "Speed", "0.0", blockName));               
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

    /**
     * 
     * @param w - warrant
     * @param time - time to wait to start ramp
     * @param speed - throttle setting
     * @param delta - throttle increment
     * @param blockName - block  where command is issued
     * @param incr - number of speed change increments
     * @return - resulting throttle setting
     */
    private float rampSpeed(Warrant w, int time, float speed, float delta, String blockName, int incr) {
        for (int i = 0; i < incr; i++) {
            speed += delta;
            w.addThrottleCommand(new ThrottleSetting(time, "Speed", Float.toString(speed), blockName));
            time = (int)_intervalTime;  // after 1st wait time, use ramp increment time.
        }
        return speed;
    }

    private boolean makeAndRunWarrant() {
        String msg = null;
        _addr = _dccNumBox.getText();
        if (_addr != null && _addr.length() != 0) {
            _addr = _addr.toUpperCase().trim();
            _isLong = false;
            Character ch = _addr.charAt(_addr.length() - 1);
            try {
                if (!Character.isDigit(ch)) {
                    if (ch != 'S' && ch != 'L' && ch != ')') {
                        msg = Bundle.getMessage("BadDccAddress", _addr);
                    }
                    if (ch == ')') {
                        _dccNum = Integer.parseInt(_addr.substring(0, _addr.length() - 3));
                        ch = _addr.charAt(_addr.length() - 2);
                        _isLong = (ch == 'L');
                    } else {
                        _dccNum = Integer.parseInt(_addr.substring(0, _addr.length() - 1));
                        _isLong = (ch == 'L');
                    }
                } else {
                    _dccNum = Integer.parseInt(_addr);
                    ch = _addr.charAt(0);
                    _isLong = (ch == '0' || _dccNum > 127);  // leading zero means long
                    _addr = _addr + (_isLong ? "L" : "S");
                }
            } catch (NumberFormatException nfe) {
                msg = Bundle.getMessage("BadDccAddress", _addr);
            }
        } else {
            msg = Bundle.getMessage("BadDccAddress", _addr);
        }
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
