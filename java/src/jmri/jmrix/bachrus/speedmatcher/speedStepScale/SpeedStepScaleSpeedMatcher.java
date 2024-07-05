package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import javax.swing.JLabel;

import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;

/**
 * This is a speed step scale speed matcher which will speed match a locomotive
 * such that its speed in mph/kph will be equal to its speed step in 128 speed
 * step mode. This uses the complex speed table, and the locomotive's speed will
 * plateau at either its actual top speed or the set max speed, whichever is
 * lower. The set max speed will be rounded up to the nearest speed table speed
 * step to maintain as smooth a curve as possible.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class SpeedStepScaleSpeedMatcher extends SpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    protected final float CONVERT_28_TO_128_SPEED_STEPS = 4.571428571428571f;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    protected final float allowedMaxSpeedKPH;
    protected final Speed.Unit speedUnit;
    protected final JLabel actualMaxSpeedField;
    //</editor-fold>

    public SpeedStepScaleSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);

        this.speedUnit = config.speedUnit;
        this.actualMaxSpeedField = config.actualMaxSpeedField;

        //set the allowed max speed to the next highest multiple of CONVERT_28_TO_128_SPEED_STEPS
        //this ensures we speed match as accurately as possible
        float targetMaxSpeed = config.speedUnit == Speed.Unit.MPH ? Speed.mphToKph(config.targetMaxSpeed) : config.targetMaxSpeed;
        this.allowedMaxSpeedKPH = (float) (CONVERT_28_TO_128_SPEED_STEPS * Math.ceil(targetMaxSpeed / CONVERT_28_TO_128_SPEED_STEPS));
    }

    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    @Override
    protected boolean validate() {
        if (dccLocoAddress.getNumber() <= 0) {
            statusLabel.setText(Bundle.getMessage("StatInvalidDCCAddress"));
            return false;
        }

        if (allowedMaxSpeedKPH < 0) {
            statusLabel.setText("Please enter a valid max speed");
            return false;
        }

        return true;
    }

    /**
     * Gets the speed in KPH for a given speed step for a speed step scale speed
     * matcher
     *
     * @param speedStep the SpeedTableStep to get the speed for
     * @return speed for the given speedStep in KPH
     */
    public float getSpeedStepScaleSpeedInKPH(SpeedTableStep speedStep) {
        //each speed steep in 28 speed step mode is roughly 4.5 speed steps in 128 speed step mode
        float speedStepSpeed = speedStep.getSpeedStep() * CONVERT_28_TO_128_SPEED_STEPS;

        //convert MPH to KPH since Bachrus does everything in KPH
        if (speedUnit == Speed.Unit.MPH) {
            speedStepSpeed = Speed.mphToKph(speedStepSpeed);
        }

        //speed must be bounded by the allowed max speed
        speedStepSpeed = Math.min(speedStepSpeed, allowedMaxSpeedKPH);

        return speedStepSpeed;
    }
    //</editor-fold>
}
