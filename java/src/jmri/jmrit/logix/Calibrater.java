package jmri.jmrit.logix;

import java.util.List;
import javax.swing.JOptionPane;
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
public class Calibrater {
    
    private int _calibrateIndex;
    private Warrant _warrant;
    private float _maxSpeed;
    
    Calibrater(Warrant warrant) {
        _warrant = warrant;
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
    /**
     * Called from Warrant goingActive
     * Compute actual speed and set throttle factor
     * @param index
     */
    protected void calibrateAt(int index) {
        if (_calibrateIndex+1 != index) {
            return;
        }
        BlockOrder bo = _warrant.getBlockOrderAt(_calibrateIndex);
        OBlock calibBlock = bo.getBlock();
        long eTime =  _warrant.getBlockAt(_calibrateIndex+1)._entryTime - calibBlock._entryTime;
        float speed = bo.getPath().getLengthIn()*SignalSpeedMap.getMap().getLayoutScale()/eTime;        // scale ins/ms
        float factor = _maxSpeed/speed;
        String speedUnits;
        if ( SignalSpeedMap.getMap().getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
            speedUnits = Bundle.getMessage("speedKmph");
            speed = speed*3600*25.4f/1000;
        } else {
            speedUnits = Bundle.getMessage("speedMph");
            speed = speed*3600*1000/(12*5280);
        }
        JOptionPane.showMessageDialog(null, Bundle.getMessage("calibrateDone", _warrant.getDccAddress(),
                calibBlock.getDisplayName(), speed, _maxSpeed, factor, speedUnits),
                Bundle.getMessage("calibBlockTitle"), JOptionPane.INFORMATION_MESSAGE);
        
        NXFrame.getInstance().setThrottleScale(factor);
    }


    static Logger log = LoggerFactory.getLogger(Calibrater.class.getName());
}
