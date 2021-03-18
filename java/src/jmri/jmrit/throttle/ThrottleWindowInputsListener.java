package jmri.jmrit.throttle;

import java.awt.event.*;
import java.util.stream.IntStream;

import javax.swing.JInternalFrame;

import jmri.DccThrottle;
import jmri.InstanceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lionel Jeanson
 */
public class ThrottleWindowInputsListener implements KeyListener, MouseWheelListener {

    private final ThrottleWindow tw;
    
    ThrottleWindowInputsListener(ThrottleWindow tw) {
        this.tw = tw;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isAltDown() || e.isMetaDown() || e.isShiftDown()) {
            return;
        }
        
        // Throttle commands
        DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
        if (throttle != null) {
            // speed
            if ( IntStream.of(ThrottleWindowKeyboardControls.ACCELERATE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                if (e.isControlDown()) {
                    incrementSpeed(throttle, throttle.getSpeedIncrement()*ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER);
                } else {
                    incrementSpeed(throttle, throttle.getSpeedIncrement());
                }
            } else if (IntStream.of(ThrottleWindowKeyboardControls.DECELERATE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                if (e.isControlDown()) {
                    incrementSpeed(throttle, -throttle.getSpeedIncrement()*ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER);
                } else {                
                    incrementSpeed(throttle, -throttle.getSpeedIncrement());
                }
            } else if (IntStream.of(ThrottleWindowKeyboardControls.ACCELERATEMORE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                incrementSpeed(throttle, throttle.getSpeedIncrement()*ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER);
            } else if (IntStream.of(ThrottleWindowKeyboardControls.DECELERATEMORE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                incrementSpeed(throttle, -throttle.getSpeedIncrement()*ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER);
            }
            // momentary function buttons
            for (int i=0;i<ThrottleWindowKeyboardControls.FUNCTIONS_KEY.length;i++) {
                if ( ThrottleWindowKeyboardControls.FUNCTIONS_KEY[i] == e.getKeyCode()) {
                    if (throttle.getFunctionMomentary(i) || ( !tw.getCurrentThrottleFrame().getFunctionPanel().getFunctionButtons()[i].getIsLockable())) {
                        throttle.setFunction(i, true );
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Throttle commands
        DccThrottle throttle = tw.getCurrentThrottleFrame().getAddressPanel().getThrottle();
        if (throttle != null) {
            // speed
            if (IntStream.of(ThrottleWindowKeyboardControls.FORWARD_KEY).anyMatch(x->x==e.getKeyCode()) ) {
                throttle.setIsForward(true);
            } else if (IntStream.of(ThrottleWindowKeyboardControls.REVERSE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                throttle.setIsForward(false);
            } else if (IntStream.of(ThrottleWindowKeyboardControls.IDLE_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                throttle.setSpeedSetting(0);
            } else if (IntStream.of(ThrottleWindowKeyboardControls.STOP_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
                throttle.setSpeedSetting(-1);
            }
            // functions
            for (int i=0;i<ThrottleWindowKeyboardControls.FUNCTIONS_KEY.length;i++) {
                if ( ThrottleWindowKeyboardControls.FUNCTIONS_KEY[i] == e.getKeyCode()) {
                    throttle.setFunction(i, ! throttle.getFunction(i));
                    break;
                }
            }            
        }
        
        // Throttle inner window cycling and focus
        if ( IntStream.of(ThrottleWindowKeyboardControls.NEXT_THROTTLE_INTW_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            tw.getCurrentThrottleFrame().activateNextJInternalFrame();
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.PREV_THROTTLE_INTW_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            tw.getCurrentThrottleFrame().activateNextJInternalFrame();
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.MOVE_TO_CONTROL_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            toFront(tw.getCurrentThrottleFrame().getControlPanel());
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.MOVE_TO_FUNCTIONS_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            toFront(tw.getCurrentThrottleFrame().getFunctionPanel());            
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.MOVE_TO_ADDRESS_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            toFront(tw.getCurrentThrottleFrame().getAddressPanel());
        }        
        
        // Throttle frames control
        if ( IntStream.of(ThrottleWindowKeyboardControls.NEXT_THROTTLE_FRAME_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            tw.nextThrottleFrame();
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.PREV_THROTTLE_FRAME_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            tw.previousThrottleFrame();
        }

        // Throttle windows control
        if ( IntStream.of(ThrottleWindowKeyboardControls.NEXT_THROTTLE_WINDOW_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForNextThrottleWindow();
        }
        if ( IntStream.of(ThrottleWindowKeyboardControls.PREV_THROTTLE_WINDOW_KEYS).anyMatch(x->x==e.getKeyCode()) ) {
            InstanceManager.getDefault(ThrottleFrameManager.class).requestFocusForNextThrottleWindow();
        }                
    }
        
    private void toFront(JInternalFrame jif) {
        if (jif == null) {
            return;
        }
        jif.requestFocus();
        jif.toFront();
        try {
            jif.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            log.debug("JInternalFrame selection vetoed");
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
                if (speed > -throttle.getSpeedIncrement() && speed < throttle.getSpeedIncrement()) {
                    speed = 0;
                }
                if (speed < 0) {
                    throttle.setIsForward(false);
                    speed = -speed;
                }
            } else {
                speed = -throttle.getSpeedSetting() + increment;
                if (speed > -throttle.getSpeedIncrement() && speed < throttle.getSpeedIncrement()) {
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
        if (speed < 0) {
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
            float multiplier = 0;
            if (e.getWheelRotation() > 0) {
                multiplier = -1f;
                if ( e.isControlDown() ) {
                    multiplier = - ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER;
                }
            } else {
                multiplier = 1f;
                if ( e.isControlDown() ) {
                    multiplier = ThrottleWindowKeyboardControls.MORE_SPEED_MULTIPLIER;
                }
            }
            incrementSpeed(throttle, throttle.getSpeedIncrement() * multiplier);
        }        
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleWindowInputsListener.class);    
}
