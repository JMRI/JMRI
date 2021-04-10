package jmri.jmrit.throttle;

import java.awt.event.*;
import java.beans.*;

import javax.swing.JInternalFrame;

import jmri.DccThrottle;
import jmri.InstanceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lionel Jeanson
 * 
 * This class implements all keyboard and mouse wheel action on a throttle frame
 * 
 */
public class ThrottleWindowInputsListener implements KeyListener, MouseWheelListener, PropertyChangeListener {

    private final ThrottleWindow tw;
    private ThrottlesPreferencesWindowKeyboardControls tpwkc;
    
    ThrottleWindowInputsListener(ThrottleWindow tw) {
        this.tw = tw;
        resetTpwkc();
    }
    
    private void resetTpwkc() {
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        tpwkc = InstanceManager.getDefault(ThrottlesPreferences.class).getThrottlesKeyboardControls();        
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

          
    @Override
    public void keyPressed(KeyEvent e) {
        log.debug("Key pressed: "+e.getKeyCode()+" / modifier: "+e.getModifiers()+" / ext. key code: "+e.getExtendedKeyCode()+" / location: "+e.getKeyLocation());               
        // Throttle commands
        DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
        if (throttle != null) {
            // speed
            if ( match(e, tpwkc.getAccelerateKeys())) {
                incrementSpeed(throttle, throttle.getSpeedIncrement());
                return;
            } 
            if (match(e, tpwkc.getDecelerateKeys())) {           
                incrementSpeed(throttle, -throttle.getSpeedIncrement());    
                return;            
            } 
            if (match(e, tpwkc.getAccelerateMoreKeys())) {
                incrementSpeed(throttle, throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
                return;
            } 
            if (match(e, tpwkc.getDecelerateMoreKeys())) {
                incrementSpeed(throttle, -throttle.getSpeedIncrement()*tpwkc.getMoreSpeedMultiplier());
                return;
            }
            // momentary function buttons
            for (int i=0;i<tpwkc.getNbFunctionsKeys();i++) {
                if (match(e,tpwkc.getFunctionsKeys(i))) {
                    if (throttle.getFunctionMomentary(i) || ( !tw.getCurrentThrottleFrame().getFunctionPanel().getFunctionButtons()[i].getIsLockable())) {
                        throttle.setFunction(i, true );                        
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        log.debug("Key pressed: "+e.getKeyCode()+" / modifier: "+e.getModifiers()+" / ext. key code: "+e.getExtendedKeyCode()+" / location: "+e.getKeyLocation());
        // Throttle commands
        DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
        if (throttle != null) {
            // speed
            if (match(e, tpwkc.getForwardKeys())) {
                throttle.setIsForward(true);
                return;
            } 
            if (match(e, tpwkc.getReverseKeys())) {
                throttle.setIsForward(false);
                return;
            } 
            if  (match(e, tpwkc.getIdleKeys())) {
                throttle.setSpeedSetting(0);
                return;
            } 
            if (match(e, tpwkc.getStopKeys())) {
                throttle.setSpeedSetting(-1);
                return;
            } 
            if (match(e, tpwkc.getSwitchDirectionKeys())) {
                throttle.setIsForward(!throttle.getIsForward());
                return;
            }
            // functions
            for (int i=0;i<tpwkc.getNbFunctionsKeys();i++) {
                if (match(e,tpwkc.getFunctionsKeys(i))) {
                    throttle.setFunction(i, ! throttle.getFunction(i));
                    return;
                }
            }            
        }
        
        // Throttle inner window cycling and focus
        if (match(e, tpwkc.getNextThrottleInternalWindowKeys())) {
            tw.getCurrentThrottleFrame().activateNextJInternalFrame();
            return;
        }
        if (match(e, tpwkc.getPrevThrottleInternalWindowKeys())) {
            tw.getCurrentThrottleFrame().activatePreviousJInternalFrame();
            return;
        }
        if (match(e, tpwkc.getMoveToControlPanelKeys())) {
            toFront(tw.getCurrentThrottleFrame().getControlPanel());
            return;
        }
        if (match(e, tpwkc.getMoveToFunctionPanelKeys())) {
            toFront(tw.getCurrentThrottleFrame().getFunctionPanel());    
            return;        
        }
        if (match(e, tpwkc.getMoveToAddressPanelKeys())) {
            toFront(tw.getCurrentThrottleFrame().getAddressPanel());
            return;
        }        
        
        // Throttle frames control
        if (match(e, tpwkc.getNextThrottleFrameKeys())) {
            tw.nextThrottleFrame();
            return;
        }
        if (match(e, tpwkc.getPrevThrottleFrameKeys())) {
            tw.previousThrottleFrame();
            return;
        }
        if (match(e, tpwkc.getNextRunThrottleFrameKeys())) {
            tw.nextRunningThrottleFrame();
            return;
        }
        if (match(e, tpwkc.getPrevRunThrottleFrameKeys())) {
            tw.previousRunningThrottleFrame();
            return;
        }
        
        // Throttle windows control
        if (match(e, tpwkc.getNextThrottleWindowKeys())) {
            InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForNextThrottleWindow();
            return;
        }
        if (match(e, tpwkc.getPrevThrottleWindowKeys())) {
            InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForPreviousThrottleWindow();
        }                
    }
    
    private boolean match(KeyEvent e, int[][] keys) {
        for (int[] key : keys) {
            if ((e.getModifiers() == key[0]) && (e.getExtendedKeyCode() == key[1])) {
                return true;
            }
        }
        return false;
    }
        
    private void toFront(JInternalFrame jif) {
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
    
    private void incrementSpeed(DccThrottle throttle, float increment) {
        if (throttle == null) {
            return;
        }
        float speed;
        if (tw.getCurrentThrottleFrame().getControlPanel().getDisplaySlider() == ControlPanel.SLIDERDISPLAYCONTINUOUS ) {
            if (throttle.getIsForward()) {
                speed = throttle.getSpeedSetting() + increment;
                if (speed > -throttle.getSpeedIncrement()/2 && speed < throttle.getSpeedIncrement()/2 ) {
                    speed = 0;
                }
                if (speed < 0) {
                    throttle.setIsForward(false);
                    speed = -speed;
                }
            } else {
                speed = -throttle.getSpeedSetting() + increment;
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
            speed = throttle.getSpeedSetting() + increment;
        }
        if ( speed < throttle.getSpeedIncrement()/2 || speed <0 ) { // force 0 bellow minimum speed
            speed = 0;
        } else if (speed > 1) {
            speed = 1;
        }
        throttle.setSpeedSetting( speed );               
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Throttle commands
        DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
        if (throttle != null) {
            float multiplier;
            if (e.getWheelRotation() > 0) {
                multiplier = -1f;
                if ( e.isControlDown() ) {
                    multiplier = - tpwkc.getMoreSpeedMultiplier();
                }
            } else {
                multiplier = 1f;
                if ( e.isControlDown() ) {
                    multiplier = tpwkc.getMoreSpeedMultiplier();
                }
            }
            incrementSpeed(throttle, throttle.getSpeedIncrement() * multiplier);
        }        
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

    private final static Logger log = LoggerFactory.getLogger(ThrottleWindowInputsListener.class);    
}
