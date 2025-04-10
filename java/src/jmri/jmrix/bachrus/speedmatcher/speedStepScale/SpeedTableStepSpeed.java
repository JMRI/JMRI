package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import java.util.Locale;

import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher.SpeedTableStep;
import static jmri.jmrix.bachrus.speedmatcher.speedStepScale.SpeedStepScaleSpeedMatcher.getSpeedForSpeedTableStep;

/**
 * Wrapper class for a SpeedTableStep and its corresponding speed, including a
 * toString override for use in a speed selector combobox.
 *
 * @author toddt
 */
public class SpeedTableStepSpeed {

    private final SpeedTableStep speedTableStep;
    private final float speed;

    /**
     * Creates a SpeedTableStepSpeed from the given speedTableStep
     * @param speedTableStep the SpeedTableStep to use
     */
    public SpeedTableStepSpeed(SpeedTableStep speedTableStep) {
        this.speedTableStep = speedTableStep;
        this.speed = getSpeedForSpeedTableStep(this.speedTableStep.getSpeedStep());
    }

    /**
     * Gets this SpeedTableStepSpeed's SpeedTableStep
     * @return this SpeedTableStepSpeed's SpeedTableStep
     */
    public SpeedTableStep getSpeedTableStep() {
        return speedTableStep;
    }

    /**
     * Gets this SpeedTableStepSpeed's speed
     * @return the Speed TableStepSpeed's speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Converts this SpeedTableStepSpeed to an I18N string.
     * @return a single decimal string of this SpeedTableStepSpeed's speed
     */
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.1f", this.speed);
    }
}
