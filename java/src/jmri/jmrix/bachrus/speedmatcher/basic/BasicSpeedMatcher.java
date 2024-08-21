package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;

/**
 * Abstract class defining the basic operations of a Basic speed matcher (sets a
 * minimum speed at speed step 1, a maximum at speed step 28, and some number of
 * points in between). All basic speed matcher implementations must extend this
 * class.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class BasicSpeedMatcher extends SpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    protected float targetStartSpeedKPH;
    protected float targetTopSpeedKPH;
    //</editor-fold>

    /**
     * Constructs the abstract BasicSpeedMatcher at the core of any Basic Speed
     * Matcher
     *
     * @param config BasicSpeedMatcherConfig
     */
    public BasicSpeedMatcher(BasicSpeedMatcherConfig config) {
        super(config);

        if (config.speedUnit == Speed.Unit.MPH) {
            this.targetStartSpeedKPH = Speed.mphToKph(config.targetStartSpeed);
            this.targetTopSpeedKPH = Speed.mphToKph(config.targetTopSpeed);
        } else {
            this.targetStartSpeedKPH = config.targetStartSpeed;
            this.targetTopSpeedKPH = config.targetTopSpeed;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    /**
     * Validates the speed matcher's configuration
     *
     * @return true if the configuration is valid, false otherwise
     */
    @Override
    protected boolean validate() {
        if (dccLocoAddress.getNumber() <= 0) {
            statusLabel.setText(Bundle.getMessage("StatInvalidDCCAddress"));
            return false;
        }

        if (targetStartSpeedKPH < 1) {
            statusLabel.setText(Bundle.getMessage("StatInvalidStartSpeed"));
            return false;
        }

        if (targetTopSpeedKPH <= targetStartSpeedKPH) {
            statusLabel.setText(Bundle.getMessage("StatInvalidTopSpeed"));
            return false;
        }

        return true;
    }

    /**
     * Gets the desired speed for a given speed step
     *
     * @param speedStep the SpeedTableStep to get the speed for
     * @param minSpeed  minimum speed in KPH (at speed step 1)
     * @param maxSpeed  maximum speed in KPH (at speed step 28)
     * @return the speed for the given speed step in KPH
     */
    protected float getSpeedForSpeedStep(SpeedTableStep speedStep, float minSpeed, float maxSpeed) {
        return minSpeed + (((maxSpeed - minSpeed) / 27) * (speedStep.getSpeedStep() - 1));
    }
    //</editor-fold>
}
