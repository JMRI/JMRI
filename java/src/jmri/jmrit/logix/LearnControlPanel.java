package jmri.jmrit.logix;

import jmri.SpeedStepMode;
import jmri.Throttle;

/**
 * A JInternalFrame that contains a JSlider to control loco speed, and buttons
 * for forward, reverse and STOP.
 *
 * @author Pete Cressman Copyright 2020
 */
public class LearnControlPanel extends jmri.jmrit.throttle.ControlPanel {

    private final LearnThrottleFrame _throttleFrame;

    LearnControlPanel(LearnThrottleFrame ltf) {
        super();
        _throttleFrame = ltf;
        
    }

    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("propertyChange: {}, newValue= {}", e.getPropertyName(), e.getNewValue());
        }
        switch (e.getPropertyName()) {
            case Throttle.SPEEDSETTING:
                float speed = ((Float) e.getNewValue());
                _throttleFrame.setSpeedSetting(speed);
                break;
            case Throttle.SPEEDSTEPS:
                SpeedStepMode steps = (SpeedStepMode)e.getNewValue();
                _throttleFrame.setSpeedStepMode(steps);
                break;
            case Throttle.ISFORWARD:
                boolean forward = ((Boolean) e.getNewValue());
                _throttleFrame.setButtonForward(forward);
                break;
            default:
                break;
        }
        super.propertyChange(e);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LearnControlPanel.class);

}
