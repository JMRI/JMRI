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
    protected final float targetMaxSpeedKPH;
    protected final Speed.Unit speedUnit;
    protected final JLabel actualMaxSpeedField;

    protected float measuredMaxSpeedKPH = 0;
    protected float speedMatchMaxSpeedKPH = 0;
    //</editor-fold>

    public SpeedStepScaleSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);

        this.actualMaxSpeedField = config.actualMaxSpeedField;
        this.speedUnit = config.speedUnit;
        this.targetMaxSpeedKPH = config.speedUnit == Speed.Unit.MPH ? Speed.mphToKph(config.targetMaxSpeed) : config.targetMaxSpeed;

        //TODO: TRW - remove if unneeded
        //saving code to set allowedMaxSpeedKPH to the next highest speed for a speed table step
        //float targetMaxSpeed = config.speedUnit == Speed.Unit.MPH ? Speed.mphToKph(config.targetMaxSpeed) : config.targetMaxSpeed;
        //this.allowedMaxSpeedKPH = (float) (CONVERT_28_TO_128_SPEED_STEPS * Math.ceil(targetMaxSpeed / CONVERT_28_TO_128_SPEED_STEPS));
    }

    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    @Override
    protected boolean validate() {
        if (dccLocoAddress.getNumber() <= 0) {
            statusLabel.setText(Bundle.getMessage("StatInvalidDCCAddress"));
            return false;
        }

        if (targetMaxSpeedKPH < 0) {
            statusLabel.setText("Please enter a valid max speed");
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
        float speedStepSpeed = convert28To128SpeedSteps(speedStep);

        //convert MPH to KPH since Bachrus does everything in KPH
        if (speedUnit == Speed.Unit.MPH) {
            speedStepSpeed = Speed.mphToKph(speedStepSpeed);
        }

        //speed must be bounded by the target max speed
        speedStepSpeed = Math.min(speedStepSpeed, targetMaxSpeedKPH);

        return speedStepSpeed;
    }

    /**
     * Converts a 28 speed step mode speed step into its 128 speed step mode
     * "equivalent"
     *
     * @param speedStep the int speed steep to get the 128 speed step mode
     *                  "equivalent" for
     * @return the 128 speed step mode "equivalent" for the given speedStep
     */
    protected float convert28To128SpeedSteps(int speedStep) {
        //speed step 1 (28) = speed step 1 (128), 28 (28) = 126 (128), so 27 speed steps (28) = 125 speed steps (128)
        //so each step (28) = 4.704 steps (128)
        return (speedStep * 4.6296f) - 3.6296f;
    }

    /**
     * Gets the lowest speed step which will run at the max speed
     *
     * @param speedMatchMaxSpeed the max speed in the user facing unit
     * @return integer speed step value for the lowest 28 speed step mode speed
     *         step which will run at the max speed
     */
    protected int getLowestMaxSpeedStep(float speedMatchMaxSpeed) {
        return (int) Math.ceil((speedMatchMaxSpeed + 3.6296f) / 4.6296f);
    }
    //</editor-fold>
}
