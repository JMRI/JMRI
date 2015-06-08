package jmri.jmrit.logix;


import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
public class Calibrater extends jmri.util.JmriJFrame {

    private static final long serialVersionUID = 991792418011219112L;
    
    private int _calibrateIndex;
    private Warrant _warrant;
    private float _maxSpeed;
    private long _entryTime;
    private float _factor;
    
    private JPanel _mainPanel;
    private JCheckBox _addBox = new JCheckBox(Bundle.getMessage("addFactor"));
    private JCheckBox _ignoreBox = new JCheckBox(Bundle.getMessage("ignoreFactor"));
    private JCheckBox _clearBox = new JCheckBox(Bundle.getMessage("clearFactor"));
    
    Calibrater(Warrant w, Point pt) {
        super(false, false);
        _warrant = w;
        ButtonGroup bg = new ButtonGroup();
        bg.add(_addBox);
        bg.add(_ignoreBox);
        _mainPanel = new JPanel();
        _mainPanel.setLayout(new BoxLayout(_mainPanel, BoxLayout.PAGE_AXIS));
        _mainPanel.add(Box.createVerticalStrut(10));
        _mainPanel.add(makeEntryPanel(0));
        _mainPanel.add(makeExitPanel(50, "kmph"));
        _mainPanel.add(makeButtonPanel());
        _mainPanel.add(Box.createVerticalStrut(10));
        getContentPane().add(_mainPanel);
        setLocation(pt.x, pt.y);
        setAlwaysOnTop(true);
        pack();
        setVisible(false);
    }
    
    protected String verifyCalibrate() {
        BlockOrder bo = _warrant.getViaOrder();
        if (bo==null) {
            return  Bundle.getMessage("noCalibBlock");                                          
        }
        OBlock calibBlock = bo.getBlock();
        if (calibBlock==null) {
            return  Bundle.getMessage("noCalibBlock");                              
        }
        _calibrateIndex = _warrant.getIndexOfBlock(calibBlock, 0);
        if (_calibrateIndex<=0 || _calibrateIndex>=_warrant.getThrottleCommands().size()-1) {
            return  Bundle.getMessage("badCalibBlock", calibBlock.getDisplayName());                    
        }
        if (bo.getPath().getLengthIn() <= 3.0) {
            return  Bundle.getMessage("CalibBlockTooSmall", calibBlock.getDisplayName());   
        }
        List <ThrottleSetting> cmds = _warrant.getThrottleCommands();
        float speed = 0.0f;
        String beforeBlk = null;
        String afterBlock = null;
        for (ThrottleSetting ts : cmds) {
            if (ts.getCommand().toUpperCase().equals("SPEED")) {
                try {
                    float s = Float.parseFloat(ts.getValue());
                    // get last acceleration block
                    if ( s>speed) {
                        speed = s;
                        beforeBlk = ts.getBlockName();
                    }
                    // get first deceleration block
                    if ( s<speed) {
                        afterBlock = ts.getBlockName();
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    log.error(ts.toString()+" - "+nfe);
                }           
            }
        }
        String msg = null;
        if (_warrant.getIndexOfBlock(beforeBlk, 0) >= _calibrateIndex) {
            msg = Bundle.getMessage("speedChangeBlock", beforeBlk);         
        } else if (_warrant.getIndexOfBlock(afterBlock, 0) <= _calibrateIndex) {
            msg = Bundle.getMessage("speedChangeBlock", afterBlock);            
        } else {
            _maxSpeed = speed;          
        }
        return msg;
    }
    
    private void dofactor() {
        NXFrame.getInstance().setThrottleScale(_factor);
        if (_addBox.isSelected()) {
            
        }
        if (_clearBox.isSelected()) {
            
        }
        dispose();
    }
    
    private JPanel makeButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createGlue());
        JButton button = new JButton(Bundle.getMessage("ButtonOK"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dofactor();
            }
        });
        panel.add(button);
        panel.add(Box.createHorizontalStrut(20));
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(button);
        panel.add(Box.createGlue());
        return panel;

    }
    
    private JPanel makeEntryPanel(int boIdx) {
        OBlock calibBlock =  _warrant.getBlockOrderAt(boIdx).getBlock();
        _entryTime = calibBlock._entryTime;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        panel.add(Box.createVerticalGlue());
        // Train {0} with address {1} has entered block {2} at throttle setting {3}.
        panel.add(new JLabel(Bundle.getMessage("trainInfo1", _warrant.getTrainName(), 
                _warrant.getDccAddress().toString())));
        panel.add(new JLabel(Bundle.getMessage("trainInfo2", calibBlock.getDisplayName(), _maxSpeed)));
//        panel.add(Box.createVerticalGlue());
//        _mainPanel.remove(0);
        return panel;
    }

    private JPanel makeExitPanel(float speed, String speedUnits) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        panel.add(Box.createVerticalGlue());
        panel.add(new JLabel(Bundle.getMessage("trainInfo3", speed, speedUnits)));
        panel.add(new JLabel(Bundle.getMessage("trainInfo4", _factor)));
//        panel.add(_addBox);
//        panel.add(_ignoreBox);
//        panel.add(_clearBox);
        return panel;
    }

    /**
     * Called from Warrant goingActive
     * Compute actual speed and set throttle factor
     * @param index
     */
    protected void calibrateAt(int index) {
        if (_calibrateIndex == index) {
            _mainPanel.remove(2);
            _mainPanel.remove(1);
            _mainPanel.add(makeEntryPanel(index), 0);
            setVisible(true);
        } else if (_calibrateIndex == index-1) {
            setVisible(false);
            BlockOrder bo = _warrant.getBlockOrderAt(index);
            OBlock nextBlock = bo.getBlock();
            long eTime = nextBlock._entryTime - _entryTime;
            float speed = bo.getPath().getLengthIn()*SignalSpeedMap.getMap().getLayoutScale()/eTime;        // scale ins/ms
            _factor = _maxSpeed/speed;
            String speedUnits;
            if ( SignalSpeedMap.getMap().getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
                speedUnits = "kmph";
                speed = speed*3600*25.4f/1000;
            } else {
                speedUnits = "mph";
                speed = speed*3600*1000/(12*5280);
            }
            _mainPanel.add(makeExitPanel(speed, speedUnits), 1);
            setVisible(true);
        }
    }

    static Logger log = LoggerFactory.getLogger(Calibrater.class.getName());
}
