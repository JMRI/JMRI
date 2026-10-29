package jmri.jmrit.throttle.actions;

import java.awt.event.*;

import jmri.DccThrottle;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;


/**
 * This class implements mouse wheel action on a throttle frame
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
