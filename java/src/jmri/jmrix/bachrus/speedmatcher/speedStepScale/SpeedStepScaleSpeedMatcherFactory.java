package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;

/**
 * Factory for creating the correct type of Speed Step Scale speed matcher for
 * the given SpeedMatcherConfig
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedMatcherFactory {

    /**
     * Gets the correct Speed Step Scale Speed Matcher for the given speedTable
     *
     * @param speedTable SpeedStepScaleSpeedMatcherConfig.SpeedTable to use for
     *                   speed matching
     * @param config     SpeedStepScaleSpeedMatcherConfig for initializing the
     *                   speed matcher
     * @return the SpeedMatcher to use for speed matching
     */
    public static SpeedMatcher getSpeedMatcher(SpeedStepScaleSpeedMatcherConfig.SpeedTable speedTable, SpeedStepScaleSpeedMatcherConfig config) {

        switch (speedTable) {
            case ESU:
                return new SpeedStepScaleESUTableSpeedMatcher(config);
            default:
                return new SpeedStepScaleSpeedTableSpeedMatcher(config);
        }
    }
}
