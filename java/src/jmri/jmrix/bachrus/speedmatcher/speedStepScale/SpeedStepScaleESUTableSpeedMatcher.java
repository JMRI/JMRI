package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleESUTableSpeedMatcher extends SpeedStepScaleSpeedMatcher {

    public SpeedStepScaleESUTableSpeedMatcher(SpeedMatcherConfig config) {
        super(config);
    }

    @Override
    public boolean startSpeedMatcher() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopSpeedMatcher() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSpeedMatcherIdle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void programmingOpReply(int value, int status) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
