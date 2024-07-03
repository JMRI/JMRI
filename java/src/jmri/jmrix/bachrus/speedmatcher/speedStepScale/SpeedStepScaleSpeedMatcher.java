package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * This is a speed step scale speed matcher which will speed match a locomotive
 * such that its speed in mph/kph will be equal to its speed step in 128 speed
 * step mode. This uses the complex speed table, and the locomotive's speed will
 * plateau at either its actual top speed or the entered max speed, whichever is
 * lower.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class SpeedStepScaleSpeedMatcher extends SpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    private final float allowedMaxSpeed; //KPH or MPH depending on unit
    private final Speed.Unit speedUnit;
    //</editor-fold>
    
    public SpeedStepScaleSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);
        
        this.allowedMaxSpeed = config.targetMaxSpeed;
        this.speedUnit = config.speedUnit;
    }

    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    @Override
    protected boolean validate() {
        if (dccLocoAddress.getNumber() <= 0) {
            statusLabel.setText(Bundle.getMessage("StatInvalidDCCAddress"));
            return false;
        }
        
        if (allowedMaxSpeed < SpeedTableStep.STEP1.get128StepScaleSpeed()){
            statusLabel.setText("Please enter a valid max speed");
            return false;
        }
        
        return true;
    }
    
    //</editor-fold>
}
