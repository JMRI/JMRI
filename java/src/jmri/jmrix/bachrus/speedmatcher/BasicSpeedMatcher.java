package jmri.jmrix.bachrus.speedmatcher;

import jmri.jmrix.bachrus.Speed;

/**
 *
 * @author toddt
 */
public abstract class BasicSpeedMatcher extends SpeedMatcher{

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    protected float targetStartSpeedKPH;
    protected float targetTopSpeedKPH;
    
    //</editor-fold>
    
    public BasicSpeedMatcher(SpeedMatcherConfig config) {
        super(config);

        if (config.speedUnit == Speed.Unit.MPH) {
            this.targetStartSpeedKPH = Speed.mphToKph(config.targetStartSpeed);
            this.targetTopSpeedKPH = Speed.mphToKph(config.targetTopSpeed);
        } else {
            this.targetStartSpeedKPH = config.targetStartSpeed;
            this.targetTopSpeedKPH = config.targetTopSpeed;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Public APIs">
    public boolean Validate() {
        if (dccLocoAddress.getNumber() <= 0) {
           statusLabel.setText("Please enter a valid DCC address");
            return false;
        }

        if (targetStartSpeedKPH < 1) {
            statusLabel.setText("Please enter a valid start speed");
            return false;
        }

        if (targetTopSpeedKPH <= targetStartSpeedKPH) {
            statusLabel.setText("Please enter a valid top speed");
            return false;
        }

        return true;
    }
    
    //</editor-fold>
    
    /**
     * Sets the PID controller's speed match error for speed matching
     *
     * @param speedTarget - target speed in KPH
     */
    protected void setSpeedMatchError(float speedTarget) {
        speedMatchError = speedTarget - currentSpeed;
    }

    /**
     * Gets the next value to try for speed matching using a PID controller
     *
     * @param lastValue - the last speed match CV value tried
     * @return the next value to try for speed matching (1-255 inclusive)
     */
    protected int getNextSpeedMatchValue(int lastValue) {
        speedMatchIntegral += speedMatchError;
        speedMatchDerivative = speedMatchError - lastSpeedMatchError;

        int value = (lastValue + Math.round((kP * speedMatchError) + (kI * speedMatchIntegral) + (kD * speedMatchDerivative)));

        if (value > 255) {
            value = 255;
        } else if (value < 1) {
            value = 1;
        }

        return value;
    }
}