package jmri.jmrit.throttle;

import java.awt.event.*;

import jmri.DccThrottle;


/**
 *
 * @author Lionel Jeanson
 * 
 * This class implements mouse wheel action on a throttle frame
 * 
 */
public class ThrottleWindowInputsListener extends ThrottleWindowActions implements MouseWheelListener {

    public ThrottleWindowInputsListener(ThrottleWindow tw) {
        super(tw);
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
}
