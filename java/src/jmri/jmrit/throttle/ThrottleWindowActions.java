package jmri.jmrit.throttle;

import java.beans.*;

import javax.swing.JInternalFrame;

import jmri.DccThrottle;
import jmri.InstanceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lionel
 */
public abstract class ThrottleWindowActions implements PropertyChangeListener {
    
    protected final ThrottleWindow tw;
    protected ThrottlesPreferencesWindowKeyboardControls tpwkc;
    
    ThrottleWindowActions(ThrottleWindow tw) {
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
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
        if (tw.getCurrentThrottleFrame().getControlPanel().getDisplaySlider() == ControlPanel.SLIDERDISPLAYCONTINUOUS ) {
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
    
    private final static Logger log = LoggerFactory.getLogger(ThrottleWindowActions.class);    
}
