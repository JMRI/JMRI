package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;

/**
 * Factory for creating the correct type of speed matcher for the given
 * SpeedMatcherConfig
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedMatcherFactory {

    public static SpeedMatcher getSpeedMatcher(SpeedStepScaleSpeedMatcherConfig.SpeedTable speedTable, SpeedStepScaleSpeedMatcherConfig config) {

        switch (speedTable) {
            case ESU:
                return new SpeedStepScaleESUTableSpeedMatcher(config);
            default:
                return new SpeedStepScaleSpeedTableSpeedMatcher(config);
        }
    }
}
