package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.SpeedStepMode;
import jmri.Throttle;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 *
 * @author Pete Cressman Copyright 2020
 */
public class LearnControlPanel extends jmri.jmrit.throttle.ControlPanel {

    private LearnThrottleFrame _throttleFrame;

    LearnControlPanel(LearnThrottleFrame ltf) {
        super();
        _throttleFrame = ltf;
        
    }
    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("propertyChange: {}, newValue= {}", e.getPropertyName(), e.getNewValue().toString());
        }
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            float speed = ((Float) e.getNewValue()).floatValue();
            _throttleFrame.setSpeedSetting(speed);
        } else if (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) {
            SpeedStepMode steps = (SpeedStepMode)e.getNewValue();
            _throttleFrame.setSpeedStepMode(steps);
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            boolean Forward = ((Boolean) e.getNewValue()).booleanValue();
            _throttleFrame.setButtonForward(Forward);
        }
        super.propertyChange(e);
    }

    private static final Logger log = LoggerFactory.getLogger(LearnControlPanel.class);
}
