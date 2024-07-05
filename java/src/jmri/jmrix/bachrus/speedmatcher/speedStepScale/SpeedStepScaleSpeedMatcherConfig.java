package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * Configuration data for a speed step scale speed matcher
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedMatcherConfig extends SpeedMatcherConfig{
    
    //<editor-fold defaultstate="collapsed" desc="Enums">
    public enum SpeedTable {
        ADVANCED, ESU
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    public float targetMaxSpeed;
    public JLabel actualMaxSpeedField;
    //</editor-fold>
    
    public SpeedStepScaleSpeedMatcherConfig(
            DccLocoAddress address, 
            float targetMaxSpeed,
            Speed.Unit speedUnit, 
            boolean trimReverseSpeed, 
            int warmUpForwardSeconds, 
            int warmUpReverseSeconds, 
            PowerManager powerManager,
            JLabel statusLabel, 
            JLabel actualMaxSpeedField,
            JButton startStopButton)
    {
        super(address, speedUnit, trimReverseSpeed, warmUpForwardSeconds, warmUpReverseSeconds, powerManager, statusLabel, startStopButton);
        
        this.targetMaxSpeed = targetMaxSpeed;
        this.actualMaxSpeedField = actualMaxSpeedField;
    }

}
