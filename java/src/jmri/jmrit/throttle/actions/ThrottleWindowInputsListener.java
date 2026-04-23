package jmri.jmrit.throttle.actions;

import java.awt.event.*;

import jmri.DccThrottle;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;


/**
 *
 * @author Lionel Jeanson
 * 
 * This class implements mouse wheel action on a throttle frame
 * 
 */
public class ThrottleWindowInputsListener extends ThrottleWindowActions implements MouseWheelListener {

    public ThrottleWindowInputsListener(ThrottleControllersUIContainer tw) {
        super(tw);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Throttle commands
        DccThrottle throttle = tw.getCurentThrottleController().getThrottle();
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
}
