package jmri.jmrix.bachrus.speedmatcher;

import jmri.jmrix.bachrus.speedmatcher.basic.*;

/**
 * Factory for creating the correct type of speed matcher for the given
 * SpeedMatcherConfig
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedMatcherFactory {

    public static SpeedMatcher getSpeedMatcher(SpeedMatcherConfig config) {

        SpeedMatcher speedMatcher;

        switch (config.type) {
            case BASIC:
                switch (config.speedTable) {
                    case ESU:
                        speedMatcher = new BasicESUTableSpeedMatcher(config);
                        break;
                    case ADVANCED:
                        speedMatcher = new BasicSpeedTableSpeedMatcher(config);
                        break;
                    default:
                        speedMatcher = new BasicSimpleCVSpeedMatcher(config);
                        break;
                }
                break;

            //TODO: TRW - respect different types
//            case SPEEDSTEPSCALE:
//                switch (config.speedTable) {
//                    case SPEEDSTEPSCALE:
//                        break;
//                    case COMBO:
//                        break;
//                }
//                break;
            default:
                speedMatcher = new BasicSimpleCVSpeedMatcher(config);
                break;
        }

        return speedMatcher;
    }
}
