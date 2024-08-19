package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import javax.swing.JLabel;

import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;

/**
 * Abstract class defining the basic operations of a Speed Step Scale speed
 * matcher (sets up the complex speed table such that the speed step equals the
 * locomotive speed when using "128" speed step mode). All speed step scale
 * speed matcher implementations must extend this class.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class SpeedStepScaleSpeedMatcher extends SpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    protected final SpeedTableStepSpeed targetMaxSpeedStep;
    protected final float targetMaxSpeedKPH;
    protected final Speed.Unit speedUnit;
    protected final JLabel actualMaxSpeedField;

    protected float measuredMaxSpeedKPH = 0;
    protected float speedMatchMaxSpeedKPH = 0;
    //</editor-fold>

    /**
     * Constructs the abstract SpeedStepScaleSpeedMatcher at the core of any
     * Speed Step Scale Speed Matcher
     *
     * @param config SpeedStepScaleSpeedMatcherConfig
     */
    public SpeedStepScaleSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);

        this.actualMaxSpeedField = config.actualMaxSpeedField;
        this.speedUnit = config.speedUnit;

        this.targetMaxSpeedStep = config.targetMaxSpeedStep;
        this.targetMaxSpeedKPH = config.speedUnit == Speed.Unit.MPH ? Speed.mphToKph(this.targetMaxSpeedStep.getSpeed()) : this.targetMaxSpeedStep.getSpeed();
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

        if (targetMaxSpeedStep == null) {
            statusLabel.setText(Bundle.getMessage("StatInvalidMaxSpeed"));
            return false;
        }

        return true;
    }

    /**
     * Gets the speed in KPH for a given speed step for a speed step scale speed
     * matcher
     *
     * @param speedStep the int speed step to get the speed for
     * @return speed for the given speedStep in KPH
     */
    protected float getSpeedStepScaleSpeedInKPH(int speedStep) {
        //speed = step in 128 speed step mode
        float speedStepSpeed = getSpeedForSpeedTableStep(speedStep);

        //convert MPH to KPH since Bachrus does everything in KPH
        if (speedUnit == Speed.Unit.MPH) {
            speedStepSpeed = Speed.mphToKph(speedStepSpeed);
        }

        //speed must be bounded by the target max speed
        speedStepSpeed = Math.min(speedStepSpeed, speedMatchMaxSpeedKPH);

        return speedStepSpeed;
    }
    
    /**
     * Gets the speed step value for a linear speed table
     * @param speedStep the inst speed step to get the value for
     * @return value for the speed step
     */
    protected int getSpeedStepLinearValue(int speedStep) {
        return (int) (speedStep / 28f * 255f);
    }

    /**
     * Gets the 128 speed step mode speed for a speed table step
     *
     * @param speedStep the int speed table step to get the 128 speed step mode
     *                  speed for
     * @return the 128 speed step mode speed for the given speedStep
     */
    public static float getSpeedForSpeedTableStep(int speedStep) {
        //speed step 1 (28) = speed step 1 (128), 28 (28) = 126 (128), 
        //so 27 speed steps (28) = 125 speed steps (128),
        //so each step (28) = 4.6296 steps (128)
        return (speedStep * 4.6296f) - 3.6296f;
    }

    /**
     * Gets the next lowest speed table step for the given speed
     *
     * @param speed float speed in the user facing unit
     * @return the next lowest int speed table step for the given speed
     */
    public static int getNextLowestSpeedTableStepForSpeed(float speed) {
        return (int) Math.floor((speed + 3.6296f) / 4.6296f);
    }
    //</editor-fold>
}
