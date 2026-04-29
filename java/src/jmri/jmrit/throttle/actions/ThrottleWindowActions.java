package jmri.jmrit.throttle.actions;

import java.beans.*;

import javax.swing.JInternalFrame;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.jmrit.throttle.preferences.ThrottlesPreferencesWindowKeyboardControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for ThrottleWindowActions, used by mousewheel and key listener
 * 
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
 * @author Lionel Jeanson
 * 
 */

public abstract class ThrottleWindowActions implements PropertyChangeListener {
    
    protected final ThrottleControllersUIContainer tw;
    protected ThrottlesPreferencesWindowKeyboardControls tpwkc;
    
    ThrottleWindowActions(ThrottleControllersUIContainer tw) {
        this.tw = tw;
        resetTpwkc();
    }
        
    private void resetTpwkc() {
        tpwkc = InstanceManager.getDefault(ThrottlesPreferences.class).getThrottlesKeyboardControls();        
    }
              
    protected void toFront(JInternalFrame jif) {
        if (jif == null) {
            return;
        }
        if (!jif.isVisible()) {
            jif.setVisible(true);
        }        
        if (jif.isIcon()) {
            try {
                jif.setIcon(false);
            } catch (PropertyVetoException ex) {
                log.debug("JInternalFrame uniconify, vetoed");
            }
        }
        jif.requestFocus();
        jif.toFront();
        try {
            jif.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            log.debug("JInternalFrame selection, vetoed");
        }
    }
    
    protected void incrementSpeed(DccThrottle throttle, float increment) {
        if (throttle == null) {
            return;
        }
        float speed;
        float curSpeed = throttle.getSpeedSetting();
        if (curSpeed < 0) {
            curSpeed = 0; // restart from 0 if was on emergency stop
        }
        if (tw.getCurentThrottleController().isSpeedDisplayContinuous()) {  
            if (throttle.getIsForward()) {
                speed = curSpeed + increment;
                if (speed > -throttle.getSpeedIncrement()/2 && speed < throttle.getSpeedIncrement()/2 ) {
                    speed = 0;
                }
                if (speed < 0) {
                    throttle.setIsForward(false);
                    speed = -speed;
                }
            } else {
                speed = -curSpeed + increment;
                if (speed > -throttle.getSpeedIncrement()/2 && speed < throttle.getSpeedIncrement()/2 ) {
                    speed = 0;
                }                
                if (speed > 0) {
                    throttle.setIsForward(true);
                } else {                        
                    speed = -speed;
                }
            }            
        } else {
            speed = curSpeed + increment;
        }
        if ( speed < throttle.getSpeedIncrement()/2 || speed <0 ) { // force 0 bellow minimum speed
            speed = 0;
        } else if (speed > 1) {
            speed = 1;
        }
        throttle.setSpeedSetting( speed );               
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt == null) || (evt.getPropertyName() == null)) {
            return;
        }
        if (evt.getPropertyName().compareTo("ThrottlePreferences") == 0) {
           resetTpwkc();
        }               
    }
    
    private static final Logger log = LoggerFactory.getLogger(ThrottleWindowActions.class);    
}
