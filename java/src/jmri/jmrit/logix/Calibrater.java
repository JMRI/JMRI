package jmri.jmrit.logix;


import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.DccThrottle;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterSpeedProfile;
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

    private int _calibrateIndex;
    private Warrant _warrant;
    private float _maxSpeed;
    private long _entryTime;
    private float _factor;
    private float _rawSpeed;
    private BlockOrder _calibBlockOrder;
    RosterSpeedProfile  _speedProfile;
    boolean _isForward;
    
    private JPanel _mainPanel;
    private JCheckBox _addBox = new JCheckBox(Bundle.getMessage("addFactor"));
    private JCheckBox _ignoreBox = new JCheckBox(Bundle.getMessage("ignoreFactor"));
    private JCheckBox _clearBox = new JCheckBox(Bundle.getMessage("clearFactor"));
    
    Calibrater(Warrant w, boolean isForward, Point pt) {
        super(false, false);
        _warrant = w;
        ButtonGroup bg = new ButtonGroup();
        bg.add(_addBox);
        bg.add(_ignoreBox);
        _mainPanel = new JPanel();
        _mainPanel.setLayout(new BoxLayout(_mainPanel, BoxLayout.PAGE_AXIS));
        _mainPanel.add(Box.createRigidArea(new java.awt.Dimension(350,10)));
        _mainPanel.add(makeEntryPanel("Calibrate a Train", isForward));
        _mainPanel.add(Box.createRigidArea(new java.awt.Dimension(50,10)));
        _mainPanel.add(makeExitPanel(true));
//        _mainPanel.add(Box.createRigidArea(new java.awt.Dimension(450,0)));
        _mainPanel.add(makeButtonPanel());
        _mainPanel.add(Box.createVerticalStrut(10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        getContentPane().add(_mainPanel);
        setLocation(pt.x, pt.y);
        setAlwaysOnTop(true);
        pack();
        setVisible(false);
    }
    
    protected String verifyCalibrate() {
        _calibBlockOrder = _warrant.getViaOrder();
        if (_calibBlockOrder==null) {
            return  Bundle.getMessage("noCalibBlock");                                          
        }
        OBlock calibBlock = _calibBlockOrder.getBlock();
        if (calibBlock==null) {
            return  Bundle.getMessage("noCalibBlock");                              
        }
        _calibrateIndex = _warrant.getIndexOfBlock(calibBlock, 0);
        if (_calibrateIndex<=0 || _calibrateIndex>=_warrant.getThrottleCommands().size()-1) {
            return  Bundle.getMessage("badCalibBlock", calibBlock.getDisplayName());                    
        }
        if (_calibBlockOrder.getPath().getLengthMm() <= 10.0) {
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
            jmri.jmrit.roster.RosterEntry ent = _warrant.getRosterEntry();
            if (ent!=null) {
                _speedProfile = ent.getSpeedProfile();
            }
        }
        return msg;
    }
    
    private void dofactor() {
        if (_clearBox.isSelected()) {
            if (_speedProfile != null) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, Bundle.getMessage(
                        "ClearSpeedProfile", _warrant.getTrainId(), _speedProfile.getProfileSize()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) ) {
                    _speedProfile.clearCurrentProfile();                     
                }
                    
            }
        }
       if (_addBox.isSelected()) {
            jmri.jmrit.roster.RosterEntry ent = _warrant.getRosterEntry();
            if (ent==null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("trainInfo6", _warrant.getTrainId()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (_speedProfile == null) {
                _speedProfile = new RosterSpeedProfile(ent);
                ent.setSpeedProfile(_speedProfile);
            }
          // _maxSpeed is now actual speedSetting
            if (_isForward) {
                _speedProfile.setForwardSpeed(_maxSpeed, _rawSpeed*1000);                
            } else {
                _speedProfile.setReverseSpeed(_maxSpeed, _rawSpeed*1000);                                
            }
            if (log.isDebugEnabled()) log.debug("Made speed profile setting for "+ _warrant.getTrainId()+
                    ": "+(_isForward ? "Forward":"Reverse")+" step= "+Math.round(_maxSpeed*1000)+", speed= "+_rawSpeed*1000);
            _warrant.getRosterEntry().updateFile();
            Roster.writeRosterFile();
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
    
    private JPanel makeEntryPanel(String name, boolean isForward) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(new JLabel(Bundle.getMessage("trainInfo1", _warrant.getTrainName(),
                    _warrant.getDccAddress().toString(), name)));
        String direction;
        if (isForward) {
            direction = Bundle.getMessage("forward");
        } else {
            direction = Bundle.getMessage("reverse");
        }
        p.add(new JLabel(Bundle.getMessage("trainInfo2", direction, _maxSpeed)));
        panel.add(p, BorderLayout.CENTER);
        panel.add(Box.createRigidArea(new java.awt.Dimension(10,10)), BorderLayout.WEST);
        return panel;
    }

    private JPanel makeExitPanel(boolean init) {
        float spFactor = 0.0f;
        float spSpeed = 0.0f;
        DccThrottle throttle = _warrant.getThrottle();
        float scale = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        float scaleSpeed = _rawSpeed*scale;          // prototype m/s
        if (!init) {
            float speedSetting = throttle.getSpeedSetting();
            int speedStep = 0;
            switch (throttle.getSpeedStepMode()) {
                case DccThrottle.SpeedStepMode14:
                    speedStep = java.lang.Math.round(speedSetting * 14);
                    break;
                case DccThrottle.SpeedStepMode27:
                    speedStep = java.lang.Math.round(speedSetting * 27);
                    break;
                case DccThrottle.SpeedStepMode28:
                    speedStep = java.lang.Math.round(speedSetting * 28);
                    break;
                case DccThrottle.SpeedStepMode128:
                default:
                    // 128 speed step mode is the default in the JMRI
                    // throttle code.
                    speedStep = java.lang.Math.round(speedSetting * 126);
                    break;
            }
            speedSetting = throttle.getSpeedIncrement()*speedStep;      // actual speedSetting
            _factor = _rawSpeed/speedSetting;
//            _factor = speedSetting*25.4f/(_rawSpeed*100);
            _isForward = throttle.getIsForward();
            if (_speedProfile!=null) {
                if (_isForward) {
                    spSpeed = _speedProfile.getForwardSpeed(speedSetting); 
                }else {
                    spSpeed = _speedProfile.getReverseSpeed(speedSetting);                 
                }
                spFactor = spSpeed/(speedSetting*1000);
            }           
            if (log.isDebugEnabled()) log.debug("Throttle speedSetting= "+speedSetting+", Set from _maxSpeed= "+_maxSpeed+
                    ", expected profile speed ="+spSpeed+", actual _rawSpeed= "+(_rawSpeed*1000)+"mm/sec, scale= "+scale);
            _maxSpeed = speedSetting;     // now is the actual setting
        }
        String speedUnits;
        if ( jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
            speedUnits = "kmph";
            scaleSpeed = 3.6f*scaleSpeed;
            spSpeed = spSpeed*scale*3.6f/1000;
        } else {
            speedUnits = "mph";
            scaleSpeed = scaleSpeed*3.6f*0.621371f;
            spSpeed = spSpeed*scale*3.6f*0.621371f/1000;
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(new JLabel(Bundle.getMessage("trainInfo3", _maxSpeed, scaleSpeed, speedUnits)));
        p.add(new JLabel(Bundle.getMessage("trainInfo4", _factor)));
        if (_speedProfile!=null) {
            p.add(new JLabel(Bundle.getMessage("trainInfo5", spSpeed, speedUnits, spFactor)));          
        } else {
            p.add(new JLabel(Bundle.getMessage("trainInfo6", _warrant.getTrainId())));                     
        }
        panel.add(p, BorderLayout.CENTER);
        panel.add(Box.createRigidArea(new java.awt.Dimension(10,100)), BorderLayout.WEST);
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(_addBox);
        p.add(_ignoreBox);
        p.add(_clearBox);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createRigidArea(new java.awt.Dimension(10,100)));
        pp.add(p);
        panel.add(pp, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Called from Warrant goingActive
     * Compute actual speed and set throttle factor
     * @param index
     */
    protected void calibrateAt(int index) {
        if (_calibrateIndex == index) {
           _mainPanel.remove(3);
           _mainPanel.remove(1);
            _entryTime = _calibBlockOrder.getBlock()._entryTime;
            _mainPanel.add(makeEntryPanel(_calibBlockOrder.getBlock().getDisplayName(),
                    _warrant.getThrottle().getIsForward()), 1);
            setVisible(true);
        } else if (_calibrateIndex == index-1) {
            setVisible(false);
            BlockOrder bo = _warrant.getBlockOrderAt(index);
            long eTime = bo.getBlock()._entryTime - _entryTime;
            _rawSpeed = _calibBlockOrder.getPath().getLengthMm()/eTime;    // layout mm/ms
            _mainPanel.add(makeExitPanel(false), 3);
            setVisible(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Calibrater.class.getName());
}
